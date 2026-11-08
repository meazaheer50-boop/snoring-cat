package com.dailydua.tracker.data

enum class TimeOfDay { MORNING, EVENING, ANYTIME }

/** One recited line, shown from [startMs] in the audio. */
data class DuaLine(val startMs: Long, val arabic: String, val translation: String = "")

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
            id = "waking", title = "On waking", tag = "Morning", time = TimeOfDay.MORNING,
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-ladhi ahyana ba’da ma amatana wa ilayhin-nushur",
            translation = "All praise is for Allah who gave us life after causing us to die, and to Him is the return.",
        ),
        Dua(
            id = "leaving", title = "Leaving home", tag = "Morning", time = TimeOfDay.MORNING,
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ",
            transliteration = "Bismillah, tawakkaltu ’alallah",
            translation = "In the name of Allah, I place my trust in Allah.",
        ),
        Dua(
            id = "before-eating", title = "Before eating", tag = "Anytime", time = TimeOfDay.MORNING,
            arabic = "بِسْمِ اللَّهِ",
            transliteration = "Bismillah",
            translation = "In the name of Allah.",
        ),
        Dua(
            id = "after-eating", title = "After eating", tag = "Anytime", time = TimeOfDay.EVENING,
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translation = "All praise is for Allah.",
        ),
        Dua(
            id = "sleep", title = "Before sleep", tag = "Evening", time = TimeOfDay.EVENING,
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya",
            translation = "In Your name, O Allah, I die and I live.",
        ),
        Dua(
            id = "qunoot", title = "Dua Qunoot", tag = "Sheikh Mishary Al-Afasy · 11 min", time = TimeOfDay.ANYTIME,
            lines = QunootData.lines,
            audioAsset = "qunoot.mp3",
            source = "Text transcribed from a video of the dua. Not yet verified.",
            category = "Recitations",
        ),
        Dua(
            id = "khatm", title = "Dua Khatm al-Quran", tag = "Sheikh Mishary Al-Afasy · 17 min", time = TimeOfDay.ANYTIME,
            lines = KhatmData.lines,
            audioAsset = "khatm.mp3",
            category = "Recitations",
            source = "Text from YouTube's automatic captions, without vowel marks; the English is an unverified draft translation. Not yet verified.",
        ),
        Dua(
            id = "mulk", title = "Surah Al-Mulk", tag = "Quran 67 \u00b7 9 min", time = TimeOfDay.ANYTIME,
            lines = MulkData.lines,
            audioAsset = "mulk.mp3",
            category = "Recitations",
            source = "Arabic: Tanzil (Uthmani). English: Saheeh International. Verse start times were matched to the recording.",
        ),
    ) + MoreDuas.all
}
