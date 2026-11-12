package com.dailydua.tracker.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import com.dailydua.tracker.ui.theme.QuranFont
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailydua.tracker.data.Mushaf
import com.dailydua.tracker.data.MushafSurah
import com.dailydua.tracker.data.SampleDuas
import com.dailydua.tracker.playback.PlayerHub
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The Quran tab: surahs to read from printed mushaf pages, plus the 99 Names. */
@Composable
fun QuranScreen(ui: UiState, onRead: (MushafSurah) -> Unit, onOpenDua: (String) -> Unit) {
    val c = Tokens.colors
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "header") {
            StaggeredEntrance(index = 0) {
                Column {
                    Text("Read", style = DuaType.caption, color = c.inkMuted)
                    Text("Quran", style = DuaType.displayMd, color = c.ink)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Read from the printed mushaf pages, or verse by verse with the English beside it.",
                        style = DuaType.caption,
                        color = c.inkMuted,
                    )
                }
            }
        }
        itemsIndexed(Mushaf.all, key = { _, s -> s.id }) { i, s ->
            StaggeredEntrance(index = 1 + i) {
                SurahCard(s, ui.lastPages[s.id] ?: 0, onRead)
            }
        }
        item(key = "names") {
            StaggeredEntrance(index = 1 + Mushaf.all.size) { NamesCard(onOpenDua) }
        }
    }
}

@Composable
private fun NamesCard(onOpenDua: (String) -> Unit) {
    val c = Tokens.colors
    val ps by PlayerHub.state.collectAsStateWithLifecycle()
    val dua = SampleDuas.all.first { it.id == "names99" }
    val playing = ps.mediaId == dua.id && ps.isPlaying
    val ask = LocalAskNotifications.current
    Column(
        Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(28.dp), variant = GlassVariant.FROST)
            .shimmerSweep()
            .bounceClick(pressedScale = 0.98f, role = Role.Button, onClickLabel = "Read the 99 Names") { onOpenDua(dua.id) }
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .glassSurface(shape = CircleShape, variant = GlassVariant.GOLD),
                contentAlignment = Alignment.Center,
            ) { Text("99", style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = c.goldInk) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("The 99 Names of Allah", style = DuaType.title, color = c.ink)
                Text("Arabic and English · with recitation", style = DuaType.caption, color = c.inkMuted)
            }
            Text("أَسْمَاءُ اللَّهِ الْحُسْنَى", style = DuaType.arabic(20), color = c.brand)
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Pill(text = "Read the names", filled = true, modifier = Modifier.weight(1f)) { onOpenDua(dua.id) }
            Pill(
                text = if (playing) "Pause" else "Listen",
                filled = false,
                icon = if (playing) AppIcons.Pause else Icons.Rounded.PlayArrow,
            ) { ask(); if (ps.mediaId == dua.id) PlayerHub.toggle() else PlayerHub.playDua(dua) }
        }
    }
}

@Composable
private fun SurahCard(s: MushafSurah, lastPage: Int, onRead: (MushafSurah) -> Unit) {
    val c = Tokens.colors
    val ps by PlayerHub.state.collectAsStateWithLifecycle()
    val dua = SampleDuas.all.firstOrNull { it.id == s.audioDuaId }
    val playingThis = dua != null && ps.mediaId == dua.id && ps.isPlaying
    val ask = LocalAskNotifications.current
    var showBenefits by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(28.dp), variant = GlassVariant.FROST)
            .shimmerSweep()
            .padding(20.dp),
    ) {
        Row(
            Modifier.bounceClick(pressedScale = 0.98f, role = Role.Button, onClickLabel = "Read ${s.nameEn}") { onRead(s) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .glassSurface(shape = CircleShape, variant = GlassVariant.GOLD),
                contentAlignment = Alignment.Center,
            ) { Text("${s.number}", style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = c.goldInk) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(s.nameEn, style = DuaType.title, color = c.ink)
                Text("${s.meaning} · ${s.verses} verses", style = DuaType.caption, color = c.inkMuted)
            }
            Text(s.nameAr, style = DuaType.arabic(26), color = c.brand)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .clip(CircleShape)
                .glassSurface(shape = CircleShape, variant = GlassVariant.GOLD)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) { Text("Read: ${s.whenToRead}", style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = c.goldInk) }
        Spacer(Modifier.height(6.dp))
        Text(
            if (showBenefits) "Hide benefits" else "Benefits",
            style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
            color = c.brand,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .bounceClick(role = Role.Button, onClickLabel = "Show benefits") { showBenefits = !showBenefits }
                .padding(vertical = 14.dp),
        )
        AnimatedVisibility(showBenefits, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                s.benefits.forEach { Text("•  $it", style = DuaType.caption, color = c.ink) }
                Text(s.benefitNote, style = DuaType.caption, color = c.inkMuted)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Pill(
                text = if (lastPage > 0) "Continue · page ${lastPage + 1} of ${if (s.pages > 0) s.pages else s.pagesB}" else "Start reading",
                filled = true,
                modifier = Modifier.weight(1f),
            ) { onRead(s) }
            if (dua != null) {
                Pill(
                    text = if (playingThis) "Pause" else "Listen",
                    filled = false,
                    icon = if (playingThis) AppIcons.Pause else Icons.Rounded.PlayArrow,
                ) {
                    ask()
                    if (ps.mediaId == dua.id) PlayerHub.toggle() else PlayerHub.playDua(dua)
                }
            }
        }
    }
}

@Composable
fun Pill(
    text: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    horizontalPadding: androidx.compose.ui.unit.Dp = 18.dp,
    onClick: () -> Unit,
) {
    val c = Tokens.colors
    Row(
        modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .then(
                if (filled) Modifier.background(c.brand) else Modifier.glassSurface(shape = CircleShape, variant = GlassVariant.SOFT_EMERALD),
            )
            .bounceClick(role = Role.Button, onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = if (filled) c.onBrand else c.brand, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = if (filled) c.onBrand else c.brand, maxLines = 1)
    }
}

/** Full-screen reader in a calm paper style. Tap the page to hide or show the controls. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MushafReader(
    surah: MushafSurah,
    startPage: Int,
    onPageChange: (Int) -> Unit,
    arabicSize: Int,
    onSize: (Int) -> Unit,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onReminder: () -> Unit,
    onClose: () -> Unit,
    tone: Int = -1,
    onTone: (Int) -> Unit = {},
) {
    // The reader has its own brightness: 0 light, 1 mid-light, 2 mid-dark, 3 dark. It follows the app theme until changed.
    val eff = if (tone in 0..3) tone else if (Tokens.colors.isDark) 3 else 0
    val bg = LocalBackgroundStyle.current
    com.dailydua.tracker.ui.theme.DuaTheme(
        if (eff >= 2) com.dailydua.tracker.ui.theme.ThemeMode.DARK else com.dailydua.tracker.ui.theme.ThemeMode.LIGHT,
        bg.accentDark, bg.accentLight,
    ) {
        MushafReaderBody(surah, startPage, onPageChange, arabicSize, onSize, favorite, onToggleFavorite, onReminder, onClose, eff) { onTone((eff + 1) % 4) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MushafReaderBody(
    surah: MushafSurah,
    startPage: Int,
    onPageChange: (Int) -> Unit,
    arabicSize: Int,
    onSize: (Int) -> Unit,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onReminder: () -> Unit,
    onClose: () -> Unit,
    readerTone: Int,
    onCycleTone: () -> Unit,
) {
    val c = Tokens.colors
    BackHandler(onBack = onClose)
    // Two printed editions: 0 is the plain one, 1 the ornate one. Some surahs have only one of them.
    val hasPlain = surah.pages > 0
    val hasOrnate = surah.pagesB > 0
    var edition by rememberSaveable { mutableStateOf(if (hasPlain) 0 else 1) }
    val pageCount = if (edition == 0) surah.pages else surah.pagesB
    val pager = rememberPagerState(initialPage = startPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))) { pageCount }
    LaunchedEffect(edition) {
        if (pager.currentPage > pageCount - 1) pager.scrollToPage(pageCount - 1)
    }
    val scope = rememberCoroutineScope()
    var zoomed by remember { mutableStateOf(false) }
    var chrome by remember { mutableStateOf(true) }
    var pagesMode by rememberSaveable { mutableStateOf(false) }
    var showEnglish by rememberSaveable { mutableStateOf(false) }
    var fullscreen by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current
    DisposableEffect(fullscreen) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (fullscreen) {
            controller?.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { onPageChange(it) } }
    val ps by PlayerHub.state.collectAsStateWithLifecycle()
    val dua = SampleDuas.all.firstOrNull { it.id == surah.audioDuaId }
    val playingThis = dua != null && ps.mediaId == dua.id && ps.isPlaying
    val ask = LocalAskNotifications.current
    val paper = listOf(Color(0xFFFBF7EC), Color(0xFFE3D6BA), Color(0xFF2A2926), Color(0xFF0A100D))[readerTone]
    val gold = if (c.isDark) Color(0xFFD9B35A) else Color(0xFFB8860B)
    val showChrome = chrome && !fullscreen

    Box(
        Modifier
            .fillMaxSize()
            .background(paper),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // Surah banner, always visible, like a printed mushaf header.
            AnimatedVisibility(!fullscreen && (!pagesMode || chrome)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Juz ${surah.juz}", style = DuaType.caption, color = c.inkMuted, modifier = Modifier.weight(1f))
                Box(
                    Modifier
                        .border(1.dp, gold, RoundedCornerShape(10.dp))
                        .padding(3.dp)
                        .border(1.dp, gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 22.dp, vertical = 4.dp),
                ) { Text(surah.nameAr, style = DuaType.arabic(22), color = c.ink) }
                Text(
                    "Surah ${surah.number}",
                    style = DuaType.caption,
                    color = c.inkMuted,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (!pagesMode) {
                    TextReader(surah, gold, showEnglish, arabicSize, onTap = { if (!fullscreen) chrome = !chrome })
                } else {
                    HorizontalPager(
                        state = pager,
                        reverseLayout = true,
                        userScrollEnabled = !zoomed,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        MushafPage(surah, page, edition, onZoomChange = { zoomed = it }, onTap = { if (!fullscreen) chrome = !chrome })
                    }
                }
            }

            // Page number medallion.
            if (pagesMode && !fullscreen && chrome) {
                Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .border(1.dp, gold, CircleShape)
                            .padding(3.dp)
                            .border(1.dp, gold.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("${pager.currentPage + 1}", style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = c.ink) }
                }
            }

            AnimatedVisibility(
                showChrome,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                Column {
                if (!pagesMode) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FavoriteButton(favorite, onToggleFavorite)
                        Spacer(Modifier.width(8.dp))
                        ReminderButton(onReminder)
                        Spacer(Modifier.width(8.dp))
                        RepeatButton()
                        Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .size(44.dp)
                            .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
                            .testTag("fullscreen")
                            .bounceClick(role = Role.Button, onClickLabel = "Full screen") { fullscreen = true; chrome = false },
                        contentAlignment = Alignment.Center,
                    ) { Icon(AppIcons.Fullscreen, contentDescription = "Full screen", tint = c.ink) }
                        Spacer(Modifier.weight(1f))
                        TextSizeControl(arabicSize, onSize)
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .glassSurface(shape = RoundedCornerShape(28.dp), variant = GlassVariant.ELEVATED)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
                            .bounceClick(role = Role.Button, onClickLabel = "Back", onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = c.ink) }
                    Pill(
                        text = if (pagesMode) "Text" else "Pages",
                        filled = false,
                        modifier = Modifier.weight(1f).testTag("verseToggle"),
                        horizontalPadding = 8.dp,
                    ) { pagesMode = !pagesMode }
                    if (pagesMode && hasPlain && hasOrnate) {
                        Pill(
                            text = if (edition == 0) "Ornate" else "Plain",
                            filled = false,
                            modifier = Modifier.weight(1f).testTag("editionToggle"),
                            horizontalPadding = 8.dp,
                        ) { edition = if (edition == 0) 1 else 0 }
                    }
                    if (!pagesMode) {
                        Pill(
                            text = if (showEnglish) "Arabic" else "English",
                            filled = false,
                            modifier = Modifier.weight(1f).testTag("englishToggle"),
                            horizontalPadding = 8.dp,
                        ) { showEnglish = !showEnglish }
                    }
                    val toneContext = LocalContext.current
                    Box(
                        Modifier
                            .size(48.dp)
                            .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
                            .testTag("toneToggle")
                            .bounceClick(role = Role.Button, onClickLabel = "Change reading brightness") {
                                onCycleTone()
                                android.widget.Toast.makeText(
                                    toneContext,
                                    listOf("Light", "Mid light", "Mid dark", "Dark")[(readerTone + 1) % 4],
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            },
                        contentAlignment = Alignment.Center,
                    ) { Icon(AppIcons.Contrast, contentDescription = "Reading brightness", tint = c.ink) }
                    if (dua != null) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(c.brand)
                                .bounceClick(role = Role.Button, onClickLabel = if (playingThis) "Pause" else "Listen") {
                                    ask(); if (ps.mediaId == dua.id) PlayerHub.toggle() else PlayerHub.playDua(dua)
                                },
                            contentAlignment = Alignment.Center,
                        ) { Icon(if (playingThis) AppIcons.Pause else Icons.Rounded.PlayArrow, contentDescription = null, tint = c.onBrand) }
                    }
                }
                }
            }
        }
        if (fullscreen) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(10.dp)
                    .size(44.dp)
                    .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
                    .testTag("exitFullscreen")
                    .bounceClick(role = Role.Button, onClickLabel = "Exit full screen") { fullscreen = false; chrome = true },
                contentAlignment = Alignment.Center,
            ) { Icon(AppIcons.FullscreenExit, contentDescription = "Exit full screen", tint = c.ink) }
        }
    }
}

private fun arabicDigits(n: String) = n.map { if (it in '0'..'9') '\u0660' + (it - '0') else it }.joinToString("")

@Composable
private fun TextReader(surah: MushafSurah, gold: Color, showEnglish: Boolean, arabicSize: Int, onTap: () -> Unit) {
    val c = Tokens.colors
    val dua = SampleDuas.all.first { it.id == surah.id }
    val numbered = Regex("^(\\d+)\\.\\s*(.*)$", RegexOption.DOT_MATCHES_ALL)
    val verses = dua.lines.mapNotNull { l -> numbered.find(l.translation)?.let { Triple(it.groupValues[1], l.arabic, it.groupValues[2]) } }
    val bismillah = dua.lines.firstOrNull { it.arabic.startsWith("بِسْمِ") }
    val ink = c.ink
    // Each verse ends with a gold ring holding its number in Arabic-Indic digits.
    val markers = remember(verses.size, arabicSize, gold, ink) {
        verses.associate { (n, _, _) ->
            "v$n" to InlineTextContent(
                Placeholder((arabicSize * 1.3f).sp, (arabicSize * 1.3f).sp, PlaceholderVerticalAlign.Center),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // A scalloped rosette like the verse-end marker in a printed mushaf.
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize().padding(1.dp)) {
                        val radius = size.minDimension / 2f
                        val path = androidx.compose.ui.graphics.Path()
                        val steps = 280
                        for (i in 0..steps) {
                            val th = (2.0 * Math.PI * i / steps).toFloat()
                            val r = radius * (0.93f + 0.07f * kotlin.math.cos(16f * th))
                            val x = center.x + r * kotlin.math.cos(th)
                            val y = center.y + r * kotlin.math.sin(th)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(path, gold.copy(alpha = 0.16f))
                        drawPath(path, gold, style = androidx.compose.ui.graphics.drawscope.Stroke(1.4.dp.toPx()))
                        drawCircle(gold.copy(alpha = 0.7f), radius = radius * 0.70f, style = androidx.compose.ui.graphics.drawscope.Stroke(0.8.dp.toPx()))
                    }
                    Text(
                        arabicDigits(n),
                        style = TextStyle(
                            fontFamily = QuranFont,
                            fontSize = (arabicSize * (if (n.length > 2) 0.40f else 0.50f)).sp,
                            lineHeight = (arabicSize * (if (n.length > 2) 0.40f else 0.50f)).sp,
                            lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                                androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                                androidx.compose.ui.text.style.LineHeightStyle.Trim.Both,
                            ),
                            color = ink,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) },
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            var open by remember { mutableStateOf(false) }
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, gold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .bounceClick(role = Role.Button, onClickLabel = "Show benefits") { open = !open }
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Read: ${surah.whenToRead}",
                        style = DuaType.caption.copy(fontWeight = FontWeight.Bold),
                        color = gold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(if (open) "Hide benefits" else "Benefits", style = DuaType.caption.copy(fontWeight = FontWeight.Bold), color = c.brand)
                }
                AnimatedVisibility(open, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        surah.benefits.forEach { Text("•  $it", style = DuaType.caption, color = c.ink) }
                        Spacer(Modifier.height(6.dp))
                        Text(surah.benefitNote, style = DuaType.caption, color = c.inkMuted)
                    }
                }
            }
        }
        if (bismillah != null) {
            item {
                Text(
                    bismillah.arabic,
                    style = DuaType.arabic(30),
                    color = c.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                )
            }
        }
        if (!showEnglish) {
            item {
                val text = buildAnnotatedString {
                    verses.forEach { (n, ar, _) ->
                        append(ar)
                        append("\u00A0")
                        ayahEnd(n, gold)
                        append(" ")
                    }
                }
                Text(
                    text,
                    style = DuaType.arabic(arabicSize).copy(textAlign = TextAlign.Justify, lineHeight = (arabicSize * QURAN_LINE_HEIGHT).sp),
                    color = c.ink,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(verses.size) { i ->
                val (n, ar, en) = verses[i]
                Column(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            append(ar); append("\u00A0")
                            ayahEnd(n, gold)
                        },
                        style = DuaType.arabic(arabicSize).copy(textAlign = TextAlign.Start, lineHeight = (arabicSize * QURAN_LINE_HEIGHT).sp),
                        color = c.ink,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(en, style = DuaType.caption, color = c.inkMuted)
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(gold.copy(alpha = 0.25f)))
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MushafPage(surah: MushafSurah, page: Int, edition: Int, onZoomChange: (Boolean) -> Unit, onTap: () -> Unit) {
    val c = Tokens.colors
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, surah.id, page, edition) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(if (edition == 0) "mushaf/${surah.id}/${page + 1}.png" else "mushaf/${surah.id}/b/${page + 1}.png").use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
            }.getOrNull()
        }
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    fun setScale(s: Float) {
        scale = s.coerceIn(1f, 3f)
        if (scale == 1f) offset = Offset.Zero
        onZoomChange(scale > 1f)
    }
    val transform = rememberTransformableState { zoom, pan, _ ->
        setScale(scale * zoom)
        if (scale > 1f) offset += pan
    }
    Box(
        Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() }, onDoubleTap = { setScale(if (scale > 1f) 1f else 2.2f) })
            }
            .transformable(transform, canPan = { scale > 1f }),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = bitmap
        if (bmp == null) {
            CircularProgressIndicator(color = c.brand)
        } else {
            Image(
                bitmap = bmp,
                contentDescription = "${surah.nameEn}, page ${page + 1}",
                colorFilter = ColorFilter.tint(c.ink),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
        }
    }
}

/**
 * Line height for Quran text, in multiples of the font size. Amiri Quran draws pause marks
 * (ۚ ۖ ۗ) up to ~1.8 em above the baseline, so tighter lines push them into the line above.
 */
private const val QURAN_LINE_HEIGHT = 2.5f

/**
 * Verse ending drawn by the Quran font itself: U+06DD END OF AYAH followed by the number in
 * Arabic-Indic digits, which Amiri Quran shapes into the ornate Mushaf marker. Because it is
 * part of the text it can never drift or overlap the last word, at any size or alignment.
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.ayahEnd(n: String, gold: Color) {
    withStyle(androidx.compose.ui.text.SpanStyle(color = gold)) {
        append("\u06DD")
        append(arabicDigits(n))
    }
}
