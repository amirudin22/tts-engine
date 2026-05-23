package com.ttsengine.pipeline

import android.content.Context
import com.ttsengine.FileUtils
import com.ttsengine.SaveResult
import com.ttsengine.ai.DeepSeekClient
import com.ttsengine.ai.NarasiRewriter
import com.ttsengine.ai.NaskahParser
import com.ttsengine.model.PipelineProgress
import com.ttsengine.model.PipelineStage
import com.ttsengine.model.Segment
import com.ttsengine.model.TTSConfig
import com.ttsengine.tts.AudioMerger
import com.ttsengine.tts.TTSManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class TTSPipeline(private val context: Context) {

    private val ttsManager = TTSManager(context)
    private val _progress = MutableStateFlow(PipelineProgress())
    val progress: StateFlow<PipelineProgress> = _progress.asStateFlow()

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log: StateFlow<List<String>> = _log.asStateFlow()

    private var tempDir: File? = null

    fun initTTS(onReady: () -> Unit) {
        ttsManager.init(onReady)
    }

    fun shutdown() {
        ttsManager.shutdown()
    }

    private fun setStage(stage: PipelineStage, current: Int = 0, total: Int = 0, message: String = "") {
        _progress.value = PipelineProgress(stage, current, total, message)
        _log.value = _log.value + message
    }

    suspend fun run(
        naskah: String,
        config: TTSConfig,
        apiKey: String? = null,
        outputFile: File
    ): SaveResult = withContext(Dispatchers.IO) {
        try {
            setStage(PipelineStage.INIT, message = "Membaca naskah...")

            val segments: List<Segment>

            if (apiKey != null && apiKey.isNotBlank()) {
                setStage(PipelineStage.AI_PARSE, message = "Menghubungi DeepSeek...")
                val client = DeepSeekClient(apiKey)

                setStage(PipelineStage.AI_PARSE, message = "Menganalisis segmen narasi & dialog...")
                segments = NaskahParser.parse(naskah, client)

                setStage(PipelineStage.AI_REWRITE, message = "Me-rewrite narasi agar natural dibacakan...")
                val rewritten = NarasiRewriter.rewrite(segments, client)
                setStage(PipelineStage.AI_REWRITE, message = "Rewrite selesai (${rewritten.size} segmen)")
            } else {
                segments = naskah.lines()
                    .filter { it.isNotBlank() }
                    .map { Segment(it.trim(), "narasi") }
            }

            val allText = segments.joinToString("\n") { it.text }

            setStage(PipelineStage.CHUNKING, message = "Membagi teks per ${Chunker.MAX_CHARS} karakter...")
            val chunks = Chunker.chunk(allText)
            setStage(PipelineStage.CHUNKING, message = "${chunks.size} chunk siap diproses")

            setStage(PipelineStage.TTS, current = 0, total = chunks.size,
                message = "Menyintesis $chunks.size chunk...")

            tempDir = File(context.cacheDir, "tts_chunks_${System.currentTimeMillis()}")
            tempDir!!.mkdirs()

            val chunkFiles = ttsManager.synthesizeBatch(
                texts = chunks,
                voiceName = config.voiceName,
                rate = config.rate,
                pitch = config.pitch,
                cacheDir = tempDir!!,
                onProgress = { current, total ->
                    setStage(PipelineStage.TTS, current, total,
                        "Chunk $current/$total selesai")
                }
            )

            val outputDir = outputFile.parentFile ?: context.cacheDir
            outputDir.mkdirs()

            setStage(PipelineStage.MERGING, message = "Menggabungkan ${chunkFiles.size} file audio...")
            AudioMerger.mergeWavFiles(chunkFiles, outputFile)

            setStage(PipelineStage.SAVING, message = "Menyimpan ke Documents/TTS Engine/...")
            val fileName = "tts_${java.text.SimpleDateFormat(
                "yyyyMMdd_HHmmss", java.util.Locale.getDefault()
            ).format(java.util.Date())}.m4a"
            val saved = FileUtils.saveOutput(context, outputFile, fileName)

            setStage(PipelineStage.DONE, message = "Output: ${saved.displayPath}")
            saved
        } catch (e: Exception) {
            setStage(PipelineStage.ERROR, message = e.message ?: "Terjadi kesalahan")
            throw e
        }
    }

    fun cleanup() {
        tempDir?.deleteRecursively()
        tempDir = null
    }
}
