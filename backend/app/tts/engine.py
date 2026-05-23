import asyncio
from pathlib import Path

import edge_tts

VOICES = {
    "id-ID-ArdiNeural": "Indonesian (Male)",
    "id-ID-GadisNeural": "Indonesian (Female)",
    "jv-ID-DimasNeural": "Javanese (Male)",
    "jv-ID-SitiNeural": "Javanese (Female)",
    "su-ID-JajangNeural": "Sundanese (Male)",
    "su-ID-TutiNeural": "Sundanese (Female)",
}

DEFAULT_VOICE = "id-ID-ArdiNeural"


async def synthesize(text: str, output_path: str | Path, voice: str = DEFAULT_VOICE,
                     rate: float = 0.0, pitch: float = 0.0) -> float:
    rate_str = f"{rate:+.0f}%"
    pitch_str = f"{pitch:+.0f}Hz"

    communicate = edge_tts.Communicate(text, voice, rate=rate_str, pitch=pitch_str)
    await communicate.save(str(output_path))

    return _get_duration(output_path)


async def synthesize_batch(texts: list[str], output_dir: str | Path, voice: str = DEFAULT_VOICE,
                           rate: float = 0.0, pitch: float = 0.0) -> list[Path]:
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    files: list[Path] = []
    for i, chunk in enumerate(texts):
        path = output_dir / f"chunk_{i:04d}.mp3"
        await synthesize(chunk, path, voice, rate, pitch)
        files.append(path)

    return files


def _get_duration(path: str | Path) -> float:
    import struct
    data = Path(path).read_bytes()
    idx = data.find(b"Xing")
    if idx == -1:
        idx = data.find(b"Info")
    if idx == -1:
        return 0.0
    try:
        frames = struct.unpack_from(">I", data, idx + 36 if data[idx:idx+4] == b"Xing" else idx + 36)[0]
        return frames * 0.026
    except Exception:
        return 0.0
