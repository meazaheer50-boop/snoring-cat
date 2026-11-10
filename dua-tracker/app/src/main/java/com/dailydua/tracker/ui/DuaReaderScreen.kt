package com.dailydua.tracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailydua.tracker.playback.PlayerHub
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.Settings
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Full-screen glassmorphic reader for a long dua. The line being recited is highlighted and followed as the audio plays. */
@Composable
fun DuaReaderScreen(
    dua: Dua,
    done: Boolean,
    settings: Settings,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSize: (Int) -> Unit,
    onToggleDone: () -> Unit,
    onClose: () -> Unit,
) {
    val c = Tokens.colors
    val context = LocalContext.current
    val ps by PlayerHub.state.collectAsStateWithLifecycle()
    val available = remember(dua.id) { PlayerHub.hasAudio(context, dua) }
    val ask = LocalAskNotifications.current
    val fileDuration by produceState(0L, dua.id) {
        value = withContext(Dispatchers.IO) { PlayerHub.assetDurationMs(context, dua) }
    }
    val isThis = ps.mediaId == dua.id
    val positionMs = if (isThis) ps.positionMs.toInt() else 0
    val playing = isThis && ps.isPlaying
    val started = isThis
    BackHandler(onBack = onClose)

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
                        .bounceClick(pressedScale = 0.92f, role = Role.Button, onClickLabel = "Back", onClick = onClose),
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
                    Text(if (dua.audioAsset == null) "${dua.lines.size} steps" else "${dua.lines.size} lines \u00b7 tap a line to play from there", style = DuaType.caption, color = c.inkMuted, maxLines = 1)
                }
                Row(
                    Modifier
                        .heightIn(min = 40.dp)
                        .glassSurface(
                            shape = CircleShape,
                            variant = if (done) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
                        )
                        .bounceClick(pressedScale = 0.94f, role = Role.Button, onClick = onToggleDone)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedVisibility(
                        visible = done,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                            fadeIn(tween(180)) +
                            expandHorizontally(tween(180)),
                        exit = scaleOut(tween(140)) +
                            fadeOut(tween(140)) +
                            shrinkHorizontally(tween(140)),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = c.brand,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                    }
                    AnimatedContent(
                        targetState = if (done) "Done" else "Mark done",
                        transitionSpec = {
                            (slideInVertically { it / 2 } + fadeIn(tween(200)))
                                .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(150)))
                        },
                        label = "readerDoneText",
                    ) { label ->
                        Text(
                            label,
                            style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
                            color = c.brand,
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FavoriteButton(favorite, onToggleFavorite)
                Spacer(Modifier.weight(1f))
                TextSizeControl(settings.arabicSize, onSize)
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "reader_banner") {
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
                        if (dua.audioAsset != null && !available) {
                            Text("Audio is not included in this build.", style = DuaType.caption, color = c.inkMuted)
                        }
                        dua.source?.let { Text(it, style = DuaType.caption, color = c.inkMuted) }
                    }
                }
                itemsIndexed(dua.lines, key = { i, _ -> i }) { i, line ->
                    val active = i == current
                    val cardScale by animateFloatAsState(
                        targetValue = if (active) 1.015f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        label = "verseCardScale",
                    )
                    val chipBg by animateColorAsState(
                        targetValue = if (active) c.brand.copy(alpha = 0.22f) else c.hairline,
                        animationSpec = tween(280),
                        label = "verseChipBg",
                    )
                    val chipInk by animateColorAsState(
                        targetValue = if (active) c.brand else c.inkMuted,
                        animationSpec = tween(280),
                        label = "verseChipInk",
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = cardScale
                                scaleY = cardScale
                            }
                            .glassSurface(
                                shape = RoundedCornerShape(22.dp),
                                variant = if (active) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
                                borderWidth = if (active) 1.5.dp else 1.dp,
                            )
                            .bounceClick(
                                pressedScale = 0.98f,
                                enabled = available,
                            ) { ask(); PlayerHub.playFrom(dua, line.startMs) }
                            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
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
                                    .background(chipBg)
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    line.heading ?: if (available) "${i + 1} · ${clock(line.startMs.toInt())}" else "${i + 1}",
                                    style = DuaType.label,
                                    color = chipInk,
                                )
                            }
                            AnimatedVisibility(
                                visible = active,
                                enter = fadeIn(tween(240)) + scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                ),
                                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.85f, animationSpec = tween(180)),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    RecitingEqualizer(playing = playing)
                                    Text(
                                        "RECITING",
                                        style = DuaType.label,
                                        color = c.brand,
                                    )
                                }
                            }
                        }
                        if (line.arabic.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                line.arabic,
                                style = DuaType.arabic(settings.arabicSize),
                                color = c.ink,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if ((settings.showTranslation || line.heading != null) && line.translation.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(line.translation, style = DuaType.body, color = if (active) c.ink else c.inkMuted)
                        }
                    }
                }
                item(key = "bottom_spacer") { Spacer(Modifier.height(16.dp)) }
            }

            if (available) {
                val duration = (if (isThis && ps.durationMs > 0) ps.durationMs else fileDuration).toInt().coerceAtLeast(1)
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
                        onValueChange = { PlayerHub.playFrom(dua, it.toLong()) },
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
                                ask()
                                if (isThis) PlayerHub.toggle() else PlayerHub.playDua(dua)
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

/** 3-bar mini equalizer indicator that animates when recitation audio is actively playing. */
@Composable
private fun RecitingEqualizer(playing: Boolean) {
    val c = Tokens.colors
    val infinite = rememberInfiniteTransition(label = "equalizer")
    val bar1 by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eqBar1",
    )
    val bar2 by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 310, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eqBar2",
    )
    val bar3 by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 440, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eqBar3",
    )
    val heights = if (playing) listOf(bar1, bar2, bar3) else listOf(0.4f, 0.65f, 0.4f)
    Row(
        modifier = Modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        heights.forEach { frac ->
            Box(
                Modifier
                    .width(3.dp)
                    .height((14f * frac).dp)
                    .clip(CircleShape)
                    .background(c.brand),
            )
        }
    }
}

@Composable
private fun PlayButton(playing: Boolean, onClick: () -> Unit) {
    val c = Tokens.colors
    val infinite = rememberInfiniteTransition(label = "playHalo")
    val haloScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "haloScale",
    )
    val haloAlpha by animateFloatAsState(
        targetValue = if (playing) 0.26f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "haloAlpha",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = if (playing) haloScale else 1f
                    scaleY = if (playing) haloScale else 1f
                    alpha = haloAlpha
                }
                .clip(CircleShape)
                .background(c.brand),
        )
        Box(
            Modifier
                .size(56.dp)
                .testTag("play")
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(c.brand, c.brand.copy(alpha = 0.80f)),
                    ),
                )
                .border(1.dp, c.glassBrandBorder, CircleShape)
                .bounceClick(
                    pressedScale = 0.90f,
                    role = Role.Button,
                    onClickLabel = if (playing) "Pause" else "Play",
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = playing,
                transitionSpec = {
                    (scaleIn(
                        initialScale = 0.65f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    ) + fadeIn(tween(180)))
                        .togetherWith(
                            scaleOut(targetScale = 0.65f, animationSpec = tween(140)) +
                                fadeOut(tween(140)),
                        )
                },
                label = "playPauseIcon",
            ) { isPlaying ->
                if (isPlaying) {
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
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = c.onBrand,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        }
    }
}

private fun clock(ms: Int): String {
    val total = ms / 1000
    return "%d:%02d".format(total / 60, total % 60)
}

