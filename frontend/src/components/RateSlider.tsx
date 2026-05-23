interface Props {
  label: string
  value: number
  onChange: (v: number) => void
  disabled: boolean
}

export function RateSlider({ label, value, onChange, disabled }: Props) {
  return (
    <div>
      <label>
        {label}: {value > 0 ? "+" : ""}{value}%
      </label>
      <input
        type="range"
        min={-50}
        max={50}
        step={5}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        disabled={disabled}
      />
      <div className="range-labels">
        <span>Lambat</span>
        <span>Cepat</span>
      </div>
    </div>
  )
}
