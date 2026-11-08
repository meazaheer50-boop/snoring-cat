package com.dailydua.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
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
    onToggleDone: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = Tokens.colors
    val sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        contentColor = c.ink,
        scrimColor = Color.Black.copy(alpha = 0.52f),
        shape = sheetShape,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .glassSurface(
                    shape = sheetShape,
                    variant = GlassVariant.ELEVATED,
                    borderWidth = 1.dp,
                )
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 18.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(c.border),
            )

            SectionLabel(dua.title, badge = dua.tag)
            Spacer(Modifier.height(14.dp))

            // Frosted glass verse card
            Column(
                Modifier
                    .fillMaxWidth()
                    .glassSurface(
                        shape = RoundedCornerShape(22.dp),
                        variant = if (done) GlassVariant.SOFT_EMERALD else GlassVariant.FROST,
                    )
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

            Spacer(Modifier.height(22.dp))
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

