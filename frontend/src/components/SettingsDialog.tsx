import { useState, useEffect } from "react"
import { getBaseUrl, setBaseUrl, fetchVoices } from "../api/tts"

interface Props {
  open: boolean
  onClose: () => void
}

export function SettingsDialog({ open, onClose }: Props) {
  const [url, setUrl] = useState(getBaseUrl())
  const [status, setStatus] = useState<"idle" | "testing" | "ok" | "error">("idle")

  useEffect(() => {
    if (open) setUrl(getBaseUrl())
  }, [open])

  async function handleTest() {
    setStatus("testing")
    const prev = getBaseUrl()
    setBaseUrl(url)
    try {
      await fetchVoices()
      setStatus("ok")
    } catch {
      setBaseUrl(prev)
      setStatus("error")
    }
  }

  function handleSave() {
    setBaseUrl(url)
    onClose()
  }

  if (!open) return null

  return (
    <div className="overlay" onClick={onClose}>
      <div className="dialog" onClick={(e) => e.stopPropagation()}>
        <h2>Pengaturan</h2>

        <label htmlFor="backend-url">URL Backend</label>
        <input
          id="backend-url"
          type="text"
          value={url}
          onChange={(e) => { setUrl(e.target.value); setStatus("idle") }}
          placeholder="http://localhost:8000"
        />

        <div className="dialog-actions">
          <button className="secondary" onClick={handleTest} disabled={status === "testing"}>
            {status === "testing" ? "Mengetes..." : status === "ok" ? "✓ Berhasil" : status === "error" ? "✗ Gagal" : "Tes Koneksi"}
          </button>
          <button onClick={handleSave} disabled={!url.trim()}>Simpan</button>
        </div>
      </div>
    </div>
  )
}
