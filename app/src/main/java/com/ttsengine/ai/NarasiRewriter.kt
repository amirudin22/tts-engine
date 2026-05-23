package com.ttsengine.ai

import com.ttsengine.model.Segment

object NarasiRewriter {

    suspend fun rewrite(segments: List<Segment>, client: DeepSeekClient): List<Segment> {
        val narasiSegments = segments.filter { it.type == "narasi" }
        if (narasiSegments.isEmpty()) return segments

        val narasiTexts = narasiSegments.joinToString("\n") { it.text }

        val prompt = """Berikut adalah segmen narasi dari sebuah cerita. 
Tulis ulang setiap segmen narasi agar terdengar alami saat dibacakan dengan suara lantang (read-aloud story telling).
Gunakan bahasa Indonesia alami. Jangan ubah dialog. Tambahkan transisi natural.
Teks narasi:
$narasiTexts

Output dalam format JSON array: [{"original": "...", "rewritten": "..."}]"""

        val response = client.chat(
            messages = listOf(ChatMessage("user", prompt))
        )

        val cleaned = cleanJson(response)
        val arr = org.json.JSONArray(cleaned)
        val rewriteMap = mutableMapOf<String, String>()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            rewriteMap[obj.getString("original")] = obj.getString("rewritten")
        }

        return segments.map { seg ->
            if (seg.type == "narasi" && rewriteMap.containsKey(seg.text)) {
                seg.copy(text = rewriteMap[seg.text]!!)
            } else seg
        }
    }

    private fun cleanJson(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("```")) {
            s = s.substringAfter("```").substringAfter("\n").trim()
        }
        if (s.endsWith("```")) {
            s = s.substringBeforeLast("```").trim()
        }
        return s
    }
}
