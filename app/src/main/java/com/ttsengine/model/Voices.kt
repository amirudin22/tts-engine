package com.ttsengine.model

data class VoiceInfo(
    val name: String,
    val displayName: String,
    val locale: String,
    val gender: String
)

val AVAILABLE_VOICES = listOf(
    VoiceInfo("id-ID-ArdiNeural", "Ardi (Pria)", "id", "Pria"),
    VoiceInfo("id-ID-GadisNeural", "Gadis (Wanita)", "id", "Wanita"),
    VoiceInfo("jv-ID-DimasNeural", "Dimas (Jawa - Pria)", "jv", "Pria"),
    VoiceInfo("jv-ID-SitiNeural", "Siti (Jawa - Wanita)", "jv", "Wanita"),
    VoiceInfo("su-ID-JajangNeural", "Jajang (Sunda - Pria)", "su", "Pria"),
    VoiceInfo("su-ID-TutiNeural", "Tuti (Sunda - Wanita)", "su", "Wanita"),
)
