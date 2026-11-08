package com.dailydua.tracker.data

enum class TimeOfDay { MORNING, EVENING, ANYTIME }

/** One recited line, shown from [startMs] in the audio. */
data class DuaLine(
    val startMs: Long,
    val arabic: String,
    val translation: String = "",
    /** Step name shown above the line, for guides such as the Janazah prayer. */
    val heading: String? = null,
)

data class Dua(
    val id: String,
    val title: String,
    val tag: String,
    val time: TimeOfDay,
    val arabic: String = "",
    val transliteration: String = "",
    val translation: String = "",
    /** Long duas shown line by line, in sync with the audio. */
    val lines: List<DuaLine> = emptyList(),
    /** File name under assets/audio, if a recording is bundled. */
    val audioAsset: String? = null,
    val source: String? = null,
    /** Heading used to group longer or topical duas on the Today screen. */
    val category: String? = null,
)

/** Sample content only. Review and replace with verified duas before release. */
object SampleDuas {
    val all = listOf(
        Dua(
            id = "waking", title = "Upon waking up", tag = "Alhamdu lillahil-ladhi ahyana", time = TimeOfDay.MORNING,
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-ladhi ahyana ba’da ma amatana wa ilayhin-nushur",
            translation = "All praise is for Allah who gave us life after causing us to die, and to Him is the return.",
        ),
        Dua(
            id = "leaving", title = "Leaving the house", tag = "Bismillah, tawakkaltu ʿala-llah", time = TimeOfDay.MORNING,
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ",
            transliteration = "Bismillah, tawakkaltu ’alallah",
            translation = "In the name of Allah, I place my trust in Allah.",
        ),
        Dua(
            id = "before-eating", title = "Before eating", tag = "Bismillah", time = TimeOfDay.MORNING,
            arabic = "بِسْمِ اللَّهِ",
            transliteration = "Bismillah",
            translation = "In the name of Allah.",
        ),
        Dua(
            id = "after-eating", title = "After eating", tag = "Alhamdulillah", time = TimeOfDay.EVENING,
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translation = "All praise is for Allah.",
        ),
        Dua(
            id = "sleep", title = "Before sleeping", tag = "Bismika-llahumma amutu wa ahya", time = TimeOfDay.EVENING,
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya",
            translation = "In Your name, O Allah, I die and I live.",
        ),
        Dua(
            id = "qunoot", title = "Dua al-Qunoot", tag = "Mishary Al-Afasy · 11 min", time = TimeOfDay.ANYTIME,
            lines = QunootData.lines,
            audioAsset = "qunoot.mp3",
            source = "Text transcribed from a video of the dua. Not yet verified.",
            category = "Listen and follow along",
        ),
        Dua(
            id = "khatm", title = "Dua on completing the Quran", tag = "Mishary Al-Afasy · 17 min", time = TimeOfDay.ANYTIME,
            lines = KhatmData.lines,
            audioAsset = "khatm.mp3",
            category = "Listen and follow along",
            source = "Text from YouTube's automatic captions, without vowel marks; the English is an unverified draft translation. Not yet verified.",
        ),
        Dua(
            id = "waqiah", title = "Surah Al-Waqi\u2019ah", tag = "Mishary Al-Afasy · 14 min · 96 verses", time = TimeOfDay.ANYTIME,
            lines = WaqiahData.lines,
            audioAsset = "waqiah.mp3",
            category = "Listen and follow along",
            source = "Arabic: Tanzil (Uthmani). English: Saheeh International. Verse start times were matched to the recording.",
        ),
        Dua(
            id = "mulk", title = "Surah Al-Mulk", tag = "Recitation · 9 min · 30 verses", time = TimeOfDay.ANYTIME,
            lines = MulkData.lines,
            audioAsset = "mulk.mp3",
            category = "Listen and follow along",
            source = "Arabic: Tanzil (Uthmani). English: Saheeh International. Verse start times were matched to the recording.",
        ),
        Dua(
            id = "names99", title = "The 99 Names of Allah", tag = "Arabic and English · 99 names", time = TimeOfDay.ANYTIME,
            lines = NamesData.lines,
            audioAsset = "names99.m4a",
            category = "Listen and follow along",
            source = "Names and meanings follow the common list; some lists differ slightly in order and wording. Timing was matched to the recording by its rhythm.",
        ),
        Dua(
            id = "janazah", title = "Janazah prayer guide", tag = "4 takbirs · what to recite", time = TimeOfDay.ANYTIME,
            lines = JanazahData.lines,
            category = "Janazah prayer",
            source = "The 3rd takbir duas follow the page you sent. Al-Fatiha from Tanzil. The other texts are written from memory (Bukhari 3370, Muslim 963). Scholars differ on some details; follow your local imam.",
        ),
    ) + MoreDuas.all
}
