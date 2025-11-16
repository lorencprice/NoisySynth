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
fun LFOTab(
    rate: Float,
    amount: Float,
    onRateChange: (Float) -> Unit,
    onAmountChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LFO → FILTER",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        ParamSlider(
            label = "LFO Rate",
            value = rate / 10.0f,
            onValueChange = onRateChange,
            valueDisplay = String.format("%.1f Hz", rate)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ParamSlider(
            label = "Modulation Amount",
            value = amount,
            onValueChange = onAmountChange,
            valueDisplay = String.format("%.0f%%", amount * 100)
        )
    }
}

