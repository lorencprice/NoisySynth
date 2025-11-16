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
fun SequencerTab(
    enabled: Boolean,
    selectedPattern: Int,
    tempo: Float,
    stepLengthIndex: Int,
    measuresIndex: Int,
    onEnabledChange: (Boolean) -> Unit,
    onPatternChange: (Int) -> Unit,
    onTempoChange: (Float) -> Unit,
    onStepLengthChange: (Int) -> Unit,
    onMeasuresChange: (Int) -> Unit
) {
    val patterns = listOf("8-STEP", "16-STEP", "POLYRYTHMIC", "RANDOM WALK")
    val noteLengthOptions = listOf("1/8", "1/4", "1/2", "1")
    val measureOptions = listOf(4, 8, 16)
    val safePatternIndex = selectedPattern.coerceIn(0, patterns.size - 1)
    val safeStepLengthIndex = stepLengthIndex.coerceIn(0, noteLengthOptions.size - 1)
    val safeMeasuresIndex = measuresIndex.coerceIn(0, measureOptions.size - 1)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "STEP SEQUENCER",
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
            title = "Sequence Length",
            options = patterns,
            selectedIndex = selectedPattern,
            onSelectionChange = onPatternChange
        )

        Spacer(modifier = Modifier.height(18.dp))

        ParamSlider(
            label = "Tempo",
            value = tempo,
            onValueChange = onTempoChange,
            valueDisplay = String.format("%d BPM", (55 + tempo * 135).roundToInt())
        )

        Spacer(modifier = Modifier.height(16.dp))

        ParamSlider(
            label = "Note Length",
            value = stepLengthIndex.toFloat() / (noteLengthOptions.size - 1),
            onValueChange = {
                val steps = (it * (noteLengthOptions.size - 1)).roundToInt()
                onStepLengthChange(steps)
            },
            valueDisplay = noteLengthOptions[safeStepLengthIndex]
        )

        Spacer(modifier = Modifier.height(16.dp))

        ParamSlider(
            label = "Measures",
            value = measuresIndex.toFloat() / (measureOptions.size - 1),
            onValueChange = {
                val steps = (it * (measureOptions.size - 1)).roundToInt()
                onMeasuresChange(steps)
            },
            valueDisplay = String.format("%d bars", measureOptions[safeMeasuresIndex])
        )
    }
}

@Composable
fun PatternSelector(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )

        options.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { option ->
                    val index = options.indexOf(option)
                    Button(
                        onClick = { onSelectionChange(index) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedIndex == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (selectedIndex == index) 6.dp else 2.dp
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

