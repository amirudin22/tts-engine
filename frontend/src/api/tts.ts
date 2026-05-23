import type { Voice, TTSRequest, TTSResponse } from "../types"

const BASE = import.meta.env.VITE_API_URL || "http://localhost:8000"

export async function fetchVoices(): Promise<Voice[]> {
  const res = await fetch(`${BASE}/api/voices`)
  const data = await res.json()
  return data.voices
}

export async function generateSpeech(req: TTSRequest): Promise<TTSResponse> {
  const res = await fetch(`${BASE}/api/tts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    const err = await res.text()
    throw new Error(err || "Gagal generate audio")
  }
  return res.json()
}

export function getAudioUrl(filename: string): string {
  return `${BASE}/api/download/${filename}`
}
