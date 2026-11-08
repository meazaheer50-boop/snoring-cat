package com.dailydua.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.SampleDuas
import com.dailydua.tracker.playback.PlayerHub
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

@Composable
private fun SmallIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val c = Tokens.colors
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = label, tint = c.ink, modifier = Modifier.size(22.dp)) }
}

@Composable
private fun PillButton(text: String, filled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Tokens.colors
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(if (filled) c.brand else c.hairline)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
            color = if (filled) c.onBrand else c.ink,
        )
    }
}

/** Build a queue of recordings and play them one after another, also with the screen off. */
@Composable
fun PlaylistScreen(ui: UiState, vm: DuaViewModel, onOpen: (Dua) -> Unit) {
    val c = Tokens.colors
    val context = LocalContext.current
    val ps by PlayerHub.state.collectAsStateWithLifecycle()
    val audioDuas = remember { SampleDuas.all.filter { PlayerHub.hasAudio(context, it) } }
    val byId = audioDuas.associateBy { it.id }
    val queue = ui.playlist.mapNotNull { byId[it] }
    val available = audioDuas.filter { d -> queue.none { it.id == d.id } }

    fun move(i: Int, delta: Int) {
        val ids = queue.map { it.id }.toMutableList()
        val j = i + delta
        if (j !in ids.indices) return
        ids[i] = ids[j].also { ids[j] = ids[i] }
        vm.setPlaylist(ids)
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 24.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Listen", style = DuaType.caption, color = c.inkMuted)
            Text("Playlist", style = DuaType.displayMd, color = c.ink)
            Spacer(Modifier.size(4.dp))
            Text(
                "Add recordings, press play and they play one after the other. Audio keeps going with the screen off.",
                style = DuaType.caption,
                color = c.inkMuted,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                PillButton(
                    "Play all (${queue.size})",
                    filled = true,
                    onClick = { PlayerHub.playQueue(queue) },
                    modifier = Modifier.weight(1f),
                )
                PillButton(
                    if (ps.repeatAll) "Repeat: on" else "Repeat: off",
                    filled = false,
                    onClick = { PlayerHub.setRepeatAll(!ps.repeatAll) },
                )
            }
        }
        item { SectionLabel("Your playlist", Modifier.padding(top = 8.dp), badge = "${queue.size}") }
        if (queue.isEmpty()) {
            item { Text("Nothing here yet. Add recordings from the list below.", style = DuaType.body, color = c.inkMuted) }
        }
        itemsIndexed(queue, key = { _, d -> "q-${d.id}" }) { i, d ->
            val now = ps.mediaId == d.id && ps.isPlaying
            Row(
                Modifier
                    .fillMaxWidth()
                    .glassSurface(
                        shape = RoundedCornerShape(22.dp),
                        variant = if (now) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
                    )
                    .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { PlayerHub.playQueue(queue, i) }
                        .padding(vertical = 8.dp),
                ) {
                    Text("${i + 1}. ${d.title}", style = DuaType.title, color = c.ink)
                    Text(if (now) "Playing" else d.tag, style = DuaType.caption, color = if (now) c.brand else c.inkMuted)
                }
                SmallIconButton(Icons.Rounded.KeyboardArrowUp, "Move up") { move(i, -1) }
                SmallIconButton(Icons.Rounded.KeyboardArrowDown, "Move down") { move(i, 1) }
                SmallIconButton(Icons.Rounded.Close, "Remove") { vm.setPlaylist(queue.map { it.id }.filter { it != d.id }) }
            }
        }
        item { SectionLabel("Add to playlist", Modifier.padding(top = 12.dp), badge = "${available.size}") }
        if (available.isEmpty()) {
            item { Text("Everything is already in your playlist.", style = DuaType.body, color = c.inkMuted) }
        } else {
            item {
                PillButton("Add all", filled = false, onClick = { vm.setPlaylist(queue.map { it.id } + available.map { it.id }) })
            }
        }
        itemsIndexed(available, key = { _, d -> "a-${d.id}" }) { _, d ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .glassSurface(shape = RoundedCornerShape(22.dp), variant = GlassVariant.FROST)
                    .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { onOpen(d) }
                        .padding(vertical = 8.dp),
                ) {
                    Text(d.title, style = DuaType.title, color = c.ink)
                    Text(d.tag, style = DuaType.caption, color = c.inkMuted)
                }
                SmallIconButton(Icons.Rounded.Add, "Add to playlist") { vm.setPlaylist(queue.map { it.id } + d.id) }
            }
        }
    }
}

/** Shown above the tab bar while something is loaded in the player. */
@Composable
fun MiniPlayer(onOpen: (Dua) -> Unit) {
    val c = Tokens.colors
    val ps by PlayerHub.state.collectAsStateWithLifecycle()
    val dua = SampleDuas.all.firstOrNull { it.id == ps.mediaId } ?: return
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .glassSurface(shape = RoundedCornerShape(28.dp), variant = GlassVariant.ELEVATED)
            .padding(start = 18.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .weight(1f)
                .clickable { onOpen(dua) },
        ) {
            Text(dua.title, style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = c.ink, maxLines = 1)
            val total = ps.queueIds.size
            Text(
                if (total > 1) "Track ${ps.index + 1} of $total" else dua.tag,
                style = DuaType.caption,
                color = c.inkMuted,
                maxLines = 1,
            )
        }
        if (ps.hasPrevious) SmallIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Previous") { PlayerHub.previous() }
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(c.brand)
                .clickable(role = Role.Button, onClickLabel = if (ps.isPlaying) "Pause" else "Play") { PlayerHub.toggle() },
            contentAlignment = Alignment.Center,
        ) {
            if (ps.isPlaying) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(2) { Box(Modifier.size(width = 5.dp, height = 16.dp).clip(RoundedCornerShape(2.dp)).background(c.onBrand)) }
                }
            } else {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = c.onBrand, modifier = Modifier.size(26.dp))
            }
        }
        if (ps.hasNext) SmallIconButton(Icons.AutoMirrored.Rounded.ArrowForward, "Next") { PlayerHub.next() }
        Spacer(Modifier.width(2.dp))
    }
}
