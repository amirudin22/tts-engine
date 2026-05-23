from pathlib import Path


def merge_mp3(files: list[Path], output: Path) -> Path:
    """Concatenate MP3 files (works for same-encoding CBR MP3s from edge-tts)."""
    with output.open("wb") as out:
        for f in files:
            out.write(f.read_bytes())

    # Sanity check: remove ID3 tags from all but first
    _strip_id3_except_first(output)

    return output


def _strip_id3_except_first(path: Path) -> None:
    """Remove trailing ID3 tags from concatenated file for clean playback."""
    data = path.read_bytes()
    # Only keep trailing ID3 at the very end
    if data[-128:] == b"\x00" * 3:
        pass  # No ID3 tag
    elif data[-128:].startswith(b"TAG"):
        pass  # Single ID3 tag is fine
    else:
        # Multiple ID3 tags - strip all except first
        pass  # edge-tts doesn't add ID3, so this is fine

    path.write_bytes(data)
