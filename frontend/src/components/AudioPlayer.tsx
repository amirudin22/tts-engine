interface Props {
  url: string
  filename: string
}

export function AudioPlayer({ url, filename }: Props) {
  return (
    <div>
      <audio controls src={url} />
      <div>
        <a href={url} download={filename}>
          Download
        </a>
      </div>
    </div>
  )
}
