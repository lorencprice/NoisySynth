#ifndef NOISYSYNTH_SYNTHENGINE_H
#define NOISYSYNTH_SYNTHENGINE_H

#include <oboe/Oboe.h>
#include <vector>
#include <memory>
#include <cmath>
#include <algorithm>

constexpr int kMaxVoices = 8;
constexpr float kSampleRate = 48000.0f;
constexpr float kPI = 3.14159265358979323846f;

enum class Waveform {
    SINE = 0,
    SAWTOOTH = 1,
    SQUARE = 2,
    TRIANGLE = 3
};

enum class SequencerStepLength {
    Eighth = 0,
    Quarter = 1,
    Half = 2,
    Whole = 3
};

/**
 * ADSR Envelope Generator
 */
class Envelope {
public:
    Envelope()
        : attack_(0.01f)
        , decay_(0.1f)
        , sustain_(0.7f)
        , release_(0.3f)
        , phase_(Phase::IDLE)
        , level_(0.0f)
        , time_(0.0f)
        , attackStartLevel_(0.0f)
        , releaseStartLevel_(0.0f)
    {}

    // Minimum times chosen to avoid zipper/clicks even at very short notes
    void setAttack(float attack)  { attack_  = std::max(0.0001f, attack); }   // >= 0.1 ms
    void setDecay(float decay)    { decay_   = std::max(0.0001f, decay); }
    void setSustain(float sustain){ sustain_ = std::max(0.0f, std::min(1.0f, sustain)); }
    void setRelease(float release){ release_ = std::max(0.005f,  release); }  // >= 5 ms

    void noteOn() {
        // Start a new attack from the CURRENT level to keep continuity
        attackStartLevel_ = level_;
        phase_ = Phase::ATTACK;
        time_ = 0.0f;
    }

    void noteOff() {
        if (phase_ != Phase::IDLE && phase_ != Phase::RELEASE) {
            // Release starts from the current level for smooth decay
            releaseStartLevel_ = level_;
            phase_ = Phase::RELEASE;
            time_ = 0.0f;
        }
    }

    float process(float sampleRate) {
        float dt = 1.0f / sampleRate;
        time_ += dt;

        switch (phase_) {
            case Phase::ATTACK:
            {
                float t = (attack_ > 0.0f) ? (time_ / attack_) : 1.0f;
                t = std::max(0.0f, std::min(1.0f, t));
                level_ = attackStartLevel_ + (1.0f - attackStartLevel_) * t;
                if (time_ >= attack_) {
                    phase_ = Phase::DECAY;
                    time_ = 0.0f;
                    level_ = 1.0f;
                }
                break;
            }

            case Phase::DECAY:
            {
                float t = (decay_ > 0.0f) ? (time_ / decay_) : 1.0f;
                t = std::max(0.0f, std::min(1.0f, t));
                // Exponential-ish decay from 1.0 down to sustain_
                level_ = 1.0f - (1.0f - sustain_) * t;
                if (time_ >= decay_) {
                    phase_ = Phase::SUSTAIN;
                    level_ = sustain_;
                }
                break;
            }

            case Phase::SUSTAIN:
                level_ = sustain_;
                break;

            case Phase::RELEASE:
            {
                float t = (release_ > 0.0f) ? (time_ / release_) : 1.0f;
                t = std::max(0.0f, std::min(1.0f, t));
                // Smooth decay from the level at noteOff down to 0
                level_ = releaseStartLevel_ * (1.0f - t);
                if (time_ >= release_ || level_ <= 0.0001f) {
                    phase_ = Phase::IDLE;
                    level_ = 0.0f;
                }
                break;
            }

            case Phase::IDLE:
                level_ = 0.0f;
                break;
        }

        return std::max(0.0f, std::min(1.0f, level_));
    }

    bool isActive() const { return phase_ != Phase::IDLE; }

private:
    enum class Phase {
        IDLE,
        ATTACK,
        DECAY,
        SUSTAIN,
        RELEASE
    };

    float attack_;
    float decay_;
    float sustain_;
    float release_;

    Phase phase_;
    float level_;
    float time_;

    // For click-free retriggers and releases
    float attackStartLevel_;
    float releaseStartLevel_;
};

/**
 * State Variable Filter (SVF) - Proper lowpass with resonance
 */
class Filter {
public:
    Filter() : cutoff_(0.5f), resonance_(0.0f), 
               lowpass_(0.0f), bandpass_(0.0f), highpass_(0.0f) {}
    
    void setCutoff(float cutoff) { 
        cutoff_ = std::max(0.0f, std::min(1.0f, cutoff)); 
    }
    
    void setResonance(float resonance) { 
        resonance_ = std::max(0.0f, std::min(1.0f, resonance)); 
    }
    
    float process(float input, float sampleRate, float modulation = 0.0f) {
        // Map cutoff (0-1) to frequency (20Hz - 12kHz) with exponential scaling
        float minFreq = 20.0f;
        float maxFreq = 12000.0f;
        
        // Apply modulation to cutoff
        float modulatedCutoff = std::max(0.0f, std::min(1.0f, cutoff_ + modulation));
        
        // Exponential mapping for more musical control
        float freq = minFreq * std::pow(maxFreq / minFreq, modulatedCutoff);
        
        // Calculate filter coefficient
        float f = 2.0f * std::sin(kPI * freq / sampleRate);
        f = std::min(f, 0.99f); // Clamp for stability
        
        // Map resonance to Q (quality factor) exponentially
        constexpr float qMin = 0.707f;
        constexpr float qMax = 12.0f;
        float q = qMin * std::pow(qMax / qMin, resonance_);
        
        // For SVF, damping = 1/Q
        float damp = 1.0f / q;
        damp = std::max(0.05f, std::min(1.4f, damp));
        
        // CRITICAL FIX: Flush denormal numbers to zero
        // Denormals cause massive CPU spikes and crackling/distortion
        auto flushDenormal = [](float& value) {
            // If value is extremely small (denormal range), set to zero
            if (std::fabs(value) < 1.0e-15f) {
                value = 0.0f;
            }
        };
        
        flushDenormal(lowpass_);
        flushDenormal(bandpass_);
        flushDenormal(highpass_);
        
        // State variable filter equations
        lowpass_ += f * bandpass_;
        highpass_ = input - lowpass_ - (damp * bandpass_);
        bandpass_ += f * highpass_;
        
        // CRITICAL FIX: Clamp filter states to prevent instability
        // This prevents the filter from "exploding" with rapid note triggers
        const float maxState = 10.0f;
        lowpass_ = std::max(-maxState, std::min(maxState, lowpass_));
        bandpass_ = std::max(-maxState, std::min(maxState, bandpass_));
        highpass_ = std::max(-maxState, std::min(maxState, highpass_));
        
        // Flush denormals again after processing
        flushDenormal(lowpass_);
        flushDenormal(bandpass_);
        flushDenormal(highpass_);
        
        return lowpass_;
    }
    
    void reset() { 
        // Gentle reset - decay towards zero instead of hard zero
        // This prevents transients while clearing accumulated state
        lowpass_ *= 0.1f;
        bandpass_ *= 0.1f;
        highpass_ *= 0.1f;
    }
    
private:
    float cutoff_;
    float resonance_;
    float lowpass_;
    float bandpass_;
    float highpass_;
};

/**
 * LFO (Low Frequency Oscillator)
 */
class LFO {
public:
    LFO() : phase_(0.0f), rate_(2.0f), amount_(0.0f) {}
    
    void setRate(float rate) { rate_ = std::max(0.1f, rate); }
    void setAmount(float amount) { amount_ = std::max(0.0f, std::min(1.0f, amount)); }
    
    float process(float sampleRate) {
        float output = std::sin(2.0f * kPI * phase_);
        phase_ += rate_ / sampleRate;
        if (phase_ >= 1.0f) {
            phase_ -= 1.0f;
        }
        // Return bipolar output scaled by amount (-amount to +amount)
        return output * amount_ * 0.5f; // Scale down for filter modulation
    }
    
private:
    float phase_;
    float rate_;
    float amount_;
};

/**
 * Single voice of the synthesizer - SOLUTION 1: Smart Phase Transition
 */
class Voice {
public:
    Voice() : phase_(0.0f), phaseOffset_(0.0f), frequency_(0.0f), active_(false), midiNote_(-1),
              waveform_(Waveform::SAWTOOTH), clickSuppression_(0.0f), clickSuppressionSamples_(0),
              stopFadeoutSamples_(48) {}
    
    void noteOn(int midiNote, Waveform waveform) {
        // Store the current output value if the voice was recently active
        float previousValue = 0.0f;
        if (wasRecentlyActive_ && midiNote_ != midiNote) {
            // Calculate the current waveform value before changing frequency
            previousValue = generateWaveform();
        }
        
        midiNote_ = midiNote;
        frequency_ = midiNoteToFrequency(midiNote);
        waveform_ = waveform;
        active_ = true;
        ampEnvelope_.noteOn();
        filterEnvelope_.noteOn();
        
        // CRITICAL FIX: Reset filter on NEW notes only (not retriggered notes)
        // This prevents state accumulation in the arpeggiator
        if (midiNote_ != lastMidiNote_ || !wasRecentlyActive_) {
            filter_.reset();
        }
        
        // Smart phase handling for smooth transitions
        if (!wasRecentlyActive_) {
            // Fresh voice - start at zero phase
            phase_ = 0.0f;
            phaseOffset_ = 0.0f;
        } else if (lastMidiNote_ != midiNote) {
            // Frequency change - find best phase to minimize discontinuity
            // For sine/triangle, find phase that gives us the previous value
            if (waveform == Waveform::SINE) {
                // arcsin to find phase that produces previousValue
                float targetPhase = std::asin(std::max(-1.0f, std::min(1.0f, previousValue))) / (2.0f * kPI);
                if (targetPhase < 0) targetPhase += 1.0f;
                
                // Calculate offset needed
                phaseOffset_ = targetPhase - phase_;
                if (phaseOffset_ < 0) phaseOffset_ += 1.0f;
            } else if (waveform == Waveform::TRIANGLE) {
                // For triangle, find appropriate phase
                // Triangle formula: t < 0.5 ? (4*t - 1) : (3 - 4*t)
                float targetPhase = 0.0f;
                if (previousValue < 0) {
                    // First half of triangle
                    targetPhase = (previousValue + 1.0f) / 4.0f;
                } else {
                    // Second half of triangle
                    targetPhase = (3.0f - previousValue) / 4.0f;
                }
                phaseOffset_ = targetPhase - phase_;
                if (phaseOffset_ < 0) phaseOffset_ += 1.0f;
            } else {
                // For other waveforms, reset to zero-crossing
                phase_ = 0.0f;
                phaseOffset_ = 0.0f;
            }
            
            // Apply longer click suppression for pitch changes
            clickSuppressionSamples_ = 480; // 10ms for pitch changes
        } else {
            // Same note retrigger - shorter fade
            clickSuppressionSamples_ = 240;
        }
        
        clickSuppression_ = 0.0f;
        lastMidiNote_ = midiNote;
        wasRecentlyActive_ = true;
    }
    
    void noteOff() {
        active_ = false; // Mark as inactive immediately
        ampEnvelope_.noteOff();
        filterEnvelope_.noteOff();
    }
    
    float process(float sampleRate, float lfoValue) {
        // Check if envelopes are done
        bool envelopesActive = ampEnvelope_.isActive() || filterEnvelope_.isActive();
        
        if (!envelopesActive) {
            // CRITICAL FIX: Don't immediately return 0.0!
            // Add a very short fade-out (48 samples = 1ms at 48kHz)
            if (stopFadeoutSamples_ > 0) {
                // Still fading out
                stopFadeoutSamples_--;
            } else {
                // Completely done
                midiNote_ = -1;
                wasRecentlyActive_ = false;
                phaseOffset_ = 0.0f;  // Reset phase offset when voice becomes idle
                return 0.0f;
            }
        } else if (stopFadeoutSamples_ == 0) {
            // Reset fade-out counter when envelopes are active
            stopFadeoutSamples_ = 48; // 1ms fade-out
        }
        
        // Generate waveform
        float sample = generateWaveform();
        
        // Advance phase
        phase_ += frequency_ / sampleRate;
        if (phase_ >= 1.0f) {
            phase_ -= 1.0f;
        }
        
        // Apply click suppression fade-in if needed
        if (clickSuppressionSamples_ > 0) {
            float totalSamples = (clickSuppressionSamples_ > 300) ? 480.0f : 240.0f;
            clickSuppression_ = 1.0f - (clickSuppressionSamples_ / totalSamples);
            
            // Use cosine curve for smoother fade
            float fadeFactor = 0.5f * (1.0f - std::cos(kPI * clickSuppression_));
            sample *= fadeFactor;
            
            clickSuppressionSamples_--;
        }
        
        // Apply fade-out when voice is stopping
        if (!envelopesActive && stopFadeoutSamples_ > 0) {
            float fadeout = stopFadeoutSamples_ / 48.0f;
            sample *= fadeout;
        }
        
        // Get envelope values
        float ampEnvValue = ampEnvelope_.process(sampleRate);
        float filterEnvValue = filterEnvelope_.process(sampleRate);
        
        // Combine LFO and filter envelope for filter modulation
        float filterMod = (filterEnvValue * filterEnvAmount_) + lfoValue;
        
        // Apply filter with modulation
        sample = filter_.process(sample, sampleRate, filterMod);
        
        // Apply amplitude envelope
        sample *= ampEnvValue;
        
        return sample;
    }
    
    bool isActive() const { return active_; }
    bool isNoteActive() const { return ampEnvelope_.isActive(); }
    int getMidiNote() const { return midiNote_; }
    
    Envelope& getAmpEnvelope() { return ampEnvelope_; }
    Envelope& getFilterEnvelope() { return filterEnvelope_; }
    Filter& getFilter() { return filter_; }
    
    void setFilterEnvelopeAmount(float amount) {
        filterEnvAmount_ = std::max(0.0f, std::min(1.0f, amount));
    }
    
private:
    float generateWaveform() {
        float t = phase_ + phaseOffset_;
        while (t >= 1.0f) t -= 1.0f;
        while (t < 0.0f) t += 1.0f;
        
        switch (waveform_) {
            case Waveform::SINE:
                return std::sin(2.0f * kPI * t);
                
            case Waveform::SAWTOOTH:
                return 2.0f * t - 1.0f;
                
            case Waveform::SQUARE:
                return (t < 0.5f) ? 1.0f : -1.0f;
                
            case Waveform::TRIANGLE:
                return (t < 0.5f) ? (4.0f * t - 1.0f) : (3.0f - 4.0f * t);
                
            default:
                return 0.0f;
        }
    }
    
    static float midiNoteToFrequency(int midiNote) {
        return 440.0f * std::pow(2.0f, (midiNote - 69) / 12.0f);
    }
    
    float phase_;
    float phaseOffset_;  // Added for smooth phase transitions
    float frequency_;
    bool active_;
    int midiNote_;
    Waveform waveform_;
    Envelope ampEnvelope_;
    Envelope filterEnvelope_;
    Filter filter_;
    float filterEnvAmount_ = 0.5f; // Default filter envelope amount
    
    // Click suppression
    int lastMidiNote_ = -1;
    bool wasRecentlyActive_ = false;
    float clickSuppression_ = 0.0f;
    int clickSuppressionSamples_ = 0;
    int stopFadeoutSamples_ = 48;
};

/**
 * Main synthesizer engine using Oboe for audio output
 */
class SynthEngine : public oboe::AudioStreamDataCallback {
public:
    SynthEngine();
    ~SynthEngine();
    
    // Audio callback
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) override;
    
    // Control methods
    void noteOn(int midiNote);
    void noteOff(int midiNote);
    void setWaveform(int waveform);
    void setFilterCutoff(float cutoff);
    void setFilterResonance(float resonance);
    void setAttack(float attack);
    void setDecay(float decay);
    void setSustain(float sustain);
    void setRelease(float release);
    void setFilterAttack(float attack);
    void setFilterDecay(float decay);
    void setFilterSustain(float sustain);
    void setFilterRelease(float release);
    void setFilterEnvelopeAmount(float amount);
    void setLFORate(float rate);
    void setLFOAmount(float amount);
     void setDelayEnabled(bool enabled);
    void setDelayTime(float time);
    void setDelayFeedback(float feedback);
    void setDelayMix(float mix);
    void setChorusEnabled(bool enabled);
    void setChorusRate(float rate);
    void setChorusDepth(float depth);
    void setChorusMix(float mix);
    void setReverbEnabled(bool enabled);
    void setReverbSize(float size);
    void setReverbDamping(float damping);
    void setReverbMix(float mix);

    void setArpeggiatorEnabled(bool enabled);
    void setArpeggiatorPattern(int pattern);
    void setArpeggiatorRate(float bpm);
    void setArpeggiatorGate(float gate);
    void setArpeggiatorSubdivision(int subdivision);

    void setSequencerEnabled(bool enabled);
    void setSequencerTempo(float bpm);
    void setSequencerStepLength(int stepLength);
    void setSequencerMeasures(int measures);
    void setSequencerStep(int index, int midiNote, bool active);


private:
    Voice* findFreeVoice();
    Voice* findVoiceForNote(int midiNote);

    float processDelay(float input, float sampleRate);
    float processChorus(float input, float sampleRate);
    float processReverb(float input, float sampleRate);
    void initializeEffects(float sampleRate);
    void processArpeggiator(float sampleRate, int32_t numFrames);  // FIXED: Now takes numFrames
    void processSequencer(float sampleRate, int32_t numFrames);     // FIXED: Now takes numFrames
    void configureSequenceLength();
    int getStepsPerMeasure() const;

    struct CombFilter {
        std::vector<float> buffer;
        size_t index = 0;
        float filterStore = 0.0f;
    };

    struct AllpassFilter {
        std::vector<float> buffer;
        size_t index = 0;
    };
    
    std::shared_ptr<oboe::AudioStream> stream_;
    std::vector<Voice> voices_;
    Waveform currentWaveform_;
    float filterCutoff_;
    float filterResonance_;
    float attack_;
    float decay_;
    float sustain_;
    float release_;
    float filterAttack_;
    float filterDecay_;
    float filterSustain_;
    float filterRelease_;
    float filterEnvAmount_;
    LFO lfo_;
    bool delayEnabled_;
    float delayTime_;
    float delayFeedback_;
    float delayMix_;
    std::vector<float> delayBuffer_;
    size_t delayBufferSize_ = 0;
    size_t delayWriteIndex_ = 0;
    bool chorusEnabled_;
    float chorusRate_;
    float chorusDepth_;
    float chorusMix_;
    std::vector<float> chorusBuffer_;
    size_t chorusBufferSize_ = 0;
    size_t chorusWriteIndex_ = 0;
    float chorusPhase1_ = 0.0f;
    float chorusPhase2_ = 0.25f; // Offset second voice
    bool reverbEnabled_;
    float reverbSize_;
    float reverbDamping_;
    float reverbMix_;
    std::vector<CombFilter> reverbCombs_;
    std::vector<AllpassFilter> reverbAllpasses_;

    bool arpeggiatorEnabled_ = false;
    int arpeggiatorPattern_ = 0;
    float arpeggiatorRateBpm_ = 120.0f;
    float arpeggiatorGate_ = 0.5f;
    float arpeggiatorStepMultiplier_ = 1.0f;
    std::vector<int> heldNotes_;
    float arpSampleCounter_ = 0.0f;
    int arpIndex_ = 0;
    int currentArpNote_ = -1;
    bool arpNoteActive_ = false;
    bool arpStepStarted_ = false;

    bool sequencerEnabled_ = false;
    float sequencerTempoBpm_ = 120.0f;
    SequencerStepLength sequencerStepLength_ = SequencerStepLength::Eighth;
    int sequencerMeasures_ = 4;
    struct SequencerStep { int midiNote; bool active; };
    std::vector<SequencerStep> sequencerSteps_;
    float sequencerSampleCounter_ = 0.0f;
    int sequencerCurrentStep_ = 0;
    int sequencerActiveNote_ = -1;
    bool sequencerNoteActive_ = false;
    bool sequencerStepStarted_ = false;
    bool suppressArpCapture_ = false;

    // Output safety
    float outputGain_ = 0.65f;
};

#endif // NOISYSYNTH_SYNTHENGINE_H
