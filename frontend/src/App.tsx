import { useState, useEffect } from "react"
import { fetchVoices, generateSpeech, getAudioUrl } from "./api/tts"
import { TextInput } from "./components/TextInput"
import { VoiceSelector } from "./components/VoiceSelector"
import { RateSlider } from "./components/RateSlider"
import { AudioPlayer } from "./components/AudioPlayer"
import type { Voice, Status } from "./types"

export default function App() {
  const [voices, setVoices] = useState<Voice[]>([])
  const [text, setText] = useState("")
  const [voice, setVoice] = useState("id-ID-ArdiNeural")
  const [rate, setRate] = useState(0)
  const [pitch, setPitch] = useState(0)
  const [status, setStatus] = useState<Status>("idle")
  const [audioUrl, setAudioUrl] = useState("")
  const [filename, setFilename] = useState("")
  const [error, setError] = useState("")

  useEffect(() => {
    fetchVoices()
      .then((v) => {
        setVoices(v)
        if (v.length > 0) setVoice(v[0].id)
      })
      .catch(() => setVoices([]))
  }, [])

  async function handleGenerate() {
    if (!text.trim()) return
    setStatus("loading")
    setError("")
    setAudioUrl("")
    try {
      const res = await generateSpeech({ text, voice, rate, pitch })
      setAudioUrl(getAudioUrl(res.filename))
      setFilename(res.filename)
      setStatus("success")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Terjadi kesalahan")
      setStatus("error")
    }
  }

  return (
    <main>
      <h1>TTS Engine</h1>
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

      {status === "success" && <AudioPlayer url={audioUrl} filename={filename} />}

      {status === "error" && <p>{error}</p>}
    </main>
  )
}
