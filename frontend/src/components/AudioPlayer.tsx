import { useState, useEffect, useRef } from "react"

interface SubEntry {
  index: number
  start: number
  end: number
  text: string
}

interface Props {
  url: string
  filename: string
  srtUrl?: string
  label?: string
}

function parseSRT(srt: string): SubEntry[] {
  const entries: SubEntry[] = []
  const blocks = srt.trim().split(/\n\n+/)
  for (const block of blocks) {
    const lines = block.split("\n")
    if (lines.length < 3) continue
    const index = parseInt(lines[0], 10)
    const [startStr, endStr] = lines[1].split(" --> ")
    const text = lines.slice(2).join("\n")
    entries.push({
      index,
      start: tsToSeconds(startStr),
      end: tsToSeconds(endStr),
      text,
    })
  }
  return entries
}

function tsToSeconds(ts: string): number {
  const [h, m, s] = ts.replace(",", ".").split(":")
  return parseInt(h) * 3600 + parseInt(m) * 60 + parseFloat(s)
}

export function AudioPlayer({ url, filename, srtUrl, label }: Props) {
  const audioRef = useRef<HTMLAudioElement>(null)
  const [subs, setSubs] = useState<SubEntry[]>([])
  const [current, setCurrent] = useState("")

  useEffect(() => {
    if (!srtUrl) return
    fetch(srtUrl)
      .then((r) => r.text())
      .then((text) => setSubs(parseSRT(text)))
      .catch(() => {})
  }, [srtUrl])

  function handleTimeUpdate() {
    if (!audioRef.current || !subs.length) return
    const t = audioRef.current.currentTime
    const match = subs.find((s) => t >= s.start && t <= s.end)
    setCurrent(match?.text || "")
  }

  async function handleShare() {
    const shareData: ShareData = {
      title: "TTS Engine",
      text: label || `Audio dari TTS Engine`,
      url: url,
    }

    if (navigator.share) {
      try {
        await navigator.share(shareData)
      } catch {
        fallbackCopy()
      }
    } else {
      fallbackCopy()
    }
  }

  function fallbackCopy() {
    navigator.clipboard?.writeText(url).then(() => {
      alert("URL audio disalin ke clipboard")
    }).catch(() => {
      window.open(url, "_blank")
    })
  }

  return (
    <div>
      {srtUrl && (
        <div style={{ marginBottom: "0.5rem" }}>
          <a href={srtUrl} download={filename.replace(".mp3", ".srt")}>
            Download SRT
          </a>
        </div>
      )}

      <audio
        ref={audioRef}
        controls
        src={url}
        onTimeUpdate={handleTimeUpdate}
      />

      {current && (
        <div className="subtitle">{current}</div>
      )}

      <div className="export-row">
        <button className="share-btn" onClick={handleShare}>
          Bagikan
        </button>
        <a href={url} download={filename} className="download-link">
          Download Audio
        </a>
      </div>
    </div>
  )
}
