import type { Voice, TTSRequest, TTSResponse } from "../types"

const STORAGE_KEY = "tts_engine_backend_url"
const DEFAULT_URL = "http://localhost:8000"

let _base = localStorage.getItem(STORAGE_KEY) || import.meta.env.VITE_API_URL || DEFAULT_URL

export function getBaseUrl(): string {
  return _base
}

export function setBaseUrl(url: string): void {
  _base = url.replace(/\/+$/, "")
  localStorage.setItem(STORAGE_KEY, _base)
}

async function api(path: string, init?: RequestInit) {
  const res = await fetch(`${_base}${path}`, init)
  if (!res.ok) {
    const msg = await res.text().catch(() => "Unknown error")
    throw new Error(msg || `HTTP ${res.status}`)
  }
  return res.json()
}

export async function fetchVoices(): Promise<Voice[]> {
  const data = await api("/api/voices")
  return data.voices
}

export async function generateSpeech(req: TTSRequest): Promise<TTSResponse> {
  return api("/api/tts", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  })
}

export function getAudioUrl(filename: string): string {
  return `${_base}/api/download/${filename}`
}

export const getSrtUrl = getAudioUrl
