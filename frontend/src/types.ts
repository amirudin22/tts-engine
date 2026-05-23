export interface Voice {
  id: string
  label: string
}

export interface TTSRequest {
  text: string
  voice: string
  rate: number
  pitch: number
}

export interface TTSResponse {
  filename: string
  duration_seconds: number
  chunks: number
}

export type Status = "idle" | "loading" | "success" | "error"
