package com.example.noisysynth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noisysynth.ui.theme.NoisySynthTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private lateinit var synthEngine: SynthEngine
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the native synth engine
        synthEngine = SynthEngine()
        
        setContent {
            NoisySynthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SynthUI(synthEngine)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        synthEngine.delete()
    }
}

@Composable
fun SynthUI(synthEngine: SynthEngine) {
    var waveform by remember { mutableStateOf(0) } // 0=sine, 1=saw, 2=square, 3=triangle
    var filterCutoff by remember { mutableStateOf(0.5f) }
    var filterResonance by remember { mutableStateOf(0.3f) }
    var attack by remember { mutableStateOf(0.01f) }
    var decay by remember { mutableStateOf(0.1f) }
    var sustain by remember { mutableStateOf(0.7f) }
    var release by remember { mutableStateOf(0.3f) }
    var lfoRate by remember { mutableStateOf(2.0f) }
    var lfoAmount by remember { mutableStateOf(0.0f) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Noisy Synth",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        // Oscillator Section
        SectionCard(title = "OSCILLATOR") {
            Column {
                // Waveform selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Sine", "Saw", "Square", "Triangle").forEachIndexed { index, name ->
                        Button(
                            onClick = {
                                waveform = index
                                synthEngine.setWaveform(index)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (waveform == index) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(name)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Section
        SectionCard(title = "FILTER") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RotaryKnob(
                    label = "Cutoff",
                    value = filterCutoff,
                    onValueChange = { 
                        filterCutoff = it
                        synthEngine.setFilterCutoff(it)
                    }
                )
                
                RotaryKnob(
                    label = "Resonance",
                    value = filterResonance,
                    onValueChange = { 
                        filterResonance = it
                        synthEngine.setFilterResonance(it)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Envelope Section
        SectionCard(title = "ENVELOPE (ADSR)") {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RotaryKnob(
                        label = "Attack",
                        value = attack,
                        onValueChange = { 
                            attack = it
                            synthEngine.setAttack(it * 2.0f) // Scale to 0-2 seconds
                        }
                    )
                    
                    RotaryKnob(
                        label = "Decay",
                        value = decay,
                        onValueChange = { 
                            decay = it
                            synthEngine.setDecay(it * 2.0f)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RotaryKnob(
                        label = "Sustain",
                        value = sustain,
                        onValueChange = { 
                            sustain = it
                            synthEngine.setSustain(it)
                        }
                    )
                    
                    RotaryKnob(
                        label = "Release",
                        value = release,
                        onValueChange = { 
                            release = it
                            synthEngine.setRelease(it * 2.0f)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // LFO Section
        SectionCard(title = "LFO") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RotaryKnob(
                    label = "Rate",
                    value = lfoRate / 10.0f, // Scale to 0-10 Hz
                    onValueChange = { 
                        lfoRate = it * 10.0f
                        synthEngine.setLFORate(lfoRate)
                    }
                )
                
                RotaryKnob(
                    label = "Amount",
                    value = lfoAmount,
                    onValueChange = { 
                        lfoAmount = it
                        synthEngine.setLFOAmount(it)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Keyboard
        SimpleKeyboard(
            onNoteOn = { note -> synthEngine.noteOn(note) },
            onNoteOff = { note -> synthEngine.noteOff(note) }
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun RotaryKnob(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Knob circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .pointerInput(Unit) {
                    var centerX = 0f
                    var centerY = 0f
                    
                    detectDragGestures(
                        onDragStart = { offset ->
                            centerX = size.width / 2f
                            centerY = size.height / 2f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            
                            // Calculate angle from center
                            val x = change.position.x - centerX
                            val y = change.position.y - centerY
                            
                            // Vertical drag is more intuitive for knobs
                            val delta = -dragAmount.y / 100f
                            val newValue = (value + delta).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Knob indicator
            val angle = -135f + (value * 270f) // Map 0-1 to -135 to +135 degrees
            val radians = Math.toRadians(angle.toDouble())
            val indicatorX = (cos(radians) * 30).toFloat()
            val indicatorY = (sin(radians) * 30).toFloat()
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(x = indicatorX.dp, y = indicatorY.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Label
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        
        // Value display
        Text(
            text = String.format("%.2f", value),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SimpleKeyboard(
    onNoteOn: (Int) -> Unit,
    onNoteOff: (Int) -> Unit
) {
    // Simple one-octave keyboard (C4 to C5)
    val notes = listOf(60, 62, 64, 65, 67, 69, 71, 72) // MIDI notes: C, D, E, F, G, A, B, C
    val noteNames = listOf("C", "D", "E", "F", "G", "A", "B", "C")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        notes.forEachIndexed { index, note ->
            Button(
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .pointerInput(note) {
                        detectDragGestures(
                            onDragStart = { onNoteOn(note) },
                            onDragEnd = { onNoteOff(note) },
                            onDragCancel = { onNoteOff(note) },
                            onDrag = { _, _ -> }
                        )
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = noteNames[index],
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
