package com.dailydua.tracker.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailydua.tracker.data.DuaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms are cleared when the phone restarts, so set them again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                DuaRepository(context.applicationContext).rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
