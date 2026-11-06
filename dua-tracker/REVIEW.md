# Review before release

The text of Dua Qunoot (data/QunootData.kt) was transcribed from the Ommah TV video "Du'a Sheikh Mishary Al Afasy ARABIC TEXT", and the timings follow that video's audio. The video's own subtitles are informal, so the Arabic needs checking by someone who knows the dua before you share the app.

Known problems in the source subtitles (kept as shown, not corrected):
- Line starting `وَجَعل هل وارِث مِنّا` looks garbled.
- `وَأَذِلَّالشِّرْكَ` has two words run together.
- Several lines have missing or inconsistent vowel marks (for example `انت`, `انزل`).
- The English and Arabic are out of step in a few places (around "no need of ours", and "not with punishment" after the rain lines).
- A line at about 8:44 is cut off by the frame edge in places; check `وَمَاتُوا عَلى ذَلِك` and nearby lines.

The five short duas in data/Dua.kt are sample content and also need checking.
