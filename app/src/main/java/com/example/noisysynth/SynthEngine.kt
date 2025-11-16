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
    private external fun native_setFilterAttack(engineHandle: Long, attack: Float)
    private external fun native_setFilterDecay(engineHandle: Long, decay: Float)
    private external fun native_setFilterSustain(engineHandle: Long, sustain: Float)
    private external fun native_setFilterRelease(engineHandle: Long, release: Float)
    private external fun native_setFilterEnvelopeAmount(engineHandle: Long, amount: Float)
    private external fun native_setLFORate(engineHandle: Long, rate: Float)
    private external fun native_setLFOAmount(engineHandle: Long, amount: Float)
    private external fun native_setDelayEnabled(engineHandle: Long, enabled: Boolean)
    private external fun native_setDelayTime(engineHandle: Long, time: Float)
    private external fun native_setDelayFeedback(engineHandle: Long, feedback: Float)
    private external fun native_setDelayMix(engineHandle: Long, mix: Float)
    private external fun native_setChorusEnabled(engineHandle: Long, enabled: Boolean)
    private external fun native_setChorusRate(engineHandle: Long, rate: Float)
    private external fun native_setChorusDepth(engineHandle: Long, depth: Float)
    private external fun native_setChorusMix(engineHandle: Long, mix: Float)
    private external fun native_setReverbEnabled(engineHandle: Long, enabled: Boolean)
    private external fun native_setReverbSize(engineHandle: Long, size: Float)
    private external fun native_setReverbDamping(engineHandle: Long, damping: Float)
    private external fun native_setReverbMix(engineHandle: Long, mix: Float)
    private external fun native_setArpeggiatorEnabled(engineHandle: Long, enabled: Boolean)
    private external fun native_setArpeggiatorPattern(engineHandle: Long, pattern: Int)
    private external fun native_setArpeggiatorRate(engineHandle: Long, bpm: Float)
    private external fun native_setArpeggiatorGate(engineHandle: Long, gate: Float)
    private external fun native_setSequencerEnabled(engineHandle: Long, enabled: Boolean)
    private external fun native_setSequencerTempo(engineHandle: Long, bpm: Float)
    private external fun native_setSequencerStepLength(engineHandle: Long, stepLength: Int)
    private external fun native_setSequencerMeasures(engineHandle: Long, measures: Int)
    private external fun native_setSequencerStep(engineHandle: Long, index: Int, midiNote: Int, active: Boolean)
    
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
    
    fun setFilterAttack(attack: Float) {
        native_setFilterAttack(engineHandle, attack)
    }
    
    fun setFilterDecay(decay: Float) {
        native_setFilterDecay(engineHandle, decay)
    }
    
    fun setFilterSustain(sustain: Float) {
        native_setFilterSustain(engineHandle, sustain)
    }
    
    fun setFilterRelease(release: Float) {
        native_setFilterRelease(engineHandle, release)
    }
    
    fun setFilterEnvelopeAmount(amount: Float) {
        native_setFilterEnvelopeAmount(engineHandle, amount)
    }
    
    fun setLFORate(rate: Float) {
        native_setLFORate(engineHandle, rate)
    }
    
    fun setLFOAmount(amount: Float) {
        native_setLFOAmount(engineHandle, amount)
    }

    
    fun setDelayEnabled(enabled: Boolean) {
        native_setDelayEnabled(engineHandle, enabled)
    }

    fun setDelayTime(time: Float) {
        native_setDelayTime(engineHandle, time)
    }

    fun setDelayFeedback(feedback: Float) {
        native_setDelayFeedback(engineHandle, feedback)
    }

    fun setDelayMix(mix: Float) {
        native_setDelayMix(engineHandle, mix)
    }

    fun setChorusEnabled(enabled: Boolean) {
        native_setChorusEnabled(engineHandle, enabled)
    }

    fun setChorusRate(rate: Float) {
        native_setChorusRate(engineHandle, rate)
    }

    fun setChorusDepth(depth: Float) {
        native_setChorusDepth(engineHandle, depth)
    }

    fun setChorusMix(mix: Float) {
        native_setChorusMix(engineHandle, mix)
    }

    fun setReverbEnabled(enabled: Boolean) {
        native_setReverbEnabled(engineHandle, enabled)
    }

    fun setReverbSize(size: Float) {
        native_setReverbSize(engineHandle, size)
    }

    fun setReverbDamping(damping: Float) {
        native_setReverbDamping(engineHandle, damping)
    }

    fun setReverbMix(mix: Float) {
        native_setReverbMix(engineHandle, mix)
    }
    
    fun setArpeggiatorEnabled(enabled: Boolean) {
        native_setArpeggiatorEnabled(engineHandle, enabled)
    }

    fun setArpeggiatorPattern(pattern: Int) {
        native_setArpeggiatorPattern(engineHandle, pattern)
    }

    fun setArpeggiatorRate(bpm: Float) {
        native_setArpeggiatorRate(engineHandle, bpm)
    }

    fun setArpeggiatorGate(gate: Float) {
        native_setArpeggiatorGate(engineHandle, gate)
    }

    fun setSequencerEnabled(enabled: Boolean) {
        native_setSequencerEnabled(engineHandle, enabled)
    }

    fun setSequencerTempo(bpm: Float) {
        native_setSequencerTempo(engineHandle, bpm)
    }

    fun setSequencerStepLength(stepLength: Int) {
        native_setSequencerStepLength(engineHandle, stepLength)
    }

    fun setSequencerMeasures(measures: Int) {
        native_setSequencerMeasures(engineHandle, measures)
    }

    fun setSequencerStep(index: Int, midiNote: Int, active: Boolean) {
        native_setSequencerStep(engineHandle, index, midiNote, active)
    }
    
    fun delete() {
        destroy(engineHandle)
    }
}
