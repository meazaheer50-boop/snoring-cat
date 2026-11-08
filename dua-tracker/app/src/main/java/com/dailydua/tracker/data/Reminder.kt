package com.dailydua.tracker.data

/** A user-defined reminder. [days] is a bit mask: bit 0 = Monday ... bit 6 = Sunday (127 = every day). */
data class Reminder(
    val id: String,
    val label: String,
    val hour: Int,
    val minute: Int,
    val days: Int = 127,
    val duaId: String? = null,
    val enabled: Boolean = true,
) {
    fun encode(): String = "$id|$hour|$minute|$days|${if (enabled) 1 else 0}|${duaId.orEmpty()}|$label"

    fun timeText(): String = "%d:%02d %s".format(
        if (hour % 12 == 0) 12 else hour % 12, minute, if (hour < 12) "AM" else "PM",
    )

    fun daysText(): String {
        if (days == 127) return "Every day"
        val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return names.filterIndexed { i, _ -> days and (1 shl i) != 0 }.joinToString(" ").ifEmpty { "Never" }
    }

    companion object {
        fun decode(s: String): Reminder? {
            val p = s.split('|', limit = 7)
            if (p.size < 7) return null
            return Reminder(
                id = p[0],
                hour = p[1].toIntOrNull() ?: return null,
                minute = p[2].toIntOrNull() ?: return null,
                days = p[3].toIntOrNull() ?: 127,
                enabled = p[4] == "1",
                duaId = p[5].ifEmpty { null },
                label = p[6],
            )
        }

        const val MORNING_ID = "builtin-morning"
        const val EVENING_ID = "builtin-evening"
        val morning = Reminder(MORNING_ID, "Morning duas", 6, 30, 127, "waking")
        val evening = Reminder(EVENING_ID, "Evening duas", 18, 0, 127, "sleep")
    }
}
