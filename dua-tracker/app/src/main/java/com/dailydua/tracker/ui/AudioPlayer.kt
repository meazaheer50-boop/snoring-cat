package com.dailydua.tracker.ui

import android.content.Context
import android.media.MediaPlayer

/** Plays an MP3 bundled under assets/audio. If the file is not in the build, [available] is false. */
class AudioPlayer(context: Context, asset: String?) {
    private var player: MediaPlayer? = null
    val available: Boolean

    init {
        var ok = false
        if (asset != null) {
            try {
                context.assets.openFd("audio/$asset").use { afd ->
                    player = MediaPlayer().apply {
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        prepare()
                    }
                }
                ok = true
            } catch (e: Exception) {
                player?.release()
                player = null
            }
        }
        available = ok
    }

    val durationMs: Int get() = player?.duration ?: 0
    val positionMs: Int get() = player?.currentPosition ?: 0
    val isPlaying: Boolean get() = player?.isPlaying ?: false

    fun play() { player?.start() }
    fun pause() { if (player?.isPlaying == true) player?.pause() }
    fun seekTo(ms: Int) { player?.seekTo(ms) }
    fun release() {
        player?.release()
        player = null
    }
}
