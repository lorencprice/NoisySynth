# NoisySynth - Android Synthesizer App

A basic subtractive synthesizer for Android featuring:
- **Oscillator** with 4 waveforms (Sine, Saw, Square, Triangle)
- **Filter** with cutoff and resonance controls
- **ADSR Envelope** for amplitude shaping
- **LFO** for modulation effects

Built with Kotlin, Jetpack Compose UI, and C++ audio engine using Google Oboe for low-latency audio.

## Features

### Sound Generation
- 4 classic waveforms
- 8-voice polyphony
- MIDI note-based control
- Low-latency audio output via Oboe

### Controls
- **Oscillator**: Waveform selection (Sine, Saw, Square, Triangle)
- **Filter**: Cutoff frequency and resonance
- **Envelope**: Attack, Decay, Sustain, Release (ADSR)
- **LFO**: Rate and modulation amount
- **Keyboard**: Simple one-octave touch keyboard

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26 (Android 8.0) or higher
- NDK 25.0 or later
- CMake 3.22.1

## Project Structure

```
NoisySynth/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── cpp/                  # C++ audio engine
│   │   │   │   ├── SynthEngine.h     # Synth engine header
│   │   │   │   ├── SynthEngine.cpp   # Synth engine implementation
│   │   │   │   ├── native-lib.cpp    # JNI bridge
│   │   │   │   └── CMakeLists.txt    # CMake build file
│   │   │   ├── java/com/example/noisysynth/
│   │   │   │   ├── MainActivity.kt   # Main UI
│   │   │   │   └── SynthEngine.kt    # Kotlin wrapper
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── ...
├── build.gradle
└── settings.gradle
```

## Building the Project

### Option 1: Using Android Studio (Recommended)

1. **Install Android Studio**
   - Download from https://developer.android.com/studio
   - Install Android SDK and NDK through SDK Manager

2. **Open the Project**
   ```bash
   File -> Open -> Select NoisySynth folder
   ```

3. **Sync Gradle**
   - Android Studio should automatically sync
   - If not, click "Sync Now" in the notification bar

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click Run (green play button) or press Shift+F10
   - Select your device

### Option 2: Command Line Build

1. **Install Prerequisites**
   ```bash
   # On Linux
   sudo apt-get install openjdk-17-jdk
   
   # On macOS
   brew install openjdk@17
   ```

2. **Set Environment Variables**
   ```bash
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
   ```

3. **Build APK**
   ```bash
   cd NoisySynth
   ./gradlew assembleDebug
   ```

4. **Install APK**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Architecture

### Audio Engine (C++)

The audio engine is written in C++ for performance:

- **SynthEngine**: Main synthesizer class
  - Manages Oboe audio stream
  - Handles voice allocation
  - Audio callback for real-time processing

- **Voice**: Individual synth voice
  - Waveform generation
  - Filter processing
  - Envelope application

- **Envelope**: ADSR envelope generator
  - Attack, Decay, Sustain, Release phases
  - Amplitude modulation

- **Filter**: One-pole lowpass filter
  - Cutoff frequency control
  - Resonance (feedback) control

- **LFO**: Low-frequency oscillator
  - Sine wave modulation
  - Adjustable rate and amount

### UI Layer (Kotlin + Compose)

- **MainActivity**: Entry point, lifecycle management
- **SynthUI**: Main Composable UI
- **RotaryKnob**: Custom knob control for parameters
- **SimpleKeyboard**: Touch-based keyboard

### Bridge (JNI)

- **native-lib.cpp**: JNI functions connecting Kotlin to C++
- **SynthEngine.kt**: Kotlin wrapper providing type-safe API

## Usage

### Playing Notes

Tap the keyboard keys (C, D, E, F, G, A, B, C) to trigger notes. Drag to slide between notes.

### Adjusting Parameters

**Oscillator**:
- Tap waveform buttons to change the oscillator shape
- Sine = pure tone
- Saw = bright, buzzy
- Square = hollow, digital
- Triangle = soft, mellow

**Filter**:
- Drag the Cutoff knob to adjust brightness
- Higher = brighter, Lower = darker
- Drag the Resonance knob to add emphasis at cutoff frequency

**Envelope**:
- **Attack**: How quickly note reaches full volume
- **Decay**: How quickly it drops to sustain level
- **Sustain**: Level held while key is pressed
- **Release**: How quickly sound fades after key release

**LFO**:
- **Rate**: Speed of modulation (Hz)
- **Amount**: Depth of modulation effect

## Customization

### Adding Noise/Texture

You can add noise to the oscillator. In `SynthEngine.h`, modify the `generateWaveform()` method:

```cpp
float generateWaveform() {
    float t = phase_;
    float sample = 0.0f;
    
    // Original waveform generation...
    // (existing code)
    
    // Add noise texture
    float noise = (rand() / (float)RAND_MAX) * 2.0f - 1.0f;
    sample = sample * 0.8f + noise * 0.2f;  // Mix 20% noise
    
    return sample;
}
```

### Changing Filter Type

The current filter is a simple one-pole lowpass. To implement a better filter:

```cpp
// In Filter class, replace process() with a state-variable filter
float process(float input) {
    // State-variable filter (SVF)
    float f = 2.0f * sin(PI * cutoff_ / sampleRate);
    float fb = resonance_;
    
    lowpass_ += f * bandpass_;
    highpass_ = input - lowpass_ - fb * bandpass_;
    bandpass_ += f * highpass_;
    
    return lowpass_;  // Or highpass_, bandpass_ for different modes
}
```

### Adding More Waveforms

In `SynthEngine.h`, add to the `Waveform` enum and `generateWaveform()`:

```cpp
enum class Waveform {
    SINE = 0,
    SAWTOOTH = 1,
    SQUARE = 2,
    TRIANGLE = 3,
    NOISE = 4  // New waveform
};

// In generateWaveform():
case Waveform::NOISE:
    return (rand() / (float)RAND_MAX) * 2.0f - 1.0f;
```

## Performance Tips

1. **Buffer Size**: Smaller = lower latency, but more CPU intensive
2. **Voice Count**: Reduce `kMaxVoices` if experiencing audio glitches
3. **Sample Rate**: 44100 Hz is sufficient for most cases
4. **Filter Complexity**: Simple filters = better performance

## Troubleshooting

### No Sound
- Check device volume
- Verify app has audio focus
- Check logcat for Oboe errors: `adb logcat | grep NoisySynth`

### Crackling/Glitches
- Increase buffer size in OboeStreamBuilder
- Reduce polyphony (kMaxVoices)
- Simplify filter algorithm

### Build Errors
- Ensure NDK is installed: Tools -> SDK Manager -> SDK Tools -> NDK
- Check CMake version matches requirement
- Clean and rebuild: Build -> Clean Project -> Rebuild Project

### JNI Errors
- Verify method signatures match between Kotlin and C++
- Check library name matches in System.loadLibrary()
- Ensure CMake builds successfully

## Next Steps

### Features to Add

1. **Effects**
   - Reverb
   - Delay
   - Distortion/Overdrive

2. **Modulation**
   - Multiple LFOs
   - Envelope → Filter modulation
   - Velocity sensitivity

3. **MIDI Support**
   - External MIDI controller input
   - MIDI learn for parameters

4. **Presets**
   - Save/load parameter sets
   - Built-in preset library

5. **Advanced Synthesis**
   - Multiple oscillators
   - FM synthesis
   - Wavetable synthesis

6. **Recording**
   - Record audio output
   - Export to WAV

## Resources

- **Oboe Documentation**: https://github.com/google/oboe
- **Android Audio**: https://developer.android.com/guide/topics/media/audio-app-overview
- **DSP Guide**: https://www.dspguide.com/
- **Jetpack Compose**: https://developer.android.com/jetpack/compose

## License

This is a learning/demonstration project. Feel free to use and modify as needed.

## Credits

Built with:
- Google Oboe for audio
- Jetpack Compose for UI
- Kotlin for app logic
- C++ for audio DSP
