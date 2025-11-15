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
    var waveform by remember { mutableStateOf(0) }
    var filterCutoff by remember { mutableStateOf(0.5f) }
    var filterResonance by remember { mutableStateOf(0.3f) }
    var attack by remember { mutableStateOf(0.01f) }
    var decay by remember { mutableStateOf(0.1f) }
    var sustain by remember { mutableStateOf(0.7f) }
    var release by remember { mutableStateOf(0.3f) }
    var filterAttack by remember { mutableStateOf(0.01f) }
    var filterDecay by remember { mutableStateOf(0.2f) }
    var filterSustain by remember { mutableStateOf(0.5f) }
    var filterRelease by remember { mutableStateOf(0.3f) }
    var filterEnvAmount by remember { mutableStateOf(0.5f) }
    var lfoRate by remember { mutableStateOf(2.0f) }
    var lfoAmount by remember { mutableStateOf(0.0f) }
    
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fixed Header with Title and Keyboard
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "NOISY SYNTH",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Keyboard
                SimpleKeyboard(
                    onNoteOn = { note -> synthEngine.noteOn(note) },
                    onNoteOff = { note -> synthEngine.noteOff(note) }
                )
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("OSC", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("FILTER", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("AMP ENV", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("FILT ENV", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("LFO", fontWeight = FontWeight.Bold) }
            )
        }
        
        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> OscillatorTab(
                    waveform = waveform,
                    onWaveformChange = { 
                        waveform = it
                        synthEngine.setWaveform(it)
                    }
                )
                1 -> FilterTab(
                    cutoff = filterCutoff,
                    resonance = filterResonance,
                    onCutoffChange = { 
                        filterCutoff = it
                        synthEngine.setFilterCutoff(it)
                    },
                    onResonanceChange = { 
                        filterResonance = it
                        synthEngine.setFilterResonance(it)
                    }
                )
                2 -> EnvelopeTab(
                    attack = attack,
                    decay = decay,
                    sustain = sustain,
                    release = release,
                    onAttackChange = { 
                        attack = it
                        synthEngine.setAttack(it * 2.0f)
                    },
                    onDecayChange = { 
                        decay = it
                        synthEngine.setDecay(it * 2.0f)
                    },
                    onSustainChange = { 
                        sustain = it
                        synthEngine.setSustain(it)
                    },
                    onReleaseChange = { 
                        release = it
                        synthEngine.setRelease(it * 2.0f)
                    },
                    title = "AMPLITUDE ENVELOPE (ADSR)"
                )
                3 -> FilterEnvelopeTab(
                    attack = filterAttack,
                    decay = filterDecay,
                    sustain = filterSustain,
                    release = filterRelease,
                    amount = filterEnvAmount,
                    onAttackChange = {
                        filterAttack = it
                        synthEngine.setFilterAttack(it * 2.0f)
                    },
                    onDecayChange = {
                        filterDecay = it
                        synthEngine.setFilterDecay(it * 2.0f)
                    },
                    onSustainChange = {
                        filterSustain = it
                        synthEngine.setFilterSustain(it)
                    },
                    onReleaseChange = {
                        filterRelease = it
                        synthEngine.setFilterRelease(it * 2.0f)
                    },
                    onAmountChange = {
                        filterEnvAmount = it
                        synthEngine.setFilterEnvelopeAmount(it)
                    }
                )
                4 -> LFOTab(
                    rate = lfoRate,
                    amount = lfoAmount,
                    onRateChange = { 
                        lfoRate = it * 10.0f
                        synthEngine.setLFORate(lfoRate)
                    },
                    onAmountChange = { 
                        lfoAmount = it
                        synthEngine.setLFOAmount(it)
                    }
                )
            }
        }
    }
}

@Composable
fun OscillatorTab(
    waveform: Int,
    onWaveformChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WAVEFORM",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "Sine Wave" to 0,
                "Sawtooth" to 1,
                "Square Wave" to 2,
                "Triangle" to 3
            ).forEach { (name, index) ->
                Button(
                    onClick = { onWaveformChange(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (waveform == index) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (waveform == index) 6.dp else 2.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        name,
                        fontSize = 18.sp,
                        fontWeight = if (waveform == index) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

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

@Composable
fun EnvelopeTab(
    attack: Float,
    decay: Float,
    sustain: Float,
    release: Float,
    onAttackChange: (Float) -> Unit,
    onDecayChange: (Float) -> Unit,
    onSustainChange: (Float) -> Unit,
    onReleaseChange: (Float) -> Unit,
    title: String = "ENVELOPE (ADSR)"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        ParamSlider(
            label = "Attack",
            value = attack,
            onValueChange = onAttackChange,
            valueDisplay = String.format("%.2fs", attack * 2.0f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ParamSlider(
            label = "Decay",
            value = decay,
            onValueChange = onDecayChange,
            valueDisplay = String.format("%.2fs", decay * 2.0f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ParamSlider(
            label = "Sustain",
            value = sustain,
            onValueChange = onSustainChange,
            valueDisplay = String.format("%.0f%%", sustain * 100)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ParamSlider(
            label = "Release",
            value = release,
            onValueChange = onReleaseChange,
            valueDisplay = String.format("%.2fs", release * 2.0f)
        )
    }
}

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
            .padding(vertical = 20.dp),
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

@Composable
fun ParamSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueDisplay: String = String.format("%.2f", value)
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueDisplay,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun SimpleKeyboard(
    onNoteOn: (Int) -> Unit,
    onNoteOff: (Int) -> Unit
) {
    val notes = listOf(60, 62, 64, 65, 67, 69, 71, 72)
    val noteNames = listOf("C", "D", "E", "F", "G", "A", "B", "C")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        notes.forEachIndexed { index, note ->
            var isPressed by remember { mutableStateOf(false) }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isPressed) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = noteNames[index],
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPressed)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (isPressed) {
                        Text(
                            text = "●",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
