package com.dailydua.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.dailydua.tracker.data.Mushaf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.SampleDuas
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

/** The Favourites tab: every dua marked with a heart, in one place. */
@Composable
fun FavoritesScreen(ui: UiState, onOpen: (Dua) -> Unit, onFavorite: (String) -> Unit) {
    val c = Tokens.colors
    val surahIds = Mushaf.all.map { it.id }.toSet()
    val saved = SampleDuas.all.filter { it.id in ui.favorites }
    // 0 = everything, 1 = duas, 2 = surahs
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    val labels = listOf("All", "Duas", "Surahs")
    val favs = when (filter) {
        1 -> saved.filter { it.id !in surahIds }
        2 -> saved.filter { it.id in surahIds }
        else -> saved
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            StaggeredEntrance(index = 0) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("Your collection", style = DuaType.caption, color = c.inkMuted)
                        Text("Favourites", style = DuaType.displayMd, color = c.ink)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (favs.isEmpty()) "Nothing here yet." else "${favs.size} saved · tap the heart to remove one.",
                            style = DuaType.caption,
                            color = c.inkMuted,
                        )
                    }
                    Box {
                        Row(
                            Modifier
                                .testTag("favFilter")
                                .glassSurface(shape = RoundedCornerShape(50), variant = GlassVariant.FROST)
                                .bounceClick(role = Role.Button, onClickLabel = "Choose what to show") { menuOpen = true }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(labels[filter], style = DuaType.caption, color = c.ink)
                            Spacer(Modifier.width(6.dp))
                            Text("\u25BE", style = DuaType.caption, color = c.inkMuted)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = c.surface200) {
                            labels.forEachIndexed { i, label ->
                                DropdownMenuItem(
                                    text = { Text(label, style = DuaType.body, color = if (i == filter) c.brand else c.ink) },
                                    onClick = { filter = i; menuOpen = false },
                                )
                            }
                        }
                    }
                }
            }
        }
        if (favs.isEmpty()) {
            item(key = "empty") {
                StaggeredEntrance(index = 1) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .glassSurface(shape = RoundedCornerShape(28.dp), variant = GlassVariant.FROST)
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .size(64.dp)
                                .glassSurface(shape = CircleShape, variant = GlassVariant.GOLD),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Rounded.Favorite, contentDescription = null, tint = c.goldInk, modifier = Modifier.size(30.dp)) }
                        Spacer(Modifier.height(16.dp))
                        Text(if (saved.isEmpty()) "No favourites yet" else "No ${labels[filter].lowercase()} saved", style = DuaType.title, color = c.ink)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Tap the heart on any dua, surah or the 99 Names and it will be kept here for quick reading.",
                            style = DuaType.caption,
                            color = c.inkMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            }
        }
        itemsIndexed(favs, key = { _, d -> "fav-${d.id}" }) { idx, d ->
            StaggeredEntrance(index = 1 + minOf(idx, 6)) {
                DuaRow(d, d.id in ui.doneToday, favorite = true, onFavorite = { onFavorite(d.id) }) { onOpen(d) }
            }
        }
    }
}
