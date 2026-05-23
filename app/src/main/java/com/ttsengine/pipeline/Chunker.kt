package com.ttsengine.pipeline

object Chunker {
    const val MAX_CHARS = 2800

    fun chunk(text: String): List<String> {
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        val chunks = mutableListOf<String>()
        val current = StringBuilder()

        for (sentence in sentences) {
            val trimmed = sentence.trim()
            if (trimmed.isEmpty()) continue

            if (current.length + trimmed.length > MAX_CHARS && current.isNotEmpty()) {
                chunks.add(current.toString().trim())
                current.clear()
            }

            if (trimmed.length > MAX_CHARS) {
                trimmed.chunked(MAX_CHARS).forEach { chunks.add(it.trim()) }
            } else {
                if (current.isNotEmpty()) current.append(" ")
                current.append(trimmed)
            }
        }

        if (current.isNotEmpty()) {
            chunks.add(current.toString().trim())
        }

        return chunks
    }
}
