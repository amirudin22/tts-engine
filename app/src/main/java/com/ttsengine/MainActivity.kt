package com.ttsengine

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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

    var pipelineState by mutableStateOf<PipelineState>(PipelineState.Idle)
        private set

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startPipeline()
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
                            if (checkStoragePermission()) {
                                runPipeline(text, config, apiKey)
                            }
                        },
                        onStop = { pipelineState = PipelineState.Idle }
                    )
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return true
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        return if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(permission)
            false
        } else true
    }

    private fun runPipeline(naskah: String, config: TTSConfig, apiKey: String?) {
        scope.launch {
            pipelineState = PipelineState.Running(
                com.ttsengine.model.PipelineProgress("Mulai", 0, 0, "Memulai...")
            )
            try {
                val outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                    ?: cacheDir
                val outputFile = File(outputDir, "tts_output.mp3")

                val result = pipeline.run(
                    naskah = naskah,
                    config = config,
                    apiKey = apiKey,
                    outputFile = outputFile
                )
                pipelineState = PipelineState.Success(result)
            } catch (e: Exception) {
                pipelineState = PipelineState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pipeline.shutdown()
        pipeline.cleanup()
        scope.cancel()
    }
}
