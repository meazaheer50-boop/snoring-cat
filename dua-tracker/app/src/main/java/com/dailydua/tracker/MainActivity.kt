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
        launchDuaId = intent?.getStringExtra(EXTRA_DUA_ID)
        setContent {
            CompositionLocalProvider(LocalAskNotifications provides ::askNotificationsOnce) {
                DuaApp(launchDuaId = launchDuaId, onLaunchHandled = { launchDuaId = null })
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
