package com.dailydua.tracker.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dailydua.tracker.MainActivity
import com.dailydua.tracker.data.Reminder
import com.dailydua.tracker.data.SampleDuas

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val r = Reminder(
            id = intent.getStringExtra(ReminderScheduler.EXTRA_ID) ?: return,
            label = intent.getStringExtra(ReminderScheduler.EXTRA_LABEL).orEmpty(),
            hour = intent.getIntExtra(ReminderScheduler.EXTRA_HOUR, 8),
            minute = intent.getIntExtra(ReminderScheduler.EXTRA_MINUTE, 0),
            days = intent.getIntExtra(ReminderScheduler.EXTRA_DAYS, 127),
            duaId = intent.getStringExtra(ReminderScheduler.EXTRA_DUA),
        )
        notify(context, r)
        ReminderScheduler.schedule(context, r) // set the next occurrence
    }

    private fun notify(context: Context, r: Reminder) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "Dua reminders", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            r.duaId?.let { putExtra(MainActivity.EXTRA_DUA_ID, it) }
        }
        val content = PendingIntent.getActivity(
            context, r.id.hashCode(), open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val duaTitle = SampleDuas.all.firstOrNull { it.id == r.duaId }?.title
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(r.label.ifBlank { "Time for your dua" })
            .setContentText(duaTitle?.let { "Tap to open: $it" } ?: "Tap to open your duas")
            .setContentIntent(content)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(r.id.hashCode(), notification)
        } catch (_: SecurityException) {
            // Notification permission was not granted.
        }
    }

    companion object { const val CHANNEL = "reminders" }
}
