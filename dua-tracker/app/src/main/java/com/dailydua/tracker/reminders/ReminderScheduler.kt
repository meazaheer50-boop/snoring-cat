package com.dailydua.tracker.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dailydua.tracker.data.Reminder
import java.time.ZonedDateTime

object ReminderScheduler {
    const val EXTRA_ID = "id"
    const val EXTRA_LABEL = "label"
    const val EXTRA_HOUR = "hour"
    const val EXTRA_MINUTE = "minute"
    const val EXTRA_DAYS = "days"
    const val EXTRA_DUA = "duaId"

    fun nextTrigger(r: Reminder, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime? {
        if (r.days and 127 == 0) return null
        for (offset in 0..7) {
            val day = now.plusDays(offset.toLong())
            val candidate = day.withHour(r.hour).withMinute(r.minute).withSecond(0).withNano(0)
            val bit = day.dayOfWeek.value - 1 // Monday = 0
            if (r.days and (1 shl bit) != 0 && candidate.isAfter(now)) return candidate
        }
        return null
    }

    private fun pending(context: Context, r: Reminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ID, r.id)
            putExtra(EXTRA_LABEL, r.label)
            putExtra(EXTRA_HOUR, r.hour)
            putExtra(EXTRA_MINUTE, r.minute)
            putExtra(EXTRA_DAYS, r.days)
            putExtra(EXTRA_DUA, r.duaId)
        }
        return PendingIntent.getBroadcast(
            context, r.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
    }

    fun schedule(context: Context, r: Reminder) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pending(context, r)
        if (!r.enabled) {
            alarms.cancel(pi)
            return
        }
        val at = nextTrigger(r) ?: run { alarms.cancel(pi); return }
        val millis = at.toInstant().toEpochMilli()
        if (canScheduleExact(context)) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        }
    }

    fun cancel(context: Context, r: Reminder) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending(context, r))
    }
}
