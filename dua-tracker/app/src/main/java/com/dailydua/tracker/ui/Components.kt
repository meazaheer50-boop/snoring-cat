package com.dailydua.tracker.ui

import android.provider.Settings as AndroidSettings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import com.dailydua.tracker.playback.AudioLevel
import com.dailydua.tracker.playback.PlayerHub
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens
import kotlinx.coroutines.delay

enum class GlassVariant {
    FROST,
    SOFT_EMERALD,
    EMERALD,
    GOLD,
    ELEVATED,
}

/** Tactile spring press-scale modifier for interactive glass surfaces and buttons. */
@Composable
fun Modifier.bounceClick(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true,
    role: Role? = null,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "bounceClickScale",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClickLabel = onClickLabel,
            onClick = onClick,
        )
}

/** False once a screen has already played its entrance, so cards show instantly. */
val LocalEntrance = androidx.compose.runtime.compositionLocalOf { true }

/** Staggered entrance animation wrapper that fades and slides items upward into place. */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animate = LocalEntrance.current
    var visible by remember { mutableStateOf(!animate) }
    LaunchedEffect(Unit) {
        if (animate) delay((index.coerceIn(0, 10) * 38L))
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (animate) tween(durationMillis = 360, easing = FastOutSlowInEasing) else snap(),
        label = "staggerAlpha",
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 28f,
        animationSpec = if (animate) spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ) else snap(),
        label = "staggerOffsetY",
    )
    Box(
        modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offsetY
        },
    ) {
        content()
    }
}

@Composable
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(24.dp),
    variant: GlassVariant = GlassVariant.FROST,
    borderWidth: Dp = 1.dp,
): Modifier {
    val c = Tokens.colors
    val (targetFillTop, targetFillBottom, targetBorderTop, targetBorderBottom) = when (variant) {
        GlassVariant.FROST -> listOf(c.glassFillTop, c.glassFillBottom, c.glassBorderTop, c.glassBorderBottom)
        GlassVariant.SOFT_EMERALD -> listOf(
            c.brandSoft,
            c.glassFillBottom,
            c.glassBrandBorder,
            c.glassBorderBottom,
        )
        GlassVariant.EMERALD -> listOf(
            c.glassBrandTop,
            c.glassBrandBottom,
            c.glassBrandBorder,
            c.glassBorderBottom,
        )
        GlassVariant.GOLD -> listOf(
            c.glassGoldTop,
            c.glassGoldBottom,
            c.glassGoldBorder,
            c.glassBorderBottom,
        )
        GlassVariant.ELEVATED -> listOf(
            c.glassElevatedTop,
            c.glassElevatedBottom,
            c.glassBorderTop,
            c.glassBorderBottom,
        )
    }
    val colorSpec = tween<Color>(durationMillis = 360, easing = FastOutSlowInEasing)
    val fillTop by animateColorAsState(targetFillTop, colorSpec, label = "glassFillTop")
    val fillBottom by animateColorAsState(targetFillBottom, colorSpec, label = "glassFillBottom")
    val borderTop by animateColorAsState(targetBorderTop, colorSpec, label = "glassBorderTop")
    val borderBottom by animateColorAsState(targetBorderBottom, colorSpec, label = "glassBorderBottom")
    val animatedBorderWidth by animateDpAsState(
        targetValue = borderWidth,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "glassBorderWidth",
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(fillTop, fillBottom),
                start = Offset(0f, 0f),
                end = Offset(900f, 900f),
            ),
            shape = shape,
        )
        .drawBehind {
            // Light catching the top edge of the glass.
            drawRect(
                brush = Brush.verticalGradient(
                    0f to c.glassHighlight.copy(alpha = c.glassHighlight.alpha * if (c.isDark) 0.5f else 0.35f),
                    0.45f to Color.Transparent,
                ),
            )
        }
        .border(
            width = animatedBorderWidth,
            brush = Brush.linearGradient(
                colors = listOf(borderTop, borderBottom),
                start = Offset(0f, 0f),
                end = Offset(800f, 800f),
            ),
            shape = shape,
        )
}

/** True when the user has turned animations off in Android settings. */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            AndroidSettings.Global.getFloat(context.contentResolver, AndroidSettings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/**
 * Living background behind the glass: drifting light orbs, a slow aurora ribbon, a faint rotating
 * eight-point star and soft rising motes. Animated values are only read while drawing, so the
 * screen content above is not recomposed every frame.
 */
@Composable
fun GlassBackground(content: @Composable BoxScope.() -> Unit) {
    val c = Tokens.colors
    val reduceMotion = rememberReduceMotion()
    val infinite = rememberInfiniteTransition(label = "ambient")
    val driftA = infinite.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(13000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "driftA",
    )
    val driftB = infinite.animateFloat(
        1f, -1f, infiniteRepeatable(tween(17000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "driftB",
    )
    // One full turn of the wave phase; every wave uses whole multiples so the loop is seamless.
    val flow = infinite.animateFloat(
        0f, 6.2831855f, infiniteRepeatable(tween(36000, easing = LinearEasing)), label = "flow",
    )

    // The background moves with the recitation: waves swell and glows brighten with the voice.
    // Only the on/off change is observed, so the position ticks do not recompose the whole app.
    val isPlaying by remember {
        PlayerHub.state.map { it.isPlaying }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(false)
    val playGain by animateFloatAsState(if (isPlaying) 1f else 0f, tween(700), label = "playGain")

    val style = LocalBackgroundStyle.current
    val design = LocalBackgroundDesign.current
    val custom = style.id != "emerald"
    val base = if (c.isDark) style.darkBase else style.lightBase
    val orbA = if (custom) style.a.copy(alpha = if (c.isDark) 0.28f else 0.22f) else c.orbPrimary
    val orbB = if (custom) style.b.copy(alpha = if (c.isDark) 0.20f else 0.18f) else c.orbSecondary
    val orbC = if (custom) style.c.copy(alpha = if (c.isDark) 0.20f else 0.16f) else c.orbTertiary
    val bgTop by animateColorAsState(base ?: c.surface100, tween(450), label = "bgTop")
    val bgMid by animateColorAsState(base ?: c.bgMid, tween(450), label = "bgMid")
    val bgBot by animateColorAsState(base ?: c.surface200, tween(450), label = "bgBot")

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgMid, bgBot))),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val voice = if (reduceMotion) 0f else AudioLevel.level * playGain
            val dA = if (reduceMotion) 0f else driftA.value
            val dB = if (reduceMotion) 0f else driftB.value
            val t = if (reduceMotion) 1.2f else flow.value

            fun orb(color: Color, x: Float, y: Float, radius: Float) {
                val center = Offset(x, y)
                drawCircle(
                    brush = Brush.radialGradient(listOf(color, Color.Transparent), center = center, radius = radius),
                    radius = radius,
                    center = center,
                )
            }
            if (!style.waves) return@Canvas
            orb(orbB, w * (0.90f + 0.05f * dA), h * (0.08f + 0.03f * dB), w * 0.70f * (1f + 0.22f * voice))
            orb(orbA, w * (0.06f + 0.06f * dB), h * (0.30f + 0.04f * dA), w * 0.78f * (1f + 0.18f * voice))

            // Layered silk: three slow ribbons of colour rolling across the lower screen.
            fun silk(baseY: Float, ampIn: Float, k1: Float, k2: Float, phase: Float, dir: Float, fillIn: Color, edge: Color) {
                val amp = ampIn * (1f + 2.4f * voice + 0.25f * playGain)
                val fill = fillIn.copy(alpha = (fillIn.alpha * (1f + 1.1f * voice)).coerceAtMost(1f))
                fun yAt(x: Float): Float {
                    val u = x / w
                    return baseY +
                        amp * kotlin.math.sin(u * 6.2831855f * k1 + dir * t + phase) +
                        amp * 0.45f * kotlin.math.sin(u * 6.2831855f * k2 - 2f * dir * t + phase * 1.7f)
                }
                val top = Path()
                var x = 0f
                top.moveTo(0f, yAt(0f))
                while (x < w) { x += 14f; top.lineTo(minOf(x, w), yAt(minOf(x, w))) }
                val body = Path().apply {
                    addPath(top)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    body,
                    brush = Brush.verticalGradient(
                        listOf(fill, fill.copy(alpha = fill.alpha * 0.25f), Color.Transparent),
                        startY = baseY - amp * 1.5f,
                        endY = baseY + h * 0.30f,
                    ),
                )
                drawPath(top, color = edge, style = Stroke(1.2.dp.toPx()))
            }
            val edgeA = Color.White.copy(alpha = if (c.isDark) 0.10f else 0.55f)
            when (design) {
                "aurora" -> {
                    // Soft curtains of light swaying from the top of the screen.
                    val cols = listOf(orbA, orbC, orbB)
                    for (i in 0 until 3) {
                        val cx = w * (0.2f + 0.3f * i)
                        val path = Path()
                        var y = -20f
                        while (y <= h * 0.75f) {
                            val u = y / h
                            val x = cx + w * 0.10f * kotlin.math.sin(u * 7f + t * (if (i % 2 == 0) 1 else -1) + i * 2f) * (1f + 0.8f * voice) +
                                w * 0.04f * dA
                            if (y < 0f) path.moveTo(x, y) else path.lineTo(x, y)
                            y += 24f
                        }
                        val col = cols[i]
                        for ((wd, al) in listOf(0.34f to 0.35f, 0.22f to 0.5f, 0.11f to 0.7f)) {
                            drawPath(
                                path,
                                brush = Brush.verticalGradient(
                                    listOf(Color.Transparent, col.copy(alpha = col.alpha * al * (1f + 0.9f * voice)), Color.Transparent),
                                    startY = 0f, endY = h * 0.75f,
                                ),
                                style = Stroke(w * wd, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            )
                        }
                    }
                }
                "bokeh" -> {
                    // Slow, out-of-focus lights rising and breathing with the voice.
                    val cols = listOf(orbA, orbB, orbC)
                    for (i in 0 until 16) {
                        val fx = ((i * 0.6180339f) % 1f)
                        val speed = 0.5f + (i % 4) * 0.25f
                        val prog = ((i * 0.37f) + t / 6.2831855f * speed) % 1f
                        val y = h * (1.1f - 1.25f * prog)
                        val x = w * (0.05f + 0.9f * fx) + w * 0.03f * kotlin.math.sin(t * 2f + i)
                        val r = w * (0.07f + 0.05f * ((i * 7) % 5) / 4f) * (1f + 0.5f * voice)
                        val fade = kotlin.math.sin(prog * 3.1415927f)
                        val col = cols[i % 3]
                        orb(col.copy(alpha = (col.alpha * 2.2f * fade).coerceIn(0f, 1f)), x, y, r)
                    }
                }
                "dunes" -> {
                    // Broad, slow hills layered like sand.
                    silk(h * (0.66f + 0.01f * dA), h * 0.060f, 0.55f, 1.2f, 0.5f, 1f, orbB.copy(alpha = orbB.alpha * 1.6f), edgeA)
                    silk(h * (0.78f + 0.01f * dB), h * 0.055f, 0.7f, 1.5f, 2.4f, -1f, orbC.copy(alpha = orbC.alpha * 1.6f), edgeA)
                    silk(h * (0.90f + 0.01f * dA), h * 0.045f, 0.9f, 1.8f, 4.1f, 1f, orbA.copy(alpha = orbA.alpha * 1.6f), edgeA)
                }
                else -> {
                    silk(h * (0.62f + 0.01f * dA), h * 0.030f, 1.1f, 2.3f, 0.0f, 1f, orbC, edgeA)
                    silk(h * (0.74f + 0.01f * dB), h * 0.034f, 0.9f, 1.9f, 2.1f, -1f, orbB, edgeA)
                    silk(h * (0.86f + 0.01f * dA), h * 0.028f, 1.3f, 2.7f, 4.0f, 1f, orbA, edgeA)
                }
            }
        }
        content()
    }
}

/** A soft band of light that glides across a surface every few seconds. */
@Composable
fun Modifier.shimmerSweep(periodMs: Int = 6500): Modifier {
    if (rememberReduceMotion()) return this
    val c = Tokens.colors
    val progress = rememberInfiniteTransition(label = "shimmer").animateFloat(
        -0.6f, 1.6f, infiniteRepeatable(tween(periodMs, easing = LinearEasing)), label = "shimmerX",
    )
    return this.drawWithContent {
        drawContent()
        val x = progress.value * size.width
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = if (c.isDark) 0.06f else 0.22f), Color.Transparent),
                start = Offset(x - size.width * 0.25f, 0f),
                end = Offset(x + size.width * 0.25f, size.height),
            ),
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, badge: String? = null) {
    val c = Tokens.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text.uppercase(), style = DuaType.label, color = c.inkMuted)
        if (badge != null) {
            Box(
                Modifier
                    .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = badge,
                    transitionSpec = {
                        (slideInVertically { it / 2 } + fadeIn(tween(220)))
                            .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(180)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "sectionBadge",
                ) { targetBadge ->
                    Text(targetBadge, style = DuaType.label, color = c.brand)
                }
            }
        }
    }
}

@Composable
fun CheckCircle(done: Boolean) {
    val c = Tokens.colors
    val scale by animateFloatAsState(
        targetValue = if (done) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "checkScale",
    )
    val bgTop by animateColorAsState(
        targetValue = if (done) c.brand else c.glassFillTop,
        animationSpec = tween(280),
        label = "checkBgTop",
    )
    val bgBot by animateColorAsState(
        targetValue = if (done) c.brand.copy(alpha = 0.78f) else c.glassFillBottom,
        animationSpec = tween(280),
        label = "checkBgBot",
    )
    val borderColor by animateColorAsState(
        targetValue = if (done) c.glassBrandBorder else c.border,
        animationSpec = tween(280),
        label = "checkBorder",
    )
    Box(
        Modifier
            .size(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                Brush.linearGradient(colors = listOf(bgTop, bgBot)),
                CircleShape,
            )
            .border(if (done) 1.dp else 1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = done,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeIn(tween(180)),
            exit = scaleOut(tween(150)) + fadeOut(tween(150)),
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = c.onBrand, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun DuaRow(dua: Dua, done: Boolean, favorite: Boolean = false, onFavorite: (() -> Unit)? = null, onClick: () -> Unit) {
    val c = Tokens.colors
    val hasAudio = dua.audioAsset != null
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp)
            .glassSurface(
                shape = RoundedCornerShape(24.dp),
                variant = if (done) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
            )
            .bounceClick(pressedScale = 0.97f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (onFavorite != null) FavoriteButton(favorite, onFavorite, size = 44.dp) else CheckCircle(done)
        Column(Modifier.weight(1f)) {
            Text(dua.title, style = DuaType.title, color = c.ink)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(c.hairline)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(dua.tag, style = DuaType.caption, color = c.inkMuted)
                }
                AnimatedVisibility(
                    visible = done,
                    enter = fadeIn(tween(220)) + expandHorizontally(tween(220)),
                    exit = fadeOut(tween(160)) + shrinkHorizontally(tween(160)),
                ) {
                    Text("· Done", style = DuaType.caption.copy(fontWeight = FontWeight.SemiBold), color = c.brand)
                }
            }
        }
        if (hasAudio) {
            Box(
                Modifier
                    .size(34.dp)
                    .glassSurface(shape = CircleShape, variant = GlassVariant.SOFT_EMERALD),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = c.brand,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Tokens.colors
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(c.brand, c.brand.copy(alpha = 0.82f)),
                ),
            )
            .border(1.dp, c.glassBrandBorder, CircleShape)
            .bounceClick(pressedScale = 0.96f, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                (fadeIn(tween(200)) + slideInVertically { it / 3 })
                    .togetherWith(fadeOut(tween(150)) + slideOutVertically { -it / 3 })
            },
            label = "primaryButtonText",
        ) { targetText ->
            Text(targetText, style = DuaType.body.copy(fontWeight = FontWeight.Bold), color = c.onBrand)
        }
    }
}

@Composable
fun OutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Tokens.colors
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
            .bounceClick(pressedScale = 0.96f, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = DuaType.body.copy(fontWeight = FontWeight.Bold), color = c.brand)
    }
}

/** Pill-shaped glassmorphic segmented selector with animated selection state. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val c = Tokens.colors
    Row(
        Modifier
            .fillMaxWidth()
            .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            val pillAlpha by animateFloatAsState(
                targetValue = if (on) 1f else 0f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "segmentPillAlpha",
            )
            val pillScale by animateFloatAsState(
                targetValue = if (on) 1f else 0.95f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "segmentPillScale",
            )
            val textColor by animateColorAsState(
                targetValue = if (on) c.onBrand else c.ink,
                animationSpec = tween(220),
                label = "segmentTextColor",
            )
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .graphicsLayer {
                        scaleX = pillScale
                        scaleY = pillScale
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                c.brand.copy(alpha = pillAlpha),
                                c.brand.copy(alpha = 0.85f * pillAlpha),
                            ),
                        ),
                        CircleShape,
                    )
                    .border(1.dp, c.glassBrandBorder.copy(alpha = pillAlpha), CircleShape)
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = DuaType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Glassmorphic toggle switch visual with spring physics. The row that contains it carries the toggle semantics. */
@Composable
fun SwitchVisual(checked: Boolean) {
    val c = Tokens.colors
    val x by animateDpAsState(
        targetValue = if (checked) 24.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "switchThumbX",
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "switchThumbScale",
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) c.onBrand else c.inkMuted.copy(alpha = 0.65f),
        animationSpec = tween(240),
        label = "switchThumbColor",
    )
    Box(
        Modifier
            .size(width = 52.dp, height = 32.dp)
            .glassSurface(
                shape = CircleShape,
                variant = if (checked) GlassVariant.EMERALD else GlassVariant.FROST,
            ),
    ) {
        Box(
            Modifier
                .offset(x = x, y = 4.dp)
                .size(24.dp)
                .graphicsLayer {
                    scaleX = thumbScale
                    scaleY = thumbScale
                }
                .clip(CircleShape)
                .background(thumbColor)
                .border(1.dp, c.glassHighlight, CircleShape),
        )
    }
}


/** A heart that fills in gold when the dua is a favourite. */
@Composable
fun FavoriteButton(favorite: Boolean, onClick: () -> Unit, size: androidx.compose.ui.unit.Dp = 44.dp) {
    val c = Tokens.colors
    val scale by androidx.compose.animation.core.animateFloatAsState(
        if (favorite) 1.12f else 1f,
        spring(dampingRatio = 0.35f, stiffness = 400f),
        label = "heart",
    )
    Box(
        Modifier
            .size(size)
            .testTag("favorite")
            .glassSurface(shape = CircleShape, variant = if (favorite) GlassVariant.GOLD else GlassVariant.FROST)
            .bounceClick(pressedScale = 0.88f, role = Role.Button, onClickLabel = if (favorite) "Remove from favourites" else "Add to favourites", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (favorite) "Favourite" else "Not a favourite",
            tint = if (favorite) c.goldInk else c.inkMuted,
            modifier = Modifier
                .size(size * 0.5f)
                .graphicsLayer { scaleX = scale; scaleY = scale },
        )
    }
}

/** A- and A+ buttons that make the Arabic text smaller or larger. */
@Composable
fun TextSizeControl(size: Int, onChange: (Int) -> Unit) {
    val c = Tokens.colors
    Row(
        Modifier
            .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        @Composable
        fun step(label: String, big: Boolean, enabled: Boolean, delta: Int) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .graphicsLayer { alpha = if (enabled) 1f else 0.35f }
                    .testTag(if (big) "sizeUp" else "sizeDown")
                    .bounceClick(pressedScale = 0.88f, role = Role.Button, onClickLabel = label) { if (enabled) onChange(delta) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (big) "A+" else "A\u2212",
                    style = DuaType.body.copy(fontWeight = FontWeight.Bold, fontSize = if (big) 18.sp else 14.sp),
                    color = c.ink,
                )
            }
        }
        step("Smaller text", false, size > MIN_ARABIC, -2)
        Text("$size", style = DuaType.caption, color = c.inkMuted, modifier = Modifier.padding(horizontal = 6.dp))
        step("Larger text", true, size < MAX_ARABIC, 2)
    }
}

/** A clock button that opens the reminder dialog for the dua being read. */
@Composable
fun ReminderButton(onClick: () -> Unit, size: androidx.compose.ui.unit.Dp = 44.dp) {
    val c = Tokens.colors
    Box(
        Modifier
            .size(size)
            .testTag("setReminder")
            .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
            .bounceClick(pressedScale = 0.88f, role = Role.Button, onClickLabel = "Set a reminder", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(AppIcons.Clock, contentDescription = "Set a reminder", tint = c.inkMuted, modifier = Modifier.size(size * 0.5f))
    }
}

/** Repeat control: choose how many rounds to play. Shows the current round while playing. */
@Composable
fun RepeatButton(modifier: Modifier = Modifier, alwaysLabel: Boolean = false) {
    val c = Tokens.colors
    val ps by com.dailydua.tracker.playback.PlayerHub.state.collectAsStateWithLifecycle()
    var open by remember { mutableStateOf(false) }
    val on = ps.rounds != 1
    val total = if (ps.rounds == 0) "\u221E" else "${ps.rounds}"
    val label = when {
        !on -> "Repeat"
        ps.queueIds.isNotEmpty() -> "${ps.round} / $total"
        else -> if (ps.rounds == 0) "\u221E" else "${ps.rounds}\u00D7"
    }
    Box(modifier) {
        Row(
            Modifier
                .heightIn(min = 44.dp)
                .testTag("repeat")
                .glassSurface(shape = CircleShape, variant = if (on) GlassVariant.SOFT_EMERALD else GlassVariant.FROST)
                .bounceClick(pressedScale = 0.92f, role = Role.Button, onClickLabel = "Choose how many times to repeat") { open = true }
                .padding(horizontal = if (on || alwaysLabel) 14.dp else 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(AppIcons.Repeat, contentDescription = "Repeat", tint = if (on) c.brand else c.inkMuted, modifier = Modifier.size(18.dp))
            if (on || alwaysLabel) Text(label, style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = if (on) c.brand else c.inkMuted, maxLines = 1)
        }
        androidx.compose.material3.DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.background(c.surface200)) {
            listOf(1, 2, 3, 5, 7, 10, 33, 0).forEach { n ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            when (n) { 1 -> "No repeat"; 0 -> "Repeat forever"; else -> "Repeat $n times" },
                            color = if (n == ps.rounds) c.brand else c.ink,
                            fontWeight = if (n == ps.rounds) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = { com.dailydua.tracker.playback.PlayerHub.setRounds(n); open = false },
                )
            }
        }
    }
}
