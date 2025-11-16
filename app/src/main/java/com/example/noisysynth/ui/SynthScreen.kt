package com.example.noisysynth

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SynthScreen(synthEngine: SynthEngine) {

    var waveform by remember { mutableStateOf("Saw") }
    var filterCutoff by remember { mutableStateOf(2000f) }
    var filterResonance by remember { mutableStateOf(0.3f) }

    var attack by remember { mutableStateOf(0.01f) }
    var decay by remember { mutableStateOf(0.2f) }
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

    var engineError by remember { mutableStateOf<String?>(null) }

    fun safeEngineCall(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            e.printStackTrace()
            engineError = e.toString()
        }
    }

    val sequencerMeasureOptions = listOf(4, 8, 16)

    fun stepsPerMeasure(stepLengthIndex: Int): Int = when (stepLengthIndex) {
        0 -> 8
        1 -> 4
        2 -> 2
        else -> 4
    }

    fun syncSequencerPattern(
        patternIndex: Int = sequencerPattern,
        stepLengthIndexValue: Int = sequencerStepLengthIndex,
        measureIndex: Int = sequencerMeasuresIndex
    ) {
        val safeMeasureIndex = measureIndex.coerceIn(0, sequencerMeasureOptions.size - 1)
        val safePatternIndex = patternIndex.coerceIn(0, 3)
        val safeStepIndex = stepLengthIndexValue.coerceIn(0, 3)

        val measureCount = sequencerMeasureOptions[safeMeasureIndex]
        val totalSteps = measureCount * stepsPerMeasure(safeStepIndex)
        val patternNotes = when (safePatternIndex) {
            1 -> listOf(72, 71, 69, 67, 65, 64, 62, 60)
            2 -> listOf(60, 64, 67, 70)
            3 -> listOf(60, 63, 67, 70, 74)
            else -> listOf(60, 62, 64, 65, 67, 69, 71, 72)
        }
        repeat(totalSteps) { idx ->
            val note = patternNotes[idx % patternNotes.size]
            safeEngineCall { synthEngine.setSequencerStep(idx, note, true) }
        }
    }

    LaunchedEffect(Unit) {
        safeEngineCall { synthEngine.setArpeggiatorPattern(arpeggiatorPattern) }
        safeEngineCall { synthEngine.setArpeggiatorRate(60 + arpeggiatorTempo * 120) }
        safeEngineCall { synthEngine.setArpeggiatorGate(0.2f + arpeggiatorGate * 0.8f) }
        safeEngineCall { synthEngine.setArpeggiatorSubdivision(arpeggiatorSubdivisionIndex) }
        safeEngineCall { synthEngine.setArpeggiatorEnabled(arpeggiatorEnabled) }
        safeEngineCall { synthEngine.setSequencerStepLength(sequencerStepLengthIndex) }
        safeEngineCall { synthEngine.setSequencerMeasures(sequencerMeasureOptions[sequencerMeasuresIndex]) }
        safeEngineCall { synthEngine.setSequencerTempo(55 + sequencerTempo * 135) }
        syncSequencerPattern()
    }

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Noisy Synth",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Desktop-style subtractive synth",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("OSC", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("FILTER", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("AMP ENV", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("FILT ENV", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("LFO", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text("FX", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    text = { Text("ARPEGGIO", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 7,
                    onClick = { selectedTab = 7 },
                    text = { Text("SEQUENCER", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> OscillatorTab(
                            waveform = waveform,
                            onWaveformChange = {
                                waveform = it
                                safeEngineCall { synthEngine.setWaveform(it) }
                            }
                        )
                        1 -> FilterTab(
                            cutoff = filterCutoff,
                            resonance = filterResonance,
                            onCutoffChange = {
                                filterCutoff = it
                                safeEngineCall { synthEngine.setFilterCutoff(it) }
                            },
                            onResonanceChange = {
                                filterResonance = it
                                safeEngineCall { synthEngine.setFilterResonance(it) }
                            }
                        )
                        2 -> EnvelopeTab(
                            attack = attack,
                            decay = decay,
                            sustain = sustain,
                            release = release,
                            onAttackChange = {
                                attack = it
                                safeEngineCall { synthEngine.setAttack(it * 2.0f) }
                            },
                            onDecayChange = {
                                decay = it
                                safeEngineCall { synthEngine.setDecay(it * 2.0f) }
                            },
                            onSustainChange = {
                                sustain = it
                                safeEngineCall { synthEngine.setSustain(it) }
                            },
                            onReleaseChange = {
                                release = it
                                safeEngineCall { synthEngine.setRelease(it * 2.0f) }
                            }
                        )
                        3 -> FilterEnvelopeTab(
                            attack = filterAttack,
                            decay = filterDecay,
                            sustain = filterSustain,
                            release = filterRelease,
                            amount = filterEnvAmount,
                            onAttackChange = {
                                filterAttack = it
                                safeEngineCall { synthEngine.setFilterAttack(it * 2.0f) }
                            },
                            onDecayChange = {
                                filterDecay = it
                                safeEngineCall { synthEngine.setFilterDecay(it * 2.0f) }
                            },
                            onSustainChange = {
                                filterSustain = it
                                safeEngineCall { synthEngine.setFilterSustain(it) }
                            },
                            onReleaseChange = {
                                filterRelease = it
                                safeEngineCall { synthEngine.setFilterRelease(it * 2.0f) }
                            },
                            onAmountChange = {
                                filterEnvAmount = it
                                safeEngineCall { synthEngine.setFilterEnvelopeAmount(it) }
                            }
                        )
                        4 -> LFOTab(
                            rate = lfoRate,
                            amount = lfoAmount,
                            onRateChange = {
                                lfoRate = it * 10.0f
                                safeEngineCall { synthEngine.setLFORate(lfoRate) }
                            },
                            onAmountChange = {
                                lfoAmount = it
                                safeEngineCall { synthEngine.setLFOAmount(it) }
                            }
                        )
                        5 -> EffectsTab(
                            delayEnabled = delayEnabled,
                            delayTime = delayTime,
                            delayFeedback = delayFeedback,
                            delayMix = delayMix,
                            chorusEnabled = chorusEnabled,
                            chorusRate = chorusRate,
                            chorusDepth = chorusDepth,
                            chorusMix = chorusMix,
                            reverbEnabled = reverbEnabled,
                            reverbSize = reverbSize,
                            reverbDamping = reverbDamping,
                            reverbMix = reverbMix,
                            onDelayEnabledChange = {
                                delayEnabled = it
                                safeEngineCall { synthEngine.setDelayEnabled(it) }
                            },
                            onDelayTimeChange = {
                                delayTime = it
                                safeEngineCall { synthEngine.setDelayTime(it) }
                            },
                            onDelayFeedbackChange = {
                                delayFeedback = it
                                safeEngineCall { synthEngine.setDelayFeedback(it) }
                            },
                            onDelayMixChange = {
                                delayMix = it
                                safeEngineCall { synthEngine.setDelayMix(it) }
                            },
                            onChorusEnabledChange = {
                                chorusEnabled = it
                                safeEngineCall { synthEngine.setChorusEnabled(it) }
                            },
                            onChorusRateChange = {
                                chorusRate = it
                                safeEngineCall { synthEngine.setChorusRate(it) }
                            },
                            onChorusDepthChange = {
                                chorusDepth = it
                                safeEngineCall { synthEngine.setChorusDepth(it) }
                            },
                            onChorusMixChange = {
                                chorusMix = it
                                safeEngineCall { synthEngine.setChorusMix(it) }
                            },
                            onReverbEnabledChange = {
                                reverbEnabled = it
                                safeEngineCall { synthEngine.setReverbEnabled(it) }
                            },
                            onReverbSizeChange = {
                                reverbSize = it
                                safeEngineCall { synthEngine.setReverbSize(it) }
                            },
                            onReverbDampingChange = {
                                reverbDamping = it
                                safeEngineCall { synthEngine.setReverbDamping(it) }
                            },
                            onReverbMixChange = {
                                reverbMix = it
                                safeEngineCall { synthEngine.setReverbMix(it) }
                            }
                        )
                        6 -> ArpeggiatorTab(
                            enabled = arpeggiatorEnabled,
                            selectedPattern = arpeggiatorPattern,
                            tempo = arpeggiatorTempo,
                            gate = arpeggiatorGate,
                            subdivisionIndex = arpeggiatorSubdivisionIndex,
                            onEnabledChange = {
                                arpeggiatorEnabled = it
                                safeEngineCall { synthEngine.setArpeggiatorEnabled(it) }
                            },
                            onPatternChange = {
                                arpeggiatorPattern = it
                                safeEngineCall { synthEngine.setArpeggiatorPattern(it) }
                            },
                            onTempoChange = {
                                arpeggiatorTempo = it
                                safeEngineCall { synthEngine.setArpeggiatorRate(60 + it * 120) }
                            },
                            onGateChange = {
                                arpeggiatorGate = it
                                safeEngineCall { synthEngine.setArpeggiatorGate(0.2f + it * 0.8f) }
                            },
                            onSubdivisionChange = { index ->
                                arpeggiatorSubdivisionIndex = index
                                safeEngineCall { synthEngine.setArpeggiatorSubdivision(index) }
                            }
                        )
                        7 -> SequencerTab(
                            enabled = sequencerEnabled,
                            selectedPattern = sequencerPattern,
                            tempo = sequencerTempo,
                            stepLengthIndex = sequencerStepLengthIndex,
                            measuresIndex = sequencerMeasuresIndex,
                            onEnabledChange = {
                                sequencerEnabled = it
                                safeEngineCall { synthEngine.setSequencerEnabled(it) }
                            },
                            onPatternChange = {
                                sequencerPattern = it
                                syncSequencerPattern(patternIndex = it)
                            },
                            onTempoChange = {
                                sequencerTempo = it
                                safeEngineCall { synthEngine.setSequencerTempo(55 + it * 135) }
                            },
                            onStepLengthChange = { index ->
                                sequencerStepLengthIndex = index
                                safeEngineCall { synthEngine.setSequencerStepLength(index) }
                                syncSequencerPattern(stepLengthIndexValue = index)
                            },
                            onMeasuresChange = { index ->
                                sequencerMeasuresIndex = index
                                safeEngineCall { synthEngine.setSequencerMeasures(sequencerMeasureOptions[index]) }
                                syncSequencerPattern(measureIndex = index)
                            }
                        )
                    }

                    if (engineError != null) {
                        Text(
                            text = "Engine error:\n$engineError",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SimpleKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            onNoteOn = { note -> safeEngineCall { synthEngine.noteOn(note) } },
            onNoteOff = { note -> safeEngineCall { synthEngine.noteOff(note) } }
        )
    }
}
