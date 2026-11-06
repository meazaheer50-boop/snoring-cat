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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceCard,
        contentColor = c.ink,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 16.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(c.border),
            )
        },
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            SectionLabel(dua.title)
            Spacer(Modifier.height(8.dp))
            Text(
                dua.arabic,
                style = DuaType.arabic(settings.arabicSize),
                color = c.ink,
                modifier = Modifier.fillMaxWidth(),
            )
            if (settings.showTransliteration) {
                Spacer(Modifier.height(8.dp))
                Text(
                    dua.transliteration,
                    style = DuaType.body.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = c.inkMuted,
                )
            }
            if (settings.showTranslation) {
                Spacer(Modifier.height(8.dp))
                Text(dua.translation, style = DuaType.body, color = c.ink)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton(if (done) "Mark as not done" else "Mark as done", onClick = {
                onToggleDone()
                onDismiss()
            })
            Spacer(Modifier.height(12.dp))
            OutlinedButton("Close", onClick = onDismiss)
        }
    }
}
