import type { Voice } from "../types"

interface Props {
  voices: Voice[]
  value: string
  onChange: (v: string) => void
  disabled: boolean
}

export function VoiceSelector({ voices, value, onChange, disabled }: Props) {
  return (
    <div>
      <label htmlFor="voice">Suara</label>
      <select
        id="voice"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={disabled}
      >
        {voices.map((v) => (
          <option key={v.id} value={v.id}>
            {v.label}
          </option>
        ))}
      </select>
    </div>
  )
}
