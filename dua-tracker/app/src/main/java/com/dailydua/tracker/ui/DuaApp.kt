package com.dailydua.tracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.height
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailydua.tracker.data.Mushaf
import androidx.compose.runtime.remember
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.Reminder
import com.dailydua.tracker.data.SampleDuas
import com.dailydua.tracker.ui.theme.DuaTheme
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

@Composable
fun DuaApp(vm: DuaViewModel = viewModel(), launchDuaId: String? = null, onLaunchHandled: () -> Unit = {}) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val bgStyle = BackgroundStyle.byId(ui.settings.background)
    DuaTheme(ui.settings.theme, bgStyle.accentDark, bgStyle.accentLight) {
      androidx.compose.runtime.CompositionLocalProvider(
          LocalBackgroundStyle provides BackgroundStyle.byId(ui.settings.background),
          LocalBackgroundDesign provides ui.settings.design,
      ) {
        GlassBackground {
            var tab by rememberSaveable { mutableStateOf(0) }
            var openId by rememberSaveable { mutableStateOf<String?>(null) }
            var readingId by rememberSaveable { mutableStateOf<String?>(null) }
            val enteredTabs = remember { mutableStateListOf<Int>() }
            var reminderFor by remember { mutableStateOf<Dua?>(null) }
            val askNotifications = LocalAskNotifications.current
            val appContext = androidx.compose.ui.platform.LocalContext.current
            val readingSurah = Mushaf.all.firstOrNull { it.id == readingId }
            LaunchedEffect(Unit) { vm.rescheduleReminders() }
            LaunchedEffect(launchDuaId) {
                if (launchDuaId != null) {
                    openId = launchDuaId
                    onLaunchHandled()
                }
            }

            val openDua = SampleDuas.all.firstOrNull { it.id == openId }
            val readerDua = openDua?.takeIf { it.lines.isNotEmpty() }
            val sheetDua = openDua?.takeIf { it.lines.isEmpty() }
            val readerOpen = readerDua != null || readingSurah != null

            // Stop the screen from locking while a reader is open, if the setting is on.
            val keepOn = ui.settings.keepScreenOn && readerOpen
            val hostView = androidx.compose.ui.platform.LocalView.current
            androidx.compose.runtime.DisposableEffect(keepOn) {
                hostView.keepScreenOn = keepOn
                onDispose { hostView.keepScreenOn = false }
            }

            // Navigate back from Settings tab to Today tab on system back press
            BackHandler(enabled = tab != 0 && openId == null && readingId == null) {
                tab = 0
            }

            // Layered depth effect on main scaffold when full-screen reader or sheet is active
            val bgScale by animateFloatAsState(
                targetValue = when {
                    readerOpen -> 0.93f
                    sheetDua != null -> 0.97f
                    else -> 1f
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "scaffoldDepthScale",
            )
            val bgAlpha by animateFloatAsState(
                targetValue = if (readerOpen) 0.35f else 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "scaffoldDepthAlpha",
            )

            Scaffold(
                modifier = Modifier.graphicsLayer {
                    scaleX = bgScale
                    scaleY = bgScale
                    alpha = bgAlpha
                },
                containerColor = Color.Transparent,
                bottomBar = {
                    Column {
                        if (!readerOpen) MiniPlayer(onOpen = { openId = it.id })
                        BottomBar(selected = tab, visible = !readerOpen, onSelect = { tab = it })
                    }
                },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    AnimatedContent(
                        targetState = tab,
                        transitionSpec = {
                            // A quick crossfade with a small slide. No scaling, so the big glass layers stay cheap to draw.
                            val dir = if (targetState > initialState) 1 else -1
                            (
                                fadeIn(tween(220, delayMillis = 60, easing = LinearOutSlowInEasing)) +
                                    slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { it / 16 * dir }
                                ).togetherWith(
                                fadeOut(tween(110, easing = LinearEasing)) +
                                    slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) { -it / 32 * dir },
                            ).using(SizeTransform(clip = false))
                        },
                        label = "tabNavigation",
                    ) { targetTab ->
                        // Card entrances play only the first time a tab opens, never on return visits or when scrolling back.
                        var settled by remember(targetTab) { mutableStateOf(targetTab in enteredTabs) }
                        LaunchedEffect(targetTab) {
                            if (!settled) {
                                kotlinx.coroutines.delay(1100)
                                settled = true
                                if (targetTab !in enteredTabs) enteredTabs.add(targetTab)
                            }
                        }
                        androidx.compose.runtime.CompositionLocalProvider(LocalEntrance provides !settled) {
                        when (targetTab) {
                            0 -> TodayScreen(ui, onOpen = { openId = it.id }, onFavorite = vm::toggleFavorite)
                            1 -> QuranScreen(ui, onRead = { readingId = it.id }, onOpenDua = { openId = it })
                            2 -> FavoritesScreen(ui, onOpen = { openId = it.id }, onFavorite = vm::toggleFavorite)
                            3 -> PlaylistScreen(ui, vm, onOpen = { openId = it.id })
                            else -> SettingsScreen(ui, vm)
                        }
                        }
                    }
                }
            }

            // Full-screen long dua reader with vertical modal slide + scale transition
            AnimatedContent(
                targetState = readerDua,
                transitionSpec = {
                    (slideInVertically(
                        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                    ) { fullHeight -> fullHeight / 4 } +
                        fadeIn(tween(durationMillis = 300)) +
                        scaleIn(
                            initialScale = 0.94f,
                            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                        ))
                        .togetherWith(
                            slideOutVertically(
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            ) { fullHeight -> fullHeight / 4 } +
                                fadeOut(tween(durationMillis = 220)) +
                                scaleOut(
                                    targetScale = 0.94f,
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                ),
                        )
                        .using(SizeTransform(clip = false))
                },
                label = "duaReaderTransition",
            ) { activeReaderDua ->
                if (activeReaderDua != null) {
                    DuaReaderScreen(
                        dua = activeReaderDua,
                        done = activeReaderDua.id in ui.doneToday,
                        settings = ui.settings,
                        favorite = activeReaderDua.id in ui.favorites,
                        onToggleFavorite = { vm.toggleFavorite(activeReaderDua.id) },
                        onReminder = { reminderFor = activeReaderDua },
                        onSize = { vm.adjustArabicSize(it) },
                        onToggleDone = { vm.toggleDone(activeReaderDua.id) },
                        onClose = { openId = null },
                    )
                }
            }

            // Mushaf page reader, same modal motion as the dua reader.
            AnimatedContent(
                targetState = readingSurah,
                transitionSpec = {
                    (slideInVertically(tween(380, easing = FastOutSlowInEasing)) { it / 4 } + fadeIn(tween(300)))
                        .togetherWith(slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it / 4 } + fadeOut(tween(220)))
                        .using(SizeTransform(clip = false))
                },
                label = "mushafTransition",
            ) { surah ->
                if (surah != null) {
                    MushafReader(
                        surah = surah,
                        startPage = ui.lastPages[surah.id] ?: 0,
                        onPageChange = { vm.setLastPage(surah.id, it) },
                        arabicSize = ui.settings.arabicSize,
                        onSize = { vm.adjustArabicSize(it) },
                        favorite = surah.id in ui.favorites,
                        onToggleFavorite = { vm.toggleFavorite(surah.id) },
                        onReminder = { reminderFor = SampleDuas.all.firstOrNull { it.id == surah.id } },
                        onClose = { readingId = null },
                        tone = vm.readTone.collectAsStateWithLifecycle().value,
                        onTone = { vm.setReadTone(it) },
                    )
                }
            }

            val introSeen by vm.introSeen.collectAsStateWithLifecycle()
            if (!introSeen) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {},
                    containerColor = Tokens.colors.surface200,
                    title = { Text("Welcome to Daily Dua", style = DuaType.title, color = Tokens.colors.ink) },
                    text = {
                        Text(
                            "Not all 114 surahs are in the app yet. For now you can read and listen to Al-Mulk, Al-Waqi\u2019ah, Al-Kahf, Ya-Sin, Ar-Rahman, Ad-Dhuha, Al-Ikhlas and An-Nas, along with the daily duas and the 99 Names. More surahs will be added in future updates.",
                            style = DuaType.body, color = Tokens.colors.inkMuted,
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { vm.dismissIntro() }) {
                            Text("Got it", color = Tokens.colors.brand)
                        }
                    },
                )
            }

            reminderFor?.let { d ->
                ReminderDialog(
                    initial = Reminder(id = System.currentTimeMillis().toString(), label = d.title, hour = 8, minute = 0, duaId = d.id),
                    isNew = true,
                    onSave = {
                        askNotifications()
                        vm.saveReminder(it)
                        reminderFor = null
                        android.widget.Toast.makeText(appContext, "Reminder set for ${it.timeText()}", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { reminderFor = null },
                    onDismiss = { reminderFor = null },
                )
            }

            if (sheetDua != null) {
                DuaSheet(
                    dua = sheetDua,
                    done = sheetDua.id in ui.doneToday,
                    settings = ui.settings,
                    favorite = sheetDua.id in ui.favorites,
                    onToggleFavorite = { vm.toggleFavorite(sheetDua.id) },
                    onReminder = { reminderFor = sheetDua },
                    onSize = { vm.adjustArabicSize(it) },
                    onToggleDone = { vm.toggleDone(sheetDua.id) },
                    onDismiss = { openId = null },
                )
            }
        }
      }
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

@Composable
private fun BottomBar(selected: Int, visible: Boolean, onSelect: (Int) -> Unit) {
    val c = Tokens.colors
    val items = listOf(
        TabItem("Today", Icons.Rounded.Home),
        TabItem("Quran", AppIcons.Book),
        TabItem("Favourites", Icons.Rounded.Favorite),
        TabItem("Playlist", AppIcons.Queue),
        TabItem("Settings", Icons.Rounded.Settings),
    )
    val barOffsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 140f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "bottomBarOffsetY",
    )
    val barAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "bottomBarAlpha",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .graphicsLayer {
                translationY = barOffsetY
                alpha = barAlpha
            },
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
                val pillAlpha by animateFloatAsState(
                    targetValue = if (on) 1f else 0f,
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                    label = "tabPillAlpha",
                )
                val tabScale by animateFloatAsState(
                    targetValue = if (on) 1f else 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "tabScale",
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (on) 1.14f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "tabIconScale",
                )
                val contentTint by animateColorAsState(
                    targetValue = if (on) c.onBrand else c.inkMuted,
                    animationSpec = tween(durationMillis = 240),
                    label = "tabContentTint",
                )
                val tabWeight by animateFloatAsState(
                    targetValue = if (on) 2.3f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "tabWeight",
                )
                Row(
                    Modifier
                        .weight(tabWeight)
                        .defaultMinSize(minHeight = 48.dp)
                        .graphicsLayer {
                            scaleX = tabScale
                            scaleY = tabScale
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    c.brand.copy(alpha = pillAlpha),
                                    c.brand.copy(alpha = 0.82f * pillAlpha),
                                ),
                            ),
                            CircleShape,
                        )
                        .border(1.dp, c.glassBrandBorder.copy(alpha = pillAlpha), CircleShape)
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(i) })
                        .semantics { contentDescription = item.label }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = if (on) null else item.label,
                        tint = contentTint,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                    )
                    AnimatedVisibility(
                        visible = on,
                        enter = fadeIn(tween(220)) + expandHorizontally(tween(260, easing = FastOutSlowInEasing)),
                        exit = fadeOut(tween(140)) + shrinkHorizontally(tween(220, easing = FastOutSlowInEasing)),
                    ) {
                        Row {
                            Spacer(Modifier.width(8.dp))
                            Text(item.label, style = DuaType.label.copy(letterSpacing = 0.sp, fontSize = 12.sp), color = contentTint, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

