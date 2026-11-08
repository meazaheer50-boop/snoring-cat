package com.dailydua.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
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
        GlassBackground {
            var tab by rememberSaveable { mutableStateOf(0) }
            var openId by rememberSaveable { mutableStateOf<String?>(null) }

            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { BottomBar(tab) { tab = it } },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        0 -> TodayScreen(ui, onOpen = { openId = it.id })
                        else -> SettingsScreen(ui.settings, vm)
                    }
                }
            }

            SampleDuas.all.firstOrNull { it.id == openId }?.let { dua ->
                if (dua.lines.isNotEmpty()) {
                    DuaReaderScreen(
                        dua = dua,
                        done = dua.id in ui.doneToday,
                        settings = ui.settings,
                        onToggleDone = { vm.toggleDone(dua.id) },
                        onClose = { openId = null },
                    )
                    return@let
                }
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
}

private data class TabItem(val label: String, val icon: ImageVector)

@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val c = Tokens.colors
    val items = listOf(TabItem("Today", Icons.Rounded.Home), TabItem("Settings", Icons.Rounded.Settings))
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .glassSurface(
                    shape = RoundedCornerShape(32.dp),
                    variant = GlassVariant.ELEVATED,
                    borderWidth = 1.dp,
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEachIndexed { i, item ->
                val on = i == selected
                Row(
                    Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(CircleShape)
                        .then(
                            if (on) {
                                Modifier
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(c.brand, c.brand.copy(alpha = 0.82f)),
                                        ),
                                        CircleShape,
                                    )
                                    .border(1.dp, c.glassBrandBorder, CircleShape)
                            } else {
                                Modifier
                            },
                        )
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(i) })
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = if (on) c.onBrand else c.inkMuted,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.label.uppercase(),
                        style = DuaType.label,
                        color = if (on) c.onBrand else c.inkMuted,
                    )
                }
            }
        }
    }
}

