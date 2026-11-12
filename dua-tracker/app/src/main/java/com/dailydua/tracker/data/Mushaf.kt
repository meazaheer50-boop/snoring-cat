package com.dailydua.tracker.data

/**
 * A surah shown as printed mushaf pages. Plain edition: assets/mushaf/<id>/1.png ... ([pages], 0 if none).
 * Ornate edition: assets/mushaf/<id>/b/1.jpg ... ([pagesB], 0 if none).
 */
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
    val pagesB: Int = 0,
)

object Mushaf {
    val all = listOf(
        MushafSurah("mulk", "سُورَةُ المُلْك", "Surah Al-Mulk", "The Sovereignty", 67, 30, 4, "mulk", juz = 29, pagesB = 3,
            whenToRead = "Before sleeping",
            benefits = listOf(
                "The Prophet ﷺ said a surah of thirty verses intercedes for its reader until he is forgiven: Tabarakalladhi bi-yadihil-mulk. (Abu Dawud 1400, Tirmidhi 2891)",
                "Jabir reported that the Prophet ﷺ would not sleep until he had recited Alif Lam Mim Tanzil (as-Sajdah) and Tabarakalladhi bi-yadihil-mulk. (Tirmidhi 2892)",
                "Companions described it as the protector that guards against the punishment of the grave.",
            ),
            benefitNote = "The first two are in the collections above. The grave-protection report is graded hasan by some scholars; ask a scholar if you want the exact grading.",
        ),
        MushafSurah("waqiah", "سُورَةُ الوَاقِعَة", "Surah Al-Waqi’ah", "The Inevitable", 56, 96, 6, "waqiah", juz = 27, pagesB = 3,
            whenToRead = "After Maghrib",
            benefits = listOf(
                "Ibn Mas'ud reported the Prophet ﷺ said: whoever reads Surah al-Waqi'ah every night will never be afflicted by poverty. (Bayhaqi, Shu'ab al-Iman)",
                "Ibn Mas'ud used to tell his daughters to read it every night.",
                "Many Muslims read it in the evening, after the Maghrib prayer.",
            ),
            benefitNote = "Scholars generally grade the first report as weak (da'if), and reading it after Maghrib is a custom, not a sound hadith. Read it for the Quran's sake and ask Allah for provision in du'a.",
        ),
        MushafSurah("kahf", "سُورَةُ الكَهْف", "Surah Al-Kahf", "The Cave", 18, 110, 0, "kahf", juz = 15, pagesB = 12,
            whenToRead = "On Friday",
            benefits = listOf(
                "Abu Sa'id al-Khudri reported that the Prophet ﷺ said: whoever reads Surah al-Kahf on Friday, a light will shine for him between the two Fridays. (Bayhaqi, al-Hakim)",
                "The Prophet ﷺ said: whoever memorises the first ten verses of Surah al-Kahf is protected from the Dajjal. (Sahih Muslim 809)",
                "Reading it on Friday is a long-standing practice among Muslims.",
            ),
            benefitNote = "The Dajjal report is in Sahih Muslim. The Friday report is graded authentic by a number of scholars and weaker by others; ask a scholar if you want the exact grading.",
        ),
        MushafSurah("yasin", "سُورَةُ يس", "Surah Ya-Sin", "Ya-Sin", 36, 83, 0, "yasin", juz = 22, pagesB = 6,
            whenToRead = "Any time",
            benefits = listOf(
                "It is reported that the Prophet ﷺ said: everything has a heart, and the heart of the Quran is Ya-Sin. (Tirmidhi 2887)",
                "The surah speaks about the Quran as revelation, the signs of Allah in creation, and the Resurrection.",
                "Many Muslims read it on Fridays or in the morning, as a custom.",
            ),
            benefitNote = "Scholars grade the \"heart of the Quran\" report as weak (da'if), and most reports about special rewards for Ya-Sin are weak too. Read it for its message and ask a scholar before treating any reward as certain.",
        ),
        MushafSurah("rahman", "سُورَةُ الرَّحْمَٰن", "Surah Ar-Rahman", "The Most Merciful", 55, 78, 0, "rahman", juz = 27, pagesB = 4,
            whenToRead = "Any time",
            benefits = listOf(
                "Jabir reported that the Prophet ﷺ recited this surah to his companions and said the jinn answered it better than they had: each time he reached \"Which of the favours of your Lord would you deny?\" they said \"We deny none of Your favours, our Lord.\" (Tirmidhi 3291)",
                "The refrain \"Which of the favours of your Lord would you deny?\" returns 31 times, to remind the reader of Allah's blessings.",
            ),
            benefitNote = "The Tirmidhi report is graded hasan by a number of scholars. The title \"bride of the Quran\" comes from a weak report, so it is not used here.",
        ),
        MushafSurah("dhuha", "سُورَةُ الضُّحَىٰ", "Surah Ad-Dhuha", "The Morning Brightness", 93, 11, 0, "dhuha", juz = 30, pagesB = 1,
            whenToRead = "Any time",
            benefits = listOf(
                "It was revealed to reassure the Prophet ﷺ after a pause in revelation: your Lord has not forsaken you. (Bukhari 4950, Muslim 1797)",
                "It reminds the reader that Allah has been generous before and that the end is better than the beginning.",
                "Verse 11 asks the believer to speak of the blessings of their Lord.",
            ),
            benefitNote = "No special reward is reported for this surah. It is read for comfort in hardship.",
        ),
        MushafSurah("ikhlas", "سُورَةُ الإِخْلَاص", "Surah Al-Ikhlas", "Sincerity", 112, 4, 0, "ikhlas", juz = 30, pagesB = 1,
            whenToRead = "Morning, evening and before sleep",
            benefits = listOf(
                "The Prophet ﷺ said Qul huwa Allahu ahad is equal to a third of the Quran. (Bukhari 5013, Muslim 811)",
                "A man said he loved this surah, and the Prophet ﷺ said his love for it would enter him into Paradise. (Bukhari 774, Tirmidhi 2901)",
                "The Prophet ﷺ said that reading it with al-Falaq and an-Nas three times in the morning and evening is enough for you against everything. (Abu Dawud 5082, Tirmidhi 3575)",
            ),
            benefitNote = "The first two are in the collections above. The morning and evening report is graded hasan by a number of scholars.",
        ),
        MushafSurah("nas", "سُورَةُ النَّاس", "Surah An-Nas", "Mankind", 114, 6, 0, "nas", juz = 30, pagesB = 1,
            whenToRead = "Morning, evening and before sleep",
            benefits = listOf(
                "The Prophet ﷺ said that no one has sought refuge with anything like al-Falaq and an-Nas. (Sahih Muslim 814)",
                "Every night before sleeping, he would blow into his cupped hands, read al-Ikhlas, al-Falaq and an-Nas, and wipe over his body. (Bukhari 5017)",
                "He told a companion to read them three times each morning and evening. (Abu Dawud 5082, Tirmidhi 3575)",
            ),
            benefitNote = "The first two are in the collections above. The third is graded hasan by a number of scholars.",
        ),
    )
}
