package com.dailydua.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Settings
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.ThemeMode
import com.dailydua.tracker.ui.theme.Tokens

private val sizeOptions = listOf("Small" to 26, "Medium" to 32, "Large" to 40)

@Composable
fun SettingsScreen(settings: Settings, vm: DuaViewModel) {
    val c = Tokens.colors
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
    ) {
        Text("Make it yours", style = DuaType.caption, color = c.inkMuted)
        Text("Settings", style = DuaType.displayMd, color = c.ink)

        Group("Appearance") {
            Padded {
                Segmented(
                    options = listOf("System", "Light", "Dark"),
                    selected = settings.theme.ordinal,
                    onSelect = { vm.setTheme(ThemeMode.entries[it]) },
                )
            }
        }

        Group("Reminders") {
            SwitchRow("Morning reminder", "6:30 AM", settings.morningReminder, vm::setMorningReminder)
            HorizontalDivider(color = c.hairline)
            SwitchRow("Evening reminder", "6:00 PM", settings.eveningReminder, vm::setEveningReminder)
        }
        Text(
            "Your choice is saved. Notifications arrive in a later update.",
            style = DuaType.caption,
            color = c.inkMuted,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
        )

        Group("Reading") {
            Padded {
                Text(
                    "Arabic text size",
                    style = DuaType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = c.ink,
                )
                Spacer(Modifier.height(10.dp))
                Segmented(
                    options = sizeOptions.map { it.first },
                    selected = sizeOptions.indexOfFirst { it.second == settings.arabicSize }.coerceAtLeast(0),
                    onSelect = { vm.setArabicSize(sizeOptions[it].second) },
                )
            }
            HorizontalDivider(color = c.hairline)
            SwitchRow("Show transliteration", null, settings.showTransliteration, vm::setShowTransliteration)
            HorizontalDivider(color = c.hairline)
            SwitchRow("Show translation", null, settings.showTranslation, vm::setShowTranslation)
        }

        SectionLabel("Live Preview", Modifier.padding(top = 24.dp, bottom = 8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .glassSurface(
                    shape = RoundedCornerShape(24.dp),
                    variant = GlassVariant.SOFT_EMERALD,
                )
                .padding(20.dp),
        ) {
            Text(
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = DuaType.arabic(settings.arabicSize),
                color = c.ink,
                modifier = Modifier.fillMaxWidth(),
            )
            if (settings.showTransliteration) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Bismillahir-Rahmanir-Rahim",
                    style = DuaType.body.copy(fontStyle = FontStyle.Italic),
                    color = c.inkMuted,
                )
            }
            if (settings.showTranslation) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                    style = DuaType.body,
                    color = c.ink,
                )
            }
        }
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    SectionLabel(title, Modifier.padding(top = 24.dp, bottom = 8.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .glassSurface(
                shape = RoundedCornerShape(24.dp),
                variant = GlassVariant.FROST,
            ),
    ) { content() }
}

@Composable
private fun Padded(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) { content() }
}

@Composable
private fun SwitchRow(title: String, caption: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = Tokens.colors
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = DuaType.body.copy(fontWeight = FontWeight.SemiBold), color = c.ink)
            if (caption != null) Text(caption, style = DuaType.caption, color = c.inkMuted)
        }
        SwitchVisual(checked)
    }
}

