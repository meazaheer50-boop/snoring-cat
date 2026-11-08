package com.dailydua.tracker.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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

/** The Quran tab: surahs to read from printed mushaf pages. */
@Composable
fun QuranScreen(ui: UiState, onRead: (MushafSurah) -> Unit) {
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
                        "Read from the printed mushaf pages. Swipe to turn a page, pinch or double-tap to zoom.",
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
    }
}

@Composable
private fun SurahCard(s: MushafSurah, lastPage: Int, onRead: (MushafSurah) -> Unit) {
    val c = Tokens.colors
    val ps by PlayerHub.state.collectAsStateWithLifecycle()
    val dua = SampleDuas.all.firstOrNull { it.id == s.audioDuaId }
    val playingThis = dua != null && ps.mediaId == dua.id && ps.isPlaying
    val ask = LocalAskNotifications.current
    Column(
        Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(28.dp), variant = GlassVariant.FROST)
            .shimmerSweep()
            .bounceClick(pressedScale = 0.98f, role = Role.Button, onClickLabel = "Read ${s.nameEn}") { onRead(s) }
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Pill(
                text = if (lastPage > 0) "Continue · page ${lastPage + 1} of ${s.pages}" else "Start reading",
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
            .padding(horizontal = 18.dp, vertical = 12.dp),
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

/** Full-screen page reader. Pages turn right-to-left like a printed mushaf. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MushafReader(
    surah: MushafSurah,
    startPage: Int,
    onPageChange: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val c = Tokens.colors
    BackHandler(onBack = onClose)
    val pager = rememberPagerState(initialPage = startPage.coerceIn(0, surah.pages - 1)) { surah.pages }
    val scope = rememberCoroutineScope()
    var zoomed by remember { mutableStateOf(false) }
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { onPageChange(it) } }
    val ps by PlayerHub.state.collectAsStateWithLifecycle()
    val dua = SampleDuas.all.firstOrNull { it.id == surah.audioDuaId }
    val playingThis = dua != null && ps.mediaId == dua.id && ps.isPlaying
    val ask = LocalAskNotifications.current

    GlassBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .glassSurface(shape = RoundedCornerShape(24.dp), variant = GlassVariant.ELEVATED)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .glassSurface(shape = CircleShape, variant = GlassVariant.FROST)
                        .bounceClick(role = Role.Button, onClickLabel = "Back", onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = c.ink) }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(surah.nameEn, style = DuaType.title, color = c.ink, maxLines = 1)
                    Text("Page ${pager.currentPage + 1} of ${surah.pages}", style = DuaType.caption, color = c.inkMuted)
                }
                if (dua != null) {
                    Pill(
                        text = if (playingThis) "Pause" else "Listen",
                        filled = playingThis,
                        icon = if (playingThis) AppIcons.Pause else Icons.Rounded.PlayArrow,
                    ) { ask(); if (ps.mediaId == dua.id) PlayerHub.toggle() else PlayerHub.playDua(dua) }
                }
            }

            HorizontalPager(
                state = pager,
                reverseLayout = true,
                userScrollEnabled = !zoomed,
                pageSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                MushafPage(surah, page, onZoomChange = { zoomed = it })
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Right-to-left, like the pages.
                for (i in surah.pages - 1 downTo 0) {
                    val on = i == pager.currentPage
                    val w by animateDpAsState(if (on) 22.dp else 8.dp, tween(260), label = "dotWidth")
                    val col by animateColorAsState(if (on) c.brand else c.border, tween(260), label = "dotColor")
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = w, height = 8.dp)
                            .clip(CircleShape)
                            .background(col)
                            .bounceClick(role = Role.Button, onClickLabel = "Go to page ${i + 1}") {
                                scope.launch { pager.animateScrollToPage(i) }
                            },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MushafPage(surah: MushafSurah, page: Int, onZoomChange: (Boolean) -> Unit) {
    val c = Tokens.colors
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, surah.id, page) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("mushaf/${surah.id}/${page + 1}.png").use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
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
    val paper = if (c.isDark) Color(0x14FFFFFF) else Color(0xF2FFFBF2)
    Box(
        Modifier
            .fillMaxSize()
            .glassSurface(shape = RoundedCornerShape(24.dp), variant = GlassVariant.ELEVATED)
            .background(paper)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { setScale(if (scale > 1f) 1f else 2.2f) })
            }
            .transformable(transform, canPan = { scale > 1f })
            .padding(6.dp),
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
