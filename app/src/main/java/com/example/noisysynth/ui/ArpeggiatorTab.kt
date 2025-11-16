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
fun ArpeggiatorTab(
    enabled: Boolean,
    selectedPattern: Int,
    tempo: Float,
    gate: Float,
    subdivisionIndex: Int,
    onEnabledChange: (Boolean) -> Unit,
    onPatternChange: (Int) -> Unit,
    onTempoChange: (Float) -> Unit,
    onGateChange: (Float) -> Unit,
    onSubdivisionChange: (Int) -> Unit
) {
    val patterns = listOf("Up", "Down", "Up-Down", "Random")
    val gateDisplay = listOf("20%", "40%", "60%", "80%", "100%")
    val subdivisionOptions = listOf("1/2", "1/4", "1/8", "1/16")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ARPEGGIATOR",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enabled",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }

        PatternSelector(
            title = "Pattern",
            options = patterns,
            selectedIndex = selectedPattern,
            onSelectionChange = onPatternChange
        )

        Spacer(modifier = Modifier.height(18.dp))

        ParamSlider(
            label = "Tempo",
            value = tempo,
            onValueChange = onTempoChange,
            valueDisplay = String.format("%d BPM", (60 + tempo * 120).roundToInt())
        )

        Spacer(modifier = Modifier.height(16.dp))

        ParamSlider(
            label = "Note Length",
            value = subdivisionIndex.toFloat() / (subdivisionOptions.size - 1),
            onValueChange = {
                val steps = (it * (subdivisionOptions.size - 1)).roundToInt()
                onSubdivisionChange(steps)
            },
            valueDisplay = subdivisionOptions[subdivisionIndex]
        )

        Spacer(modifier = Modifier.height(16.dp))

        ParamSlider(
            label = "Gate",
            value = gate,
            onValueChange = {
                val steps = (it * (gateDisplay.size - 1)).roundToInt()
                onGateChange(steps.toFloat() / (gateDisplay.size - 1))
            },
            valueDisplay = gateDisplay[(gate * (gateDisplay.size - 1)).roundToInt()]
        )
    }
}

