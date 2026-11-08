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
    val juz: Int,
    /** When it is customarily read, e.g. "After Maghrib". */
    val whenToRead: String,
    val benefits: List<String>,
    val benefitNote: String,
)

object Mushaf {
    val all = listOf(
        MushafSurah("mulk", "سُورَةُ المُلْك", "Surah Al-Mulk", "The Sovereignty", 67, 30, 4, "mulk", juz = 29,
            whenToRead = "Before sleeping",
            benefits = listOf(
                "The Prophet ﷺ said a surah of thirty verses intercedes for its reader until he is forgiven: Tabarakalladhi bi-yadihil-mulk. (Abu Dawud 1400, Tirmidhi 2891)",
                "Jabir reported that the Prophet ﷺ would not sleep until he had recited Alif Lam Mim Tanzil (as-Sajdah) and Tabarakalladhi bi-yadihil-mulk. (Tirmidhi 2892)",
                "Companions described it as the protector that guards against the punishment of the grave.",
            ),
            benefitNote = "The first two are in the collections above. The grave-protection report is graded hasan by some scholars; ask a scholar if you want the exact grading.",
        ),
        MushafSurah("waqiah", "سُورَةُ الوَاقِعَة", "Surah Al-Waqi’ah", "The Inevitable", 56, 96, 6, "waqiah", juz = 27,
            whenToRead = "After Maghrib",
            benefits = listOf(
                "Ibn Mas'ud reported the Prophet ﷺ said: whoever reads Surah al-Waqi'ah every night will never be afflicted by poverty. (Bayhaqi, Shu'ab al-Iman)",
                "Ibn Mas'ud used to tell his daughters to read it every night.",
                "Many Muslims read it in the evening, after the Maghrib prayer.",
            ),
            benefitNote = "Scholars generally grade the first report as weak (da'if), and reading it after Maghrib is a custom, not a sound hadith. Read it for the Quran's sake and ask Allah for provision in du'a.",
        ),
    )
}
