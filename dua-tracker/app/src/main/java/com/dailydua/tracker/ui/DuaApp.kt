package com.dailydua.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailydua.tracker.data.SampleDuas
import com.dailydua.tracker.ui.theme.DuaTheme
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

@Composable
fun DuaApp(vm: DuaViewModel = viewModel()) {
    val ui by vm.state.collectAsStateWithLifecycle()
    DuaTheme(ui.settings.theme) {
        var tab by rememberSaveable { mutableStateOf(0) }
        var openId by rememberSaveable { mutableStateOf<String?>(null) }

        Scaffold(
            containerColor = Tokens.colors.surface100,
            bottomBar = { BottomBar(tab) { tab = it } },
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (tab) {
                    0 -> TodayScreen(ui, onOpen = { openId = it.id })
                    else -> SettingsScreen(ui.settings, vm)
                }
            }
        }

        SampleDuas.all.firstOrNull { it.id == openId }?.let { dua ->
            DuaSheet(
                dua = dua,
                done = dua.id in ui.doneToday,
                settings = ui.settings,
                onToggleDone = { vm.toggleDone(dua.id) },
                onDismiss = { openId = null },
            )
        }
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val c = Tokens.colors
    val items = listOf(TabItem("Today", Icons.Rounded.Home), TabItem("Settings", Icons.Rounded.Settings))
    Row(
        Modifier
            .fillMaxWidth()
            .background(c.surface200)
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEachIndexed { i, item ->
            val on = i == selected
            Column(
                Modifier
                    .defaultMinSize(minWidth = 72.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(i) }),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(width = 56.dp, height = 28.dp)
                        .clip(CircleShape)
                        .background(if (on) c.brandSoft else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.icon, contentDescription = null, tint = if (on) c.brand else c.inkMuted, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(item.label.uppercase(), style = DuaType.label, color = if (on) c.brand else c.inkMuted)
            }
        }
    }
}
