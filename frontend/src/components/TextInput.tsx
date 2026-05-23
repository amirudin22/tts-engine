interface Props {
  value: string
  onChange: (v: string) => void
  disabled: boolean
}

export function TextInput({ value, onChange, disabled }: Props) {
  return (
    <div>
      <label htmlFor="naskah">Naskah</label>
      <textarea
        id="naskah"
        rows={8}
        placeholder="Tulis naskah di sini..."
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={disabled}
      />
    </div>
  )
}
