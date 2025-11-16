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
import kotlin.math.roundToInt


@Composable
fun EffectsTab(
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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EffectSection(
            title = "Delay",
            enabled = delayEnabled,
            onEnabledChange = onDelayEnabledChange
        ) {
            ParamSlider(
                label = "Delay Time",
                value = delayTime,
                onValueChange = onDelayTimeChange,
                valueDisplay = String.format("%.0f ms", delayTime * 1000)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ParamSlider(
                label = "Feedback",
                value = delayFeedback,
                onValueChange = onDelayFeedbackChange,
                valueDisplay = String.format("%.0f%%", delayFeedback * 100)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ParamSlider(
                label = "Wet / Dry Mix",
                value = delayMix,
                onValueChange = onDelayMixChange,
                valueDisplay = String.format("%.0f%%", delayMix * 100)
            )
        }

        EffectSection(
            title = "Chorus",
            enabled = chorusEnabled,
            onEnabledChange = onChorusEnabledChange
        ) {
            ParamSlider(
                label = "Rate",
                value = chorusRate,
                onValueChange = onChorusRateChange,
                valueDisplay = String.format("%.2f Hz", chorusRate * 5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ParamSlider(
                label = "Depth",
                value = chorusDepth,
                onValueChange = onChorusDepthChange,
                valueDisplay = String.format("%.0f%%", chorusDepth * 100)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ParamSlider(
                label = "Wet / Dry Mix",
                value = chorusMix,
                onValueChange = onChorusMixChange,
                valueDisplay = String.format("%.0f%%", chorusMix * 100)
            )
        }

        EffectSection(
            title = "Reverb",
            enabled = reverbEnabled,
            onEnabledChange = onReverbEnabledChange
        ) {
            ParamSlider(
                label = "Room Size",
                value = reverbSize,
                onValueChange = onReverbSizeChange,
                valueDisplay = String.format("%.0f%%", reverbSize * 100)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ParamSlider(
                label = "Damping",
                value = reverbDamping,
                onValueChange = onReverbDampingChange,
                valueDisplay = String.format("%.0f%%", reverbDamping * 100)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ParamSlider(
                label = "Wet / Dry Mix",
                value = reverbMix,
                onValueChange = onReverbMixChange,
                valueDisplay = String.format("%.0f%%", reverbMix * 100)
            )
        }
    }
}

@Composable
fun EffectSection(
    title: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (enabled) "ON" else "OFF",
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChange
                    )
                }
            }

            content()
        }
    }
}

