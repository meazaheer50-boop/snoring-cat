package com.dailydua.tracker.ui.theme

import android.app.Activity
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
fun DuaTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val c = if (dark) DarkDuaColors else LightDuaColors
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
