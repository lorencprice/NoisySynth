# Technical Implementation Details

## State Variable Filter (SVF) Deep Dive

### Why SVF Instead of One-Pole?

The original one-pole filter had fundamental limitations:

**Original One-Pole:**
```cpp
buffer_ += coefficient * (input - buffer_);
output = buffer_ + resonance * buffer_;  // Wrong!
```

Problems:
1. Coefficient was 0-1 directly (no frequency scaling)
2. "Resonance" just added feedback - doesn't create a peak
3. No Q factor or proper frequency response
4. Audible only at very low cutoff values

**New SVF Implementation:**
```cpp
// Map cutoff to actual frequency (exponential)
float freq = 20.0f * pow(12000.0f / 20.0f, cutoff);

// Calculate filter coefficient
float f = 2.0f * sin(PI * freq / sampleRate);

// Map resonance to Q (damping factor)
float q = 1.0f - resonance * 0.95f;

// SVF state equations
lowpass += f * bandpass;
highpass = input - lowpass - q * bandpass;
bandpass += f * highpass;
```

### Frequency Mapping Explanation

**Exponential Scaling:**
```
freq = minFreq * (maxFreq / minFreq) ^ cutoff
freq = 20 * (12000 / 20) ^ cutoff
freq = 20 * 600 ^ cutoff
```

This gives musical distribution:
- 0% → 20 Hz (sub bass)
- 25% → ~110 Hz (low bass)
- 50% → ~490 Hz (mid range)
- 75% → ~2200 Hz (presence)
- 100% → 12000 Hz (air)

Linear mapping (old way) would have:
- 0-90% → barely audible changes
- 90-100% → all the action crammed in

### Resonance as Q Factor

Resonance controls damping in the filter:

```cpp
float q = 1.0f - resonance * 0.95f;
```

- High damping (q = 1.0) = no resonance, smooth response
- Low damping (q = 0.05) = ringing, self-oscillation
- 0.95 limit prevents instability

The Q factor appears in the bandpass feedback:
```cpp
highpass = input - lowpass - q * bandpass;
```

This creates the resonant peak at the cutoff frequency.

### Stability and Soft Clipping

At high resonance, filters can become unstable or produce extreme outputs:

```cpp
lowpass_ = tanh(lowpass_);
```

`tanh()` provides:
- Soft saturation instead of hard clipping
- Keeps output in [-1, 1] range
- Adds subtle warmth
- Prevents runaway feedback

## Modulation Architecture

### Signal Flow

```
Oscillator → Filter → Amplitude Envelope → Output
              ↑
              |
         Modulation Sources:
         - Base Cutoff (slider)
         - Filter Envelope
         - LFO
```

### Modulation Calculation

In `Voice::process()`:

```cpp
// Get envelope values
float ampEnvValue = ampEnvelope_.process(sampleRate);
float filterEnvValue = filterEnvelope_.process(sampleRate);

// Combine modulation sources
float filterMod = (filterEnvValue * filterEnvAmount_) + lfoValue;

// Apply to filter
sample = filter_.process(sample, sampleRate, filterMod);
```

**Key Points:**
- Filter envelope is unipolar (0 to 1) - opens filter
- LFO is bipolar (-0.5 to +0.5) - sweeps around current position
- Both add to base cutoff value
- Total cutoff is clamped to [0, 1] in filter

### Filter Process with Modulation

```cpp
float Filter::process(float input, float sampleRate, float modulation) {
    // Apply modulation to cutoff
    float modulatedCutoff = clamp(cutoff_ + modulation, 0.0f, 1.0f);
    
    // Map to frequency
    float freq = 20.0f * pow(12000.0f / 20.0f, modulatedCutoff);
    
    // Calculate coefficient
    float f = 2.0f * sin(PI * freq / sampleRate);
    f = min(f, 1.0f);  // Stability
    
    // SVF equations...
}
```

## Note Management and Voice Allocation

### The Double-Tap Problem

**Original Code:**
```cpp
void noteOn(int midiNote) {
    Voice* voice = findFreeVoice();  // Might return same voice!
    voice->noteOn(midiNote, waveform);
    // If same note pressed twice, envelope in undefined state
}
```

**New Code:**
```cpp
void noteOn(int midiNote) {
    // Check if note already playing
    Voice* existingVoice = findVoiceForNote(midiNote);
    if (existingVoice) {
        // Retrigger existing voice
        existingVoice->noteOn(midiNote, waveform);
        return;
    }
    
    // Otherwise allocate new voice
    Voice* voice = findFreeVoice();
    voice->noteOn(midiNote, waveform);
}
```

### Voice States

Each voice tracks multiple states:

```cpp
class Voice {
    bool active_;        // Is note key currently held?
    int midiNote_;       // Which MIDI note (-1 = none)
    Envelope ampEnvelope_;
    Envelope filterEnvelope_;
};
```

**State Transitions:**

1. **Idle:** `active_ = false, midiNote_ = -1, envelopes inactive`
2. **Note On:** `active_ = true, midiNote_ = X, envelopes attack`
3. **Note Off:** `active_ = false, envelopes release`
4. **Releasing:** `active_ = false, envelopes still active`
5. **Back to Idle:** `envelopes finish, midiNote_ = -1`

### Voice Allocation Priority

```cpp
Voice* findFreeVoice() {
    // 1st priority: Completely idle voices
    for (auto& voice : voices_) {
        if (!voice.isNoteActive() && voice.getMidiNote() == -1) {
            return &voice;
        }
    }
    
    // 2nd priority: Releasing voices (envelope fading)
    for (auto& voice : voices_) {
        if (!voice.isActive()) {
            return &voice;
        }
    }
    
    // Last resort: Steal oldest voice
    return &voices_[0];
}
```

## Audio Mixing and Normalization

### Per-Voice Normalization

With multiple voices playing, simple addition causes clipping:

**Naive approach (causes clipping):**
```cpp
for (auto& voice : voices_) {
    sample += voice.process(...);
}
// If 8 voices play, sample could be 8.0!
```

**Better approach (sqrt normalization):**
```cpp
int activeVoices = 0;
for (auto& voice : voices_) {
    if (voice.isNoteActive()) {
        sample += voice.process(...);
        activeVoices++;
    }
}

if (activeVoices > 0) {
    sample /= sqrt(activeVoices);
}
```

**Why sqrt?**
- Linear division (÷ activeVoices) makes chords too quiet
- No division makes single notes correct, chords clip
- `sqrt(n)` is perceptual compromise:
  - 1 voice: ÷1 = full volume
  - 4 voices: ÷2 = 50% each
  - 8 voices: ÷2.83 = 35% each

### Final Limiting

After mixing:

```cpp
// Soft saturation
sample = tanh(sample * 0.7f);

// Hard limit (safety)
sample = clamp(sample, -1.0f, 1.0f);
```

`tanh(x * 0.7)` provides gentle compression:
- Small signals pass through unchanged
- Large signals smoothly approach ±1
- More musical than hard clipping

## Envelope Implementation

### State Machine

```cpp
enum class Phase {
    IDLE,      // Envelope off
    ATTACK,    // Rising to peak
    DECAY,     // Falling to sustain
    SUSTAIN,   // Holding at sustain level
    RELEASE    // Falling to zero
};
```

### Time-Based Processing

Each envelope phase is time-based:

```cpp
float Envelope::process(float sampleRate) {
    float dt = 1.0f / sampleRate;  // Time per sample
    time_ += dt;
    
    switch (phase_) {
        case Phase::ATTACK:
            level_ = time_ / attack_;  // Linear ramp
            if (time_ >= attack_) {
                phase_ = DECAY;
                time_ = 0.0f;
                level_ = 1.0f;
            }
            break;
        // ... other phases
    }
    
    return clamp(level_, 0.0f, 1.0f);
}
```

**Key Design Choices:**
- Linear ramps (not exponential) for simplicity
- Time in seconds (user-friendly)
- Clamping to [0, 1] range for safety
- State transitions when time thresholds met

### Envelope Safety Limits

```cpp
void setAttack(float attack) { 
    attack_ = max(0.001f, attack);  // Min 1ms
}
```

Prevents:
- Zero-length envelopes (divide by zero)
- Negative times (undefined behavior)
- Clicks from instant transitions

## LFO Implementation

### Simple Sine Oscillator

```cpp
float LFO::process(float sampleRate) {
    // Generate sine wave
    float output = sin(2.0f * PI * phase_);
    
    // Advance phase
    phase_ += rate_ / sampleRate;
    
    // Wrap phase to [0, 1)
    if (phase_ >= 1.0f) {
        phase_ -= 1.0f;
    }
    
    // Scale by amount and halve for filter range
    return output * amount_ * 0.5f;
}
```

**Why × 0.5?**
- Sine output is [-1, 1]
- Amount is [0, 1]
- Output is [-amount, +amount]
- Scaling by 0.5 gives [-0.5 × amount, +0.5 × amount]
- This prevents LFO from sweeping filter through full range (too extreme)

### Phase Accumulation

Using fractional phase (0 to 1):
- Clean, no drift
- Easy frequency control: `phase += freq / sampleRate`
- Simple wrapping with modulo or subtraction

## Performance Considerations

### Computational Cost

Per voice per sample:
1. Waveform generation: ~5 operations
2. SVF filter: ~15 operations (2 sins, multiplies, adds)
3. Two envelopes: ~20 operations
4. Total: ~40 operations/voice/sample

At 48kHz, 8 voices:
- 48000 × 8 × 40 = 15.36M operations/second
- Modern ARM cores handle this easily

### Optimization Opportunities (Future)

1. **Fast sin approximation** instead of `std::sin()`
2. **Table lookup** for waveforms
3. **SIMD** for parallel voice processing
4. **Filter coefficient caching** (only recalc on modulation change)

Currently prioritizing **correctness** over **maximum performance**.

## JNI Bridge Architecture

### Memory Management

```cpp
JNIEXPORT jlong JNICALL Java_..._create(JNIEnv *env, jobject thiz) {
    auto *engine = new SynthEngine();
    return reinterpret_cast<jlong>(engine);  // Pointer as long
}

JNIEXPORT void JNICALL Java_..._destroy(JNIEnv *env, jobject thiz, jlong handle) {
    auto *engine = reinterpret_cast<SynthEngine *>(handle);
    delete engine;  // Clean up
}
```

**Kotlin side:**
```kotlin
private val engineHandle: Long = create()  // Store pointer

fun delete() {
    destroy(engineHandle)  // Explicit cleanup
}
```

### Thread Safety

Oboe audio callback runs on **real-time audio thread**:
- High priority
- Must not block
- No allocations
- No locks

JNI calls from Kotlin happen on **UI thread**:
- Lower priority
- Can block
- Can allocate

**How we handle it:**
- All parameter changes are simple atomic writes (floats)
- No complex data structures shared
- Voice allocation happens in audio thread (fast)
- Parameters read without locks (float writes are atomic on ARM)

**Potential race:**
```cpp
// Audio thread reads
float cutoff = filterCutoff_;

// UI thread writes (simultaneously)
filterCutoff_ = newValue;
```

This is **safe** because:
- Float writes are atomic on ARM
- Stale value for one buffer is acceptable (48 samples = 1ms)
- Next buffer gets new value

---

## Testing and Validation

### Unit Test Ideas (Future)

1. **Filter frequency response**
   - Sweep cutoff, measure -3dB point
   - Should match expected frequency

2. **Resonance Q measurement**
   - Measure peak amplitude at cutoff
   - Should increase with resonance

3. **Envelope timing**
   - Measure attack time
   - Should match parameter ±10%

4. **Voice allocation**
   - Trigger same note twice
   - Should retrigger, not allocate new

### Debug Logging

Already included:
```cpp
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

LOGD("Note ON: %d", midiNote);
LOGD("Note RETRIGGER: %d", midiNote);
```

View logs:
```bash
adb logcat | grep NoisySynth
```

---

## Comparison: Before vs After

### Filter

| Aspect | Old One-Pole | New SVF |
|--------|-------------|---------|
| Type | One-pole lowpass | State Variable Filter |
| Cutoff range | 0-1 coefficient | 20 Hz - 12 kHz |
| Resonance | Fake (just feedback) | Real (Q factor) |
| Audibility | Only at low end | Full range |
| CPU cost | ~3 ops | ~15 ops |

### LFO

| Aspect | Before | After |
|--------|--------|-------|
| Generated | Yes | Yes |
| Routed | No | Yes (to filter) |
| Audible effect | None | Filter modulation |

### Voice Management

| Aspect | Before | After |
|--------|--------|-------|
| Double-tap | Stuck notes | Retriggers |
| State tracking | Incomplete | Complete (active + midiNote) |
| Voice stealing | Random | Prioritized (idle → releasing → oldest) |

---
