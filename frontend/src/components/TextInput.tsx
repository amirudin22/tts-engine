interface Props {
  value: string
  onChange: (v: string) => void
  disabled: boolean
}

export function TextInput({ value, onChange, disabled }: Props) {
  return (
    <div>
      <div className="label-row">
        <label htmlFor="naskah">Naskah</label>
        {value && (
          <button className="clear-btn" onClick={() => onChange("")} disabled={disabled}>
            Hapus
          </button>
        )}
      </div>
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
