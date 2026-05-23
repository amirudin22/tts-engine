import re

MAX_CHARS = 2800


def chunk_text(text: str) -> list[str]:
    sentences = re.split(r"(?<=[.!?])\s+", text.strip())
    chunks: list[str] = []
    current: list[str] = []

    for sentence in sentences:
        sentence = sentence.strip()
        if not sentence:
            continue

        trial = " ".join(current + [sentence]) if current else sentence
        if len(trial) > MAX_CHARS and current:
            chunks.append(" ".join(current))
            current = [sentence]
            continue

        if len(sentence) > MAX_CHARS:
            if current:
                chunks.append(" ".join(current))
                current = []
            for i in range(0, len(sentence), MAX_CHARS):
                chunks.append(sentence[i:i + MAX_CHARS])
        else:
            current.append(sentence)

    if current:
        chunks.append(" ".join(current))

    return chunks
