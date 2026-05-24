# Catatan API Backend TTS Engine

## Stack

Python 3.12 + FastAPI + Uvicorn + edge-tts (Microsoft Edge Neural TTS)

## Instalasi

```bash
pip install fastapi uvicorn[standard] edge-tts python-multipart python-dotenv
```

## Menjalankan Server

```bash
cd backend
uvicorn app.main:app --host 0.0.0.0 --port 8000
# → http://localhost:8000
```

## Endpoint

### Health Check

```bash
GET /health
```

Response: `{"status":"ok"}`

### Daftar Suara

```bash
GET /api/voices
```

Response:
```json
{
  "voices": [
    {"id": "id-ID-ArdiNeural", "label": "Indonesian (Male)"},
    {"id": "id-ID-GadisNeural", "label": "Indonesian (Female)"},
    {"id": "jv-ID-DimasNeural", "label": "Javanese (Male)"},
    {"id": "jv-ID-SitiNeural", "label": "Javanese (Female)"},
    {"id": "su-ID-JajangNeural", "label": "Sundanese (Male)"},
    {"id": "su-ID-TutiNeural", "label": "Sundanese (Female)"}
  ]
}
```

### Generate Audio

```bash
POST /api/tts
Content-Type: application/json

{
  "text": "Halo selamat datang di TTS Engine.",
  "voice": "id-ID-ArdiNeural",
  "rate": 0,
  "pitch": 0
}
```

**Parameter:**
| Field | Type | Default | Deskripsi |
|---|---|---|---|
| `text` | string | — | Teks naskah (wajib) |
| `voice` | string | `id-ID-ArdiNeural` | ID suara dari `/api/voices` |
| `rate` | number | `0` | Kecepatan (-50 s/d +50, dalam %) |
| `pitch` | number | `0` | Pitch (-50Hz s/d +50Hz) |

Response:
```json
{
  "filename": "tts_1712312312.mp3",
  "duration_seconds": 3.2,
  "chunks": 1
}
```

### Download File Audio

```bash
GET /api/download/{filename}
```

Response: file MP3 (audio/mpeg).

## Contoh CURL Lengkap

```bash
# 1. Cek server
curl https://backend-url.com/health

# 2. Lihat suara
curl https://backend-url.com/api/voices

# 3. Generate + download
FILENAME=$(curl -s -X POST https://backend-url.com/api/tts \
  -H "Content-Type: application/json" \
  -d '{"text":"Halo dunia","voice":"id-ID-GadisNeural","rate":0,"pitch":0}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['filename'])")

curl -o output.mp3 https://backend-url.com/api/download/$FILENAME
```

## Integrasi JavaScript/TypeScript

```typescript
const BASE = "https://backend-url.com"

async function getVoices() {
  const res = await fetch(`${BASE}/api/voices`)
  const data = await res.json()
  return data.voices
}

async function generateAudio(text: string, voice = "id-ID-ArdiNeural") {
  const res = await fetch(`${BASE}/api/tts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text, voice, rate: 0, pitch: 0 }),
  })
  const data = await res.json()
  return {
    url: `${BASE}/api/download/${data.filename}`,
    duration: data.duration_seconds,
  }
}
```

## Integrasi Python

```python
import httpx  # atau requests

BASE = "https://backend-url.com"

def get_voices():
    res = httpx.get(f"{BASE}/api/voices")
    return res.json()["voices"]

def generate_audio(text: str, voice="id-ID-ArdiNeural"):
    res = httpx.post(f"{BASE}/api/tts", json={
        "text": text,
        "voice": voice,
        "rate": 0,
        "pitch": 0,
    })
    data = res.json()
    # Download file
    audio = httpx.get(f"{BASE}/api/download/{data['filename']}")
    with open("output.mp3", "wb") as f:
        f.write(audio.content)
    return data
```

## Integrasi PHP

```php
$base = "https://backend-url.com";

$res = file_get_contents("$base/api/voices");
$voices = json_decode($res, true)['voices'];

$payload = json_encode([
    'text' => 'Halo dunia',
    'voice' => 'id-ID-GadisNeural',
    'rate' => 0,
    'pitch' => 0,
]);
$opts = [
    'http' => [
        'method' => 'POST',
        'header' => 'Content-Type: application/json',
        'content' => $payload,
    ]
];
$res = file_get_contents("$base/api/tts", false, stream_context_create($opts));
$data = json_decode($res, true);

file_put_contents('output.mp3', file_get_contents("$base/api/download/{$data['filename']}"));
```

## Integrasi Dart/Flutter

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

final base = Uri.parse('https://backend-url.com');

Future<List<Map>> getVoices() async {
  final res = await http.get(Uri.parse('$base/api/voices'));
  return jsonDecode(res.body)['voices'];
}

Future<void> generateAudio(String text) async {
  final res = await http.post(
    Uri.parse('$base/api/tts'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({
      'text': text,
      'voice': 'id-ID-ArdiNeural',
      'rate': 0,
      'pitch': 0,
    }),
  );
  final data = jsonDecode(res.body);
  final audioRes = await http.get(Uri.parse('$base/api/download/${data['filename']}'));
  // Simpan audioRes.bodyBytes ke file
}
```

## Deploy ke Railway

1. Push repo ke GitHub
2. https://railway.app → New Project → Deploy from GitHub
3. Root Directory: `backend`
4. Auto-deploy tiap push

## Struktur Folder

```
backend/
├── requirements.txt
├── .env.example
├── Procfile               # Start command untuk Railway
├── app/
│   ├── main.py            # FastAPI app + CORS
│   ├── config.py          # HOST/PORT
│   ├── schemas.py         # Pydantic models
│   ├── api/routes.py      # Endpoints
│   ├── tts/engine.py      # edge-tts wrapper
│   └── pipeline/
│       ├── chunker.py     # Sentence-aware chunking
│       └── merger.py      # MP3 concatenation
```

## Catatan

- Rate range: -50% to +50% (float). Negative = lambat, positive = cepat.
- Pitch range: -50Hz to +50Hz (float). Negative = lebih berat, positive = lebih ringan.
- File output di `output/` akan di-cleanup otomatis setiap ada request baru (TTL 1 jam, max 50 file).
- Wajib koneksi internet — edge-tts call Microsoft Edge TTS server via WebSocket.
