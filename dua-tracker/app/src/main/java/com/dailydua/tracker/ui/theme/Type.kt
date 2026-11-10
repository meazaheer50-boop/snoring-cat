package com.dailydua.tracker.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.dailydua.tracker.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

/** Mirrors the design system type styles. System fonts are used until font files are added under res/font. */
/** Amiri Quran (SIL Open Font License), a Madinah-style Quranic naskh face. */
val QuranFont = FontFamily(Font(R.font.amiri_quran))

object DuaType {
    val displayLg = TextStyle(fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.8).sp)
    val displayMd = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.28).sp)
    val title = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
    val body = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
    val caption = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
    val label = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.72.sp)

    fun arabic(sizeSp: Int) = TextStyle(
        fontSize = sizeSp.sp,
        lineHeight = (sizeSp * 2.3f).sp,
        fontFamily = QuranFont,
        fontWeight = FontWeight.Normal,
        textDirection = TextDirection.Rtl,
        textAlign = TextAlign.Start,
    )
}
