package com.dailydua.tracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dailydua.tracker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
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
)

class DuaRepository(private val context: Context) {
    private val theme = stringPreferencesKey("theme")
    private val morning = booleanPreferencesKey("morning_reminder")
    private val evening = booleanPreferencesKey("evening_reminder")
    private val arabicSize = intPreferencesKey("arabic_size")
    private val translit = booleanPreferencesKey("show_transliteration")
    private val translation = booleanPreferencesKey("show_translation")
    /** Entries look like "2026-11-03|waking". */
    private val done = stringSetPreferencesKey("done")

    val settings: Flow<Settings> = context.store.data.map { p ->
        Settings(
            theme = p[theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            morningReminder = p[morning] ?: false,
            eveningReminder = p[evening] ?: false,
            arabicSize = p[arabicSize] ?: 32,
            showTransliteration = p[translit] ?: true,
            showTranslation = p[translation] ?: true,
        )
    }

    val doneEntries: Flow<Set<String>> = context.store.data.map { it[done] ?: emptySet() }

    suspend fun setTheme(mode: ThemeMode) { context.store.edit { it[theme] = mode.name } }
    suspend fun setMorningReminder(on: Boolean) { context.store.edit { it[morning] = on } }
    suspend fun setEveningReminder(on: Boolean) { context.store.edit { it[evening] = on } }
    suspend fun setArabicSize(size: Int) { context.store.edit { it[arabicSize] = size } }
    suspend fun setShowTransliteration(on: Boolean) { context.store.edit { it[translit] = on } }
    suspend fun setShowTranslation(on: Boolean) { context.store.edit { it[translation] = on } }

    suspend fun toggleDone(date: LocalDate, duaId: String) {
        val entry = "$date|$duaId"
        context.store.edit { p ->
            val current = p[done] ?: emptySet()
            p[done] = if (entry in current) current - entry else current + entry
        }
    }
}
