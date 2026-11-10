package com.dailydua.tracker.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/** A selectable backdrop: three glow colours (as opaque RGB) and a base tint for dark and light themes. */
data class BackgroundStyle(
    val id: String,
    val label: String,
    val a: Color,
    val b: Color,
    val c: Color,
    val darkBase: Color?,
    val lightBase: Color?,
    val waves: Boolean = true,
    val accentDark: Color? = null,
    val accentLight: Color? = null,
) {
    companion object {
        val all = listOf(
            BackgroundStyle("emerald", "Emerald", Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF14B8A6), null, null),
            BackgroundStyle("ocean", "Ocean", Color(0xFF0EA5E9), Color(0xFF22D3EE), Color(0xFF6366F1), Color(0xFF07161F), Color(0xFFEAF5FB), accentDark = Color(0xFF5CCBFF), accentLight = Color(0xFF0369A1)),
            BackgroundStyle("dusk", "Dusk", Color(0xFFF97316), Color(0xFFEC4899), Color(0xFFF59E0B), Color(0xFF1A0F12), Color(0xFFFFF1E8), accentDark = Color(0xFFFFA45C), accentLight = Color(0xFFB45309)),
            BackgroundStyle("twilight", "Twilight", Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFF120F1F), Color(0xFFF1EEFB), accentDark = Color(0xFFB79CFF), accentLight = Color(0xFF6D28D9)),
            BackgroundStyle("rose", "Rose", Color(0xFFF43F5E), Color(0xFFF59E0B), Color(0xFFD946EF), Color(0xFF1A0E13), Color(0xFFFEEEF1), accentDark = Color(0xFFFF8FA3), accentLight = Color(0xFFBE123C)),
            BackgroundStyle("plain", "Plain", Color(0xFF10B981), Color(0xFF10B981), Color(0xFF10B981), null, null, waves = false),
        )
        fun byId(id: String) = all.firstOrNull { it.id == id } ?: all.first()
    }
}

val LocalBackgroundStyle = compositionLocalOf { BackgroundStyle.all.first() }

/** Motion designs for the backdrop, independent of its colours. */
val backgroundDesigns = listOf(
    "silk" to "Silk",
    "aurora" to "Aurora",
    "dunes" to "Dunes",
    "contour" to "Contour",
    "veil" to "Veil",
    "rain" to "Rain",
    "moon" to "Moonlight",
    "embers" to "Embers",
    "mihrab" to "Mihrab",
    "lanterns" to "Lanterns",
    "calligraphy" to "Calligraphy",
    "hilal" to "Crescent",
)

val LocalBackgroundDesign = compositionLocalOf { "silk" }
