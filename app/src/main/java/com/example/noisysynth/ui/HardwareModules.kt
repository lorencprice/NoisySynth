package com.example.noisysynth

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Example: Oscillator module with hardware-style controls
 */
@Composable
fun HardwareOscillatorModule(
    waveform: Int,
    onWaveformChange: (Int) -> Unit,
    accentColor: Color = Color(0xFF6200EA),
    modifier: Modifier = Modifier
) {
    HardwareModulePanel(
        title = "OSCILLATOR",
        accentColor = accentColor,
        modifier = modifier
    ) {
        // Waveform buttons in 2x2 grid
        val waveforms = listOf("SINE", "SAW", "SQUARE", "TRI")
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            waveforms.take(2).forEachIndexed { index, name ->
                HardwareButton(
                    text = name,
                    selected = waveform == index,
                    onClick = { onWaveformChange(index) },
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            waveforms.drop(2).forEachIndexed { relativeIndex, name ->
                val index = relativeIndex + 2
                HardwareButton(
                    text = name,
                    selected = waveform == index,
                    onClick = { onWaveformChange(index) },
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Example: Filter module with rotary knobs
 */
@Composable
fun HardwareFilterModule(
    cutoff: Float,
    resonance: Float,
    onCutoffChange: (Float) -> Unit,
    onResonanceChange: (Float) -> Unit,
    accentColor: Color = Color(0xFF03DAC5),
    modifier: Modifier = Modifier
) {
    HardwareModulePanel(
        title = "FILTER",
        accentColor = accentColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RotaryKnob(
                label = "CUTOFF",
                value = cutoff,
                onValueChange = onCutoffChange,
                accentColor = accentColor
            )
            
            RotaryKnob(
                label = "RES",
                value = resonance,
                onValueChange = onResonanceChange,
                accentColor = accentColor
            )
        }
    }
}

/**
 * Example: Envelope module with vertical faders
 */
@Composable
fun HardwareEnvelopeModule(
    attack: Float,
    decay: Float,
    sustain: Float,
    release: Float,
    onAttackChange: (Float) -> Unit,
    onDecayChange: (Float) -> Unit,
    onSustainChange: (Float) -> Unit,
    onReleaseChange: (Float) -> Unit,
    title: String = "AMP ENV",
    accentColor: Color = Color(0xFFFF6D00),
    modifier: Modifier = Modifier
) {
    HardwareModulePanel(
        title = title,
        accentColor = accentColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VerticalFader(
                label = "A",
                value = attack,
                onValueChange = onAttackChange,
                valueDisplay = String.format("%.2f", attack * 2.0f),
                accentColor = accentColor,
                height = 60
            )
            
            VerticalFader(
                label = "D",
                value = decay,
                onValueChange = onDecayChange,
                valueDisplay = String.format("%.2f", decay * 2.0f),
                accentColor = accentColor,
                height = 60
            )
            
            VerticalFader(
                label = "S",
                value = sustain,
                onValueChange = onSustainChange,
                accentColor = accentColor,
                height = 60
            )
            
            VerticalFader(
                label = "R",
                value = release,
                onValueChange = onReleaseChange,
                valueDisplay = String.format("%.2f", release * 2.0f),
                accentColor = accentColor,
                height = 60
            )
        }
    }
}

/**
 * Example: LFO module with knobs
 */
@Composable
fun HardwareLFOModule(
    rate: Float,
    amount: Float,
    onRateChange: (Float) -> Unit,
    onAmountChange: (Float) -> Unit,
    accentColor: Color = Color(0xFFFFAB00),
    modifier: Modifier = Modifier
) {
    HardwareModulePanel(
        title = "LFO → FILTER",
        accentColor = accentColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RotaryKnob(
                label = "RATE",
                value = rate / 10.0f,
                onValueChange = { onRateChange(it * 10.0f) },
                valueDisplay = String.format("%.1f Hz", rate),
                accentColor = accentColor
            )
            
            RotaryKnob(
                label = "AMOUNT",
                value = amount,
                onValueChange = onAmountChange,
                accentColor = accentColor
            )
        }
    }
}

/**
 * Effects module with tabs for different effects
 */
@Composable
fun HardwareEffectsModule(
    delayEnabled: Boolean,
    delayTime: Float,
    delayFeedback: Float,
    delayMix: Float,
    onDelayEnabledChange: (Boolean) -> Unit,
    onDelayTimeChange: (Float) -> Unit,
    onDelayFeedbackChange: (Float) -> Unit,
    onDelayMixChange: (Float) -> Unit,
    chorusEnabled: Boolean,
    chorusRate: Float,
    chorusDepth: Float,
    chorusMix: Float,
    onChorusEnabledChange: (Boolean) -> Unit,
    onChorusRateChange: (Float) -> Unit,
    onChorusDepthChange: (Float) -> Unit,
    onChorusMixChange: (Float) -> Unit,
    reverbEnabled: Boolean,
    reverbSize: Float,
    reverbDamping: Float,
    reverbMix: Float,
    onReverbEnabledChange: (Boolean) -> Unit,
    onReverbSizeChange: (Float) -> Unit,
    onReverbDampingChange: (Float) -> Unit,
    onReverbMixChange: (Float) -> Unit,
    accentColor: Color = Color(0xFFE91E63),
    modifier: Modifier = Modifier
) {
    var selectedEffect by remember { mutableStateOf(0) }
    
    HardwareModulePanel(
        title = "EFFECTS",
        accentColor = accentColor,
        modifier = modifier
    ) {
        // Effect selector tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HardwareButton(
                text = "DLY",
                selected = selectedEffect == 0,
                onClick = { selectedEffect = 0 },
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            HardwareButton(
                text = "CHR",
                selected = selectedEffect == 1,
                onClick = { selectedEffect = 1 },
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            HardwareButton(
                text = "REV",
                selected = selectedEffect == 2,
                onClick = { selectedEffect = 2 },
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Effect controls based on selection
        when (selectedEffect) {
            0 -> {
                // DELAY
                HardwareSwitch(
                    label = "DELAY",
                    checked = delayEnabled,
                    onCheckedChange = onDelayEnabledChange,
                    accentColor = accentColor
                )
                
                if (delayEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RotaryKnob(
                            label = "TIME",
                            value = delayTime,
                            onValueChange = onDelayTimeChange,
                            valueDisplay = String.format("%.0f", delayTime * 1000),
                            accentColor = accentColor
                        )
                        
                        RotaryKnob(
                            label = "FDBK",
                            value = delayFeedback,
                            onValueChange = onDelayFeedbackChange,
                            accentColor = accentColor
                        )
                        
                        RotaryKnob(
                            label = "MIX",
                            value = delayMix,
                            onValueChange = onDelayMixChange,
                            accentColor = accentColor
                        )
                    }
                }
            }
            
            1 -> {
                // CHORUS
                HardwareSwitch(
                    label = "CHORUS",
                    checked = chorusEnabled,
                    onCheckedChange = onChorusEnabledChange,
                    accentColor = accentColor
                )
                
                if (chorusEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RotaryKnob(
                            label = "RATE",
                            value = chorusRate,
                            onValueChange = onChorusRateChange,
                            valueDisplay = String.format("%.2f", chorusRate * 5f),
                            accentColor = accentColor
                        )
                        
                        RotaryKnob(
                            label = "DPTH",
                            value = chorusDepth,
                            onValueChange = onChorusDepthChange,
                            accentColor = accentColor
                        )
                        
                        RotaryKnob(
                            label = "MIX",
                            value = chorusMix,
                            onValueChange = onChorusMixChange,
                            accentColor = accentColor
                        )
                    }
                }
            }
            
            2 -> {
                // REVERB
                HardwareSwitch(
                    label = "REVERB",
                    checked = reverbEnabled,
                    onCheckedChange = onReverbEnabledChange,
                    accentColor = accentColor
                )
                
                if (reverbEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RotaryKnob(
                            label = "SIZE",
                            value = reverbSize,
                            onValueChange = onReverbSizeChange,
                            accentColor = accentColor
                        )
                        
                        RotaryKnob(
                            label = "DAMP",
                            value = reverbDamping,
                            onValueChange = onReverbDampingChange,
                            accentColor = accentColor
                        )
                        
                        RotaryKnob(
                            label = "MIX",
                            value = reverbMix,
                            onValueChange = onReverbMixChange,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }
}
