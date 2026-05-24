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
                     rate: float = 0.0, pitch: float = 0.0, sub_offset: int = 0) -> tuple[float, str]:
    rate_str = f"{rate:+.0f}%"
    pitch_str = f"{pitch:+.0f}Hz"

    communicate = edge_tts.Communicate(text, voice, rate=rate_str, pitch=pitch_str)
    submaker = edge_tts.SubMaker()

    with open(output_path, "wb") as f:
        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                f.write(chunk["data"])
            elif chunk["type"] in ("WordBoundary", "SentenceBoundary"):
                submaker.feed(chunk)

    srt_raw = submaker.get_srt()
    srt_offset = _offset_srt(srt_raw, sub_offset) if sub_offset else srt_raw
    duration = _parse_srt_duration(submaker) / 1_000_000

    return duration, srt_offset


async def synthesize_batch(texts: list[str], output_dir: str | Path, voice: str = DEFAULT_VOICE,
                           rate: float = 0.0, pitch: float = 0.0) -> tuple[list[Path], str]:
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    files: list[Path] = []
    all_srt_parts: list[str] = []
    cum_offset = 0

    for i, chunk in enumerate(texts):
        path = output_dir / f"chunk_{i:04d}.mp3"
        dur, srt = await synthesize(chunk, path, voice, rate, pitch, sub_offset=cum_offset)
        files.append(path)
        if srt.strip():
            all_srt_parts.append(srt.strip())
            cum_offset += _srt_last_time_s(srt) or dur
        else:
            cum_offset += dur

    merged_srt = _merge_srt(all_srt_parts)
    return files, merged_srt


def _offset_srt(srt: str, offset_us: int) -> str:
    if not srt.strip():
        return srt
    result: list[str] = []
    for line in srt.split("\n"):
        if "-->" in line:
            parts = line.split(" --> ")
            result.append(f"{_ts_add(parts[0], offset_us)} --> {_ts_add(parts[1], offset_us)}")
        else:
            result.append(line)
    return "\n".join(result)


def _ts_add(ts: str, us: int) -> str:
    h, m, s = ts.replace(",", ".").split(":")
    sec = float(h) * 3600 + float(m) * 60 + float(s) + us / 1_000_000
    hh = int(sec // 3600)
    mm = int((sec % 3600) // 60)
    ss = sec % 60
    return f"{hh:02d}:{mm:02d}:{ss:06.3f}".replace(".", ",")


def _parse_srt_duration(sm: edge_tts.SubMaker) -> float:
    if not sm.cues:
        return 0.0
    last = sm.cues[-1]
    return (last.end.total_seconds() * 1_000_000)


def _srt_last_time_s(srt: str) -> float:
    for line in reversed(srt.split("\n")):
        if "-->" in line:
            _, end = line.split(" --> ")
            end = end.replace(",", ".")
            h, m, s = end.split(":")
            return float(h) * 3600 + float(m) * 60 + float(s)
    return 0.0


def _reindex_srt(srt: str) -> str:
    result: list[str] = []
    idx = 1
    for line in srt.split("\n"):
        if line.strip().isdigit():
            result.append(str(idx))
            idx += 1
        else:
            result.append(line)
    return "\n".join(result)


def _merge_srt(parts: list[str]) -> str:
    merged = "\n\n".join(parts)
    return _reindex_srt(merged) + "\n"
