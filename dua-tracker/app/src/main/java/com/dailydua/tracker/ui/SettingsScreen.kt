package com.dailydua.tracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.dailydua.tracker.data.Reminder
import com.dailydua.tracker.data.SampleDuas
import com.dailydua.tracker.reminders.ReminderScheduler
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
import androidx.compose.runtime.getValue
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
fun SettingsScreen(ui: UiState, vm: DuaViewModel) {
    val c = Tokens.colors
    val settings = ui.settings
    val context = LocalContext.current
    var editing by remember { mutableStateOf<Reminder?>(null) }
    val animatedArabicSize by animateIntAsState(
        targetValue = settings.arabicSize,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "previewArabicSize",
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
    ) {
        StaggeredEntrance(index = 0) {
            Column {
                Text("Make it yours", style = DuaType.caption, color = c.inkMuted)
                Text("Settings", style = DuaType.displayMd, color = c.ink)
            }
        }

        StaggeredEntrance(index = 1) {
            Column {
                Group("Appearance") {
                    Padded {
                        Segmented(
                            options = listOf("System", "Light", "Dark"),
                            selected = settings.theme.ordinal,
                            onSelect = { vm.setTheme(ThemeMode.entries[it]) },
                        )
                    }
                }
            }
        }

        StaggeredEntrance(index = 2) {
            Column {
        Group("Reminders") {
            SwitchRow("Morning reminder", Reminder.morning.timeText(), settings.morningReminder, vm::setMorningReminder)
            HorizontalDivider(color = c.hairline)
            SwitchRow("Evening reminder", Reminder.evening.timeText(), settings.eveningReminder, vm::setEveningReminder)
            ui.reminders.forEach { r ->
                HorizontalDivider(color = c.hairline)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 66.dp)
                        .clickable { editing = r }
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(r.label.ifBlank { "Reminder" }, style = DuaType.body.copy(fontWeight = FontWeight.SemiBold), color = c.ink)
                        Text("${r.timeText()} \u00b7 ${r.daysText()}", style = DuaType.caption, color = c.inkMuted)
                    }
                    Box(
                        Modifier
                            .toggleable(value = r.enabled, role = Role.Switch, onValueChange = { vm.saveReminder(r.copy(enabled = it)) })
                            .padding(8.dp),
                    ) { SwitchVisual(r.enabled) }
                }
            }
            HorizontalDivider(color = c.hairline)
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable {
                        editing = Reminder(
                            id = System.currentTimeMillis().toString(),
                            label = "",
                            hour = 8,
                            minute = 0,
                        )
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("+ Add a custom reminder", style = DuaType.body.copy(fontWeight = FontWeight.SemiBold), color = c.brand)
            }
        }
        Text(
            "Reminders show a notification at the time you choose and open the dua you pick.",
            style = DuaType.caption,
            color = c.inkMuted,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
        )
        if (!ReminderScheduler.canScheduleExact(context)) {
            Text(
                "Tap here to allow exact alarms, so reminders arrive on the minute.",
                style = DuaType.caption.copy(fontWeight = FontWeight.SemiBold),
                color = c.brand,
                modifier = Modifier
                    .padding(top = 6.dp, start = 4.dp)
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).setData(Uri.parse("package:${context.packageName}")))
                        }
                    },
            )
        }
            }
        }

        StaggeredEntrance(index = 3) {
            Column {
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
            }
        }

        StaggeredEntrance(index = 4) {
            Column {
                SectionLabel("Live Preview", Modifier.padding(top = 24.dp, bottom = 8.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .glassSurface(
                            shape = RoundedCornerShape(24.dp),
                            variant = GlassVariant.SOFT_EMERALD,
                        )
                        .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                        .padding(20.dp),
                ) {
                    Text(
                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        style = DuaType.arabic(animatedArabicSize),
                        color = c.ink,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AnimatedVisibility(
                        visible = settings.showTransliteration,
                        enter = fadeIn(tween(240)) + expandVertically(tween(240)),
                        exit = fadeOut(tween(180)) + shrinkVertically(tween(180)),
                    ) {
                        Column {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Bismillahir-Rahmanir-Rahim",
                                style = DuaType.body.copy(fontStyle = FontStyle.Italic),
                                color = c.inkMuted,
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = settings.showTranslation,
                        enter = fadeIn(tween(240)) + expandVertically(tween(240)),
                        exit = fadeOut(tween(180)) + shrinkVertically(tween(180)),
                    ) {
                        Column {
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
        }
        Text(
            "Quran text from Tanzil.net (Uthmani). English translation: Saheeh International. Other duas are written from memory and the recordings are transcribed from videos. Please check them against a trusted source.",
            style = DuaType.caption,
            color = c.inkMuted,
            modifier = Modifier.padding(top = 24.dp, start = 4.dp),
        )
    }

    editing?.let { r ->
        ReminderDialog(
            initial = r,
            isNew = ui.reminders.none { it.id == r.id },
            onSave = { vm.saveReminder(it); editing = null },
            onDelete = { vm.deleteReminder(r); editing = null },
            onDismiss = { editing = null },
        )
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

@Composable
private fun Stepper(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    val c = Tokens.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = DuaType.label, color = c.inkMuted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(c.hairline).clickable(onClick = onMinus), contentAlignment = Alignment.Center) {
                Text("\u2212", style = DuaType.title, color = c.ink)
            }
            Text(value, style = DuaType.displayMd, color = c.ink, modifier = Modifier.width(72.dp), textAlign = TextAlign.Center)
            Box(Modifier.size(44.dp).clip(CircleShape).background(c.hairline).clickable(onClick = onPlus), contentAlignment = Alignment.Center) {
                Text("+", style = DuaType.title, color = c.ink)
            }
        }
    }
}

@Composable
private fun ReminderDialog(
    initial: Reminder,
    isNew: Boolean,
    onSave: (Reminder) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = Tokens.colors
    var label by remember { mutableStateOf(initial.label) }
    var hour by remember { mutableIntStateOf(initial.hour) }
    var minute by remember { mutableIntStateOf(initial.minute) }
    var days by remember { mutableIntStateOf(initial.days) }
    var duaIndex by remember { mutableIntStateOf(SampleDuas.all.indexOfFirst { it.id == initial.duaId }) } // -1 = none
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface200,
        title = { Text(if (isNew) "New reminder" else "Edit reminder", style = DuaType.title, color = c.ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(40) },
                    label = { Text("Name (for example, Dua for success)") },
                    singleLine = true,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stepper("HOUR", "%02d".format(hour), { hour = (hour + 23) % 24 }, { hour = (hour + 1) % 24 })
                    Stepper("MINUTE", "%02d".format(minute), { minute = (minute + 55) % 60 }, { minute = (minute + 5) % 60 })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    dayNames.forEachIndexed { i, n ->
                        val on = days and (1 shl i) != 0
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (on) c.brand else c.hairline)
                                .clickable { days = days xor (1 shl i) },
                            contentAlignment = Alignment.Center,
                        ) { Text(n, style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = if (on) c.onBrand else c.ink) }
                    }
                }
                val duaTitle = SampleDuas.all.getOrNull(duaIndex)?.title ?: "None"
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(c.hairline)
                        .clickable { duaIndex = if (duaIndex + 1 >= SampleDuas.all.size) -1 else duaIndex + 1 }
                        .padding(12.dp),
                ) {
                    Text("OPENS (TAP TO CHANGE)", style = DuaType.label, color = c.inkMuted)
                    Text(duaTitle, style = DuaType.body.copy(fontWeight = FontWeight.SemiBold), color = c.ink)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = days and 127 != 0,
                onClick = {
                    onSave(
                        initial.copy(
                            label = label.trim(),
                            hour = hour,
                            minute = minute,
                            days = days,
                            duaId = SampleDuas.all.getOrNull(duaIndex)?.id,
                            enabled = true,
                        ),
                    )
                },
            ) { Text("Save", color = c.brand) }
        },
        dismissButton = {
            Row {
                if (!isNew) TextButton(onClick = onDelete) { Text("Delete", color = c.ink) }
                TextButton(onClick = onDismiss) { Text("Cancel", color = c.inkMuted) }
            }
        },
    )
}
