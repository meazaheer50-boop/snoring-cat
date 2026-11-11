package com.dailydua.tracker.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.SampleDuas
import com.dailydua.tracker.data.TimeOfDay
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

@Composable
fun TodayScreen(ui: UiState, onOpen: (Dua) -> Unit, onFavorite: (String) -> Unit) {
    val all = SampleDuas.all
    // Progress counts the daily morning and evening duas. The rest is a library to browse.
    val daily = all.filter { it.time != TimeOfDay.ANYTIME }
    val doneCount = daily.count { it.id in ui.doneToday }
    val groups = all.filter { it.category != null }.groupBy { it.category!! }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            StaggeredEntrance(index = 0) { Header(ui.streak) }
        }
        item(key = "hero") {
            StaggeredEntrance(index = 1) { Hero(doneCount, daily.size) }
        }
        daySection("Through the day", daily, ui, onOpen, onFavorite)
        groups.forEach { (title, list) -> daySection(title, list, ui, onOpen, onFavorite) }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.daySection(
    title: String,
    list: List<Dua>,
    ui: UiState,
    onOpen: (Dua) -> Unit,
    onFavorite: (String) -> Unit,
) {
    item(key = "section-$title") {
        StaggeredEntrance(index = 2) {
            SectionLabel(
                text = title,
                modifier = Modifier.padding(top = 12.dp),
                badge = "${list.count { it.id in ui.doneToday }}/${list.size}",
            )
        }
    }
    itemsIndexed(list, key = { _, d -> "$title-${d.id}" }) { idx, d ->
        StaggeredEntrance(index = 3 + minOf(idx, 5)) {
            DuaRow(d, d.id in ui.doneToday, favorite = d.id in ui.favorites, onFavorite = { onFavorite(d.id) }) { onOpen(d) }
        }
    }
}

@Composable
private fun Header(streak: Int) {
    val c = Tokens.colors
    val infinite = rememberInfiniteTransition(label = "streakDotPulse")
    val dotScale by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "streakDotScale",
    )
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
                    .graphicsLayer {
                        val s = if (streak > 0) dotScale else 1f
                        scaleX = s
                        scaleY = s
                    }
                    .clip(CircleShape)
                    .background(c.gold),
            )
            Spacer(Modifier.width(8.dp))
            AnimatedContent(
                targetState = if (streak > 0) "$streak day streak" else "Start your streak",
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn(tween(240)))
                        .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(180)))
                        .using(SizeTransform(clip = false))
                },
                label = "streakText",
            ) { label ->
                Text(
                    label,
                    style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
                    color = c.goldInk,
                )
            }
        }
    }
}

@Composable
private fun Hero(done: Int, total: Int) {
    val c = Tokens.colors
    val fraction by animateFloatAsState(
        targetValue = if (total == 0) 0f else done.toFloat() / total,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "progressFraction",
    )
    val pct = if (total == 0) 0 else (done * 100) / total
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
            .shimmerSweep()
            .padding(22.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    "Daily remembrance",
                    style = DuaType.label,
                    color = Color.White.copy(alpha = 0.76f),
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedContent(
                        targetState = done,
                        transitionSpec = {
                            val up = targetState > initialState
                            (slideInVertically { if (up) it else -it } + fadeIn(tween(240)))
                                .togetherWith(slideOutVertically { if (up) -it else it } + fadeOut(tween(180)))
                                .using(SizeTransform(clip = false))
                        },
                        label = "doneCountRoll",
                    ) { count ->
                        Text("$count", style = DuaType.displayLg, color = Color.White)
                    }
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
                AnimatedContent(
                    targetState = pct,
                    transitionSpec = {
                        val up = targetState > initialState
                        (slideInVertically { if (up) it else -it } + fadeIn(tween(240)))
                            .togetherWith(slideOutVertically { if (up) -it else it } + fadeOut(tween(180)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "pctRoll",
                ) { targetPct ->
                    Text(
                        "$targetPct%",
                        style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
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
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
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
        AnimatedContent(
            targetState = message,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn(tween(260)))
                    .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(180)))
            },
            label = "heroMessage",
        ) { targetMessage ->
            Text(targetMessage, style = DuaType.caption, color = Color.White.copy(alpha = 0.90f))
        }
    }
}

