# Adding a surah to Daily Dua

Every surah in the app follows the same standard so that it renders cleanly in the
Quran reader (Text and Pages modes) and highlights in time with its recitation.

## 1. Text source
* Arabic: Tanzil **Uthmani** text (the `quran-json` npm package ships it).
* English: Saheeh International (same package).
* Never retype or copy Arabic from a web page — that mixes encodings and breaks words.

## 2. Data file — `app/src/main/java/com/dailydua/tracker/data/<Name>Data.kt`
One `DuaLine` per spoken unit, in recitation order:

```kotlin
DuaLine(400L,   "أَعُوذُ بِٱللَّهِ مِنَ ٱلشَّيۡطَٰنِ ٱلرَّجِيمِ", "I seek refuge in Allah from Satan, the accursed."),
DuaLine(7300L,  "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ", "In the name of Allah, the Most Gracious, the Most Merciful."),
DuaLine(13200L, "<verse 1 Arabic>", "1. <verse 1 English>"),
DuaLine(24599L, "<verse 2 Arabic>", "2. <verse 2 English>"),
```

* The first number is when the line **starts** in the audio, in milliseconds.
* Verse lines' English **must** begin with `N. ` (the verse number, a dot, a space).
  The reader uses it to place the gold verse-number rosette. Lines without it
  (isti'adha, bismillah, closing) are shown without a number.

## 3. Audio
`app/src/main/assets/audio/<name>.mp3` (or `.m4a`), referenced by the surah's `Dua`
entry (`audioAsset`). Timings are found from the audio's quiet gaps between verses.

## 4. Register it
* `Dua.kt` — the `Dua` entry (id, title, `lines = <Name>Lines`, `audioAsset`).
* `Mushaf.kt` — a `MushafSurah` with number, juz, Arabic/English name, meaning, and
  optional `whenToRead` and `benefits`.

## 5. Normalize and check (required — CI fails otherwise)
```
python3 tools/quran_text.py normalize
python3 tools/quran_text.py check
```
`normalize` converts Tanzil's open-tanween marks to the code points the Amiri Quran
font draws (U+0657→U+08F0, U+065E→U+08F1, U+0656→U+08F2). `check` fails if any Arabic
character is missing from the Quran font, which is what makes letters break apart.
