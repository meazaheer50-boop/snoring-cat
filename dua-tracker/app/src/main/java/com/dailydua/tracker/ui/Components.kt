package com.dailydua.tracker.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailydua.tracker.data.Dua
import com.dailydua.tracker.ui.theme.DuaType
import com.dailydua.tracker.ui.theme.Tokens

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = DuaType.label, color = Tokens.colors.inkMuted, modifier = modifier)
}

@Composable
fun CheckCircle(done: Boolean) {
    val c = Tokens.colors
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .then(if (done) Modifier.background(c.brand) else Modifier.border(2.dp, c.border, CircleShape)),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(Icons.Rounded.Check, contentDescription = null, tint = c.onBrand, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun DuaRow(dua: Dua, done: Boolean, onClick: () -> Unit) {
    val c = Tokens.colors
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (done) c.brandSoft else c.surfaceCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CheckCircle(done)
        Column(Modifier.weight(1f)) {
            Text(dua.title, style = DuaType.title, color = c.ink)
            Text(dua.tag + if (done) " · Done" else "", style = DuaType.caption, color = c.inkMuted)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Tokens.colors
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(CircleShape)
            .background(c.brand)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = DuaType.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = c.onBrand)
    }
}

@Composable
fun OutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Tokens.colors
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(CircleShape)
            .border(2.dp, c.border, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = DuaType.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = c.brand)
    }
}

/** Pill-shaped segmented selector. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val c = Tokens.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(c.surface200)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(if (on) c.brand else Color.Transparent)
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = DuaType.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = if (on) c.onBrand else c.ink,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Visual only. The row that contains it carries the toggle semantics. */
@Composable
fun SwitchVisual(checked: Boolean) {
    val c = Tokens.colors
    val x by animateDpAsState(if (checked) 26.dp else 6.dp, label = "thumb")
    Box(
        Modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(CircleShape)
            .background(if (checked) c.brand else Color.Transparent)
            .border(2.dp, if (checked) c.brand else c.border, CircleShape),
    ) {
        Box(
            Modifier
                .offset(x = x, y = 6.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (checked) c.onBrand else c.border),
        )
    }
}
