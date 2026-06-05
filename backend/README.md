---
title: TTS Engine API
emoji: 🎙️
colorFrom: indigo
colorTo: purple
sdk: docker
app_port: 7860
pinned: false
---

# TTS Engine API

FastAPI backend for Text-to-Speech using **edge-tts** (Microsoft Edge neural TTS).

## API Endpoints

- `GET /health` — Health check
- `GET /api/voices` — List available voices (Indonesian, Javanese, Sundanese)
- `POST /api/tts` — Generate speech from text
- `GET /api/download/{filename}` — Download generated audio

## Voices

| Voice ID | Label |
|---|---|
| id-ID-ArdiNeural | Indonesian (Male) |
| id-ID-GadisNeural | Indonesian (Female) |
| jv-ID-DimasNeural | Javanese (Male) |
| jv-ID-SitiNeural | Javanese (Female) |
| su-ID-JajangNeural | Sundanese (Male) |
| su-ID-TutiNeural | Sundanese (Female) |

## Usage

```bash
curl -X POST https://{your-space}.hf.space/api/tts \
  -H "Content-Type: application/json" \
  -d '{"text": "Halo dunia!", "voice": "id-ID-ArdiNeural"}'
```
