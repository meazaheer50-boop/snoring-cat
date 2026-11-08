package com.dailydua.tracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.Settings
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens
import kotlinx.coroutines.delay

/** Full-screen glassmorphic reader for a long dua. The line being recited is highlighted and followed as the audio plays. */
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

    GlassBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Floating glass top bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .glassSurface(
                        shape = RoundedCornerShape(24.dp),
                        variant = GlassVariant.ELEVATED,
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
                        .clickable(role = Role.Button, onClickLabel = "Back", onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = c.ink)
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(dua.title, style = DuaType.title, color = c.ink)
                    Text("${dua.lines.size} verses", style = DuaType.caption, color = c.inkMuted)
                }
                Row(
                    Modifier
                        .heightIn(min = 40.dp)
                        .glassSurface(
                            shape = CircleShape,
                            variant = if (done) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
                        )
                        .clickable(role = Role.Button, onClick = onToggleDone)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (done) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = c.brand,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        if (done) "Done" else "Mark done",
                        style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
                        color = c.brand,
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .glassSurface(
                                shape = RoundedCornerShape(20.dp),
                                variant = GlassVariant.GOLD,
                            )
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            dua.tag,
                            style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
                            color = c.goldInk,
                        )
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
                            .glassSurface(
                                shape = RoundedCornerShape(22.dp),
                                variant = if (active) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
                                borderWidth = if (active) 1.5.dp else 1.dp,
                            )
                            .clickable(enabled = player.available) {
                                started = true
                                player.seekTo(line.startMs.toInt())
                                player.play()
                            }
                            .padding(18.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .clip(CircleShape)
                                    .background(if (active) c.brand.copy(alpha = 0.22f) else c.hairline)
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    "${i + 1} · ${clock(line.startMs.toInt())}",
                                    style = DuaType.label,
                                    color = if (active) c.brand else c.inkMuted,
                                )
                            }
                            if (active) {
                                Text(
                                    "RECITING",
                                    style = DuaType.label,
                                    color = c.brand,
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            line.arabic,
                            style = DuaType.arabic(settings.arabicSize),
                            color = c.ink,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (settings.showTranslation && line.translation.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
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
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .glassSurface(
                            shape = RoundedCornerShape(28.dp),
                            variant = GlassVariant.ELEVATED,
                        )
                        .padding(horizontal = 18.dp, vertical = 12.dp),
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
}

@Composable
private fun PlayButton(playing: Boolean, onClick: () -> Unit) {
    val c = Tokens.colors
    Box(
        Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(c.brand, c.brand.copy(alpha = 0.80f)),
                ),
            )
            .border(1.dp, c.glassBrandBorder, CircleShape)
            .clickable(role = Role.Button, onClickLabel = if (playing) "Pause" else "Play", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (playing) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(2) {
                    Box(
                        Modifier
                            .size(width = 6.dp, height = 20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(c.onBrand),
                    )
                }
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

