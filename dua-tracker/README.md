# Daily Dua Tracker

Native Android app (Kotlin, Jetpack Compose). Look and feel follow the Daily Dua Tracker design system.

## Open and run
1. Android Studio (Ladybug or newer) > File > Open > select this folder.
2. Let Gradle sync finish (it downloads Gradle 8.9 and the libraries).
3. Pick an emulator or a phone with USB debugging, then press Run.

## Build an APK
Build > Generate App Bundles or APKs > Generate APKs (debug), or Generate Signed App Bundle / APK for a release build.

## What is in it
- Today screen: progress card, streak chip, morning and evening duas, tap to read and mark done.
- Settings: System / Light / Dark theme, reminder switches, Arabic text size, transliteration and translation switches.
- Saved on the device with DataStore (theme, settings, daily completions).
- Streak: a day counts when at least one dua is done.

## Next steps
- Replace the five sample duas in data/Dua.kt with verified content.
- Reminders are saved but do not send notifications yet.
- Add Plus Jakarta Sans and Noto Naskh Arabic font files under res/font and use them in ui/theme/Type.kt.
- Add Duas (library) and History screens.
