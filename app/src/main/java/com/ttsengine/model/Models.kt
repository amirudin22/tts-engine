package com.ttsengine.model

data class Segment(
    val text: String,
    val type: String,
    val subject: String = "",
    val emotion: String = ""
)

data class Story(
    val title: String = "",
    val segments: List<Segment> = emptyList()
)

data class PipelineProgress(
    val stage: PipelineStage = PipelineStage.INIT,
    val current: Int = 0,
    val total: Int = 0,
    val message: String = ""
)

enum class PipelineStage(val label: String) {
    INIT("Memulai"),
    AI_PARSE("Menganalisis naskah"),
    AI_REWRITE("Me-rewrite narasi"),
    CHUNKING("Membagi teks"),
    TTS("Menyintesis suara"),
    MERGING("Menggabungkan audio"),
    SAVING("Menyimpan hasil"),
    DONE("Selesai"),
    ERROR("Gagal")
}

data class PipelineResult(
    val displayPath: String,
    val uri: android.net.Uri?,
    val filePath: String?
)

sealed interface PipelineState {
    data object Idle : PipelineState
    data class Running(val progress: PipelineProgress) : PipelineState
    data class Success(val result: PipelineResult) : PipelineState
    data class Error(val message: String) : PipelineState
}

data class TTSConfig(
    val voiceName: String = "id-ID-ArdiNeural",
    val rate: Float = 0f,
    val pitch: Float = 0f
)
