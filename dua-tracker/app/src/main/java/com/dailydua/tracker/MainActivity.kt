package com.dailydua.tracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.dailydua.tracker.playback.PlayerHub
import com.dailydua.tracker.ui.DuaApp
import com.dailydua.tracker.ui.LocalAskNotifications

class MainActivity : ComponentActivity() {
    private var launchDuaId by mutableStateOf<String?>(null)
    private val askNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PlayerHub.init(this)
        requestHighRefreshRate()
        com.dailydua.tracker.data.AutoBackup.start(this, androidx.lifecycle.lifecycleScope)
        launchDuaId = intent?.getStringExtra(EXTRA_DUA_ID)
        setContent {
            CompositionLocalProvider(LocalAskNotifications provides ::askNotificationsOnce) {
                DuaApp(launchDuaId = launchDuaId, onLaunchHandled = { launchDuaId = null })
            }
        }
    }

    /** Ask for the display's fastest mode (90 Hz or more where the phone supports it). */
    private fun requestHighRefreshRate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        runCatching {
            @Suppress("DEPRECATION")
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) display else windowManager.defaultDisplay
            val current = display?.mode ?: return
            val best = display.supportedModes
                .filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
                .maxByOrNull { it.refreshRate } ?: return
            window.attributes = window.attributes.apply {
                preferredDisplayModeId = best.modeId
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) preferredRefreshRate = best.refreshRate
            }
        }
    }

    private var askedNotifications = false

    private fun askNotificationsOnce() {
        if (askedNotifications || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        askedNotifications = true
        askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchDuaId = intent.getStringExtra(EXTRA_DUA_ID)
    }

    companion object { const val EXTRA_DUA_ID = "duaId" }
}
