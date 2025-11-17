package com.example.noisysynth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Demo screen showcasing the new hardware-style aesthetic
 * Replace your current screen with this to see the new look
 */
@Composable
fun HardwareSynthDemo(synthEngine: SynthEngine) {
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
    

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF1A1A1A)
                    )
                )
            )
    ) {
        // Enhanced Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1A1A1A),
                                Color(0xFF2A2A2A),
                                Color(0xFF1A1A1A)
                            )
                        )
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LED indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00E5FF), androidx.compose.foundation.shape.CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "NOISY SYNTH",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 3.sp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "v2.0",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF808080),
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Module Panels
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: OSC + Filter + LFO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HardwareOscillatorModule(
                    waveform = waveform,
                    onWaveformChange = {
                        waveform = it
                        synthEngine.setWaveform(it)
                    },
                    modifier = Modifier.weight(1f)
                )

                HardwareFilterModule(
                    cutoff = filterCutoff,
                    resonance = filterResonance,
                    onCutoffChange = {
                        filterCutoff = it
                        synthEngine.setFilterCutoff(it)
                    },
                    onResonanceChange = {
                        filterResonance = it
                        synthEngine.setFilterResonance(it)
                    },
                    modifier = Modifier.weight(1f)
                )

                HardwareLFOModule(
                    rate = lfoRate,
                    amount = lfoAmount,
                    onRateChange = {
                        lfoRate = it
                        synthEngine.setLFORate(it)
                    },
                    onAmountChange = {
                        lfoAmount = it
                        synthEngine.setLFOAmount(it)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Envelopes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HardwareEnvelopeModule(
                    attack = attack,
                    decay = decay,
                    sustain = sustain,
                    release = release,
                    onAttackChange = {
                        attack = it
                        synthEngine.setAttack(it * 2.0f)
                    },
                    onDecayChange = {
                        decay = it
                        synthEngine.setDecay(it * 2.0f)
                    },
                    onSustainChange = {
                        sustain = it
                        synthEngine.setSustain(it)
                    },
                    onReleaseChange = {
                        release = it
                        synthEngine.setRelease(it * 2.0f)
                    },
                    title = "AMP ENV",
                    accentColor = Color(0xFFFF6D00),
                    modifier = Modifier.weight(1f)
                )

                HardwareEnvelopeModule(
                    attack = filterAttack,
                    decay = filterDecay,
                    sustain = filterSustain,
                    release = filterRelease,
                    onAttackChange = {
                        filterAttack = it
                        synthEngine.setFilterAttack(it * 2.0f)
                    },
                    onDecayChange = {
                        filterDecay = it
                        synthEngine.setFilterDecay(it * 2.0f)
                    },
                    onSustainChange = {
                        filterSustain = it
                        synthEngine.setFilterSustain(it)
                    },
                    onReleaseChange = {
                        filterRelease = it
                        synthEngine.setFilterRelease(it * 2.0f)
                    },
                    title = "FILT ENV",
                    accentColor = Color(0xFF00C853),
                    modifier = Modifier.weight(1f)
                )

                 HardwareEffectsModule(
                    delayEnabled = delayEnabled,
                    delayTime = delayTime,
                    delayFeedback = delayFeedback,
                    delayMix = delayMix,
                    onDelayEnabledChange = {
                        delayEnabled = it
                        synthEngine.setDelayEnabled(it)
                    },
                    onDelayTimeChange = {
                        delayTime = it
                        synthEngine.setDelayTime(it)
                    },
                    onDelayFeedbackChange = {
                        delayFeedback = it
                        synthEngine.setDelayFeedback(it)
                    },
                    onDelayMixChange = {
                        delayMix = it
                        synthEngine.setDelayMix(it)
                    },
                    chorusEnabled = chorusEnabled,
                    chorusRate = chorusRate,
                    chorusDepth = chorusDepth,
                    chorusMix = chorusMix,
                    onChorusEnabledChange = {
                        chorusEnabled = it
                        synthEngine.setChorusEnabled(it)
                    },
                    onChorusRateChange = {
                        chorusRate = it
                        synthEngine.setChorusRate(it)
                    },
                    onChorusDepthChange = {
                        chorusDepth = it
                        synthEngine.setChorusDepth(it)
                    },
                    onChorusMixChange = {
                        chorusMix = it
                        synthEngine.setChorusMix(it)
                    },
                    reverbEnabled = reverbEnabled,
                    reverbSize = reverbSize,
                    reverbDamping = reverbDamping,
                    reverbMix = reverbMix,
                    onReverbEnabledChange = {
                        reverbEnabled = it
                        synthEngine.setReverbEnabled(it)
                    },
                    onReverbSizeChange = {
                        reverbSize = it
                        synthEngine.setReverbSize(it)
                    },
                    onReverbDampingChange = {
                        reverbDamping = it
                        synthEngine.setReverbDamping(it)
                    },
                    onReverbMixChange = {
                        reverbMix = it
                        synthEngine.setReverbMix(it)
                    },
                    modifier = Modifier.weight(1f)
                )

        // Piano Keyboard
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            tonalElevation = 0.dp,
            shadowElevation = 12.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A0A))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                SimplePianoKeyboard(
                    onNoteOn = { note -> synthEngine.noteOn(note) },
                    onNoteOff = { note -> synthEngine.noteOff(note) },
                    height = 90
                )
            }
        }
    }
}
