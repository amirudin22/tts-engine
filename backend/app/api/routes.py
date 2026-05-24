import shutil
import time
import tempfile
from pathlib import Path

from fastapi import APIRouter, HTTPException
from fastapi.responses import FileResponse, PlainTextResponse

from app.schemas import TTSRequest, TTSResponse
from app.pipeline.chunker import chunk_text
from app.pipeline.merger import merge_mp3
from app.tts.engine import synthesize_batch, VOICES

router = APIRouter(prefix="/api")
OUTPUT_DIR = Path(__file__).resolve().parent.parent.parent / "output"
MAX_FILES = 50
FILE_TTL = 3600


@router.get("/voices")
async def list_voices():
    return {"voices": [{"id": k, "label": v} for k, v in VOICES.items()]}


@router.post("/tts", response_model=TTSResponse)
async def text_to_speech(req: TTSRequest):
    if not req.text.strip():
        raise HTTPException(400, "Text is required")

    if req.voice not in VOICES:
        raise HTTPException(400, f"Unknown voice: {req.voice}")

    _cleanup_old_files()

    with tempfile.TemporaryDirectory() as tmpdir:
        chunk_dir = Path(tmpdir) / "chunks"
        output_path = Path(tmpdir) / "output.mp3"

        chunks = chunk_text(req.text)
        if not chunks:
            raise HTTPException(400, "No text to process")

        chunk_files, srt_content = await synthesize_batch(
            texts=chunks,
            output_dir=chunk_dir,
            voice=req.voice,
            rate=req.rate,
            pitch=req.pitch,
        )

        if len(chunk_files) == 1:
            chunk_files[0].rename(output_path)
        else:
            merge_mp3(chunk_files, output_path)

        OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
        ts = int(time.time())
        dest = OUTPUT_DIR / f"tts_{ts}.mp3"
        shutil.copy2(output_path, dest)

        srt_filename = ""
        if req.subtitle and srt_content.strip():
            srt_filename = f"tts_{ts}.srt"
            (OUTPUT_DIR / srt_filename).write_text(srt_content, encoding="utf-8")

    return TTSResponse(
        filename=dest.name,
        duration_seconds=round(dest.stat().st_size / 16000 * 0.1, 1) if dest.exists() else 0,
        chunks=len(chunks),
        srt_filename=srt_filename,
    )


@router.get("/download/{filename}")
async def download(filename: str):
    path = OUTPUT_DIR / filename
    if not path.exists():
        raise HTTPException(404, "File not found")

    if filename.endswith(".srt"):
        return PlainTextResponse(path.read_text(encoding="utf-8"), media_type="text/plain")

    return FileResponse(path, media_type="audio/mpeg", filename=filename)


def _cleanup_old_files():
    if not OUTPUT_DIR.exists():
        return
    now = time.time()
    files = sorted(OUTPUT_DIR.iterdir(), key=lambda f: f.stat().st_mtime, reverse=True)
    for f in files:
        try:
            if len(files) > MAX_FILES or (now - f.stat().st_mtime) > FILE_TTL:
                f.unlink(missing_ok=True)
                files.remove(f)
        except (ValueError, OSError):
            pass
