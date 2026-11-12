package com.dailydua.tracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dailydua.tracker.reminders.ReminderScheduler
import com.dailydua.tracker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.store by preferencesDataStore(name = "dua_prefs")

data class Settings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val morningReminder: Boolean = false,
    val eveningReminder: Boolean = false,
    val arabicSize: Int = 32,
    val showTransliteration: Boolean = true,
    val showTranslation: Boolean = true,
    val keepScreenOn: Boolean = true,
    val background: String = "emerald",
    val design: String = "silk",
)

class DuaRepository(private val context: Context) {
    private val theme = stringPreferencesKey("theme")
    private val morning = booleanPreferencesKey("morning_reminder")
    private val evening = booleanPreferencesKey("evening_reminder")
    private val arabicSize = intPreferencesKey("arabic_size")
    private val translit = booleanPreferencesKey("show_transliteration")
    private val translation = booleanPreferencesKey("show_translation")
    private val keepOn = booleanPreferencesKey("keep_screen_on")
    private val backgroundKey = stringPreferencesKey("background")
    private val designKey = stringPreferencesKey("background_design")
    private val startedKey = booleanPreferencesKey("backup_started")
    private val introKey = booleanPreferencesKey("intro_seen")
    private val toneKey = intPreferencesKey("reading_tone")
    /** Entries look like "2026-11-03|waking". */
    private val done = stringSetPreferencesKey("done")
    private val remindersKey = stringSetPreferencesKey("reminders")
    private val playlistKey = stringPreferencesKey("playlist")
    private val favoritesKey = stringSetPreferencesKey("favorites")

    val settings: Flow<Settings> = context.store.data.map { p ->
        Settings(
            theme = p[theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            morningReminder = p[morning] ?: false,
            eveningReminder = p[evening] ?: false,
            arabicSize = p[arabicSize] ?: 32,
            showTransliteration = p[translit] ?: true,
            showTranslation = p[translation] ?: true,
            keepScreenOn = p[keepOn] ?: true,
            background = p[backgroundKey] ?: "emerald",
            design = p[designKey] ?: "silk",
        )
    }

    val favorites: Flow<Set<String>> = context.store.data.map { it[favoritesKey] ?: emptySet() }

    suspend fun toggleFavorite(duaId: String) {
        context.store.edit { p ->
            val current = p[favoritesKey] ?: emptySet()
            p[favoritesKey] = if (duaId in current) current - duaId else current + duaId
        }
    }

    val doneEntries: Flow<Set<String>> = context.store.data.map { it[done] ?: emptySet() }

    suspend fun setTheme(mode: ThemeMode) { context.store.edit { it[theme] = mode.name } }
    suspend fun setMorningReminder(on: Boolean) {
        context.store.edit { it[morning] = on }
        ReminderScheduler.schedule(context, Reminder.morning.copy(enabled = on))
    }
    suspend fun setEveningReminder(on: Boolean) {
        context.store.edit { it[evening] = on }
        ReminderScheduler.schedule(context, Reminder.evening.copy(enabled = on))
    }
    suspend fun setArabicSize(size: Int) { context.store.edit { it[arabicSize] = size } }
    suspend fun setShowTransliteration(on: Boolean) { context.store.edit { it[translit] = on } }
    suspend fun setShowTranslation(on: Boolean) { context.store.edit { it[translation] = on } }
    suspend fun setKeepScreenOn(on: Boolean) { context.store.edit { it[keepOn] = on } }
    suspend fun setBackground(id: String) { context.store.edit { it[backgroundKey] = id } }
    suspend fun setDesign(id: String) { context.store.edit { it[designKey] = id } }

    suspend fun toggleDone(date: LocalDate, duaId: String) {
        val entry = "$date|$duaId"
        context.store.edit { p ->
            val current = p[done] ?: emptySet()
            p[done] = if (entry in current) current - entry else current + entry
        }
    }

    val reminders: Flow<List<Reminder>> = context.store.data.map { p ->
        (p[remindersKey] ?: emptySet()).mapNotNull { Reminder.decode(it) }.sortedWith(compareBy({ it.hour }, { it.minute }, { it.id }))
    }

    suspend fun saveReminder(r: Reminder) {
        context.store.edit { p ->
            val current = (p[remindersKey] ?: emptySet()).filterNot { it.startsWith(r.id + "|") }.toSet()
            p[remindersKey] = current + r.encode()
        }
        ReminderScheduler.schedule(context, r)
    }

    suspend fun deleteReminder(r: Reminder) {
        context.store.edit { p -> p[remindersKey] = (p[remindersKey] ?: emptySet()).filterNot { it.startsWith(r.id + "|") }.toSet() }
        ReminderScheduler.cancel(context, r)
    }

    /** Called on app start and after a reboot so every enabled reminder has an alarm. */
    suspend fun rescheduleAll() {
        val s = settings.first()
        ReminderScheduler.schedule(context, Reminder.morning.copy(enabled = s.morningReminder))
        ReminderScheduler.schedule(context, Reminder.evening.copy(enabled = s.eveningReminder))
        reminders.first().forEach { ReminderScheduler.schedule(context, it) }
    }

    val playlist: Flow<List<String>> = context.store.data.map { p ->
        p[playlistKey]?.split(',')?.filter { it.isNotBlank() } ?: emptyList()
    }

    private fun pageKey(id: String) = intPreferencesKey("page_$id")

    val lastPages: Flow<Map<String, Int>> = context.store.data.map { p ->
        Mushaf.all.associate { it.id to (p[pageKey(it.id)] ?: 0) }
    }

    suspend fun setLastPage(id: String, page: Int) { context.store.edit { it[pageKey(id)] = page } }

    suspend fun setPlaylist(ids: List<String>) { context.store.edit { it[playlistKey] = ids.joinToString(",") } }

    /** True until the app has run once on this install (a restored cloud backup counts as already started). */
    suspend fun isFreshInstall(): Boolean = context.store.data.first().let { it[startedKey] != true && it.asMap().isEmpty() }
    /** Reading brightness: 0 light, 1 mid-light, 2 mid-dark, 3 dark; -1 follows the app theme. */
    val readTone: Flow<Int> = context.store.data.map { it[toneKey] ?: -1 }
    suspend fun setReadTone(t: Int) { context.store.edit { it[toneKey] = t } }
    val introSeen: Flow<Boolean> = context.store.data.map { it[introKey] == true }
    suspend fun markIntroSeen() { context.store.edit { it[introKey] = true } }
    suspend fun markStarted() { context.store.edit { it[startedKey] = true } }
    fun changes(): Flow<Any> = context.store.data

    /** Everything the user has set up (reminders, favourites, playlist, progress, settings) as one JSON text. */
    suspend fun exportBackup(): String {
        val root = org.json.JSONObject()
        root.put("app", "daily-dua")
        root.put("version", 1)
        val items = org.json.JSONObject()
        context.store.data.first().asMap().forEach { (k, v) ->
            if (k.name == "backup_started") return@forEach
            val o = org.json.JSONObject()
            when (v) {
                is Boolean -> o.put("t", "b").put("v", v)
                is Int -> o.put("t", "i").put("v", v)
                is Long -> o.put("t", "l").put("v", v)
                is String -> o.put("t", "s").put("v", v)
                is Set<*> -> o.put("t", "set").put("v", org.json.JSONArray(v.map { it.toString() }))
                else -> return@forEach
            }
            items.put(k.name, o)
        }
        root.put("items", items)
        return root.toString(2)
    }

    /** Replaces the current setup with a saved backup and sets the reminders' alarms again. False if the text is not a backup. */
    suspend fun importBackup(text: String): Boolean {
        val items = try {
            val root = org.json.JSONObject(text)
            if (root.optString("app") != "daily-dua") return false
            root.getJSONObject("items")
        } catch (e: org.json.JSONException) {
            return false
        }
        reminders.first().forEach { ReminderScheduler.cancel(context, it) }
        context.store.edit { p ->
            p.clear()
            for (name in items.keys()) {
                val o = items.getJSONObject(name)
                when (o.optString("t")) {
                    "b" -> p[booleanPreferencesKey(name)] = o.getBoolean("v")
                    "i" -> p[intPreferencesKey(name)] = o.getInt("v")
                    "l" -> p[androidx.datastore.preferences.core.longPreferencesKey(name)] = o.getLong("v")
                    "s" -> p[stringPreferencesKey(name)] = o.getString("v")
                    "set" -> {
                        val arr = o.getJSONArray("v")
                        p[stringSetPreferencesKey(name)] = (0 until arr.length()).map { arr.getString(it) }.toSet()
                    }
                }
            }
        }
        rescheduleAll()
        return true
    }
}
