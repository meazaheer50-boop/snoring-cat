package com.dailydua.tracker.playback

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.dailydua.tracker.data.Dua
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The app's remote control for [PlaybackService]. Everything runs on the main thread. */
object PlayerHub {
    data class PState(
        val mediaId: String? = null,
        val isPlaying: Boolean = false,
        val positionMs: Long = 0,
        val durationMs: Long = 0,
        val queueIds: List<String> = emptyList(),
        val index: Int = 0,
        val repeatAll: Boolean = false,
        /** How many times the queue plays; 0 means forever. */
        val rounds: Int = 1,
        val round: Int = 1,
        val hasNext: Boolean = false,
        val hasPrevious: Boolean = false,
    )

    private val _state = MutableStateFlow(PState())
    val state: StateFlow<PState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var started = false
    private var rounds = 1
    private var round = 1
    private val pending = mutableListOf<(MediaController) -> Unit>()
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 250)
        }
    }

    fun init(context: Context) {
        if (started) return
        started = true
        val app = context.applicationContext
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            val c = runCatching { future.get() }.getOrNull() ?: run { started = false; return@addListener }
            controller = c
            c.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) = refresh()
                override fun onPlaybackStateChanged(state: Int) {
                    // The queue finished: go round again if more rounds were asked for.
                    if (state == Player.STATE_ENDED && (rounds == 0 || round < rounds)) {
                        round++
                        c.seekTo(0, 0L)
                        c.play()
                        refresh()
                    }
                }
            })
            pending.forEach { it(c) }
            pending.clear()
            refresh()
            handler.post(ticker)
        }, ContextCompat.getMainExecutor(app))
    }

    private fun refresh() {
        val c = controller ?: return
        _state.value = PState(
            mediaId = c.currentMediaItem?.mediaId,
            isPlaying = c.isPlaying,
            positionMs = c.currentPosition.coerceAtLeast(0),
            durationMs = c.duration.takeIf { it > 0 } ?: 0,
            queueIds = (0 until c.mediaItemCount).map { c.getMediaItemAt(it).mediaId },
            index = c.currentMediaItemIndex,
            repeatAll = c.repeatMode == Player.REPEAT_MODE_ALL,
            rounds = rounds,
            round = round,
            hasNext = c.hasNextMediaItem(),
            hasPrevious = c.hasPreviousMediaItem(),
        )
    }

    private fun withController(block: (MediaController) -> Unit) {
        val c = controller
        if (c != null) block(c) else pending.add(block)
    }

    fun hasAudio(context: Context, dua: Dua): Boolean {
        val asset = dua.audioAsset ?: return false
        return runCatching { context.assets.list("audio")?.contains(asset) == true }.getOrDefault(false)
    }

    private val durations = mutableMapOf<String, Long>()

    /** Length of a bundled recording, read from the file so it can be shown before playback starts. */
    fun assetDurationMs(context: Context, dua: Dua): Long {
        val asset = dua.audioAsset ?: return 0
        synchronized(durations) { durations[asset]?.let { return it } }
        val ms = runCatching {
            context.assets.openFd("audio/$asset").use { fd ->
                val r = MediaMetadataRetriever()
                try {
                    r.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                    r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                } finally {
                    r.release()
                }
            }
        }.getOrDefault(0L)
        synchronized(durations) { durations[asset] = ms }
        return ms
    }

    private fun item(dua: Dua): MediaItem = MediaItem.Builder()
        .setMediaId(dua.id)
        .setUri("asset:///audio/${dua.audioAsset}")
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(dua.title)
                .setArtist(dua.tag)
                .build(),
        )
        .build()

    /** Plays [duas] one after another, starting at [startIndex]. */
    fun playQueue(duas: List<Dua>, startIndex: Int = 0, startMs: Long = 0) {
        if (duas.isEmpty()) return
        round = 1
        withController { c ->
            c.setMediaItems(duas.map(::item), startIndex.coerceIn(0, duas.lastIndex), startMs)
            c.prepare()
            c.play()
        }
    }

    fun playDua(dua: Dua, startMs: Long = 0) = playQueue(listOf(dua), 0, startMs)

    /** Seek inside [dua] if it is the current track, otherwise start it at [positionMs]. */
    fun playFrom(dua: Dua, positionMs: Long) {
        if (_state.value.mediaId == dua.id) {
            withController { it.seekTo(positionMs); it.play() }
        } else {
            playDua(dua, positionMs)
        }
    }

    fun toggle() = withController { c ->
        if (c.isPlaying) c.pause() else {
            if (c.playbackState == Player.STATE_ENDED) c.seekTo(0)
            c.play()
        }
    }

    fun next() = withController { it.seekToNextMediaItem() }
    fun previous() = withController { it.seekToPreviousMediaItem() }
    fun seekTo(ms: Long) = withController { it.seekTo(ms) }
    /** Play the queue [count] times (0 = forever, 1 = once). */
    fun setRounds(count: Int) {
        rounds = count.coerceAtLeast(0)
        if (rounds != 0 && round > rounds) round = rounds
        refresh()
    }

    fun setRepeatAll(on: Boolean) = withController { it.repeatMode = if (on) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF }
}
