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
 * Example: Effects module with switches
 */
@Composable
fun HardwareEffectsModule(
    delayEnabled: Boolean,
    chorusEnabled: Boolean,
    reverbEnabled: Boolean,
    onDelayChange: (Boolean) -> Unit,
    onChorusChange: (Boolean) -> Unit,
    onReverbChange: (Boolean) -> Unit,
    accentColor: Color = Color(0xFFE91E63),
    modifier: Modifier = Modifier
) {
    HardwareModulePanel(
        title = "EFFECTS",
        accentColor = accentColor,
        modifier = modifier
    ) {
        HardwareSwitch(
            label = "DELAY",
            checked = delayEnabled,
            onCheckedChange = onDelayChange,
            accentColor = accentColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        HardwareSwitch(
            label = "CHORUS",
            checked = chorusEnabled,
            onCheckedChange = onChorusChange,
            accentColor = accentColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        HardwareSwitch(
            label = "REVERB",
            checked = reverbEnabled,
            onCheckedChange = onReverbChange,
            accentColor = accentColor
        )
    }
}
