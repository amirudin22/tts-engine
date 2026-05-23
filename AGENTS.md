# TTS Engine — React + Capacitor + Python Backend

## Overview

**Architecture**: React (Vite) frontend → Python (FastAPI) backend → TTS via edge-tts.
Wrapped as Android app with Capacitor.

**Pipeline**: naskah → chunk → edge-tts TTS → merge audio

## Backend (`backend/`)

```
backend/
├── requirements.txt
├── .env.example
├── app/
│   ├── main.py              # FastAPI app + CORS
│   ├── config.py            # HOST/PORT settings
│   ├── schemas.py           # TTSRequest, TTSResponse
│   ├── api/
│   │   └── routes.py        # POST /api/tts, GET /api/voices, GET /api/download/{file}
│   ├── tts/
│   │   └── engine.py        # edge-tts wrapper (synthesize, synthesize_batch)
│   └── pipeline/
│       ├── chunker.py       # Split text per ~2800 chars (sentence-aware)
│       └── merger.py        # MP3 concatenation merge
```

## API Endpoints

- `GET  /api/voices`      — Daftar suara tersedia
- `POST /api/tts`          — Generate audio dari teks
  - Body: `{ text, voice, rate, pitch }`
  - Return: `{ filename, duration_seconds, chunks }`
- `GET  /api/download/{file}` — Download file audio
- `GET  /health`           — Health check

## TTS Engine

Python `edge-tts` — Microsoft Edge neural TTS, multibahasa (ID, JV, SU), via HTTP.
Rate: -50% to +50%, pitch: -50Hz to +50Hz.

## Edge TTS voices

| Voice | Gender |
|---|---|
| `id-ID-ArdiNeural` | Male (default) |
| `id-ID-GadisNeural` | Female |
| `jv-ID-DimasNeural` | Javanese Male |
| `jv-ID-SitiNeural` | Javanese Female |
| `su-ID-JajangNeural` | Sundanese Male |
| `su-ID-TutiNeural` | Sundanese Female |

## Run Backend

```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## Setup (Frontend — coming)

```bash
cd frontend
npm install
npm run dev
```
