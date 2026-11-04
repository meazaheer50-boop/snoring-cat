# Snoring Cat

A harmless prank app. Open it, tap the button, and a cat curls up and falls asleep, snoring.

- One button, one sleeping cat, a looping snore (synthesized in the browser, no audio files).
- Sound starts on the tap (browsers only allow audio after a tap). There is a mute button.
- Single-file web app, installable to the phone home screen.

## Run locally

```
python3 -m http.server 8000
```

Then open http://localhost:8000.

## Deploy

Every push to `main` deploys to GitHub Pages through `.github/workflows/pages.yml`.

## Android APK

`.github/workflows/android.yml` wraps the web app with Capacitor (`mobile/`) and builds a debug APK on GitHub.
It runs on every change to the app, or manually from the Actions tab (**Build Android APK → Run workflow**).
Download `snoring-cat-apk` from the finished run, unzip it, and install `app-debug.apk` on the phone.
