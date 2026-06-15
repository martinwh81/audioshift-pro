# AudioShift Pro

Audio converter with pitch shift, trim, BPM detection and metadata editor.

## Features
- Convert MP3, WAV, FLAC, AAC, OGG, WebM
- Pitch shift (STFT phase vocoder) — speed unchanged
- BPM detection with metronome
- Trim with fade in/out
- Metadata editor with batch support
- Parallel processing with Web Workers
- Volume normalize

## Build APK (GitHub Actions — no PC needed)

1. Push this repository to GitHub
2. Go to **Actions** tab
3. Click **Build AudioShift Pro APK**
4. Click **Run workflow** → **Run workflow**
5. Wait ~10 minutes
6. Download APK from **Artifacts** section

## Manual build (PC required)

```bash
npm install
npx cap add android
npx cap sync android
npx cap open android
# Then Build → Build APK in Android Studio
```

## Update the app

1. Replace `www/index.html` with new version
2. Push to GitHub
3. New APK builds automatically

## Tech stack
- Pure HTML/JS/CSS — no framework
- Capacitor 6 for Android wrapper
- lamejs for MP3 encoding
- WebCodecs API for AAC/OGG/WebM
- STFT phase vocoder for pitch shift
- Web Workers for parallel processing
