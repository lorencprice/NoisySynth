package com.example.noisysynth

/**
 * Kotlin wrapper for the native C++ synthesizer engine
 * This class interfaces with the Oboe-based audio engine written in C++
 */
class SynthEngine {
    
    init {
        System.loadLibrary("noisysynth")
    }
    
    // Native method declarations
    private external fun create(): Long
    private external fun destroy(engineHandle: Long)
    private external fun native_noteOn(engineHandle: Long, midiNote: Int)
    private external fun native_noteOff(engineHandle: Long, midiNote: Int)
    private external fun native_setWaveform(engineHandle: Long, waveform: Int)
    private external fun native_setFilterCutoff(engineHandle: Long, cutoff: Float)
    private external fun native_setFilterResonance(engineHandle: Long, resonance: Float)
    private external fun native_setAttack(engineHandle: Long, attack: Float)
    private external fun native_setDecay(engineHandle: Long, decay: Float)
    private external fun native_setSustain(engineHandle: Long, sustain: Float)
    private external fun native_setRelease(engineHandle: Long, release: Float)
    private external fun native_setLFORate(engineHandle: Long, rate: Float)
    private external fun native_setLFOAmount(engineHandle: Long, amount: Float)
    
    private val engineHandle: Long = create()
    
    fun noteOn(midiNote: Int) {
        native_noteOn(engineHandle, midiNote)
    }
    
    fun noteOff(midiNote: Int) {
        native_noteOff(engineHandle, midiNote)
    }
    
    fun setWaveform(waveform: Int) {
        native_setWaveform(engineHandle, waveform)
    }
    
    fun setFilterCutoff(cutoff: Float) {
        native_setFilterCutoff(engineHandle, cutoff)
    }
    
    fun setFilterResonance(resonance: Float) {
        native_setFilterResonance(engineHandle, resonance)
    }
    
    fun setAttack(attack: Float) {
        native_setAttack(engineHandle, attack)
    }
    
    fun setDecay(decay: Float) {
        native_setDecay(engineHandle, decay)
    }
    
    fun setSustain(sustain: Float) {
        native_setSustain(engineHandle, sustain)
    }
    
    fun setRelease(release: Float) {
        native_setRelease(engineHandle, release)
    }
    
    fun setLFORate(rate: Float) {
        native_setLFORate(engineHandle, rate)
    }
    
    fun setLFOAmount(amount: Float) {
        native_setLFOAmount(engineHandle, amount)
    }
    
    fun delete() {
        destroy(engineHandle)
    }
}
