package com.dailydua.tracker.data

/** A surah shown as printed mushaf pages (images under assets/mushaf/<id>/1.png ...). */
data class MushafSurah(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val meaning: String,
    val number: Int,
    val verses: Int,
    val pages: Int,
    /** The recitation in [SampleDuas] that plays this surah. */
    val audioDuaId: String?,
)

object Mushaf {
    val all = listOf(
        MushafSurah("mulk", "سُورَةُ المُلْك", "Surah Al-Mulk", "The Sovereignty", 67, 30, 4, "mulk"),
        MushafSurah("waqiah", "سُورَةُ الوَاقِعَة", "Surah Al-Waqi’ah", "The Inevitable Event", 56, 96, 6, "waqiah"),
    )
}
