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
    onNoteOff: (Int) -> Unit
) {
    // Define one octave from C (60) to C (72)
    val keys = listOf(
        // White keys
        PianoKey(60, false, 0f, "C"),
        PianoKey(62, false, 1f, "D"),
        PianoKey(64, false, 2f, "E"),
        PianoKey(65, false, 3f, "F"),
        PianoKey(67, false, 4f, "G"),
        PianoKey(69, false, 5f, "A"),
        PianoKey(71, false, 6f, "B"),
        PianoKey(72, false, 7f, "C"),
        // Black keys (positioned between white keys)
        PianoKey(61, true, 0.7f, "C#"),
        PianoKey(63, true, 1.7f, "D#"),
        PianoKey(66, true, 3.7f, "F#"),
        PianoKey(68, true, 4.7f, "G#"),
        PianoKey(70, true, 5.7f, "A#")
    )

    val whiteKeys = keys.filter { !it.isBlack }
    val blackKeys = keys.filter { it.isBlack }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(vertical = 8.dp)
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
            val whiteKeyWidth = (maxWidth - (2.dp * 7)) / 8f
            
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
                color = if (isPressed) Color(0xFF606060) else Color(0xFF808080),
                modifier = Modifier.padding(bottom = 8.dp)
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
