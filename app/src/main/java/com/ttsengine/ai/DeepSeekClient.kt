package com.ttsengine.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DeepSeekClient(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json".toMediaType()

    suspend fun chat(messages: List<ChatMessage>, systemPrompt: String = ""): String =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("model", "deepseek-chat")
                put("messages", JSONArray().apply {
                    if (systemPrompt.isNotBlank()) {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                    }
                    messages.forEach { msg ->
                        put(JSONObject().apply {
                            put("role", msg.role)
                            put("content", msg.content)
                        })
                    }
                })
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url("https://api.deepseek.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body.toString().toRequestBody(jsonMedia))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("API error: ${response.code} ${response.body?.string()}")
                }
                val json = JSONObject(response.body!!.string())
                json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            }
        }
}

data class ChatMessage(val role: String, val content: String)
