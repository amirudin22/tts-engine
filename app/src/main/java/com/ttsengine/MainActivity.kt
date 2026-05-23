package com.ttsengine

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ttsengine.model.PipelineProgress
import com.ttsengine.model.PipelineResult
import com.ttsengine.model.PipelineStage
import com.ttsengine.model.PipelineState
import com.ttsengine.model.TTSConfig
import com.ttsengine.pipeline.TTSPipeline
import com.ttsengine.ui.MainScreen
import com.ttsengine.ui.theme.TTSEngineTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var pipeline: TTSPipeline
    private var mediaPlayer: MediaPlayer? = null

    var pipelineState by mutableStateOf<PipelineState>(PipelineState.Idle)
        private set

    private var pendingPipeline: (() -> Unit)? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingPipeline?.invoke()
            pendingPipeline = null
        } else {
            Toast.makeText(this, "Izin penyimpanan diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pipeline = TTSPipeline(applicationContext)
        pipeline.initTTS {
            setContent {
                TTSEngineTheme {
                    MainScreen(
                        pipelineState = pipelineState,
                        onRunPipeline = { text, config, apiKey ->
                            if (checkStoragePermission(text, config, apiKey)) {
                                runPipeline(text, config, apiKey)
                            }
                        },
                        onStop = { cancelPipeline() },
                        onPlay = { playAudio(it) },
                        onShare = { shareFile(it) }
                    )
                }
            }
        }
    }

    private fun checkStoragePermission(text: String, config: TTSConfig, apiKey: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true
        val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
        return if (ContextCompat.checkSelfPermission(this, perm)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingPipeline = { runPipeline(text, config, apiKey) }
            requestPermissionLauncher.launch(perm)
            false
        } else true
    }

    private fun cancelPipeline() {
        pipelineState = PipelineState.Idle
    }

    private fun runPipeline(naskah: String, config: TTSConfig, apiKey: String?) {
        if (pipelineState is PipelineState.Running) return

        scope.launch {
            pipelineState = PipelineState.Running(
                PipelineProgress(PipelineStage.INIT, message = "Memulai...")
            )
            try {
                val outputFile = File(cacheDir, "tts_temp_output.m4a")
                val saved = pipeline.run(
                    naskah = naskah,
                    config = config,
                    apiKey = apiKey,
                    outputFile = outputFile
                )
                val result = PipelineResult(
                    displayPath = saved.displayPath,
                    uri = saved.uri,
                    filePath = saved.filePath
                )
                pipelineState = PipelineState.Success(result)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("api.deepseek.com") == true ->
                        "Gagal menghubungi DeepSeek. Periksa koneksi internet."
                    e.message?.contains("TTS") == true ->
                        "Gagal sintesis suara: ${e.message}"
                    else -> e.message ?: "Terjadi kesalahan"
                }
                pipelineState = PipelineState.Error(msg)
            }
        }
    }

    private fun playAudio(result: PipelineResult) {
        try {
            val uri = getResultUri(result)
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MainActivity, uri)
                setOnCompletionListener {
                    Toast.makeText(this@MainActivity, "Selesai diputar", Toast.LENGTH_SHORT).show()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memutar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(result: PipelineResult) {
        try {
            val uri = getResultUri(result)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Bagikan audio"))
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal berbagi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getResultUri(result: PipelineResult): Uri {
        return result.uri ?: result.filePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                FileProvider.getUriForFile(
                    this, "$packageName.fileprovider", file
                )
            } else null
        } ?: throw Exception("File tidak ditemukan")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        pipeline.shutdown()
        pipeline.cleanup()
        scope.cancel()
    }
}
