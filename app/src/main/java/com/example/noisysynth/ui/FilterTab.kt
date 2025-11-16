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
fun FilterTab(
    cutoff: Float,
    resonance: Float,
    onCutoffChange: (Float) -> Unit,
    onResonanceChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FILTER (SVF LOWPASS)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        ParamSlider(
            label = "Cutoff Frequency",
            value = cutoff,
            onValueChange = onCutoffChange,
            valueDisplay = String.format("%.0f%%", cutoff * 100)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ParamSlider(
            label = "Resonance",
            value = resonance,
            onValueChange = onResonanceChange,
            valueDisplay = String.format("%.0f%%", resonance * 100)
        )
    }
}

