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
fun SynthScreenModular(synthEngine: SynthEngine) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Enhanced Header
        SynthHeader()

        // Scrollable Module Grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Oscillator + Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // OSCILLATOR MODULE
                ModulePanel(
                    title = "OSCILLATOR",
                    accentColor = Color(0xFF6200EA),
                    modifier = Modifier.weight(1f)
                ) {
                    val waveforms = listOf("Sine", "Saw", "Square", "Triangle")
                    waveforms.forEachIndexed { index, name ->
                        Button(
                            onClick = {
                                waveform = index
                                synthEngine.setWaveform(index)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (waveform == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                name,
                                fontSize = 12.sp,
                                fontWeight = if (waveform == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (index < waveforms.size - 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                // FILTER MODULE
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
                    Spacer(modifier = Modifier.height(8.dp))
                    CompactParamControl(
                        label = "Resonance",
                        value = filterResonance,
                        onValueChange = {
                            filterResonance = it
                            synthEngine.setFilterResonance(it)
                        }
                    )
                }
            }

            // Row 2: Amp Envelope + Filter Envelope
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AMP ENVELOPE MODULE
                ModulePanel(
                    title = "AMP ENV",
                    accentColor = Color(0xFFFF6D00),
                    modifier = Modifier.weight(1f)
                ) {
                    CompactParamControl(
                        label = "Attack",
                        value = attack,
                        onValueChange = {
                            attack = it
                            synthEngine.setAttack(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2fs", attack * 2.0f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactParamControl(
                        label = "Decay",
                        value = decay,
                        onValueChange = {
                            decay = it
                            synthEngine.setDecay(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2fs", decay * 2.0f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactParamControl(
                        label = "Sustain",
                        value = sustain,
                        onValueChange = {
                            sustain = it
                            synthEngine.setSustain(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactParamControl(
                        label = "Release",
                        value = release,
                        onValueChange = {
                            release = it
                            synthEngine.setRelease(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2fs", release * 2.0f)
                    )
                }

                // FILTER ENVELOPE MODULE
                ModulePanel(
                    title = "FILT ENV",
                    accentColor = Color(0xFF00C853),
                    modifier = Modifier.weight(1f)
                ) {
                    CompactParamControl(
                        label = "Amount",
                        value = filterEnvAmount,
                        onValueChange = {
                            filterEnvAmount = it
                            synthEngine.setFilterEnvelopeAmount(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactParamControl(
                        label = "Attack",
                        value = filterAttack,
                        onValueChange = {
                            filterAttack = it
                            synthEngine.setFilterAttack(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2fs", filterAttack * 2.0f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactParamControl(
                        label = "Decay",
                        value = filterDecay,
                        onValueChange = {
                            filterDecay = it
                            synthEngine.setFilterDecay(it * 2.0f)
                        },
                        valueDisplay = String.format("%.2fs", filterDecay * 2.0f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactParamControl(
                        label = "Sustain",
                        value = filterSustain,
                        onValueChange = {
                            filterSustain = it
                            synthEngine.setFilterSustain(it)
                        }
                    )
                }
            }

            // Row 3: LFO + Effects Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LFO MODULE
                ModulePanel(
                    title = "LFO → FILTER",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    CompactParamControl(
                        label = "Amount",
                        value = lfoAmount,
                        onValueChange = {
                            lfoAmount = it
                            synthEngine.setLFOAmount(it)
                        }
                    )
                }

                // EFFECTS MODULE (Compact Summary)
                ModulePanel(
                    title = "EFFECTS",
                    accentColor = Color(0xFFE91E63),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delay", fontSize = 11.sp)
                        Switch(
                            checked = delayEnabled,
                            onCheckedChange = {
                                delayEnabled = it
                                synthEngine.setDelayEnabled(it)
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chorus", fontSize = 11.sp)
                        Switch(
                            checked = chorusEnabled,
                            onCheckedChange = {
                                chorusEnabled = it
                                synthEngine.setChorusEnabled(it)
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reverb", fontSize = 11.sp)
                        Switch(
                            checked = reverbEnabled,
                            onCheckedChange = {
                                reverbEnabled = it
                                synthEngine.setReverbEnabled(it)
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            // Row 4: Arpeggiator + Sequencer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ARPEGGIATOR MODULE
                ModulePanel(
                    title = "ARPEGGIATOR",
                    accentColor = Color(0xFF2979FF),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enabled", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = arpeggiatorEnabled,
                            onCheckedChange = {
                                arpeggiatorEnabled = it
                                synthEngine.setArpeggiatorEnabled(it)
                            }
                        )
                    }
                    if (arpeggiatorEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CompactParamControl(
                            label = "Tempo",
                            value = arpeggiatorTempo,
                            onValueChange = {
                                arpeggiatorTempo = it
                                synthEngine.setArpeggiatorRate(60 + it * 120)
                            },
                            valueDisplay = String.format("%d BPM", (60 + arpeggiatorTempo * 120).roundToInt())
                        )
                    }
                }

                // SEQUENCER MODULE
                ModulePanel(
                    title = "SEQUENCER",
                    accentColor = Color(0xFF00BFA5),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enabled", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = sequencerEnabled,
                            onCheckedChange = {
                                sequencerEnabled = it
                                synthEngine.setSequencerEnabled(it)
                            }
                        )
                    }
                    if (sequencerEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CompactParamControl(
                            label = "Tempo",
                            value = sequencerTempo,
                            onValueChange = {
                                sequencerTempo = it
                                synthEngine.setSequencerTempo(55 + it * 135)
                            },
                            valueDisplay = String.format("%d BPM", (55 + sequencerTempo * 135).roundToInt())
                        )
                    }
                }
            }
        }

        // Piano Keyboard (Always visible at bottom)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SimplePianoKeyboard(
                    onNoteOn = { note -> synthEngine.noteOn(note) },
                    onNoteOff = { note -> synthEngine.noteOff(note) }
                )
            }
        }
    }
}
