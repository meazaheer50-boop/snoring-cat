package com.dailydua.tracker.ui

import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.data.Settings
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaSheet(
    dua: Dua,
    done: Boolean,
    settings: Settings,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onReminder: () -> Unit,
    onSize: (Int) -> Unit,
    onToggleDone: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = Tokens.colors
    val sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        contentColor = c.ink,
        scrimColor = Color.Black.copy(alpha = 0.52f),
        shape = sheetShape,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(sheetShape)
                // A near-solid backing so the list behind the sheet does not show through the glass.
                .background(c.surface100.copy(alpha = 0.94f))
                .glassSurface(
                    shape = sheetShape,
                    variant = GlassVariant.ELEVATED,
                    borderWidth = 1.dp,
                )
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 18.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(c.border),
            )

            StaggeredEntrance(index = 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(dua.title, style = DuaType.displayMd.copy(fontSize = 22.sp, lineHeight = 28.sp), color = c.ink)
                        Spacer(Modifier.height(2.dp))
                        Text(dua.tag, style = DuaType.caption, color = c.brand)
                    }
                    ReminderButton(onReminder)
                    Spacer(Modifier.width(8.dp))
                    FavoriteButton(favorite, onToggleFavorite)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextSizeControl(settings.arabicSize, onSize) }
            Spacer(Modifier.height(8.dp))

            // Frosted glass verse card
            StaggeredEntrance(index = 1) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .glassSurface(
                            shape = RoundedCornerShape(22.dp),
                            variant = if (done) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
                        )
                        .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                        // Long duas scroll inside the card so the buttons stay on screen.
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                ) {
                    Text(
                        dua.arabic,
                        style = DuaType.arabic(settings.arabicSize),
                        color = c.ink,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (settings.showTransliteration && dua.transliteration.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = c.hairline)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            dua.transliteration,
                            style = DuaType.body.copy(fontStyle = FontStyle.Italic),
                            color = c.inkMuted,
                        )
                    }
                    if (settings.showTranslation && dua.translation.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(dua.translation, style = DuaType.body, color = c.ink)
                    }
                }
            }

            dua.source?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, style = DuaType.caption, color = c.inkMuted, modifier = Modifier.padding(horizontal = 4.dp))
            }
            Spacer(Modifier.height(18.dp))
            StaggeredEntrance(index = 2) {
                Column {
                    PrimaryButton(
                        text = if (done) "Mark as not done" else "Mark as done",
                        onClick = {
                            onToggleDone()
                            onDismiss()
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton("Close", onClick = onDismiss)
                }
            }
        }
    }
}

