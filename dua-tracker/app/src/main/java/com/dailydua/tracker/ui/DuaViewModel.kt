package com.dailydua.tracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailydua.tracker.data.DuaRepository
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
)

class DuaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DuaRepository(app)

    val state: StateFlow<UiState> = combine(repo.settings, repo.doneEntries) { settings, entries ->
        val today = LocalDate.now()
        UiState(
            settings = settings,
            doneToday = entries.filter { it.startsWith("$today|") }.map { it.substringAfter('|') }.toSet(),
            streak = streak(entries, today),
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
    fun setArabicSize(size: Int) = viewModelScope.launch { repo.setArabicSize(size) }
    fun setShowTransliteration(on: Boolean) = viewModelScope.launch { repo.setShowTransliteration(on) }
    fun setShowTranslation(on: Boolean) = viewModelScope.launch { repo.setShowTranslation(on) }
}
