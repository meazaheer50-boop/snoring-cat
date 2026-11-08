package com.dailydua.tracker.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

enum class GlassVariant {
    FROST,
    SOFT_EMERALD,
    EMERALD,
    GOLD,
    ELEVATED,
}

@Composable
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(24.dp),
    variant: GlassVariant = GlassVariant.FROST,
    borderWidth: Dp = 1.dp,
): Modifier {
    val c = Tokens.colors
    val (fillTop, fillBottom, borderTop, borderBottom) = when (variant) {
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
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(borderTop, borderBottom),
                start = Offset(0f, 0f),
                end = Offset(800f, 800f),
            ),
            shape = shape,
        )
}

/** Ambient multi-orb background that gives translucent glass surfaces depth and refraction. */
@Composable
fun GlassBackground(content: @Composable BoxScope.() -> Unit) {
    val c = Tokens.colors
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(c.surface100, c.bgMid, c.surface200),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-right warm golden lantern aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(c.orbSecondary, Color.Transparent),
                    center = Offset(w * 0.88f, h * 0.10f),
                    radius = w * 0.72f,
                ),
                radius = w * 0.72f,
                center = Offset(w * 0.88f, h * 0.10f),
            )

            // Upper-left emerald aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(c.orbPrimary, Color.Transparent),
                    center = Offset(w * 0.12f, h * 0.24f),
                    radius = w * 0.78f,
                ),
                radius = w * 0.78f,
                center = Offset(w * 0.12f, h * 0.24f),
            )

            // Center-right aqua mist orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(c.orbTertiary, Color.Transparent),
                    center = Offset(w * 0.92f, h * 0.56f),
                    radius = w * 0.68f,
                ),
                radius = w * 0.68f,
                center = Offset(w * 0.92f, h * 0.56f),
            )

            // Bottom-left deep emerald glow behind floating dock
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(c.orbPrimary, Color.Transparent),
                    center = Offset(w * 0.22f, h * 0.88f),
                    radius = w * 0.75f,
                ),
                radius = w * 0.75f,
                center = Offset(w * 0.22f, h * 0.88f),
            )

            // Subtle celestial geometric light rings in top-right
            val ringColor = c.glassBorderTop.copy(alpha = if (c.isDark) 0.07f else 0.22f)
            drawCircle(
                color = ringColor,
                radius = w * 0.34f,
                center = Offset(w * 0.86f, h * 0.08f),
                style = Stroke(width = 1.dp.toPx()),
            )
            drawCircle(
                color = ringColor,
                radius = w * 0.46f,
                center = Offset(w * 0.86f, h * 0.08f),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
        content()
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
                Text(badge, style = DuaType.label, color = c.brand)
            }
        }
    }
}

@Composable
fun CheckCircle(done: Boolean) {
    val c = Tokens.colors
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .then(
                if (done) {
                    Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(c.brand, c.brand.copy(alpha = 0.78f)),
                            ),
                            CircleShape,
                        )
                        .border(1.dp, c.glassBrandBorder, CircleShape)
                } else {
                    Modifier
                        .background(c.glassFillTop, CircleShape)
                        .border(1.5.dp, c.border, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = c.onBrand, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun DuaRow(dua: Dua, done: Boolean, onClick: () -> Unit) {
    val c = Tokens.colors
    val hasAudio = dua.lines.isNotEmpty()
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp)
            .glassSurface(
                shape = RoundedCornerShape(24.dp),
                variant = if (done) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CheckCircle(done)
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
                if (done) {
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
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = DuaType.body.copy(fontWeight = FontWeight.Bold), color = c.onBrand)
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
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = DuaType.body.copy(fontWeight = FontWeight.Bold), color = c.brand)
    }
}

/** Pill-shaped glassmorphic segmented selector. */
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
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(CircleShape)
                    .then(
                        if (on) {
                            Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(c.brand, c.brand.copy(alpha = 0.85f)),
                                    ),
                                    CircleShape,
                                )
                                .border(1.dp, c.glassBrandBorder, CircleShape)
                        } else {
                            Modifier
                        },
                    )
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = DuaType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = if (on) c.onBrand else c.ink,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Glassmorphic toggle switch visual. The row that contains it carries the toggle semantics. */
@Composable
fun SwitchVisual(checked: Boolean) {
    val c = Tokens.colors
    val x by animateDpAsState(if (checked) 24.dp else 4.dp, label = "thumb")
    Box(
        Modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(CircleShape)
            .then(
                if (checked) {
                    Modifier
                        .background(
                            Brush.linearGradient(listOf(c.brand, c.brand.copy(alpha = 0.80f))),
                            CircleShape,
                        )
                        .border(1.dp, c.glassBrandBorder, CircleShape)
                } else {
                    Modifier.glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
                },
            ),
    ) {
        Box(
            Modifier
                .offset(x = x, y = 4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) c.onBrand else c.inkMuted.copy(alpha = 0.65f))
                .border(1.dp, c.glassHighlight, CircleShape),
        )
    }
}

