import { useState, useEffect, useCallback } from "react"
import { fetchVoices, generateSpeech, getAudioUrl } from "./api/tts"
import { TextInput } from "./components/TextInput"
import { VoiceSelector } from "./components/VoiceSelector"
import { RateSlider } from "./components/RateSlider"
import { AudioPlayer } from "./components/AudioPlayer"
import { SettingsDialog } from "./components/SettingsDialog"
import type { Voice, Status } from "./types"

interface Result {
  id: number
  audioUrl: string
  filename: string
  srtUrl: string
  label: string
}

export default function App() {
  const [voices, setVoices] = useState<Voice[]>([])
  const [text, setText] = useState("")
  const [voice, setVoice] = useState("id-ID-ArdiNeural")
  const [rate, setRate] = useState(0)
  const [pitch, setPitch] = useState(0)
  const [status, setStatus] = useState<Status>("idle")
  const [error, setError] = useState("")
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [results, setResults] = useState<Result[]>([])
  const [nextId, setNextId] = useState(1)

  const loadVoices = useCallback(async () => {
    try {
      const v = await fetchVoices()
      setVoices(v)
      if (v.length > 0) setVoice(v[0].id)
    } catch {
      setVoices([])
    }
  }, [])

  useEffect(() => { loadVoices() }, [loadVoices])

  async function handleGenerate() {
    if (!text.trim()) return
    setStatus("loading")
    setError("")
    const label = text.trim().slice(0, 60) + (text.trim().length > 60 ? "..." : "")
    try {
      const res = await generateSpeech({ text, voice, rate, pitch, subtitle: true })
      const result: Result = {
        id: nextId,
        audioUrl: getAudioUrl(res.filename),
        filename: res.filename,
        srtUrl: res.srt_filename ? getAudioUrl(res.srt_filename) : "",
        label,
      }
      setResults((prev) => [result, ...prev])
      setNextId((n) => n + 1)
      setText("")
      setStatus("idle")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Terjadi kesalahan")
      setStatus("idle")
    }
  }

  function removeResult(id: number) {
    setResults((prev) => prev.filter((r) => r.id !== id))
  }

  return (
    <main>
      <div className="header">
        <h1>TTS Engine</h1>
        <button className="icon-btn" onClick={() => setSettingsOpen(true)} title="Pengaturan">
          ⚙
        </button>
      </div>
      <p>Ubah naskah menjadi suara alami</p>

      <TextInput value={text} onChange={setText} disabled={status === "loading"} />
      <VoiceSelector
        voices={voices}
        value={voice}
        onChange={setVoice}
        disabled={status === "loading"}
      />
      <RateSlider label="Kecepatan" value={rate} onChange={setRate} disabled={status === "loading"} />
      <RateSlider label="Pitch" value={pitch} onChange={setPitch} disabled={status === "loading"} />

      <button onClick={handleGenerate} disabled={status === "loading" || !text.trim()}>
        {status === "loading" ? "Memproses..." : "Hasilkan Audio"}
      </button>

      {status === "loading" && <p>Sedang memproses naskah...</p>}

      {error && <p>{error}</p>}

      {results.length > 0 && (
        <section>
          <h2>Hasil ({results.length})</h2>
          {results.map((r) => (
            <div key={r.id} className="result-card">
              <div className="result-header">
                <span className="result-label">{r.label}</span>
                <button className="clear-btn" onClick={() => removeResult(r.id)}>Hapus</button>
              </div>
              <AudioPlayer url={r.audioUrl} filename={r.filename} srtUrl={r.srtUrl} label={r.label} />
            </div>
          ))}
        </section>
      )}

      <SettingsDialog
        open={settingsOpen}
        onClose={() => { setSettingsOpen(false); loadVoices() }}
      />
    </main>
  )
}
