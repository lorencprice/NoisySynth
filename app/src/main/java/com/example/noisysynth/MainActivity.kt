package com.example.noisysynth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Noisy Synth",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        
        // Keyboard at TOP for easy access
        SimpleKeyboard(
            onNoteOn = { note -> synthEngine.noteOn(note) },
            onNoteOff = { note -> synthEngine.noteOff(note) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Scrollable controls
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        
        // Oscillator Section
        SectionCard(title = "OSCILLATOR") {
            Column {
                // Waveform selector - larger buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Sine", "Saw", "Square", "Tri").forEachIndexed { index, name ->
                        Button(
                            onClick = {
                                waveform = index
                                synthEngine.setWaveform(index)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (waveform == index) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                name,
                                fontSize = 13.sp,
                                fontWeight = if (waveform == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Section
        SectionCard(title = "FILTER") {
            Column {
                ParamSlider(
                    label = "Cutoff",
                    value = filterCutoff,
                    onValueChange = { 
                        filterCutoff = it
                        synthEngine.setFilterCutoff(it)
                    }
                )
                
                ParamSlider(
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
                ParamSlider(
                    label = "Attack",
                    value = attack,
                    onValueChange = { 
                        attack = it
                        synthEngine.setAttack(it * 2.0f) // Scale to 0-2 seconds
                    },
                    valueDisplay = String.format("%.2fs", attack * 2.0f)
                )
                
                ParamSlider(
                    label = "Decay",
                    value = decay,
                    onValueChange = { 
                        decay = it
                        synthEngine.setDecay(it * 2.0f)
                    },
                    valueDisplay = String.format("%.2fs", decay * 2.0f)
                )
                
                ParamSlider(
                    label = "Sustain",
                    value = sustain,
                    onValueChange = { 
                        sustain = it
                        synthEngine.setSustain(it)
                    }
                )
                
                ParamSlider(
                    label = "Release",
                    value = release,
                    onValueChange = { 
                        release = it
                        synthEngine.setRelease(it * 2.0f)
                    },
                    valueDisplay = String.format("%.2fs", release * 2.0f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // LFO Section
        SectionCard(title = "LFO") {
            Column {
                ParamSlider(
                    label = "Rate",
                    value = lfoRate / 10.0f, // Scale to 0-10 Hz
                    onValueChange = { 
                        lfoRate = it * 10.0f
                        synthEngine.setLFORate(lfoRate)
                    },
                    valueDisplay = String.format("%.1f Hz", lfoRate)
                )
                
                ParamSlider(
                    label = "Amount",
                    value = lfoAmount,
                    onValueChange = { 
                        lfoAmount = it
                        synthEngine.setLFOAmount(it)
                    }
                )
            }
            }
        }
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
fun ParamSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueDisplay: String = String.format("%.2f", value)
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueDisplay,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            valueRange = 0f..1f
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
            .height(120.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        notes.forEachIndexed { index, note ->
            var isPressed by remember { mutableStateOf(false) }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .background(
                        color = if (isPressed) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .pointerInput(note) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                onNoteOn(note)
                                tryAwaitRelease()
                                isPressed = false
                                onNoteOff(note)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = noteNames[index],
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPressed)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
