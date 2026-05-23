package com.ttsengine.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ttsengine.model.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onRunPipeline: (String, TTSConfig, String?) -> Unit,
    pipelineState: PipelineState,
    onStop: () -> Unit,
    onPlay: (com.ttsengine.model.PipelineResult) -> Unit,
    onShare: (com.ttsengine.model.PipelineResult) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var naskahText by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf(AVAILABLE_VOICES.first()) }
    var rate by remember { mutableStateOf(0f) }
    var pitch by remember { mutableStateOf(0f) }
    var useAI by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showSample by remember { mutableStateOf(false) }

    LaunchedEffect(pipelineState) {
        when (pipelineState) {
            is PipelineState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (pipelineState as PipelineState.Error).message,
                    actionLabel = "OK",
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("TTS Engine") },
                actions = {
                    TextButton(onClick = { showSample = !showSample }) {
                        Text(if (showSample) "Tutup" else "Contoh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (showSample) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Contoh Naskah", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Kancil dan Buaya\n\n" +
                            "Kancil: \"Hei Buaya! Bangunlah!\"\n" +
                            "Kancil melompat dari punggung buaya.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            naskahText = "Kancil dan Buaya\n\n" +
                                "Suatu hari, Kancil berjalan-jalan di tepi sungai. " +
                                "Ia melihat banyak buah segar di seberang sungai.\n\n" +
                                "Kancil: \"Aduh, enak sekali buah-buahan itu! " +
                                "Tapi bagaimana caranya aku bisa ke seberang?\"\n\n" +
                                "Kancil berpikir keras. Ia pun mendapat ide.\n\n" +
                                "Kancil: \"Hei Buaya! Bangunlah kalian semua!\"\n\n" +
                                "Buaya muncul dari air: \"Ada apa, Kancil?\"\n\n" +
                                "Kancil: \"Aku punya pesan penting dari Raja Hutan! " +
                                "Kalian harus berbaris dari tepi ke tepi!\"\n\n" +
                                "Buaya-buaya itu pun berbaris. Kancil melompat " +
                                "dari punggung buaya ke buaya lainnya.\n\n" +
                                "Setelah sampai di seberang, Kancil melompat ke darat dan tertawa.\n\n" +
                                "Kancil: \"Terima kasih, Buaya!\"\n\n" +
                                "Buaya-buaya marah karena tertipu. " +
                                "Tapi Kancil sudah pergi menikmati buah-buahan."
                            showSample = false
                        }) {
                            Text("Gunakan")
                        }
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Naskah", style = MaterialTheme.typography.titleSmall)
                        Text("${naskahText.length} karakter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = naskahText,
                        onValueChange = { naskahText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 250.dp),
                        placeholder = { Text("Tempel naskah di sini...") },
                        maxLines = Int.MAX_VALUE,
                        isError = pipelineState is PipelineState.Error
                    )
                }
            }

            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Suara", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedVoice.displayName,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                label = { Text("Voice") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                AVAILABLE_VOICES.forEach { voice ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(voice.displayName,
                                                    style = MaterialTheme.typography.bodyMedium)
                                                Text("${voice.gender} · ${voice.locale.uppercase()}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            selectedVoice = voice
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Kecepatan: ${rate.toInt()}%",
                        style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = rate,
                        onValueChange = { rate = it },
                        valueRange = -50f..50f,
                        steps = 20
                    )

                    Text("Pitch: ${pitch.toInt()}%",
                        style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = pitch,
                        onValueChange = { pitch = it },
                        valueRange = -50f..50f,
                        steps = 20
                    )
                }
            }

            Card {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("AI Rewrite",
                                style = MaterialTheme.typography.titleSmall)
                            Text("DeepSeek: narasi lebih natural",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = useAI, onCheckedChange = { useAI = it })
                    }

                    AnimatedVisibility(visible = useAI) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("DeepSeek API Key") },
                                placeholder = { Text("sk-...") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            when (val state = pipelineState) {
                is PipelineState.Idle -> {
                    Button(
                        onClick = {
                            onRunPipeline(
                                naskahText,
                                TTSConfig(selectedVoice.name, rate, pitch),
                                if (useAI && apiKey.isNotBlank()) apiKey else null
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = naskahText.isNotBlank()
                    ) {
                        if (apiKey.isNotBlank() && useAI) {
                            Text("Proses dengan AI Rewrite")
                        } else {
                            Text("Proses")
                        }
                    }
                }

                is PipelineState.Running -> {
                    val p = state.progress
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (p.total > 0) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp,
                                        progress = { p.current.toFloat() / p.total }
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(p.stage.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium)
                                    Text(p.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            if (p.total > 0) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { p.current.toFloat() / p.total },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "${p.current} / ${p.total}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onStop,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Batalkan")
                            }
                        }
                    }
                }

                is PipelineState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Selesai!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Icon(Icons.Default.Info, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text("Tersimpan di ${state.result.displayPath}",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.height(16.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onPlay(state.result) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Putar")
                                }
                                OutlinedButton(
                                    onClick = { onShare(state.result) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Bagikan")
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onRunPipeline(
                                        naskahText,
                                        TTSConfig(selectedVoice.name, rate, pitch),
                                        if (useAI && apiKey.isNotBlank()) apiKey else null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Proses Naskah Lain")
                            }
                        }
                    }
                }

                is PipelineState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text("Terjadi Kesalahan",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(state.message,
                                style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onRunPipeline(
                                        naskahText,
                                        TTSConfig(selectedVoice.name, rate, pitch),
                                        if (useAI && apiKey.isNotBlank()) apiKey else null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
    }
}
