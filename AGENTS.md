# TTS Engine — Android App

## Overview

Kotlin + Jetpack Compose Android app for Text-to-Speech with AI-powered narrative rewriting.

**Pipeline**: naskah → AI (DeepSeek API) → parse → rewrite narasi → chunk → TTS → merge audio

## Architecture

```
app/src/main/java/com/ttsengine/
├── MainActivity.kt          # Entry point + permission handling
├── model/
│   ├── Models.kt            # Segment, Story, PipelineState, TTSConfig
│   └── Voices.kt            # Voice list & VoiceInfo
├── ai/
│   ├── DeepSeekClient.kt    # HTTP client for DeepSeek API (OkHttp)
│   ├── NaskahParser.kt      # Parse naskah → segments (narasi/dialog)
│   └── NarasiRewriter.kt    # Rewrite narasi via AI
├── tts/
│   ├── TTSManager.kt        # Android TextToSpeech wrapper (synthesizeToFile)
│   └── AudioMerger.kt       # Merge WAV files (PCM concatenation)
├── pipeline/
│   ├── Chunker.kt           # Split text per ~2800 chars
│   └── TTSPipeline.kt       # Orchestrator
└── ui/
    ├── MainScreen.kt        # Compose UI
    └── theme/Theme.kt       # Material3 theme
```

## TTS Engine

Android `TextToSpeech` API — neural voices, multibahasa (ID, JV, SU), gratis, no pip.

No ffmpeg/pydub. Audio merge via raw WAV concatenation.

## AI Module

Opsional — via DeepSeek API. Parse naskah jadi segmen (narasi/dialog/subjek/emosi) + rewrite narasi agar natural dibacakan.

## Setup

```bash
# Build APK
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/ttsengine-debug.apk
```

## GitHub Actions

Push ke main → build otomatis via `.github/workflows/android.yml`.
Download APK: Actions → Build Android APK → artifacts.

## Edge TTS voices (display only — not used in app)

| Voice | Gender |
|---|---|
| `id-ID-ArdiNeural` | Male (default) |
| `id-ID-GadisNeural` | Female |
| `jv-ID-DimasNeural` | Javanese Male |
| `jv-ID-SitiNeural` | Javanese Female |
| `su-ID-JajangNeural` | Sundanese Male |
| `su-ID-TutiNeural` | Sundanese Female |

## Voice parameters (app)

- `rate`: Kecepatan bicara, slider -50% to +50%
- `pitch`: Pitch suara, slider -50% to +50%
- Voice selector: dropdown daftar suara

## Dependencies

- Jetpack Compose + Material3 (UI)
- OkHttp (DeepSeek API)
- Kotlin Coroutines (async)
- AndroidX (core, lifecycle, activity)
