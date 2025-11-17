package com.example.noisysynth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

data class PianoKey(
    val midiNote: Int,
    val isBlack: Boolean,
    val position: Float, // Position relative to white key width (0.0 = start of octave)
    val label: String = ""
)

@Composable
fun SimplePianoKeyboard(
    onNoteOn: (Int) -> Unit,
    onNoteOff: (Int) -> Unit,
    height: Int = 100 // Default height in dp, can be customized
) {
    // Define 2 octaves from C3 (48) to C5 (72) for better screen coverage
    val keys = mutableListOf<PianoKey>()
    
    // First octave: C3 (48) to B3 (59)
    val octave1Start = 48
    val octave1WhitePositions = listOf(
        PianoKey(48, false, 0f, "C"),
        PianoKey(50, false, 1f, "D"),
        PianoKey(52, false, 2f, "E"),
        PianoKey(53, false, 3f, "F"),
        PianoKey(55, false, 4f, "G"),
        PianoKey(57, false, 5f, "A"),
        PianoKey(59, false, 6f, "B")
    )
    val octave1BlackPositions = listOf(
        PianoKey(49, true, 0.7f),
        PianoKey(51, true, 1.7f),
        PianoKey(54, true, 3.7f),
        PianoKey(56, true, 4.7f),
        PianoKey(58, true, 5.7f)
    )
    
    // Second octave: C4 (60) to B4 (71)
    val octave2WhitePositions = listOf(
        PianoKey(60, false, 7f, "C"),
        PianoKey(62, false, 8f, "D"),
        PianoKey(64, false, 9f, "E"),
        PianoKey(65, false, 10f, "F"),
        PianoKey(67, false, 11f, "G"),
        PianoKey(69, false, 12f, "A"),
        PianoKey(71, false, 13f, "B")
    )
    val octave2BlackPositions = listOf(
        PianoKey(61, true, 7.7f),
        PianoKey(63, true, 8.7f),
        PianoKey(66, true, 10.7f),
        PianoKey(68, true, 11.7f),
        PianoKey(70, true, 12.7f)
    )
    
    // Final high C (72)
    val finalC = PianoKey(72, false, 14f, "C")
    
    // Combine all keys
    keys.addAll(octave1WhitePositions)
    keys.addAll(octave1BlackPositions)
    keys.addAll(octave2WhitePositions)
    keys.addAll(octave2BlackPositions)
    keys.add(finalC)

    val whiteKeys = keys.filter { !it.isBlack }
    val blackKeys = keys.filter { it.isBlack }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .padding(vertical = 4.dp)
    ) {
        // White keys layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            whiteKeys.forEach { key ->
                WhiteKey(
                    key = key,
                    onNoteOn = onNoteOn,
                    onNoteOff = onNoteOff,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Black keys layer (positioned absolutely on top)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val whiteKeyWidth = (maxWidth - (2.dp * 14)) / 15f
            
            blackKeys.forEach { key ->
                BlackKey(
                    key = key,
                    onNoteOn = onNoteOn,
                    onNoteOff = onNoteOff,
                    modifier = Modifier
                        .offset(x = whiteKeyWidth * key.position)
                        .zIndex(1f)
                )
            }
        }
    }
}

@Composable
fun WhiteKey(
    key: PianoKey,
    onNoteOn: (Int) -> Unit,
    onNoteOff: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = RoundedCornerShape(
                    bottomStart = 8.dp,
                    bottomEnd = 8.dp
                )
            )
            .background(
                brush = if (isPressed) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD0D0D0),
                            Color(0xFFE8E8E8)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFAFAFA),
                            Color(0xFFFFFFFF)
                        )
                    )
                },
                shape = RoundedCornerShape(
                    bottomStart = 8.dp,
                    bottomEnd = 8.dp
                )
            )
            .border(
                width = 1.dp,
                color = if (isPressed) Color(0xFFB0B0B0) else Color(0xFFD0D0D0),
                shape = RoundedCornerShape(
                    bottomStart = 8.dp,
                    bottomEnd = 8.dp
                )
            )
            .pointerInput(key.midiNote) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onNoteOn(key.midiNote)
                        tryAwaitRelease()
                        isPressed = false
                        onNoteOff(key.midiNote)
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (key.label.isNotEmpty()) {
            Text(
                text = key.label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 9.sp,
                color = if (isPressed) Color(0xFF606060) else Color(0xFF808080),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
fun BlackKey(
    key: PianoKey,
    onNoteOn: (Int) -> Unit,
    onNoteOff: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(36.dp)
            .fillMaxHeight(0.6f)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(
                    bottomStart = 6.dp,
                    bottomEnd = 6.dp
                )
            )
            .background(
                brush = if (isPressed) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF2A2A2A)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A0A),
                            Color(0xFF1A1A1A)
                        )
                    )
                },
                shape = RoundedCornerShape(
                    bottomStart = 6.dp,
                    bottomEnd = 6.dp
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF000000),
                shape = RoundedCornerShape(
                    bottomStart = 6.dp,
                    bottomEnd = 6.dp
                )
            )
            .pointerInput(key.midiNote) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onNoteOn(key.midiNote)
                        tryAwaitRelease()
                        isPressed = false
                        onNoteOff(key.midiNote)
                    }
                )
            }
    )
}
