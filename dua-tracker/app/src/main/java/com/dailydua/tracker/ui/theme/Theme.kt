package com.dailydua.tracker.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LocalDuaColors = staticCompositionLocalOf { LightDuaColors }

object Tokens {
    val colors: DuaColors
        @Composable @ReadOnlyComposable get() = LocalDuaColors.current
}

@Composable
fun DuaTheme(mode: ThemeMode, accentDark: Color? = null, accentLight: Color? = null, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val base = if (dark) DarkDuaColors else LightDuaColors
    val accent = if (dark) accentDark else accentLight
    // Retint the brand colours (tabs, buttons, highlights) to follow the chosen background colour.
    fun tint(col: Color, k: Float) = lerp(col, accent ?: col, k).copy(alpha = col.alpha)
    val tinted = if (accent == null) base else base.copy(
        surfaceCard = tint(base.surfaceCard, if (dark) 0.30f else 0.15f),
        glassFillTop = tint(base.glassFillTop, if (dark) 0.35f else 0.18f),
        glassFillBottom = tint(base.glassFillBottom, if (dark) 0.35f else 0.18f),
        glassElevatedTop = tint(base.glassElevatedTop, if (dark) 0.35f else 0.18f),
        glassElevatedBottom = tint(base.glassElevatedBottom, if (dark) 0.35f else 0.18f),
        glassBorderTop = tint(base.glassBorderTop, 0.35f),
        glassBorderBottom = tint(base.glassBorderBottom, 0.35f),
        inkMuted = tint(base.inkMuted, if (dark) 0.40f else 0.25f),
        ink = tint(base.ink, if (dark) 0.10f else 0.12f),
        border = tint(base.border, 0.4f),
        hairline = tint(base.hairline, 0.4f),
    )
    val c = if (accent == null) base else if (dark) tinted.copy(
        brand = accent,
        onBrand = Color(0xFF14090F),
        brandSoft = accent.copy(alpha = 0.20f),
        glassBrandTop = lerp(accent, Color.Black, 0.55f).copy(alpha = 0.58f),
        glassBrandBottom = lerp(accent, Color.Black, 0.75f).copy(alpha = 0.42f),
        glassBrandBorder = accent.copy(alpha = 0.45f),
    ) else tinted.copy(
        brand = accent,
        onBrand = Color.White,
        brandSoft = accent.copy(alpha = 0.14f),
        glassBrandTop = accent.copy(alpha = 0.88f),
        glassBrandBottom = lerp(accent, Color.Black, 0.30f).copy(alpha = 0.78f),
        glassBrandBorder = lerp(accent, Color.White, 0.6f).copy(alpha = 0.55f),
    )
    val scheme = if (dark) {
        darkColorScheme(primary = c.brand, onPrimary = c.onBrand, background = c.surface100, onBackground = c.ink, surface = c.surfaceCard, onSurface = c.ink)
    } else {
        lightColorScheme(primary = c.brand, onPrimary = c.onBrand, background = c.surface100, onBackground = c.ink, surface = c.surfaceCard, onSurface = c.ink)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalDuaColors provides c) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
