package com.dailydua.tracker.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.SampleDuas
import com.dailydua.tracker.data.TimeOfDay
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

@Composable
fun TodayScreen(ui: UiState, onOpen: (Dua) -> Unit) {
    val duas = SampleDuas.all
    val doneCount = duas.count { it.id in ui.doneToday }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Header(ui.streak) }
        item { Hero(doneCount, duas.size) }
        item { SectionLabel("Morning", Modifier.padding(top = 12.dp)) }
        items(duas.filter { it.time == TimeOfDay.MORNING }, key = { it.id }) { d ->
            DuaRow(d, d.id in ui.doneToday) { onOpen(d) }
        }
        item { SectionLabel("Evening", Modifier.padding(top = 12.dp)) }
        items(duas.filter { it.time == TimeOfDay.EVENING }, key = { it.id }) { d ->
            DuaRow(d, d.id in ui.doneToday) { onOpen(d) }
        }
    }
}

@Composable
private fun Header(streak: Int) {
    val c = Tokens.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column {
            Text("Assalamu alaikum", style = DuaType.caption, color = c.inkMuted)
            Text("Today", style = DuaType.displayMd, color = c.ink)
        }
        Box(
            Modifier
                .heightIn(min = 32.dp)
                .clip(CircleShape)
                .background(c.gold)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (streak > 0) "$streak day streak" else "Start your streak",
                style = DuaType.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = c.onGold,
            )
        }
    }
}

@Composable
private fun Hero(done: Int, total: Int) {
    val c = Tokens.colors
    val fraction by animateFloatAsState(if (total == 0) 0f else done.toFloat() / total, label = "progress")
    val message = when {
        done == total -> "All done for today. Well done."
        done > 0 -> "Good progress. Take your time."
        else -> "A calm start. Tap a dua to read it."
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(c.brand)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$done", style = DuaType.displayLg, color = c.onBrand)
            Text(" of $total duas done", style = DuaType.body, color = c.onBrand, modifier = Modifier.padding(bottom = 6.dp))
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(c.onBrand.copy(alpha = 0.3f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(c.onBrand),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(message, style = DuaType.caption, color = c.onBrand)
    }
}
