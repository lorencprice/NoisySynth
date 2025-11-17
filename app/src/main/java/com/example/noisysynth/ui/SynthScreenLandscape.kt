package com.example.noisysynth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SynthScreenLandscape(synthEngine: SynthEngine) {
    var waveform by remember { mutableStateOf(0) }
    var filterCutoff by remember { mutableStateOf(0.5f) }
    var filterResonance by remember { mutableStateOf(0.3f) }
    var attack by remember { mutableStateOf(0.01f) }
    var decay by remember { mutableStateOf(0.1f) }
    var sustain by remember { mutableStateOf(0.7f) }
    var release by remember { mutableStateOf(0.3f) }
    var filterAttack by remember { mutableStateOf(0.01f) }
    var filterDecay by remember { mutableStateOf(0.2f) }
    var filterSustain by remember { mutableStateOf(0.5f) }
    var filterRelease by remember { mutableStateOf(0.3f) }
    var filterEnvAmount by remember { mutableStateOf(0.5f) }
    var lfoRate by remember { mutableStateOf(2.0f) }
    var lfoAmount by remember { mutableStateOf(0.0f) }

    var delayEnabled by remember { mutableStateOf(false) }
    var delayTime by remember { mutableStateOf(0.35f) }
    var delayFeedback by remember { mutableStateOf(0.4f) }
    var delayMix by remember { mutableStateOf(0.3f) }

    var chorusEnabled by remember { mutableStateOf(false) }
    var chorusRate by remember { mutableStateOf(0.25f) }
    var chorusDepth by remember { mutableStateOf(0.3f) }
    var chorusMix by remember { mutableStateOf(0.25f) }

    var reverbEnabled by remember { mutableStateOf(false) }
    var reverbSize by remember { mutableStateOf(0.6f) }
    var reverbDamping by remember { mutableStateOf(0.35f) }
    var reverbMix by remember { mutableStateOf(0.4f) }

    var arpeggiatorEnabled by remember { mutableStateOf(false) }
    var arpeggiatorPattern by remember { mutableStateOf(0) }
    var arpeggiatorTempo by remember { mutableStateOf(0.5f) }
    var arpeggiatorGate by remember { mutableStateOf(0.5f) }
    var arpeggiatorSubdivisionIndex by remember { mutableStateOf(1) }

    var sequencerEnabled by remember { mutableStateOf(false) }
    var sequencerPattern by remember { mutableStateOf(0) }
    var sequencerTempo by remember { mutableStateOf(0.45f) }
    var sequencerStepLengthIndex by remember { mutableStateOf(1) }
    var sequencerMeasuresIndex by remember { mutableStateOf(0) }

    val sequencerMeasureOptions = listOf(4, 8, 16)

    fun stepsPerMeasure(stepLengthIndex: Int): Int = when (stepLengthIndex) {
        0 -> 8
        1 -> 4
        2 -> 2
        else -> 1
    }

    fun syncSequencerPattern(
        patternIndex: Int = sequencerPattern,
        stepLengthIndexValue: Int = sequencerStepLengthIndex,
        measureIndex: Int = sequencerMeasuresIndex
    ) {
        val measureCount = sequencerMeasureOptions[measureIndex]
        val totalSteps = measureCount * stepsPerMeasure(stepLengthIndexValue)
        val patternNotes = when (patternIndex) {
            1 -> listOf(72, 71, 69, 67, 65, 64, 62, 60)
            2 -> listOf(60, 64, 67, 70)
            3 -> listOf(60, 63, 67, 70, 74)
            else -> listOf(60, 62, 64, 65, 67, 69, 71, 72)
        }
        repeat(totalSteps) { idx ->
            val note = patternNotes[idx % patternNotes.size]
            synthEngine.setSequencerStep(idx, note, true)
        }
    }

    LaunchedEffect(Unit) {
        synthEngine.setArpeggiatorPattern(arpeggiatorPattern)
        synthEngine.setArpeggiatorRate(60 + arpeggiatorTempo * 120)
        synthEngine.setArpeggiatorGate(0.2f + arpeggiatorGate * 0.8f)
        synthEngine.setArpeggiatorSubdivision(arpeggiatorSubdivisionIndex)
        synthEngine.setArpeggiatorEnabled(arpeggiatorEnabled)
        synthEngine.setSequencerStepLength(sequencerStepLengthIndex)
        synthEngine.setSequencerMeasures(sequencerMeasureOptions[sequencerMeasuresIndex])
        synthEngine.setSequencerTempo(55 + sequencerTempo * 135)
        syncSequencerPattern()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // LEFT SIDE: Controls (scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOISY SYNTH",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 3.sp
                        )
                        Text(
                            text = "Subtractive Synthesizer",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }

            // Three-column grid for compact modules
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // OSCILLATOR
                ModulePanel(
                    title = "OSC",
                    accentColor = Color(0xFF6200EA),
                    modifier = Modifier.weight(1f)
                ) {
                    val waveforms = listOf("Sine", "Saw", "Sqr", "Tri")
                    waveforms.forEachIndexed { index, name ->
                        Button(
                            onClick = {
                                waveform = index
                                synthEngine.setWaveform(index)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (waveform == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(name, fontSize = 10.sp)
                        }
                        if (index < waveforms.size - 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // FILTER
                ModulePanel(
                    title = "FILTER",
                    accentColor = Color(0xFF03DAC5),
                    modifier = Modifier.weight(1f)
                ) {
                    CompactParamControl(
                        label = "Cutoff",
                        value = filterCutoff,
                        onValueChange = {
                            filterCutoff = it
                            synthEngine.setFilterCutoff(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactParamControl(
                        label = "Res",
                        value = filterResonance,
                        onValueChange = {
                            filterResonance = it
                            synthEngine.setFilterResonance(it)
                        }
                    )
                }

                // LFO
                ModulePanel(
                    title = "LFO",
                    accentColor = Color(0xFFFFAB00),
                    modifier = Modifier.weight(1f)
                ) {
                    CompactParamControl(
                        label = "Rate",
                        value = lfoRate / 10.0f,
                        onValueChange = {
                            lfoRate = it * 10.0f
                            synthEngine.setLFORate(lfoRate)
                        },
                        valueDisplay = String.format("%.1f Hz", lfoRate)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactParamControl(
                        label = "Amt",
                        value = lfoAmount,
                        onValueChange = {
                            lfoAmount = it
                            synthEngine.setLFOAmount(it)
                        }
                    )
                }
            }

            // Envelopes row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // AMP ENVELOPE
                ModulePanel(
                    title = "AMP ENV",
                    accentColor = Color(0xFFFF6D00),
                    modifier = Modifier.weight(1f)
                ) {
                    CompactParamControl(
                        label = "A",
                        value = attack,
                        onValueChange = {
                            attack = it
                            synthEngine.setAttack(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2f", attack * 2.0f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactParamControl(
                        label = "D",
                        value = decay,
                        onValueChange = {
                            decay = it
                            synthEngine.setDecay(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2f", decay * 2.0f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactParamControl(
                        label = "S",
                        value = sustain,
                        onValueChange = {
                            sustain = it
                            synthEngine.setSustain(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactParamControl(
                        label = "R",
                        value = release,
                        onValueChange = {
                            release = it
                            synthEngine.setRelease(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2f", release * 2.0f)
                    )
                }

                // FILTER ENVELOPE
                ModulePanel(
                    title = "FILT ENV",
                    accentColor = Color(0xFF00C853),
                    modifier = Modifier.weight(1f)
                ) {
                    CompactParamControl(
                        label = "Amt",
                        value = filterEnvAmount,
                        onValueChange = {
                            filterEnvAmount = it
                            synthEngine.setFilterEnvelopeAmount(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactParamControl(
                        label = "A",
                        value = filterAttack,
                        onValueChange = {
                            filterAttack = it
                            synthEngine.setFilterAttack(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2f", filterAttack * 2.0f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactParamControl(
                        label = "D",
                        value = filterDecay,
                        onValueChange = {
                            filterDecay = it
                            synthEngine.setFilterDecay(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2f", filterDecay * 2.0f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactParamControl(
                        label = "S",
                        value = filterSustain,
                        onValueChange = {
                            filterSustain = it
                            synthEngine.setFilterSustain(it)
                        }
                    )
                }
            }

            // Effects + Sequencing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // EFFECTS
                ModulePanel(
                    title = "FX",
                    accentColor = Color(0xFFE91E63),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delay", fontSize = 10.sp)
                        Switch(
                            checked = delayEnabled,
                            onCheckedChange = {
                                delayEnabled = it
                                synthEngine.setDelayEnabled(it)
                            },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chorus", fontSize = 10.sp)
                        Switch(
                            checked = chorusEnabled,
                            onCheckedChange = {
                                chorusEnabled = it
                                synthEngine.setChorusEnabled(it)
                            },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Reverb", fontSize = 10.sp)
                        Switch(
                            checked = reverbEnabled,
                            onCheckedChange = {
                                reverbEnabled = it
                                synthEngine.setReverbEnabled(it)
                            },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }

                // ARP/SEQ
                ModulePanel(
                    title = "SEQ",
                    accentColor = Color(0xFF2979FF),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Arp", fontSize = 10.sp)
                        Switch(
                            checked = arpeggiatorEnabled,
                            onCheckedChange = {
                                arpeggiatorEnabled = it
                                synthEngine.setArpeggiatorEnabled(it)
                            },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                    if (arpeggiatorEnabled) {
                        CompactParamControl(
                            label = "Tempo",
                            value = arpeggiatorTempo,
                            onValueChange = {
                                arpeggiatorTempo = it
                                synthEngine.setArpeggiatorRate(60 + it * 120)
                            },
                            valueDisplay = "${(60 + arpeggiatorTempo * 120).roundToInt()}"
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Seq", fontSize = 10.sp)
                        Switch(
                            checked = sequencerEnabled,
                            onCheckedChange = {
                                sequencerEnabled = it
                                synthEngine.setSequencerEnabled(it)
                            },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
            }
        }

        // RIGHT SIDE: Piano Keyboard (fixed, always visible)
        Surface(
            modifier = Modifier
                .width(400.dp)
                .fillMaxHeight(),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                SimplePianoKeyboard(
                    onNoteOn = { note -> synthEngine.noteOn(note) },
                    onNoteOff = { note -> synthEngine.noteOff(note) }
                )
            }
        }
    }
}
