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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.repeatOnLifecycle
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.QuranFont
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
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
    // One clock drives every wave at the display's full refresh rate; it stops while the app is in the background.
    var clock by remember { mutableLongStateOf(0L) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            // Counted from when the screen opened, so the numbers stay small and the motion stays smooth.
            val start = withFrameNanos { it } - clock * 1_000_000L
            while (true) {
                val n = withFrameNanos { it }
                clock = (n - start) / 1_000_000L
            }
        }
    }

    // The background moves with the recitation: waves swell and glows brighten with the voice.
    // Only the on/off change is observed, so the position ticks do not recompose the whole app.
    val isPlaying by remember {
        PlayerHub.state.map { it.isPlaying }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(false)
    val playGain by animateFloatAsState(if (isPlaying) 1f else 0f, tween(700), label = "playGain")

    val style = LocalBackgroundStyle.current
    val design = LocalBackgroundDesign.current
    val textMeasurer = rememberTextMeasurer(cacheSize = 32)
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
        // Own layer: the moving background redraws without re-drawing the screens on top, and vice versa.
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val w = size.width
            val h = size.height
            val voice = if (reduceMotion) 0f else AudioLevel.level * playGain
            val ms = clock
            val dA = if (reduceMotion) 0f else kotlin.math.sin(ms * 6.2831855f / 26000f)
            val dB = if (reduceMotion) 0f else -kotlin.math.sin(ms * 6.2831855f / 34000f)
            val t = if (reduceMotion) 1.2f else (ms % 36000L) / 36000f * 6.2831855f

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
                while (x < w) { x += 28f; top.lineTo(minOf(x, w), yAt(minOf(x, w))) }
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
                    // Curtains of light swaying across the whole screen, top to bottom.
                    val cols = listOf(orbA, orbC, orbB, orbC, orbA)
                    for (i in 0 until 5) {
                        val cx = w * (0.1f + 0.2f * i)
                        val path = Path()
                        var y = -40f
                        while (y <= h + 40f) {
                            val u = y / h
                            val x = cx + w * 0.09f * kotlin.math.sin(u * 6f + t * (if (i % 2 == 0) 1 else -1) + i * 2f) * (1f + 0.8f * voice) +
                                w * 0.04f * (if (i % 2 == 0) dA else dB)
                            if (y < 0f) path.moveTo(x, y) else path.lineTo(x, y)
                            y += 48f
                        }
                        val col = cols[i]
                        val glow = (1f + 0.9f * voice)
                        for ((wd, al) in listOf(0.26f to 0.40f, 0.10f to 0.75f)) {
                            val a = (col.alpha * al * glow).coerceAtMost(1f)
                            drawPath(
                                path,
                                brush = Brush.verticalGradient(
                                    listOf(col.copy(alpha = a * 0.5f), col.copy(alpha = a), col.copy(alpha = a * 0.6f), col.copy(alpha = a)),
                                    startY = 0f, endY = h,
                                ),
                                style = Stroke(w * wd, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            )
                        }
                    }
                }
                "contour" -> {
                    // Thin flowing lines, like contours on a map of water.
                    val cols = listOf(orbA, orbC, orbB)
                    for (j in 0 until 10) {
                        val baseY = h * (0.12f + 0.085f * j)
                        val path = Path()
                        var x = 0f
                        fun yAt(xx: Float): Float {
                            val u = xx / w
                            return baseY +
                                h * 0.020f * (1f + 1.5f * voice) * kotlin.math.sin(u * 6.2831855f * 1.3f + t + j * 0.6f) +
                                h * 0.012f * kotlin.math.sin(u * 6.2831855f * 2.7f - 2f * t + j)
                        }
                        path.moveTo(0f, yAt(0f))
                        while (x < w) { x += 28f; path.lineTo(minOf(x, w), yAt(minOf(x, w))) }
                        val col = cols[j % 3]
                        drawPath(path, col.copy(alpha = (col.alpha * 2.6f).coerceAtMost(0.9f)), style = Stroke(1.4.dp.toPx()))
                    }
                }
                "veil" -> {
                    // Broad diagonal sheets of light sliding across the screen.
                    val cols = listOf(orbA, orbB, orbC)
                    val periods = longArrayOf(24000L, 31000L, 40000L)
                    for (i in 0 until 3) {
                        val prog = (ms % periods[i]).toFloat() / periods[i]
                        val x0 = w * (-0.9f + 2.1f * prog)
                        val col = cols[i]
                        drawRect(
                            Brush.linearGradient(
                                listOf(Color.Transparent, col.copy(alpha = (col.alpha * 1.8f * (1f + voice)).coerceAtMost(1f)), Color.Transparent),
                                start = Offset(x0, 0f), end = Offset(x0 + w * 0.8f, h * 0.55f),
                            ),
                        )
                    }
                }
                "rain" -> {
                    // Fine streaks of light falling at different speeds.
                    val cols = listOf(style.a, style.c, style.b)
                    for (i in 0 until 38) {
                        val x = w * ((i * 0.6180339f) % 1f)
                        val prog = (ms * (0.00005f + (i % 5) * 0.000018f) + i * 0.137f) % 1f
                        val y = h * (-0.1f + 1.2f * prog)
                        val len = h * (0.035f + 0.03f * (i % 4)) * (1f + voice)
                        val fade = kotlin.math.sin(prog * 3.1415927f)
                        val col = cols[i % 3].copy(alpha = (if (c.isDark) 0.45f else 0.35f) * fade)
                        drawLine(col, Offset(x, y - len), Offset(x, y), strokeWidth = (1f + (i % 3) * 0.5f) * density,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    }
                }
                "moon" -> {
                    // A soft moon glowing above slow drifting cloud.
                    val mx = w * 0.76f
                    val my = h * 0.17f
                    orb(style.c.copy(alpha = if (c.isDark) 0.30f else 0.22f), mx, my, w * (0.75f + 0.25f * voice))
                    orb(style.a.copy(alpha = if (c.isDark) 0.22f else 0.16f), mx, my, w * 0.38f)
                    drawCircle(
                        Color.White.copy(alpha = if (c.isDark) 0.92f else 0.85f),
                        radius = w * 0.075f, center = Offset(mx, my),
                    )
                    for (k in 0 until 3) {
                        val cx = w * (((ms / (60000f + k * 20000f)) + k * 0.37f) % 1.4f - 0.2f)
                        orb(Color.White.copy(alpha = if (c.isDark) 0.07f else 0.30f), cx, h * (0.22f + 0.07f * k), w * 0.5f)
                    }
                }
                "embers" -> {
                    // Warm motes rising from the bottom and fading out.
                    val cols = listOf(style.b, style.a, style.c)
                    orb(orbB.copy(alpha = (orbB.alpha * 1.6f).coerceAtMost(1f)), w * 0.5f, h * 1.05f, w * (0.9f + 0.4f * voice))
                    for (i in 0 until 30) {
                        val prog = (ms * (0.00004f + (i % 4) * 0.000015f) + i * 0.31f) % 1f
                        val x = w * ((i * 0.7236f) % 1f) + w * 0.05f * kotlin.math.sin(prog * 10f + i)
                        val y = h * (1.05f - 1.1f * prog)
                        val r = (1.2f + (i % 3)) * density * (1f + voice)
                        val col = cols[i % 3]
                        val fade = kotlin.math.sin(prog * 3.1415927f)
                        drawCircle(col.copy(alpha = 0.12f * fade), r * 3.5f, Offset(x, y))
                        drawCircle(col.copy(alpha = 0.9f * fade), r, Offset(x, y))
                    }
                }
                "mihrab" -> {
                    // Nested prayer-niche arches glowing softly from within.
                    val cx = w * 0.5f
                    val breathe = 0.5f + 0.5f * kotlin.math.sin(ms / 5200f)
                    orb(orbB.copy(alpha = (orbB.alpha * (1.8f + 0.8f * breathe + voice)).coerceAtMost(1f)), cx, h * 0.95f, w * (0.8f + 0.3f * voice))
                    for (k in 0 until 4) {
                        val hw = w * (0.44f - 0.085f * k)
                        val base = h * 1.02f
                        val spring = h * (0.42f + 0.07f * k)
                        val top = h * (0.16f + 0.085f * k)
                        val arch = Path().apply {
                            moveTo(cx - hw, base)
                            lineTo(cx - hw, spring)
                            cubicTo(cx - hw, spring - (spring - top) * 0.62f, cx - hw * 0.30f, top + (spring - top) * 0.12f, cx, top)
                            cubicTo(cx + hw * 0.30f, top + (spring - top) * 0.12f, cx + hw, spring - (spring - top) * 0.62f, cx + hw, spring)
                            lineTo(cx + hw, base)
                        }
                        val col = if (k % 2 == 0) style.b else style.a
                        val a = (if (c.isDark) 0.30f else 0.40f) * (1f - 0.15f * k) * (0.75f + 0.25f * breathe + 0.6f * voice)
                        drawPath(arch, col.copy(alpha = a.coerceAtMost(1f) * 0.25f), style = Stroke(6.dp.toPx()))
                        drawPath(arch, col.copy(alpha = a.coerceAtMost(1f)), style = Stroke(1.3.dp.toPx()))
                    }
                }
                "lanterns" -> {
                    // Ramadan lanterns hanging from above, swaying and glowing.
                    val xs = floatArrayOf(0.14f, 0.38f, 0.63f, 0.87f)
                    val drops = floatArrayOf(0.20f, 0.30f, 0.16f, 0.26f)
                    val sizes = floatArrayOf(0.9f, 1.15f, 0.8f, 1.0f)
                    for (i in 0 until 4) {
                        val px = w * xs[i]
                        val ly = h * drops[i]
                        val sc = w * 0.06f * sizes[i]
                        val angle = 5f * kotlin.math.sin(ms / (2300f + i * 300f) + i * 1.7f)
                        rotate(angle, Offset(px, 0f)) {
                            val warm = style.b
                            val flicker = 0.85f + 0.15f * kotlin.math.sin(ms / (180f + i * 37f))
                            drawLine(warm.copy(alpha = 0.35f), Offset(px, 0f), Offset(px, ly - sc * 1.6f), strokeWidth = 1.dp.toPx())
                            orb(warm.copy(alpha = ((if (c.isDark) 0.35f else 0.28f) * flicker * (1f + voice)).coerceAtMost(1f)), px, ly, sc * 4.5f)
                            val body = Path().apply {
                                moveTo(px, ly - sc * 1.6f)
                                quadraticTo(px + sc * 1.1f, ly - sc * 0.6f, px + sc * 0.8f, ly + sc * 0.6f)
                                quadraticTo(px + sc * 0.5f, ly + sc * 1.3f, px, ly + sc * 1.9f)
                                quadraticTo(px - sc * 0.5f, ly + sc * 1.3f, px - sc * 0.8f, ly + sc * 0.6f)
                                quadraticTo(px - sc * 1.1f, ly - sc * 0.6f, px, ly - sc * 1.6f)
                                close()
                            }
                            drawPath(body, warm.copy(alpha = 0.22f * flicker))
                            drawPath(body, warm.copy(alpha = 0.85f), style = Stroke(1.4.dp.toPx()))
                            drawLine(warm.copy(alpha = 0.6f), Offset(px - sc * 0.8f, ly + sc * 0.6f), Offset(px + sc * 0.8f, ly + sc * 0.6f), strokeWidth = 1.dp.toPx())
                            drawCircle(Color.White.copy(alpha = 0.85f * flicker), sc * 0.22f, Offset(px, ly + sc * 0.2f))
                        }
                    }
                }
                "hilal" -> {
                    // A new crescent moon over quiet desert dunes at dusk.
                    val mx = w * 0.72f
                    val my = h * 0.18f
                    val r = w * 0.10f
                    orb(style.b.copy(alpha = if (c.isDark) 0.22f else 0.18f), mx, my, w * (0.6f + 0.2f * voice))
                    val crescent = Path().apply {
                        op(
                            Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(mx, my), r)) },
                            Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(mx + r * 0.42f, my - r * 0.22f), r * 0.9f)) },
                            PathOperation.Difference,
                        )
                    }
                    drawPath(crescent, Color(0xFFFFF4D6).copy(alpha = if (c.isDark) 0.95f else 0.9f))
                    silk(h * (0.74f + 0.01f * dA), h * 0.05f, 0.5f, 1.1f, 0.4f, 1f, orbB.copy(alpha = (orbB.alpha * 1.8f).coerceAtMost(1f)), edgeA)
                    silk(h * (0.84f + 0.01f * dB), h * 0.045f, 0.65f, 1.4f, 2.6f, -1f, orbA.copy(alpha = (orbA.alpha * 1.8f).coerceAtMost(1f)), edgeA)
                }
                "tasbih" -> {
                    // A string of prayer beads hanging in a slow sway; one bead after another lights up.
                    val n = 33
                    val sway = w * 0.03f * kotlin.math.sin(ms / 2600f)
                    val warm = style.b
                    fun bead(u: Float) = Offset(
                        w * (0.08f + 0.84f * u) + sway * kotlin.math.sin(3.1415927f * u),
                        h * (0.02f + 0.42f * 4f * u * (1f - u)),
                    )
                    val cord = Path()
                    for (k in 0..40) { val p = bead(k / 40f); if (k == 0) cord.moveTo(p.x, p.y) else cord.lineTo(p.x, p.y) }
                    drawPath(cord, warm.copy(alpha = 0.35f), style = Stroke(1.dp.toPx()))
                    val active = ((ms / 650L) % n).toInt()
                    for (k in 0 until n) {
                        val p = bead((k + 0.5f) / n)
                        val on = k == active
                        val r = w * 0.014f * (if (on) 1.5f + voice else 1f)
                        if (on) orb(warm.copy(alpha = 0.55f), p.x, p.y, r * 6f)
                        drawCircle(warm.copy(alpha = if (on) 1f else if (c.isDark) 0.55f else 0.65f), r, p)
                        drawCircle(Color.White.copy(alpha = if (on) 0.9f else 0.25f), r * 0.35f, Offset(p.x - r * 0.3f, p.y - r * 0.3f))
                    }
                    val mid = bead(0.5f)
                    drawLine(warm.copy(alpha = 0.6f), mid, Offset(mid.x, mid.y + h * 0.07f), strokeWidth = 1.5.dp.toPx())
                    drawCircle(warm.copy(alpha = 0.8f), w * 0.02f, Offset(mid.x, mid.y + h * 0.08f))
                }
                "minarets" -> {
                    // A skyline of domes and minarets against the evening glow.
                    orb(orbB.copy(alpha = (orbB.alpha * (2.2f + voice)).coerceAtMost(1f)), w * 0.5f, h * 0.86f, w * (1.0f + 0.3f * voice))
                    fun skyline(baseY: Float, scale: Float, shift: Float, col: Color) {
                        val p = Path()
                        p.addRect(Rect(0f, baseY, w, h))
                        fun dome(cx: Float, r: Float) {
                            p.addOval(Rect(Offset(cx, baseY), r))
                            p.moveTo(cx - r * 0.08f, baseY - r); p.lineTo(cx, baseY - r * 1.35f); p.lineTo(cx + r * 0.08f, baseY - r); p.close()
                        }
                        fun minaret(cx: Float, height: Float) {
                            val mw = w * 0.022f * scale
                            p.addRect(Rect(cx - mw / 2f, baseY - height, cx + mw / 2f, baseY))
                            p.addRect(Rect(cx - mw, baseY - height * 0.72f, cx + mw, baseY - height * 0.69f))
                            p.addOval(Rect(Offset(cx, baseY - height), mw * 0.75f))
                            p.moveTo(cx - mw * 0.5f, baseY - height); p.lineTo(cx, baseY - height - mw * 3f); p.lineTo(cx + mw * 0.5f, baseY - height); p.close()
                        }
                        dome(w * (0.50f + shift), w * 0.17f * scale)
                        dome(w * (0.30f + shift), w * 0.08f * scale)
                        dome(w * (0.70f + shift), w * 0.08f * scale)
                        minaret(w * (0.14f + shift), h * 0.30f * scale)
                        minaret(w * (0.86f + shift), h * 0.30f * scale)
                        minaret(w * (0.36f + shift), h * 0.17f * scale)
                        minaret(w * (0.64f + shift), h * 0.17f * scale)
                        drawPath(p, col)
                    }
                    val shade = if (c.isDark) Color.Black else style.a
                    skyline(h * 0.82f, 0.7f, 0.03f * dA + 0.08f, shade.copy(alpha = if (c.isDark) 0.28f else 0.14f))
                    skyline(h * 0.88f, 1f, 0.01f * dB, shade.copy(alpha = if (c.isDark) 0.55f else 0.26f))
                }
                "fajr" -> {
                    // First light: thin bands of colour spreading along the horizon.
                    val rise = 0.5f + 0.5f * kotlin.math.sin(ms / 9000f)
                    orb(orbB.copy(alpha = (orbB.alpha * (2f + voice)).coerceAtMost(1f)), w * 0.5f, h * (0.80f - 0.04f * rise), w * (0.9f + 0.3f * voice))
                    val cols = listOf(orbB, orbA, orbC)
                    for (k in 0 until 9) {
                        val y = h * (0.42f + 0.05f * k) + h * 0.004f * kotlin.math.sin(ms / 3000f + k)
                        val col = cols[k % 3]
                        val mid = w * (0.5f + 0.25f * kotlin.math.sin(ms / 7000f + k * 1.1f))
                        drawRect(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, col.copy(alpha = (col.alpha * (1.6f + 0.2f * k) * (1f + voice)).coerceAtMost(1f)), Color.Transparent),
                                startX = mid - w * 0.7f, endX = mid + w * 0.7f,
                            ),
                            topLeft = Offset(0f, y), size = Size(w, h * 0.004f * (1f + k * 0.35f)),
                        )
                    }
                }
                "kiswah" -> {
                    // A brocade of gold script repeated across the screen, the rows drifting in opposite directions.
                    val gold = c.gold
                    val layout = textMeasurer.measure(
                        "لَا إِلَٰهَ إِلَّا ٱللَّهُ",
                        androidx.compose.ui.text.TextStyle(fontFamily = QuranFont, fontSize = (w * 0.062f).toSp()),
                    )
                    val tileW = layout.size.width + w * 0.10f
                    val rowH = layout.size.height + h * 0.018f
                    val rows = (h / rowH).toInt() + 2
                    val glow = (if (c.isDark) 0.22f else 0.30f) * (1f + 0.8f * voice)
                    for (r in 0 until rows) {
                        val y = r * rowH
                        val dir = if (r % 2 == 0) 1f else -1f
                        val off = ((ms % 90000L) / 90000f * tileW * dir) + (if (r % 2 == 0) 0f else tileW / 2f)
                        var x = -tileW + (off % tileW)
                        while (x < w) {
                            drawText(layout, color = gold, topLeft = Offset(x, y), alpha = glow.coerceAtMost(0.6f))
                            x += tileW
                        }
                        drawLine(gold.copy(alpha = 0.18f), Offset(0f, y + rowH - h * 0.004f), Offset(w, y + rowH - h * 0.004f), strokeWidth = 1.dp.toPx())
                    }
                }
                "kaaba" -> {
                    // The Kaaba at night: black kiswah with its gold belt and door, the white marble base, the
                    // minarets of the Haram behind it and pilgrims circling in rings.
                    val gold = c.gold
                    val cx = w * 0.5f
                    val glow = 1f + 0.9f * voice
                    val night = Color(0xFF07070A)
                    val baseY = h * 0.58f
                    val fw = w * 0.34f
                    val fh = fw * 1.10f
                    val depth = fw * 0.52f
                    val x0 = cx - (fw + depth) / 2f
                    val x1 = x0 + fw
                    val x2 = x1 + depth
                    val top = baseY - fh
                    val farTop = top + fh * 0.035f
                    val farBot = baseY - fh * 0.045f
                    orb(Color.White.copy(alpha = ((if (c.isDark) 0.14f else 0.28f) * glow).coerceAtMost(0.5f)), cx, h * 0.30f, w * 1.0f)
                    orb(orbB.copy(alpha = (orbB.alpha * 2f).coerceAtMost(1f)), cx, h * 0.56f, w * 0.8f)
                    // a soft column of light rising from the house
                    drawRect(
                        Brush.verticalGradient(listOf(Color.Transparent, gold.copy(alpha = (0.10f * glow).coerceAtMost(0.28f))), startY = 0f, endY = top),
                        topLeft = Offset(x0 + fw * 0.1f, 0f), size = Size(fw + depth * 0.8f, top),
                    )
                    // minarets of the Haram
                    fun minaret(x: Float, hgt: Float, sc: Float) {
                        val sh = night.copy(alpha = if (c.isDark) 0.60f else 0.34f)
                        val base = baseY - fh * 0.12f
                        val bw = w * 0.011f * sc
                        drawPath(Path().apply { moveTo(x - bw, base); lineTo(x - bw * 0.7f, base - hgt); lineTo(x + bw * 0.7f, base - hgt); lineTo(x + bw, base); close() }, sh)
                        for (k in 1..3) drawRect(sh, Offset(x - bw * 1.8f, base - hgt * (0.42f + 0.17f * k)), Size(bw * 3.6f, h * 0.0035f))
                        drawPath(Path().apply { moveTo(x - bw * 1.0f, base - hgt); quadraticTo(x, base - hgt - bw * 3.6f, x + bw * 1.0f, base - hgt); close() }, sh)
                        orb(gold.copy(alpha = (0.20f * glow).coerceAtMost(0.4f)), x, base - hgt - bw * 3.4f, w * 0.05f)
                        drawCircle(gold.copy(alpha = 0.85f), bw * 0.30f, Offset(x, base - hgt - bw * 3.6f))
                    }
                    minaret(w * 0.09f, h * 0.30f, 1.25f)
                    minaret(w * 0.23f, h * 0.21f, 1f)
                    minaret(w * 0.77f, h * 0.21f, 1f)
                    minaret(w * 0.91f, h * 0.30f, 1.25f)
                    // the mataf: marble floor and the rings the pilgrims walk
                    val gy = baseY + fh * 0.02f
                    drawOval(
                        Brush.radialGradient(listOf(Color.White.copy(alpha = if (c.isDark) 0.10f else 0.26f), Color.Transparent), center = Offset(cx, gy), radius = w * 0.52f),
                        topLeft = Offset(cx - w * 0.52f, gy - w * 0.09f), size = Size(w * 1.04f, w * 0.18f),
                    )
                    for (k in 0 until 3) {
                        val rx = w * (0.30f + 0.07f * k)
                        drawOval(gold.copy(alpha = 0.14f - 0.03f * k), Offset(cx - rx, gy - rx * 0.17f), Size(rx * 2f, rx * 0.34f), style = Stroke(1.dp.toPx()))
                    }
                    fun pilgrim(ring: Int, i: Int, behind: Boolean) {
                        val n = 26 + ring * 8
                        val a = i * 6.2831855f / n + ring * 0.4f - ms * (0.00016f - 0.00003f * ring)
                        val d = kotlin.math.sin(a)
                        if ((d < 0f) != behind) return
                        val rx = w * (0.30f + 0.07f * ring)
                        val px = cx + rx * kotlin.math.cos(a)
                        val py = gy + rx * 0.17f * d
                        val sc = 0.8f + 0.5f * d
                        val body = Color.White.copy(alpha = (0.45f + 0.4f * d).coerceIn(0.3f, 0.9f))
                        val bw = 1.5f * density * sc
                        val bh = 5.2f * density * sc
                        drawRoundRect(body, Offset(px - bw, py - bh), Size(bw * 2f, bh), androidx.compose.ui.geometry.CornerRadius(bw))
                        drawCircle(body, bw * 0.95f, Offset(px, py - bh - bw * 0.9f))
                    }
                    for (ring in 0 until 3) for (i in 0 until 26 + ring * 8) pilgrim(ring, i, true)
                    // the house: front and right faces of black cloth
                    val front = Path().apply { moveTo(x0, top); lineTo(x1, top); lineTo(x1, baseY); lineTo(x0, baseY); close() }
                    val side = Path().apply { moveTo(x1, top); lineTo(x2, farTop); lineTo(x2, farBot); lineTo(x1, baseY); close() }
                    drawPath(front, Brush.verticalGradient(listOf(Color(0xFF15151A), Color(0xFF050507)), startY = top, endY = baseY))
                    drawPath(side, Brush.horizontalGradient(listOf(Color(0xFF1E1E25), Color(0xFF0D0D11)), startX = x1, endX = x2))
                    // folds in the cloth, swaying a little
                    for (k in 1..10) {
                        val fx = x0 + fw * k / 11f
                        val sway = 1.1f * density * kotlin.math.sin(ms / 1900f + k)
                        drawLine(Color.White.copy(alpha = 0.045f), Offset(fx + sway, top), Offset(fx - sway, baseY), strokeWidth = 1.2f * density)
                    }
                    for (k in 1..5) {
                        val t = k / 6f
                        val fx = x1 + depth * t
                        drawLine(Color.White.copy(alpha = 0.035f), Offset(fx, top + (farTop - top) * t), Offset(fx, baseY + (farBot - baseY) * t), strokeWidth = 1.1f * density)
                    }
                    // the hizam: gold belt with the name of Allah woven along it
                    val bTop = top + fh * 0.20f
                    val bBot = top + fh * 0.34f
                    val beltBrush = Brush.horizontalGradient(
                        listOf(gold.copy(alpha = 0.70f), gold.copy(alpha = 0.95f), gold.copy(alpha = 0.72f), gold.copy(alpha = 0.95f)), startX = x0, endX = x1,
                    )
                    drawRect(beltBrush, Offset(x0, bTop), Size(fw, bBot - bTop))
                    val beltSide = Path().apply {
                        moveTo(x1, bTop); lineTo(x2, farTop + (bTop - top) * 0.86f); lineTo(x2, farTop + (bBot - top) * 0.86f); lineTo(x1, bBot); close()
                    }
                    drawPath(beltSide, gold.copy(alpha = 0.58f))
                    drawLine(night.copy(alpha = 0.55f), Offset(x0, bTop + 2.5f * density), Offset(x1, bTop + 2.5f * density), 1f * density)
                    drawLine(night.copy(alpha = 0.55f), Offset(x0, bBot - 2.5f * density), Offset(x1, bBot - 2.5f * density), 1f * density)
                    val word = textMeasurer.measure(
                        "ٱللَّهُ",
                        androidx.compose.ui.text.TextStyle(fontFamily = QuranFont, fontSize = ((bBot - bTop) * 0.62f).toSp()),
                    )
                    val step = word.size.width + fw * 0.05f
                    var wx = x0 + fw * 0.03f
                    while (wx + word.size.width < x1 - fw * 0.02f) {
                        drawText(word, color = night, topLeft = Offset(wx, (bTop + bBot) / 2f - word.size.height / 2f), alpha = 0.78f)
                        wx += step
                    }
                    var sx = x1 + depth * 0.10f
                    while (sx < x2 - depth * 0.06f) {
                        val t = (sx - x1) / depth
                        val yMid = (bTop + bBot) / 2f + (farTop - top) * t * 0.86f
                        drawCircle(night.copy(alpha = 0.6f), 1.6f * density, Offset(sx, yMid))
                        sx += depth * 0.16f
                    }
                    // the embroidered panel and the golden door
                    val pw = fw * 0.30f
                    val px = x0 + fw * 0.58f
                    drawRect(gold.copy(alpha = 0.40f), Offset(px, bBot + fh * 0.04f), Size(pw, fh * 0.20f), style = Stroke(1.2f * density))
                    drawRect(gold.copy(alpha = 0.22f), Offset(px + pw * 0.07f, bBot + fh * 0.07f), Size(pw * 0.86f, fh * 0.14f), style = Stroke(1f * density))
                    val dw = fw * 0.18f
                    val dh = fh * 0.36f
                    val dx = px + (pw - dw) / 2f
                    val dy = baseY - fh * 0.06f - dh
                    drawRoundRect(
                        Brush.verticalGradient(listOf(gold, gold.copy(alpha = 0.7f)), startY = dy, endY = dy + dh),
                        Offset(dx, dy), Size(dw, dh), androidx.compose.ui.geometry.CornerRadius(dw * 0.16f),
                    )
                    for (r in 0 until 3) for (k in 0 until 2) {
                        drawRect(
                            night.copy(alpha = 0.45f),
                            Offset(dx + dw * (0.10f + 0.46f * k), dy + dh * (0.07f + 0.31f * r)), Size(dw * 0.38f, dh * 0.25f), style = Stroke(1f * density),
                        )
                    }
                    orb(gold.copy(alpha = (0.18f * glow).coerceAtMost(0.4f)), dx + dw / 2f, dy + dh * 0.5f, w * 0.12f)
                    // white marble base (shadharwan) and the gold hem of the cloth
                    drawRect(Color.White.copy(alpha = 0.55f), Offset(x0, baseY - fh * 0.035f), Size(fw, fh * 0.035f))
                    drawPath(Path().apply { moveTo(x1, baseY - fh * 0.035f); lineTo(x2, farBot - fh * 0.03f); lineTo(x2, farBot); lineTo(x1, baseY); close() }, Color.White.copy(alpha = 0.38f))
                    drawLine(gold.copy(alpha = 0.55f), Offset(x0, baseY - fh * 0.05f), Offset(x1, baseY - fh * 0.05f), 1.2f * density)
                    // the Black Stone corner, set in silver
                    val bs = Offset(x0 + fw * 0.035f, baseY - fh * 0.22f)
                    drawCircle(Color(0xFFCFCFD6).copy(alpha = 0.85f), fw * 0.026f, bs)
                    drawCircle(Color(0xFF0B0B0D), fw * 0.017f, bs)
                    drawPath(front, gold.copy(alpha = 0.30f), style = Stroke(1.dp.toPx()))
                    drawPath(side, gold.copy(alpha = 0.20f), style = Stroke(1.dp.toPx()))
                    for (ring in 0 until 3) for (i in 0 until 26 + ring * 8) pilgrim(ring, i, false)
                    orb(gold.copy(alpha = if (c.isDark) 0.07f else 0.16f), cx, gy + h * 0.02f, w * 0.55f)
                }
                "dome" -> {
                    // Looking up into a dome: rings of petals turning slowly around the light at its crown.
                    val centre = Offset(w * 0.5f, h * 0.30f)
                    orb(orbB.copy(alpha = (orbB.alpha * (2f + voice)).coerceAtMost(1f)), centre.x, centre.y, w * (0.5f + 0.2f * voice))
                    val cols = listOf(style.b, style.a, style.c, style.a)
                    for (ring in 0 until 4) {
                        val count = 10 + ring * 6
                        val rad = w * (0.13f + 0.13f * ring)
                        val len = w * 0.12f
                        val wid = (2f * 3.1415927f * rad / count) * 0.42f
                        val turn = (ms % 240000L) / 240000f * 360f * (if (ring % 2 == 0) 1 else -1)
                        val petal = Path().apply {
                            moveTo(centre.x, centre.y - rad)
                            quadraticTo(centre.x + wid, centre.y - rad - len * 0.5f, centre.x, centre.y - rad - len)
                            quadraticTo(centre.x - wid, centre.y - rad - len * 0.5f, centre.x, centre.y - rad)
                        }
                        val col = cols[ring].copy(alpha = (if (c.isDark) 0.38f else 0.42f) * (1f - 0.15f * ring) * (1f + 0.5f * voice))
                        for (k in 0 until count) {
                            rotate(turn + k * 360f / count, centre) {
                                drawPath(petal, col.copy(alpha = col.alpha * 0.18f))
                                drawPath(petal, col, style = Stroke(1.dp.toPx()))
                            }
                        }
                    }
                }
                "palms" -> {
                    // Date palms leaning over dunes in the last light.
                    orb(orbB.copy(alpha = (orbB.alpha * (2f + voice)).coerceAtMost(1f)), w * 0.55f, h * 0.80f, w * (0.9f + 0.3f * voice))
                    val shade = (if (c.isDark) Color.Black else style.a).copy(alpha = if (c.isDark) 0.55f else 0.30f)
                    fun palm(x: Float, topY: Float, lean: Float, sc: Float, phase: Float) {
                        val sw = kotlin.math.sin(ms / 2400f + phase)
                        val tx = x + lean + w * 0.01f * sw
                        val trunk = Path().apply { moveTo(x, h); quadraticTo(x + lean * 0.2f, (h + topY) / 2f, tx, topY) }
                        drawPath(trunk, shade, style = Stroke(w * 0.022f * sc, cap = StrokeCap.Round))
                        for (k in 0 until 8) {
                            val ang = (-200f + k * 31f + 4f * sw) * 3.1415927f / 180f
                            val len = w * 0.20f * sc
                            val ex = tx + len * kotlin.math.cos(ang)
                            val ey = topY + len * kotlin.math.sin(ang) * 0.6f + len * 0.45f
                            val frond = Path().apply { moveTo(tx, topY); quadraticTo(tx + len * 0.6f * kotlin.math.cos(ang), topY + len * 0.6f * kotlin.math.sin(ang) - len * 0.25f, ex, ey) }
                            drawPath(frond, shade, style = Stroke(w * 0.012f * sc, cap = StrokeCap.Round))
                        }
                    }
                    palm(w * 0.16f, h * 0.42f, w * 0.06f, 1.1f, 0f)
                    palm(w * 0.30f, h * 0.54f, -w * 0.03f, 0.8f, 1.3f)
                    palm(w * 0.86f, h * 0.48f, -w * 0.07f, 1f, 2.6f)
                    silk(h * (0.88f + 0.005f * dA), h * 0.03f, 0.5f, 1.1f, 0.6f, 1f, orbA.copy(alpha = (orbA.alpha * 1.8f).coerceAtMost(1f)), edgeA)
                }
                "ink" -> {
                    // Drops of ink blooming in water, one after another.
                    val cols = listOf(orbA, orbB, orbC)
                    for (i in 0 until 7) {
                        val period = 9000f + i * 1700f
                        val x0 = ms / period + i * 0.17f
                        val cyc = kotlin.math.floor(x0)
                        val prog = x0 - cyc
                        fun rnd(seed: Float): Float { val v = kotlin.math.sin(seed) * 43758.547f; return v - kotlin.math.floor(v) }
                        val cx = w * rnd(cyc * 12.9898f + i * 78.233f)
                        val cy = h * rnd(cyc * 39.346f + i * 11.135f)
                        val col = cols[i % 3]
                        val a = (col.alpha * 3f * (1f - prog) * (1f - prog) * kotlin.math.min(1f, prog * 8f) * (1f + voice)).coerceAtMost(1f)
                        val r = w * (0.08f + 0.55f * prog)
                        orb(col.copy(alpha = a), cx, cy, r)
                        orb(col.copy(alpha = a * 0.8f), cx, cy, r * 0.45f)
                    }
                }
                "tawaf" -> {
                    // Points of light circling one centre, the nearer ones moving faster.
                    val centre = Offset(w * 0.5f, h * 0.42f)
                    orb(orbB.copy(alpha = (orbB.alpha * (2.2f + voice)).coerceAtMost(1f)), centre.x, centre.y, w * (0.35f + 0.15f * voice))
                    val cols = listOf(style.a, style.b, style.c)
                    for (i in 0 until 70) {
                        val f = (i * 0.6180339f) % 1f
                        val rad = w * (0.13f + 0.40f * f)
                        val ang = i * 2.399f - ms * 0.00042f / (0.35f + f)
                        val p = Offset(centre.x + rad * kotlin.math.cos(ang), centre.y + rad * 0.62f * kotlin.math.sin(ang))
                        val depth = 0.6f + 0.4f * kotlin.math.sin(ang)
                        val r = (1.2f + (i % 3) * 0.6f) * density * depth * (1f + 0.5f * voice)
                        val col = cols[i % 3]
                        drawCircle(col.copy(alpha = 0.10f * depth), r * 3.5f, p)
                        drawCircle(col.copy(alpha = (if (c.isDark) 0.9f else 0.7f) * depth), r, p)
                    }
                }
                "candle" -> {
                    // One candle, its flame leaning and steadying.
                    val cx = w * 0.5f
                    val base = h * 0.74f
                    val flick = 1f + 0.06f * kotlin.math.sin(ms / 95f) + 0.04f * kotlin.math.sin(ms / 41f) + 0.35f * voice
                    val fh = h * 0.075f * flick
                    val fw = w * 0.035f
                    val sx = w * 0.008f * kotlin.math.sin(ms / 430f)
                    val warm = Color(0xFFFFB45C)
                    orb(warm.copy(alpha = (if (c.isDark) 0.30f else 0.22f) * flick.coerceAtMost(1.4f)), cx, base - fh * 0.5f, w * 0.85f)
                    orb(warm.copy(alpha = 0.35f), cx, base - fh * 0.5f, w * 0.22f)
                    val bw = w * 0.07f
                    drawRoundRect(
                        Brush.verticalGradient(listOf(Color(0xFFFFF3DC).copy(alpha = 0.75f), Color(0xFFFFF3DC).copy(alpha = 0.15f)), startY = base + h * 0.012f, endY = base + h * 0.2f),
                        topLeft = Offset(cx - bw / 2f, base + h * 0.012f), size = Size(bw, h * 0.2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(bw * 0.2f),
                    )
                    drawLine(Color.Black.copy(alpha = 0.6f), Offset(cx, base + h * 0.012f), Offset(cx, base - h * 0.004f), strokeWidth = 1.5.dp.toPx())
                    val flame = Path().apply {
                        moveTo(cx, base)
                        cubicTo(cx - fw, base - fh * 0.15f, cx - fw * 0.7f + sx, base - fh * 0.65f, cx + sx * 2f, base - fh)
                        cubicTo(cx + fw * 0.7f + sx, base - fh * 0.65f, cx + fw, base - fh * 0.15f, cx, base)
                    }
                    drawPath(flame, Brush.verticalGradient(listOf(Color(0xFFFFF7E0), warm, Color(0xFFFF7A3C).copy(alpha = 0.85f)), startY = base - fh, endY = base))
                }
                "huruf" -> {
                    // Arabic letters rising and fading like breath on cold air.
                    val letters = listOf("ا", "ل", "م", "ص", "ر", "ك", "ه", "ي", "ع", "ط", "س", "ح", "ق", "ن")
                    val cols = listOf(style.a, style.b, style.c)
                    for (i in letters.indices) {
                        val prog = (ms * (0.000022f + (i % 4) * 0.000007f) + i * 0.21f) % 1f
                        val x = w * ((i * 0.6180339f + 0.07f) % 0.86f) + w * 0.03f * kotlin.math.sin(prog * 8f + i)
                        val y = h * (1.02f - 1.15f * prog)
                        val layout = textMeasurer.measure(
                            letters[i],
                            androidx.compose.ui.text.TextStyle(fontFamily = QuranFont, fontSize = (w * (0.09f + 0.05f * (i % 3))).toSp()),
                        )
                        val fade = kotlin.math.sin(prog * 3.1415927f)
                        drawText(layout, color = cols[i % 3], topLeft = Offset(x, y), alpha = ((if (c.isDark) 0.38f else 0.30f) * fade * (1f + 0.6f * voice)).coerceIn(0f, 1f))
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
        Box(Modifier.fillMaxSize().graphicsLayer()) { content() }
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
        Text(text, style = DuaType.label.copy(fontSize = 13.sp, letterSpacing = 0.1.sp), color = c.inkMuted)
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
