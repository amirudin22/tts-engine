from pydantic import BaseModel, Field


class TTSRequest(BaseModel):
    text: str = Field(min_length=1)
    voice: str = "id-ID-ArdiNeural"
    rate: float = 0.0
    pitch: float = 0.0
    subtitle: bool = True


class TTSResponse(BaseModel):
    filename: str
    duration_seconds: float
    chunks: int
    srt_filename: str = ""
