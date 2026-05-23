package com.ttsengine.ai

import com.ttsengine.model.Segment
import org.json.JSONArray

object NaskahParser {

    suspend fun parse(naskah: String, client: DeepSeekClient): List<Segment> {
        val prompt = """Analisis teks naskah story telling berikut. Kelompokkan setiap segmen sebagai "narasi" atau "dialog".
Jika dialog, sebutkan subjek (nama karakter) dan emosi.
Output JSON array: [{"text": "...", "type": "narasi/dialog", "subject": "...", "emotion": "..."}]

Teks:
$naskah"""

        val response = client.chat(
            messages = listOf(com.ttsengine.ai.ChatMessage("user", prompt))
        )

        val cleaned = cleanJson(response)
        val arr = JSONArray(cleaned)
        val segments = mutableListOf<Segment>()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            segments.add(Segment(
                text = obj.getString("text"),
                type = obj.getString("type"),
                subject = obj.optString("subject", ""),
                emotion = obj.optString("emotion", "")
            ))
        }
        return segments
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
