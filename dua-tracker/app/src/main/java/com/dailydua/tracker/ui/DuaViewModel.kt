package com.dailydua.tracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailydua.tracker.data.DuaRepository
import com.dailydua.tracker.data.Reminder
import com.dailydua.tracker.data.Settings
import com.dailydua.tracker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UiState(
    val settings: Settings = Settings(),
    val doneToday: Set<String> = emptySet(),
    val streak: Int = 0,
    val reminders: List<Reminder> = emptyList(),
    val playlist: List<String> = emptyList(),
    val lastPages: Map<String, Int> = emptyMap(),
    val favorites: Set<String> = emptySet(),
)

const val MIN_ARABIC = 20
const val MAX_ARABIC = 56

class DuaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DuaRepository(app)

    val state: StateFlow<UiState> = combine(repo.settings, repo.doneEntries, repo.reminders, repo.playlist, repo.lastPages, repo.favorites) { values ->
        @Suppress("UNCHECKED_CAST")
        val settings = values[0] as Settings
        @Suppress("UNCHECKED_CAST")
        val entries = values[1] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val reminders = values[2] as List<Reminder>
        @Suppress("UNCHECKED_CAST")
        val playlist = values[3] as List<String>
        @Suppress("UNCHECKED_CAST")
        val lastPages = values[4] as Map<String, Int>
        @Suppress("UNCHECKED_CAST")
        val favorites = values[5] as Set<String>
        val today = LocalDate.now()
        UiState(
            settings = settings,
            doneToday = entries.filter { it.startsWith("$today|") }.map { it.substringAfter('|') }.toSet(),
            streak = streak(entries, today),
            reminders = reminders,
            playlist = playlist,
            lastPages = lastPages,
            favorites = favorites,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    /** A day counts when at least one dua was done. The streak may end today or yesterday. */
    private fun streak(entries: Set<String>, today: LocalDate): Int {
        val days = entries.mapNotNull { runCatching { LocalDate.parse(it.substringBefore('|')) }.getOrNull() }.toSet()
        var day = if (today in days) today else today.minusDays(1)
        var count = 0
        while (day in days) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    fun toggleDone(duaId: String) = viewModelScope.launch { repo.toggleDone(LocalDate.now(), duaId) }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repo.setTheme(mode) }
    fun setMorningReminder(on: Boolean) = viewModelScope.launch { repo.setMorningReminder(on) }
    fun setEveningReminder(on: Boolean) = viewModelScope.launch { repo.setEveningReminder(on) }
    fun toggleFavorite(duaId: String) = viewModelScope.launch { repo.toggleFavorite(duaId) }
    fun adjustArabicSize(delta: Int) = viewModelScope.launch {
        val now = state.value.settings.arabicSize
        repo.setArabicSize((now + delta).coerceIn(MIN_ARABIC, MAX_ARABIC))
    }
    fun setArabicSize(size: Int) = viewModelScope.launch { repo.setArabicSize(size) }
    fun setShowTransliteration(on: Boolean) = viewModelScope.launch { repo.setShowTransliteration(on) }
    fun setShowTranslation(on: Boolean) = viewModelScope.launch { repo.setShowTranslation(on) }
    fun setKeepScreenOn(on: Boolean) = viewModelScope.launch { repo.setKeepScreenOn(on) }
    fun setBackground(id: String) = viewModelScope.launch { repo.setBackground(id) }

    fun saveReminder(r: Reminder) = viewModelScope.launch { repo.saveReminder(r) }
    fun deleteReminder(r: Reminder) = viewModelScope.launch { repo.deleteReminder(r) }
    fun setPlaylist(ids: List<String>) = viewModelScope.launch { repo.setPlaylist(ids) }
    fun setLastPage(id: String, page: Int) = viewModelScope.launch { repo.setLastPage(id, page) }
    fun rescheduleReminders() = viewModelScope.launch { repo.rescheduleAll() }
}
