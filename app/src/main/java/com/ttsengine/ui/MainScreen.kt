package com.ttsengine.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ttsengine.model.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onRunPipeline: (String, TTSConfig, String?) -> Unit,
    pipelineState: PipelineState,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var naskahText by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf(AVAILABLE_VOICES.first()) }
    var rate by remember { mutableStateOf(0f) }
    var pitch by remember { mutableStateOf(0f) }
    var useAI by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }
    var showVoicePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("TTS Engine") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Naskah", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = naskahText,
                        onValueChange = { naskahText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("Tempel naskah di sini...") },
                        maxLines = Int.MAX_VALUE
                    )
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Suara", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedVoice.displayName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Voice") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            AVAILABLE_VOICES.forEach { voice ->
                                DropdownMenuItem(
                                    text = { Text("${voice.displayName} — ${voice.gender}") },
                                    onClick = {
                                        selectedVoice = voice
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("Kecepatan: ${rate.toInt()}%")
                    Slider(
                        value = rate,
                        onValueChange = { rate = it },
                        valueRange = -50f..50f,
                        steps = 20
                    )

                    Text("Pitch: ${pitch.toInt()}%")
                    Slider(
                        value = pitch,
                        onValueChange = { pitch = it },
                        valueRange = -50f..50f,
                        steps = 20
                    )
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI Rewrite", style = MaterialTheme.typography.titleSmall)
                        Switch(checked = useAI, onCheckedChange = { useAI = it })
                    }

                    if (useAI) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("DeepSeek API Key") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }
                }
            }

            when (val state = pipelineState) {
                is PipelineState.Idle -> {
                    Button(
                        onClick = {
                            onRunPipeline(naskahText, TTSConfig(
                                voiceName = selectedVoice.name,
                                rate = rate,
                                pitch = pitch
                            ), if (useAI && apiKey.isNotBlank()) apiKey else null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = naskahText.isNotBlank()
                    ) {
                        Text("Proses")
                    }
                }
                is PipelineState.Running -> {
                    LinearProgressIndicator(
                        progress = {
                            if (state.progress.total > 0)
                                state.progress.current.toFloat() / state.progress.total
                            else 0f
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(state.progress.message,
                        style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = onStop,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Batal")
                    }
                }
                is PipelineState.Success -> {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Selesai!", style = MaterialTheme.typography.titleMedium)
                            Text("Output: ${state.outputFile}")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { onRunPipeline(naskahText, TTSConfig(
                                voiceName = selectedVoice.name,
                                rate = rate,
                                pitch = pitch
                            ), if (useAI && apiKey.isNotBlank()) apiKey else null) }) {
                                Text("Proses Lagi")
                            }
                        }
                    }
                }
                is PipelineState.Error -> {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Error", style = MaterialTheme.typography.titleMedium)
                            Text(state.message)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                onRunPipeline(naskahText, TTSConfig(
                                    voiceName = selectedVoice.name,
                                    rate = rate,
                                    pitch = pitch
                                ), if (useAI && apiKey.isNotBlank()) apiKey else null)
                            }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
    }
}
