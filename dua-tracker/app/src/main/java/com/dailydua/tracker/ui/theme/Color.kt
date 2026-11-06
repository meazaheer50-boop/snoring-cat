package com.dailydua.tracker.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Mirrors the Daily Dua Tracker design system color tokens. */
@Immutable
data class DuaColors(
    val surface100: Color,
    val surface200: Color,
    val surfaceCard: Color,
    val ink: Color,
    val inkMuted: Color,
    val brand: Color,
    val onBrand: Color,
    val brandSoft: Color,
    val gold: Color,
    val onGold: Color,
    val goldInk: Color,
    val border: Color,
    val hairline: Color,
)

val LightDuaColors = DuaColors(
    surface100 = Color(0xFFF4F7F5),
    surface200 = Color(0xFFE6EDEA),
    surfaceCard = Color(0xFFFFFFFF),
    ink = Color(0xFF0E1C17),
    inkMuted = Color(0xFF4F5F58),
    brand = Color(0xFF0A6B57),
    onBrand = Color(0xFFFFFFFF),
    brandSoft = Color(0xFFD5EFE6),
    gold = Color(0xFFF0B429),
    onGold = Color(0xFF0E1C17),
    goldInk = Color(0xFF7A5200),
    border = Color(0xFF7B8C84),
    hairline = Color(0xFFD3DDD8),
)

val DarkDuaColors = DuaColors(
    surface100 = Color(0xFF0B1411),
    surface200 = Color(0xFF121E1A),
    surfaceCard = Color(0xFF172622),
    ink = Color(0xFFEEF4F1),
    inkMuted = Color(0xFFA3B3AB),
    brand = Color(0xFF4FD1B0),
    onBrand = Color(0xFF06201A),
    brandSoft = Color(0xFF17372F),
    gold = Color(0xFFF5C04A),
    onGold = Color(0xFF1A1300),
    goldInk = Color(0xFFF5C04A),
    border = Color(0xFF6F857B),
    hairline = Color(0xFF25362F),
)
