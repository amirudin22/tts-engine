package com.ttsengine.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.ttsengine.model.VoiceInfo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.util.Locale

class TTSManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false

    fun init(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            ready = (status == TextToSpeech.SUCCESS)
            if (ready) {
                tts?.language = Locale("id", "ID")
            }
            onReady()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    fun getAvailableVoices(): List<VoiceInfo> {
        val engineVoices = tts?.voices ?: return emptyList()
        val indoVoices = engineVoices.filter {
            it.locale.language == "id" ||
            it.locale.language == "jv" ||
            it.locale.language == "su"
        }
        return indoVoices.map {
            VoiceInfo(
                name = it.name,
                displayName = it.name.substringAfterLast("/").substringBefore("-"),
                locale = it.locale.language,
                gender = if (it.isNetworkConnectionRequired) "Neural" else "Local"
            )
        }
    }

    suspend fun synthesizeBatch(
        texts: List<String>,
        voiceName: String,
        rate: Float,
        pitch: Float,
        cacheDir: File,
        onProgress: (Int, Int) -> Unit
    ): List<File> = coroutineScope {
        val engine = tts ?: throw Exception("TTS not initialized")

        val voice = engine.voices.find { it.name == voiceName }
            ?: engine.voices.firstOrNull()
            ?: throw Exception("No voice available")

        engine.voice = voice
        engine.setSpeechRate(1f + rate / 100f)
        engine.setPitch(1f + pitch / 100f)

        val deferreds = texts.mapIndexed { index, text ->
            async {
                val outputFile = File(cacheDir, "chunk_${"%03d".format(index)}.wav")
                val done = java.util.concurrent.CountDownLatch(1)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { done.countDown() }
                    override fun onError(utteranceId: String?) { done.countDown() }
                })

                val params = android.os.Bundle()
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "chunk_$index")

                val result = engine.synthesizeToFile(text, params, outputFile, "chunk_$index")
                if (result != TextToSpeech.SUCCESS) {
                    throw Exception("TTS synthesis failed for chunk $index")
                }

                done.await()
                onProgress(index + 1, texts.size)
                outputFile
            }
        }

        deferreds.map { it.await() }
    }
}
