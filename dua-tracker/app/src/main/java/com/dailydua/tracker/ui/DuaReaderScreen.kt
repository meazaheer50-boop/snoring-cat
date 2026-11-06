package com.dailydua.tracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.Settings
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens
import kotlinx.coroutines.delay

/** Full-screen reader for a long dua. The line being recited is highlighted and followed as the audio plays. */
@Composable
fun DuaReaderScreen(
    dua: Dua,
    done: Boolean,
    settings: Settings,
    onToggleDone: () -> Unit,
    onClose: () -> Unit,
) {
    val c = Tokens.colors
    val context = LocalContext.current
    val player = remember(dua.id) { AudioPlayer(context, dua.audioAsset) }
    DisposableEffect(player) { onDispose { player.release() } }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { player.pause() }
    BackHandler(onBack = onClose)

    var positionMs by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(player) {
        while (true) {
            positionMs = player.positionMs
            playing = player.isPlaying
            delay(150)
        }
    }

    val current = if (!started) -1 else dua.lines.indexOfLast { it.startMs <= positionMs }
    val listState = rememberLazyListState()
    LaunchedEffect(current) {
        if (current >= 0 && playing) listState.animateScrollToItem(current + 1, scrollOffset = -160)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.surface100)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Back", onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = c.ink) }
            Text(dua.title, style = DuaType.title, color = c.ink, modifier = Modifier.weight(1f).padding(start = 4.dp))
            Box(
                Modifier
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onToggleDone)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (done) "Done" else "Mark as done", style = DuaType.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = c.brand)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Column(Modifier.padding(bottom = 8.dp)) {
                    Text(dua.tag, style = DuaType.caption, color = c.inkMuted)
                    if (!player.available) {
                        Text("Audio is not included in this build.", style = DuaType.caption, color = c.inkMuted)
                    }
                    dua.source?.let { Text(it, style = DuaType.caption, color = c.inkMuted) }
                }
            }
            itemsIndexed(dua.lines) { i, line ->
                val active = i == current
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (active) c.brandSoft else c.surfaceCard)
                        .clickable(enabled = player.available) {
                            started = true
                            player.seekTo(line.startMs.toInt())
                            player.play()
                        }
                        .padding(16.dp),
                ) {
                    Text(
                        line.arabic,
                        style = DuaType.arabic(settings.arabicSize),
                        color = c.ink,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (settings.showTranslation && line.translation.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(line.translation, style = DuaType.body, color = if (active) c.ink else c.inkMuted)
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }

        if (player.available) {
            val duration = player.durationMs.coerceAtLeast(1)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(c.surface200)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Slider(
                    value = positionMs.toFloat().coerceIn(0f, duration.toFloat()),
                    onValueChange = { started = true; positionMs = it.toInt(); player.seekTo(it.toInt()) },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = c.brand,
                        activeTrackColor = c.brand,
                        inactiveTrackColor = c.border.copy(alpha = 0.4f),
                    ),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(clock(positionMs), style = DuaType.caption, color = c.inkMuted, modifier = Modifier.width(56.dp))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        PlayButton(playing) {
                            started = true
                            if (playing) player.pause() else player.play()
                            playing = player.isPlaying
                        }
                    }
                    Text(clock(duration), style = DuaType.caption, color = c.inkMuted, modifier = Modifier.width(56.dp))
                }
            }
        } else {
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun PlayButton(playing: Boolean, onClick: () -> Unit) {
    val c = Tokens.colors
    Box(
        Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(c.brand)
            .clickable(role = Role.Button, onClickLabel = if (playing) "Pause" else "Play", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (playing) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(2) { Box(Modifier.size(width = 6.dp, height = 20.dp).clip(RoundedCornerShape(2.dp)).background(c.onBrand)) }
            }
        } else {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = c.onBrand, modifier = Modifier.size(30.dp))
        }
    }
}

private fun clock(ms: Int): String {
    val total = ms / 1000
    return "%d:%02d".format(total / 60, total % 60)
}
