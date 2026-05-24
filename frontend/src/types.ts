export interface Voice {
  id: string
  label: string
}

export interface TTSRequest {
  text: string
  voice: string
  rate: number
  pitch: number
  subtitle?: boolean
}

export interface TTSResponse {
  filename: string
  duration_seconds: number
  chunks: number
  srt_filename: string
}

export type Status = "idle" | "loading" | "success" | "error"
