# NoisySynth Sound Design Guide

## Classic Synth Patches

### 1. TB-303 Style Acid Bass
*The classic squelchy acid sound*

**OSC Tab:**
- Waveform: **Sawtooth**

**FILTER Tab:**
- Cutoff: **35%**
- Resonance: **80%**

**AMP ENV Tab:**
- Attack: **0.01s**
- Decay: **0.15s**
- Sustain: **0%** (yes, zero!)
- Release: **0.05s**

**FILT ENV Tab:**
- Envelope Amount: **85%**
- Attack: **0.01s**
- Decay: **0.30s**
- Sustain: **10%**
- Release: **0.15s**

**LFO Tab:**
- Rate: **0 Hz** (off for this patch)
- Amount: **0%**

**Tip:** For more "squelch," increase resonance to 90-95%. For variation, adjust filter envelope decay time.

---

### 2. Dubstep Wobble Bass
*That iconic "wub-wub" sound*

**OSC Tab:**
- Waveform: **Sawtooth**

**FILTER Tab:**
- Cutoff: **25%** (start dark)
- Resonance: **85%**

**AMP ENV Tab:**
- Attack: **0.01s**
- Decay: **0.5s**
- Sustain: **80%**
- Release: **0.2s**

**FILT ENV Tab:**
- Envelope Amount: **0%** (let LFO handle it)
- Attack: **0.1s**
- Decay: **0.2s**
- Sustain: **50%**
- Release: **0.2s**

**LFO Tab:**
- Rate: **6 Hz** (adjust to taste: 4-8 Hz typical)
- Amount: **90%**

**Tip:** The LFO rate controls the "wobble speed." Try 4 Hz for quarter notes at 120 BPM, 8 Hz for eighth notes.

---

### 3. Warm Analog Pad
*Lush, evolving pad sound*

**OSC Tab:**
- Waveform: **Triangle** (or Sawtooth for brighter)

**FILTER Tab:**
- Cutoff: **45%**
- Resonance: **25%**

**AMP ENV Tab:**
- Attack: **0.8s** (slow)
- Decay: **0.6s**
- Sustain: **70%**
- Release: **1.2s**

**FILT ENV Tab:**
- Envelope Amount: **60%**
- Attack: **1.0s** (slow)
- Decay: **0.8s**
- Sustain: **60%**
- Release: **1.0s**

**LFO Tab:**
- Rate: **0.5 Hz** (very slow)
- Amount: **25%** (subtle movement)

**Tip:** The slow LFO adds gentle filter movement. Hold notes and listen to the evolution.

---

### 4. Pluck / Kalimba
*Short, percussive melodic sound*

**OSC Tab:**
- Waveform: **Triangle** (or Sine for mellower)

**FILTER Tab:**
- Cutoff: **55%**
- Resonance: **45%**

**AMP ENV Tab:**
- Attack: **0.01s**
- Decay: **0.35s**
- Sustain: **0%**
- Release: **0.15s**

**FILT ENV Tab:**
- Envelope Amount: **80%**
- Attack: **0.01s**
- Decay: **0.40s**
- Sustain: **5%**
- Release: **0.2s**

**LFO Tab:**
- Rate: **0 Hz**
- Amount: **0%**

**Tip:** The filter envelope creates the "pluck" character. Try increasing resonance to 60% for a more metallic tone.

---

### 5. Hoover / Rave Stab
*That classic 90s rave sound*

**OSC Tab:**
- Waveform: **Sawtooth**

**FILTER Tab:**
- Cutoff: **40%**
- Resonance: **70%**

**AMP ENV Tab:**
- Attack: **0.01s**
- Decay: **0.8s**
- Sustain: **40%**
- Release: **0.4s**

**FILT ENV Tab:**
- Envelope Amount: **75%**
- Attack: **0.05s**
- Decay: **0.6s**
- Sustain: **30%**
- Release: **0.5s**

**LFO Tab:**
- Rate: **5.5 Hz**
- Amount: **40%**

**Tip:** Play chords (hold multiple keys). The combination of LFO and resonance creates that characteristic "hover" texture.

---

### 6. Deep Sub Bass
*Low-end foundation*

**OSC Tab:**
- Waveform: **Sine**

**FILTER Tab:**
- Cutoff: **30%** (keep it dark)
- Resonance: **15%** (subtle)

**AMP ENV Tab:**
- Attack: **0.03s**
- Decay: **0.2s**
- Sustain: **90%**
- Release: **0.3s**

**FILT ENV Tab:**
- Envelope Amount: **30%**
- Attack: **0.05s**
- Decay: **0.3s**
- Sustain: **50%**
- Release: **0.2s**

**LFO Tab:**
- Rate: **0 Hz**
- Amount: **0%**

💡 **Tip:** The slight filter envelope adds a bit of "click" on the attack for definition. Keep resonance low to avoid muddy bass.

---

### 7. Laser Zap SFX
*Sci-fi sound effect*

**OSC Tab:**
- Waveform: **Square**

**FILTER Tab:**
- Cutoff: **70%** (start bright)
- Resonance: **90%** (high!)

**AMP ENV Tab:**
- Attack: **0.01s**
- Decay: **0.25s**
- Sustain: **0%**
- Release: **0.1s**

**FILT ENV Tab:**
- Envelope Amount: **95%**
- Attack: **0.01s**
- Decay: **0.30s** (slow fall)
- Sustain: **0%**
- Release: **0.1s**

**LFO Tab:**
- Rate: **0 Hz**
- Amount: **0%**

**Tip:** The filter sweeps from bright to dark with high resonance, creating the "zap" sound. Try different decay times for variety.

---

### 8. Noisy Texture
*Embracing imperfection*

**OSC Tab:**
- Waveform: **Square** or **Sawtooth**

**FILTER Tab:**
- Cutoff: **25-35%** (dark, gritty)
- Resonance: **65-85%** (adds character)

**AMP ENV Tab:**
- Attack: **0.1s**
- Decay: **0.4s**
- Sustain: **60%**
- Release: **0.5s**

**FILT ENV Tab:**
- Envelope Amount: **70%**
- Attack: **0.15s**
- Decay: **0.5s**
- Sustain: **40%**
- Release: **0.6s**

**LFO Tab:**
- Rate: **3.5 Hz** (not perfectly synced)
- Amount: **50%**

---

## Modulation Combinations

Understanding how filter modulation works:

**Final Filter Cutoff = Base Cutoff + (Filter Env × Amount) + LFO**

- **Base Cutoff:** Set by the FILTER tab slider
- **Filter Envelope:** Opens filter on note attack (upward modulation only)
- **LFO:** Oscillates around the current position (bipolar)

### Example Scenario:
- Base Cutoff: 30%
- Filter Env Amount: 50%, Envelope at peak (100%)
- LFO: 3 Hz, Amount 20%, currently at peak (+10%)

**Actual Cutoff = 30% + 50% + 10% = 90%** (filter is wide open at this moment)

Half a second later:
- Filter Envelope decayed to sustain (40%)
- LFO at trough (-10%)

**Actual Cutoff = 30% + 20% + (-10%) = 40%** (filter has closed)

This is how you get complex, evolving filter movement!

---

## Advanced Techniques

### Pseudo-FM via Filter Modulation
Use fast LFO (8-10 Hz) with high amount on a high resonance setting. The filter's self-resonance acts almost like an additional oscillator, creating FM-like timbres.

### Envelope Follower Emulation
Set filter envelope to mimic amp envelope but with different timings. Creates natural "talking" quality to sounds.

### Percussive Filter Pops
Very fast filter attack (0.01s) with high envelope amount creates a percussive "click" even on sustained waveforms.

---

## Troubleshooting Sounds

**"Filter doesn't seem to do much"**
- Increase resonance above 50%
- Try more extreme cutoff positions (below 30% or above 70%)
- Increase filter envelope amount above 60%

**"LFO effect is too subtle"**
- Increase LFO Amount above 50%
- Lower base cutoff to 20-40% range where sweeps are more obvious
- Try faster rates (5-8 Hz)

**"Sound is too harsh/digital"**
- Lower resonance below 60%
- Use Triangle or Sine waveform instead of Saw/Square
- Increase cutoff to let more highs through naturally

**"Notes sound mushy/unclear"**
- Shorten attack and decay times
- Lower sustain level
- Reduce filter envelope amount
- Lower LFO amount

---

