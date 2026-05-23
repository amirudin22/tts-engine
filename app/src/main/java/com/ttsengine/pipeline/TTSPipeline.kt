package com.ttsengine.pipeline

import android.content.Context
import com.ttsengine.ai.DeepSeekClient
import com.ttsengine.ai.NarasiRewriter
import com.ttsengine.ai.NaskahParser
import com.ttsengine.model.PipelineProgress
import com.ttsengine.model.Segment
import com.ttsengine.model.TTSConfig
import com.ttsengine.tts.AudioMerger
import com.ttsengine.tts.TTSManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class TTSPipeline(private val context: Context) {

    private val ttsManager = TTSManager(context)
    private val _progress = MutableStateFlow(PipelineProgress())
    val progress: StateFlow<PipelineProgress> = _progress.asStateFlow()

    private var tempDir: File? = null

    fun initTTS(onReady: () -> Unit) {
        ttsManager.init(onReady)
    }

    fun shutdown() {
        ttsManager.shutdown()
    }

    suspend fun run(
        naskah: String,
        config: TTSConfig,
        apiKey: String? = null,
        outputFile: File
    ): String {
        _progress.value = PipelineProgress("Parsing", 0, 0, "Membaca naskah...")
        val lines = naskah.split("\n")
        val title = lines.firstOrNull()?.take(50) ?: "Untitled"
        _progress.value = PipelineProgress("Parsing", 0, 0, "Judul: $title")

        val segments: List<Segment>

        if (apiKey != null && apiKey.isNotBlank()) {
            _progress.value = PipelineProgress("AI", 0, 0, "Menghubungi DeepSeek...")
            val client = DeepSeekClient(apiKey)

            _progress.value = PipelineProgress("AI", 0, 0, "Menganalisis naskah...")
            segments = NaskahParser.parse(naskah, client)

            _progress.value = PipelineProgress("AI", 0, 0, "Me-rewrite narasi...")
            val rewritten = NarasiRewriter.rewrite(segments, client)
            _progress.value = PipelineProgress("AI", 0, 0, "Rewrite selesai")
        } else {
            segments = naskah.lines()
                .filter { it.isNotBlank() }
                .map { Segment(it.trim(), "narasi") }
        }

        val allText = segments.joinToString("\n") { it.text }
        val chunks = Chunker.chunk(allText)

        _progress.value = PipelineProgress("TTS", 0, chunks.size, "Menyintesis suara...")

        tempDir = File(context.cacheDir, "tts_chunks_${System.currentTimeMillis()}")
        tempDir!!.mkdirs()

        val chunkFiles = ttsManager.synthesizeBatch(
            texts = chunks,
            voiceName = config.voiceName,
            rate = config.rate,
            pitch = config.pitch,
            cacheDir = tempDir!!,
            onProgress = { current, total ->
                _progress.value = PipelineProgress("TTS", current, total,
                    "Chunk $current/$total")
            }
        )

        _progress.value = PipelineProgress("Merging", 0, 0, "Menggabungkan audio...")
        AudioMerger.mergeWavFiles(chunkFiles, outputFile)

        _progress.value = PipelineProgress("Selesai", 0, 0, "Selesai!")
        return outputFile.absolutePath
    }

    fun cleanup() {
        tempDir?.deleteRecursively()
        tempDir = null
    }
}
