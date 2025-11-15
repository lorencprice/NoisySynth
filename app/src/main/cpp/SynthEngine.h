#ifndef NOISYSYNTH_SYNTHENGINE_H
#define NOISYSYNTH_SYNTHENGINE_H

#include <oboe/Oboe.h>
#include <vector>
#include <memory>
#include <cmath>

constexpr int kMaxVoices = 8;
constexpr float kSampleRate = 48000.0f;
constexpr float kPI = 3.14159265358979323846f;

enum class Waveform {
    SINE = 0,
    SAWTOOTH = 1,
    SQUARE = 2,
    TRIANGLE = 3
};

/**
 * ADSR Envelope Generator
 */
class Envelope {
public:
    Envelope() : attack_(0.01f), decay_(0.1f), sustain_(0.7f), release_(0.3f),
                 phase_(Phase::IDLE), level_(0.0f), time_(0.0f) {}
    
    void setAttack(float attack) { attack_ = std::max(0.001f, attack); }
    void setDecay(float decay) { decay_ = std::max(0.001f, decay); }
    void setSustain(float sustain) { sustain_ = std::max(0.0f, std::min(1.0f, sustain)); }
    void setRelease(float release) { release_ = std::max(0.001f, release); }
    
    void noteOn() {
        phase_ = Phase::ATTACK;
        time_ = 0.0f;
    }
    
    void noteOff() {
        if (phase_ != Phase::IDLE && phase_ != Phase::RELEASE) {
            phase_ = Phase::RELEASE;
            time_ = 0.0f;
        }
    }
    
    float process(float sampleRate) {
        float dt = 1.0f / sampleRate;
        time_ += dt;
        
        switch (phase_) {
            case Phase::ATTACK:
                level_ = time_ / attack_;
                if (time_ >= attack_) {
                    phase_ = Phase::DECAY;
                    time_ = 0.0f;
                    level_ = 1.0f;
                }
                break;
                
            case Phase::DECAY:
                level_ = 1.0f - (1.0f - sustain_) * (time_ / decay_);
                if (time_ >= decay_) {
                    phase_ = Phase::SUSTAIN;
                    level_ = sustain_;
                }
                break;
                
            case Phase::SUSTAIN:
                level_ = sustain_;
                break;
                
            case Phase::RELEASE:
                level_ = sustain_ * (1.0f - time_ / release_);
                if (time_ >= release_ || level_ <= 0.0001f) {
                    phase_ = Phase::IDLE;
                    level_ = 0.0f;
                }
                break;
                
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
        
     
        // Map resonance to Q (quality factor) exponentially for a smoother,
        // ear-friendly progression. Q values below ~0.7 flatten the peak;
        // higher values create a sharper resonance.
        constexpr float qMin = 0.707f;   // Butterworth-ish (no peak, flat)
        constexpr float qMax = 12.0f;    // Strong but controlled resonance
        float q = qMin * std::pow(qMax / qMin, resonance_);
        
        // For SVF, damping = 1/Q
        // High Q = low damping = high resonance
        // Low Q = high damping = low resonance
        float damp = 1.0f / q;
        damp = std::max(0.05f, std::min(1.4f, damp)); // Safety clamp
        
        // State variable filter equations
        lowpass_ += f * bandpass_;
        highpass_ = input - lowpass_ - (damp * bandpass_);
        bandpass_ += f * highpass_;
        
        return lowpass_;
    }
    
    void reset() { 
        lowpass_ = 0.0f;
        bandpass_ = 0.0f;
        highpass_ = 0.0f;
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
 * Single voice of the synthesizer
 */
class Voice {
public:
    Voice() : phase_(0.0f), frequency_(0.0f), active_(false), midiNote_(-1),
              waveform_(Waveform::SAWTOOTH) {}
    
    void noteOn(int midiNote, Waveform waveform) {
        midiNote_ = midiNote;
        frequency_ = midiNoteToFrequency(midiNote);
        waveform_ = waveform;
        active_ = true;
        ampEnvelope_.noteOn();
        filterEnvelope_.noteOn();
        filter_.reset();
        phase_ = 0.0f; // Reset phase on note on
    }
    
    void noteOff() {
        active_ = false; // Mark as inactive immediately
        ampEnvelope_.noteOff();
        filterEnvelope_.noteOff();
    }
    
    float process(float sampleRate, float lfoValue) {
        if (!ampEnvelope_.isActive() && !filterEnvelope_.isActive()) {
            midiNote_ = -1; // Clear note assignment
            return 0.0f;
        }
        
        // Generate waveform
        float sample = generateWaveform();
        
        // Advance phase
        phase_ += frequency_ / sampleRate;
        if (phase_ >= 1.0f) {
            phase_ -= 1.0f;
        }
        
        // Get envelope values
        float ampEnvValue = ampEnvelope_.process(sampleRate);
        float filterEnvValue = filterEnvelope_.process(sampleRate);
        
        // Combine LFO and filter envelope for filter modulation
        // Filter envelope gives upward modulation, LFO gives bipolar modulation
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
        float t = phase_;
        
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
    float frequency_;
    bool active_;
    int midiNote_;
    Waveform waveform_;
    Envelope ampEnvelope_;
    Envelope filterEnvelope_;
    Filter filter_;
    float filterEnvAmount_ = 0.5f; // Default filter envelope amount
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
    
private:
    Voice* findFreeVoice();
    Voice* findVoiceForNote(int midiNote);
    
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
};

#endif // NOISYSYNTH_SYNTHENGINE_H
