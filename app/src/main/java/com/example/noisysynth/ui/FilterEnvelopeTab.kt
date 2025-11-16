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
fun FilterEnvelopeTab(
    attack: Float,
    decay: Float,
    sustain: Float,
    release: Float,
    amount: Float,
    onAttackChange: (Float) -> Unit,
    onDecayChange: (Float) -> Unit,
    onSustainChange: (Float) -> Unit,
    onReleaseChange: (Float) -> Unit,
    onAmountChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FILTER ENVELOPE",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        ParamSlider(
            label = "Envelope Amount",
            value = amount,
            onValueChange = onAmountChange,
            valueDisplay = String.format("%.0f%%", amount * 100)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        ParamSlider(
            label = "Attack",
            value = attack,
            onValueChange = onAttackChange,
            valueDisplay = String.format("%.2fs", attack * 2.0f)
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        ParamSlider(
            label = "Decay",
            value = decay,
            onValueChange = onDecayChange,
            valueDisplay = String.format("%.2fs", decay * 2.0f)
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        ParamSlider(
            label = "Sustain",
            value = sustain,
            onValueChange = onSustainChange,
            valueDisplay = String.format("%.0f%%", sustain * 100)
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        ParamSlider(
            label = "Release",
            value = release,
            onValueChange = onReleaseChange,
            valueDisplay = String.format("%.2fs", release * 2.0f)
        )
    }
}
