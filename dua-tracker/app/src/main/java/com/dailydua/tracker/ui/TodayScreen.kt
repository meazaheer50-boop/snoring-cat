package com.dailydua.tracker.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val morning = duas.filter { it.time == TimeOfDay.MORNING }
    val evening = duas.filter { it.time == TimeOfDay.EVENING }
    val anytime = duas.filter { it.time == TimeOfDay.ANYTIME }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Header(ui.streak) }
        item { Hero(doneCount, duas.size) }
        item {
            SectionLabel(
                text = "Morning",
                modifier = Modifier.padding(top = 12.dp),
                badge = "${morning.count { it.id in ui.doneToday }}/${morning.size}",
            )
        }
        items(morning, key = { it.id }) { d ->
            DuaRow(d, d.id in ui.doneToday) { onOpen(d) }
        }
        item {
            SectionLabel(
                text = "Evening",
                modifier = Modifier.padding(top = 12.dp),
                badge = "${evening.count { it.id in ui.doneToday }}/${evening.size}",
            )
        }
        items(evening, key = { it.id }) { d ->
            DuaRow(d, d.id in ui.doneToday) { onOpen(d) }
        }
        item {
            SectionLabel(
                text = "Longer duas",
                modifier = Modifier.padding(top = 12.dp),
                badge = "${anytime.count { it.id in ui.doneToday }}/${anytime.size}",
            )
        }
        items(anytime, key = { it.id }) { d ->
            DuaRow(d, d.id in ui.doneToday) { onOpen(d) }
        }
    }
}

@Composable
private fun Header(streak: Int) {
    val c = Tokens.colors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Assalamu alaikum", style = DuaType.caption, color = c.inkMuted)
            Text("Today", style = DuaType.displayMd, color = c.ink)
        }
        Row(
            Modifier
                .heightIn(min = 36.dp)
                .glassSurface(shape = CircleShape, variant = GlassVariant.GOLD)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(c.gold),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (streak > 0) "$streak day streak" else "Start your streak",
                style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
                color = c.goldInk,
            )
        }
    }
}

@Composable
private fun Hero(done: Int, total: Int) {
    val c = Tokens.colors
    val fraction by animateFloatAsState(if (total == 0) 0f else done.toFloat() / total, label = "progress")
    val pct = (fraction * 100).toInt()
    val message = when {
        done == total -> "All done for today. Well done."
        done > 0 -> "Good progress. Take your time."
        else -> "A calm start. Tap a dua to read it."
    }
    Column(
        Modifier
            .fillMaxWidth()
            .glassSurface(
                shape = RoundedCornerShape(28.dp),
                variant = GlassVariant.EMERALD,
                borderWidth = 1.dp,
            )
            .padding(22.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    "DAILY REMEMBRANCE",
                    style = DuaType.label,
                    color = Color.White.copy(alpha = 0.76f),
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$done", style = DuaType.displayLg, color = Color.White)
                    Text(
                        " of $total duas done",
                        style = DuaType.body,
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$pct%",
                    style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.20f))
                .border(0.5.dp, Color.White.copy(alpha = 0.30f), CircleShape),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.White, c.gold),
                        ),
                    ),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(message, style = DuaType.caption, color = Color.White.copy(alpha = 0.90f))
    }
}

