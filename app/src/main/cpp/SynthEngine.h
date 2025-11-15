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
    
    void setAttack(float attack) { attack_ = attack; }
    void setDecay(float decay) { decay_ = decay; }
    void setSustain(float sustain) { sustain_ = sustain; }
    void setRelease(float release) { release_ = release; }
    
    void noteOn() {
        phase_ = Phase::ATTACK;
        time_ = 0.0f;
    }
    
    void noteOff() {
        if (phase_ != Phase::IDLE) {
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
                if (time_ >= release_ || level_ <= 0.0f) {
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
 * Low-pass filter (simple one-pole)
 */
class Filter {
public:
    Filter() : cutoff_(1.0f), resonance_(0.0f), buffer_(0.0f) {}
    
    void setCutoff(float cutoff) { 
        cutoff_ = std::max(0.01f, std::min(1.0f, cutoff)); 
    }
    
    void setResonance(float resonance) { 
        resonance_ = std::max(0.0f, std::min(0.99f, resonance)); 
    }
    
    float process(float input) {
        // Simple one-pole lowpass filter
        float coefficient = cutoff_;
        buffer_ += coefficient * (input - buffer_);
        
        // Add resonance (feedback)
        float output = buffer_ + resonance_ * buffer_;
        
        return output;
    }
    
    void reset() { buffer_ = 0.0f; }
    
private:
    float cutoff_;
    float resonance_;
    float buffer_;
};

/**
 * LFO (Low Frequency Oscillator)
 */
class LFO {
public:
    LFO() : phase_(0.0f), rate_(2.0f), amount_(0.0f) {}
    
    void setRate(float rate) { rate_ = rate; }
    void setAmount(float amount) { amount_ = amount; }
    
    float process(float sampleRate) {
        float output = std::sin(2.0f * kPI * phase_);
        phase_ += rate_ / sampleRate;
        if (phase_ >= 1.0f) {
            phase_ -= 1.0f;
        }
        return output * amount_;
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
    Voice() : phase_(0.0f), frequency_(0.0f), active_(false), midiNote_(0),
              waveform_(Waveform::SAWTOOTH) {}
    
    void noteOn(int midiNote, Waveform waveform) {
        midiNote_ = midiNote;
        frequency_ = midiNoteToFrequency(midiNote);
        waveform_ = waveform;
        active_ = true;
        envelope_.noteOn();
        filter_.reset();
    }
    
    void noteOff() {
        envelope_.noteOff();
    }
    
    float process(float sampleRate, float lfoValue) {
        if (!active_ && !envelope_.isActive()) {
            return 0.0f;
        }
        
        // Generate waveform
        float sample = generateWaveform();
        
        // Advance phase
        phase_ += frequency_ / sampleRate;
        if (phase_ >= 1.0f) {
            phase_ -= 1.0f;
        }
        
        // Apply envelope
        float envValue = envelope_.process(sampleRate);
        sample *= envValue;
        
        // Apply filter with LFO modulation
        // Note: We'll apply the LFO modulation in the main process loop
        // For now, just process with current filter settings
        sample = filter_.process(sample);
        
        // Check if voice should be deactivated
        if (!envelope_.isActive()) {
            active_ = false;
        }
        
        return sample;
    }
    
    bool isActive() const { return active_; }
    int getMidiNote() const { return midiNote_; }
    
    Envelope& getEnvelope() { return envelope_; }
    Filter& getFilter() { return filter_; }
    
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
    Envelope envelope_;
    Filter filter_;
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
    LFO lfo_;
};

#endif // NOISYSYNTH_SYNTHENGINE_H
