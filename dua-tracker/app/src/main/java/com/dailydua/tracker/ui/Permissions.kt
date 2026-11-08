package com.dailydua.tracker.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Asks for the notification permission (Android 13+) the first time it is useful: when audio starts
 * (for the lock-screen player) or when a reminder is turned on. Provided by MainActivity.
 */
val LocalAskNotifications = staticCompositionLocalOf<() -> Unit> { {} }
