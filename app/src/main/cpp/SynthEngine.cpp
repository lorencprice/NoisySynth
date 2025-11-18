#include "SynthEngine.h"
#include <android/log.h>
#include <cstdlib>

#define LOG_TAG "NoisySynth"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

SynthEngine::SynthEngine() 
    : currentWaveform_(Waveform::SAWTOOTH),
      filterCutoff_(0.5f),
      filterResonance_(0.3f),
      attack_(0.01f),
      decay_(0.1f),
      sustain_(0.7f),
      release_(0.3f),
      filterAttack_(0.01f),
      filterDecay_(0.2f),
      filterSustain_(0.5f),
      filterRelease_(0.3f),
      filterEnvAmount_(0.5f),
      delayEnabled_(false),
      delayTime_(0.35f),
      delayFeedback_(0.4f),
      delayMix_(0.3f),
      chorusEnabled_(false),
      chorusRate_(0.25f),
      chorusDepth_(0.3f),
      chorusMix_(0.25f),
      reverbEnabled_(false),
      reverbSize_(0.6f),
      reverbDamping_(0.35f),
      reverbMix_(0.4f),
      arpeggiatorEnabled_(false),
      arpeggiatorPattern_(0),
      arpeggiatorRateBpm_(120.0f),
      arpeggiatorGate_(0.5f),
      arpSampleCounter_(0.0f),
      arpIndex_(0),
      currentArpNote_(-1),
      arpNoteActive_(false),
      sequencerEnabled_(false),
      sequencerTempoBpm_(120.0f),
      sequencerStepLength_(SequencerStepLength::Eighth),
      sequencerMeasures_(4),
      sequencerSampleCounter_(0.0f),
      sequencerCurrentStep_(0),
      sequencerActiveNote_(-1),
      sequencerNoteActive_(false) {
    
    // Initialize voices
    voices_.resize(kMaxVoices);
    
    // Create audio stream
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output);
    builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    builder.setSharingMode(oboe::SharingMode::Exclusive);
    builder.setFormat(oboe::AudioFormat::Float);
    builder.setChannelCount(oboe::ChannelCount::Mono);
    builder.setSampleRate(kSampleRate);
    builder.setDataCallback(this);
    
    oboe::Result result = builder.openStream(stream_);
    if (result != oboe::Result::OK) {
        LOGE("Failed to create stream. Error: %s", oboe::convertToText(result));
        return;
    }

    
    initializeEffects(stream_->getSampleRate());
    configureSequenceLength();
        
    LOGD("Stream created: SR=%d, BufferSize=%d",
         stream_->getSampleRate(),
         stream_->getBufferSizeInFrames());
    
    // Start the stream
    result = stream_->requestStart();
    if (result != oboe::Result::OK) {
        LOGE("Failed to start stream. Error: %s", oboe::convertToText(result));
    }
}

SynthEngine::~SynthEngine() {
    if (stream_) {
        stream_->close();
    }
}

oboe::DataCallbackResult SynthEngine::onAudioReady(
    oboe::AudioStream *audioStream,
    void *audioData,
    int32_t numFrames) {
    
    float *outputBuffer = static_cast<float *>(audioData);
    float sampleRate = audioStream->getSampleRate();
    
    // Clear output buffer
    std::fill_n(outputBuffer, numFrames, 0.0f);
    
    // CRITICAL FIX: Process arpeggiator/sequencer ONCE per buffer, not per sample!
    // This prevents timing chaos and stuck notes
    processSequencer(sampleRate, numFrames);
    if (!sequencerEnabled_) {
        processArpeggiator(sampleRate, numFrames);
    }
    
    // Process each frame
    for (int32_t i = 0; i < numFrames; i++) {
        float sample = 0.0f;
 
        // Generate LFO value
        float lfoValue = lfo_.process(sampleRate);
        
        // Mix all active voices
        int activeVoices = 0;
        for (auto& voice : voices_) {
            if (voice.isActive()) {
                sample += voice.process(sampleRate, lfoValue);
                activeVoices++;
            }
        }
        
        // Polyphony-aware gain with smoothing (no sudden jumps)
        float targetPolyGain = 1.0f;
        if (activeVoices > 0) {
            targetPolyGain = 1.0f / std::sqrt(static_cast<float>(activeVoices));
        }
        
        // Simple one-pole smoothing
        const float smoothing = 0.001f;  // ~10–20 ms depending on buffer size
        polyGain_ += smoothing * (targetPolyGain - polyGain_);
        
        sample *= polyGain_;


        
        // Apply modulation effects
        sample = processChorus(sample, sampleRate);
        sample = processDelay(sample, sampleRate);
        sample = processReverb(sample, sampleRate);

        // Apply master headroom and gentle limiting
        sample *= outputGain_;
        const float limiterThreshold = 0.9f;
        float absSample = std::fabs(sample);
        if (absSample > limiterThreshold) {
            float excess = absSample - limiterThreshold;
            sample = (limiterThreshold + excess * 0.2f) * (sample < 0.0f ? -1.0f : 1.0f);
        }

        // Soft clipping / saturation
        sample = std::tanh(sample * 0.5f);
        
        // Final limiting
        sample = std::max(-1.0f, std::min(1.0f, sample));
        
        outputBuffer[i] = sample;
    }
    
    return oboe::DataCallbackResult::Continue;
}

void SynthEngine::noteOn(int midiNote) {
    if (arpeggiatorEnabled_ && !suppressArpCapture_) {
        if (std::find(heldNotes_.begin(), heldNotes_.end(), midiNote) == heldNotes_.end()) {
            heldNotes_.push_back(midiNote);
        }
        return;
    }

    // First check if this note is already playing
    Voice* existingVoice = findVoiceForNote(midiNote);
    if (existingVoice) {
        // Retrigger the existing voice
        existingVoice->noteOn(midiNote, currentWaveform_);
        existingVoice->getAmpEnvelope().setAttack(attack_);
        existingVoice->getAmpEnvelope().setDecay(decay_);
        existingVoice->getAmpEnvelope().setSustain(sustain_);
        existingVoice->getAmpEnvelope().setRelease(release_);
        existingVoice->getFilterEnvelope().setAttack(filterAttack_);
        existingVoice->getFilterEnvelope().setDecay(filterDecay_);
        existingVoice->getFilterEnvelope().setSustain(filterSustain_);
        existingVoice->getFilterEnvelope().setRelease(filterRelease_);
        existingVoice->setFilterEnvelopeAmount(filterEnvAmount_);
        existingVoice->getFilter().setCutoff(filterCutoff_);
        existingVoice->getFilter().setResonance(filterResonance_);
        LOGD("Note RETRIGGER: %d", midiNote);
        return;
    }
    
    // Find a free voice
    Voice* voice = findFreeVoice();
    if (voice) {
        voice->noteOn(midiNote, currentWaveform_);
        voice->getAmpEnvelope().setAttack(attack_);
        voice->getAmpEnvelope().setDecay(decay_);
        voice->getAmpEnvelope().setSustain(sustain_);
        voice->getAmpEnvelope().setRelease(release_);
        voice->getFilterEnvelope().setAttack(filterAttack_);
        voice->getFilterEnvelope().setDecay(filterDecay_);
        voice->getFilterEnvelope().setSustain(filterSustain_);
        voice->getFilterEnvelope().setRelease(filterRelease_);
        voice->setFilterEnvelopeAmount(filterEnvAmount_);
        voice->getFilter().setCutoff(filterCutoff_);
        voice->getFilter().setResonance(filterResonance_);
        LOGD("Note ON: %d", midiNote);
    } else {
        LOGD("No free voice for note: %d", midiNote);
    }
}

void SynthEngine::noteOff(int midiNote) {
    if (arpeggiatorEnabled_ && !suppressArpCapture_) {
        heldNotes_.erase(std::remove(heldNotes_.begin(), heldNotes_.end(), midiNote), heldNotes_.end());
        if (midiNote == currentArpNote_ && arpNoteActive_) {
            Voice* voice = findVoiceForNote(midiNote);
            if (voice) {
                voice->noteOff();
            }
            arpNoteActive_ = false;
            currentArpNote_ = -1;
        }
        return;
    }

    Voice* voice = findVoiceForNote(midiNote);
    if (voice) {
        voice->noteOff();
        LOGD("Note OFF: %d", midiNote);
    }
}

void SynthEngine::setWaveform(int waveform) {
    currentWaveform_ = static_cast<Waveform>(waveform);
    LOGD("Waveform: %d", waveform);
}

void SynthEngine::setFilterCutoff(float cutoff) {
    filterCutoff_ = cutoff;
    for (auto& voice : voices_) {
        voice.getFilter().setCutoff(cutoff);
    }
}

void SynthEngine::setFilterResonance(float resonance) {
    filterResonance_ = resonance;
    for (auto& voice : voices_) {
        voice.getFilter().setResonance(resonance);
    }
}

void SynthEngine::setAttack(float attack) {
    attack_ = attack;
    for (auto& voice : voices_) {
        voice.getAmpEnvelope().setAttack(attack);
    }
}

void SynthEngine::setDecay(float decay) {
    decay_ = decay;
    for (auto& voice : voices_) {
        voice.getAmpEnvelope().setDecay(decay);
    }
}

void SynthEngine::setSustain(float sustain) {
    sustain_ = sustain;
    for (auto& voice : voices_) {
        voice.getAmpEnvelope().setSustain(sustain);
    }
}

void SynthEngine::setRelease(float release) {
    release_ = release;
    for (auto& voice : voices_) {
        voice.getAmpEnvelope().setRelease(release);
    }
}

void SynthEngine::setFilterAttack(float attack) {
    filterAttack_ = attack;
    for (auto& voice : voices_) {
        voice.getFilterEnvelope().setAttack(attack);
    }
}

void SynthEngine::setFilterDecay(float decay) {
    filterDecay_ = decay;
    for (auto& voice : voices_) {
        voice.getFilterEnvelope().setDecay(decay);
    }
}

void SynthEngine::setFilterSustain(float sustain) {
    filterSustain_ = sustain;
    for (auto& voice : voices_) {
        voice.getFilterEnvelope().setSustain(sustain);
    }
}

void SynthEngine::setFilterRelease(float release) {
    filterRelease_ = release;
    for (auto& voice : voices_) {
        voice.getFilterEnvelope().setRelease(release);
    }
}

void SynthEngine::setFilterEnvelopeAmount(float amount) {
    filterEnvAmount_ = amount;
    for (auto& voice : voices_) {
        voice.setFilterEnvelopeAmount(amount);
    }
}

void SynthEngine::setLFORate(float rate) {
    lfo_.setRate(rate);
}

void SynthEngine::setLFOAmount(float amount) {
    lfo_.setAmount(amount);
}

void SynthEngine::setDelayEnabled(bool enabled) {
    delayEnabled_ = enabled;
}

void SynthEngine::setDelayTime(float time) {
    delayTime_ = std::max(0.0f, time);
}

void SynthEngine::setDelayFeedback(float feedback) {
    delayFeedback_ = std::max(0.0f, std::min(0.99f, feedback));
}

void SynthEngine::setDelayMix(float mix) {
    delayMix_ = std::max(0.0f, std::min(1.0f, mix));
}

void SynthEngine::setChorusEnabled(bool enabled) {
    chorusEnabled_ = enabled;
}

void SynthEngine::setChorusRate(float rate) {
    chorusRate_ = std::max(0.0f, rate);
}

void SynthEngine::setChorusDepth(float depth) {
    chorusDepth_ = std::max(0.0f, std::min(1.0f, depth));
}

void SynthEngine::setChorusMix(float mix) {
    chorusMix_ = std::max(0.0f, std::min(1.0f, mix));
}

void SynthEngine::setReverbEnabled(bool enabled) {
    reverbEnabled_ = enabled;
}

void SynthEngine::setReverbSize(float size) {
    reverbSize_ = std::max(0.0f, std::min(1.0f, size));
}

void SynthEngine::setReverbDamping(float damping) {
    reverbDamping_ = std::max(0.0f, std::min(1.0f, damping));
}

void SynthEngine::setReverbMix(float mix) {
    reverbMix_ = std::max(0.0f, std::min(1.0f, mix));
}

void SynthEngine::setArpeggiatorEnabled(bool enabled) {
    if (!enabled && arpeggiatorEnabled_ && arpNoteActive_) {
        // Turn off any active arp note when disabling
        suppressArpCapture_ = true;
        noteOff(currentArpNote_);
        suppressArpCapture_ = false;
    }

    arpeggiatorEnabled_ = enabled;

    if (!enabled) {
        // Reset arp state completely when disabling
        heldNotes_.clear();
        arpSampleCounter_ = 0.0f;
        arpIndex_ = 0;
        arpNoteActive_ = false;
        arpStepStarted_ = false;
        currentArpNote_ = -1;
    } else {
        // When enabling, start from a clean step boundary
        arpSampleCounter_ = 0.0f;
        arpIndex_ = 0;
        arpNoteActive_ = false;
        arpStepStarted_ = false;
        currentArpNote_ = -1;
    }
}


void SynthEngine::setArpeggiatorPattern(int pattern) {
    arpeggiatorPattern_ = std::max(0, std::min(3, pattern));
}

void SynthEngine::setArpeggiatorRate(float bpm) {
    arpeggiatorRateBpm_ = std::max(20.0f, bpm);
}

void SynthEngine::setArpeggiatorGate(float gate) {
    arpeggiatorGate_ = std::max(0.05f, std::min(1.0f, gate));
}

void SynthEngine::setArpeggiatorSubdivision(int subdivision) {
    int clamped = std::max(0, std::min(3, subdivision));
    switch (clamped) {
        case 0:
            arpeggiatorStepMultiplier_ = 2.0f;
            break;
        case 2:
            arpeggiatorStepMultiplier_ = 0.5f;
            break;
        case 3:
            arpeggiatorStepMultiplier_ = 0.25f;
            break;
        case 1:
        default:
            arpeggiatorStepMultiplier_ = 1.0f;
            break;
    }
}

void SynthEngine::setSequencerEnabled(bool enabled) {
    if (!enabled && sequencerNoteActive_) {
        // Turn off any active sequencer note when disabling
        suppressArpCapture_ = true;
        noteOff(sequencerActiveNote_);
        suppressArpCapture_ = false;
    }

    sequencerEnabled_ = enabled;

    if (!enabled) {
        // Reset sequencer state completely when disabling
        sequencerSampleCounter_ = 0.0f;
        sequencerCurrentStep_ = 0;
        sequencerActiveNote_ = -1;
        sequencerNoteActive_ = false;
        sequencerStepStarted_ = false;
    } else {
        // When enabling, start from the first step with a clean state
        sequencerSampleCounter_ = 0.0f;
        sequencerCurrentStep_ = 0;
        sequencerActiveNote_ = -1;
        sequencerNoteActive_ = false;
        sequencerStepStarted_ = false;
    }
}


void SynthEngine::setSequencerTempo(float bpm) {
    sequencerTempoBpm_ = std::max(20.0f, bpm);
}

void SynthEngine::setSequencerStepLength(int stepLength) {
    int clamped = std::max(0, std::min(3, stepLength));
    sequencerStepLength_ = static_cast<SequencerStepLength>(clamped);
    configureSequenceLength();
}

void SynthEngine::setSequencerMeasures(int measures) {
    sequencerMeasures_ = std::max(1, measures);
    configureSequenceLength();
}

void SynthEngine::setSequencerStep(int index, int midiNote, bool active) {
    if (index < 0 || index >= static_cast<int>(sequencerSteps_.size())) {
        return;
    }
    sequencerSteps_[index].midiNote = std::max(0, std::min(127, midiNote));
    sequencerSteps_[index].active = active;
}

// CRITICAL FIX: Process arpeggiator ONCE per buffer, not per sample!
void SynthEngine::processArpeggiator(float sampleRate, int32_t numFrames) {
    if (!arpeggiatorEnabled_ || heldNotes_.empty()) {
        return;
    }

    // Advance position within the current step
    arpSampleCounter_ += numFrames;

    float stepDuration = (60.0f / arpeggiatorRateBpm_) * arpeggiatorStepMultiplier_;
    float stepDurationSamples = stepDuration * sampleRate;
    float gateTimeSamples = stepDurationSamples * arpeggiatorGate_;

    // Start a new step (and its note) if we haven't yet
    if (!arpStepStarted_) {
        int noteCount = static_cast<int>(heldNotes_.size());
        if (noteCount <= 0) {
            return;
        }

        int idx = 0;
        switch (arpeggiatorPattern_) {
            case 0: // Up
                idx = arpIndex_ % noteCount;
                break;
            case 1: // Down
                idx = noteCount - 1 - (arpIndex_ % noteCount);
                break;
            case 2: { // Up-Down
                if (noteCount == 1) {
                    idx = 0;
                    break;
                }
                int cycle = noteCount * 2 - 2;
                int pos = arpIndex_ % cycle;
                idx = (pos < noteCount) ? pos : (cycle - pos);
                break;
            }
            case 3: // Random
            default:
                idx = std::rand() % noteCount;
                break;
        }

        int newNote = heldNotes_[idx];

        // Ensure any previous arp note is turned off before starting a new one
        if (arpNoteActive_ && currentArpNote_ >= 0) {
            suppressArpCapture_ = true;
            noteOff(currentArpNote_);
            suppressArpCapture_ = false;
            arpNoteActive_ = false;
        }

        currentArpNote_ = newNote;

        suppressArpCapture_ = true;
        noteOn(currentArpNote_);
        suppressArpCapture_ = false;
        arpNoteActive_ = true;
        arpStepStarted_ = true;

    }

    // Gate the current note
    if (arpNoteActive_ && arpSampleCounter_ >= gateTimeSamples) {
        suppressArpCapture_ = true;
        noteOff(currentArpNote_);
        suppressArpCapture_ = false;
        arpNoteActive_ = false;
    }

    // Advance to the next step when the duration has elapsed
    if (arpSampleCounter_ >= stepDurationSamples) {
        // Make sure note is off before advancing
        if (arpNoteActive_) {
            suppressArpCapture_ = true;
            noteOff(currentArpNote_);
            suppressArpCapture_ = false;
            arpNoteActive_ = false;
        }

        // Reset counter and advance to the next arp index
        arpSampleCounter_ -= stepDurationSamples;

        if (!heldNotes_.empty()) {
            arpIndex_ = (arpIndex_ + 1) % std::max<int>(1, heldNotes_.size());
        } else {
            arpIndex_ = 0;
        }

        // Mark that the next call should start a new step
        arpStepStarted_ = false;
    }
}


// CRITICAL FIX: Process sequencer ONCE per buffer, not per sample!
void SynthEngine::processSequencer(float sampleRate, int32_t numFrames) {
    if (!sequencerEnabled_ || sequencerSteps_.empty()) {
        return;
    }

    // Advance position within the current step
    sequencerSampleCounter_ += numFrames;

    float beatSeconds = 60.0f / sequencerTempoBpm_;
    float lengthMultiplier = 1.0f;
    switch (sequencerStepLength_) {
        case SequencerStepLength::Eighth:
            lengthMultiplier = 0.5f;
            break;
        case SequencerStepLength::Quarter:
            lengthMultiplier = 1.0f;
            break;
        case SequencerStepLength::Half:
            lengthMultiplier = 2.0f;
            break;
        case SequencerStepLength::Whole:
            lengthMultiplier = 4.0f;
            break;
    }

    float stepDuration = beatSeconds * lengthMultiplier;
    float stepDurationSamples = stepDuration * sampleRate;
    float gateTimeSamples = stepDurationSamples * 0.9f;

    // Start the current step's note if we haven't yet
    if (!sequencerStepStarted_) {
        const auto& step = sequencerSteps_[sequencerCurrentStep_ % sequencerSteps_.size()];
        sequencerActiveNote_ = step.midiNote;

        if (step.active) {
            suppressArpCapture_ = true;
            noteOn(step.midiNote);
            suppressArpCapture_ = false;
            sequencerNoteActive_ = true;
        } else {
            sequencerNoteActive_ = false;
        }

        sequencerStepStarted_ = true;
    }

    // Gate off the note part-way through the step
    if (sequencerNoteActive_ && sequencerSampleCounter_ >= gateTimeSamples) {
        suppressArpCapture_ = true;
        noteOff(sequencerActiveNote_);
        suppressArpCapture_ = false;
        sequencerNoteActive_ = false;
    }

    // Advance to the next step when its duration has elapsed
    if (sequencerSampleCounter_ >= stepDurationSamples) {
        // Make sure note is off before advancing
        if (sequencerNoteActive_) {
            suppressArpCapture_ = true;
            noteOff(sequencerActiveNote_);
            suppressArpCapture_ = false;
            sequencerNoteActive_ = false;
        }

        sequencerSampleCounter_ -= stepDurationSamples;
        if (!sequencerSteps_.empty()) {
            sequencerCurrentStep_ = (sequencerCurrentStep_ + 1) % sequencerSteps_.size();
        } else {
            sequencerCurrentStep_ = 0;
        }
        sequencerStepStarted_ = false;
    }
}


void SynthEngine::configureSequenceLength() {
    int stepsPerMeasure = getStepsPerMeasure();
    int totalSteps = std::max(1, sequencerMeasures_ * stepsPerMeasure);
    static const int patternNotes[] = {60, 62, 64, 65, 67, 69, 71, 72};

    if (sequencerActiveNote_ >= 0 && sequencerNoteActive_) {
        suppressArpCapture_ = true;
        noteOff(sequencerActiveNote_);
        suppressArpCapture_ = false;
    }

    std::vector<SequencerStep> newSteps(totalSteps);
    for (int i = 0; i < totalSteps; ++i) {
        if (i < static_cast<int>(sequencerSteps_.size())) {
            newSteps[i] = sequencerSteps_[i];
        } else {
            newSteps[i] = {patternNotes[i % 8], true};
        }
    }

    sequencerSteps_.swap(newSteps);
    sequencerCurrentStep_ = std::min(sequencerCurrentStep_, static_cast<int>(sequencerSteps_.size()) - 1);
    sequencerSampleCounter_ = 0.0f;
    sequencerActiveNote_ = -1;
    sequencerNoteActive_ = false;
}

int SynthEngine::getStepsPerMeasure() const {
    switch (sequencerStepLength_) {
        case SequencerStepLength::Eighth:
            return 8;
        case SequencerStepLength::Quarter:
            return 4;
        case SequencerStepLength::Half:
            return 2;
        case SequencerStepLength::Whole:
            return 1;
    }
    return 4;
}

float SynthEngine::processDelay(float input, float sampleRate) {
    if (!delayEnabled_ || delayBuffer_.empty()) {
        return input;
    }

    size_t delaySamples = static_cast<size_t>(delayTime_ * sampleRate);
    delaySamples = std::max<size_t>(1, std::min(delaySamples, delayBufferSize_ - 1));

    size_t readIndex = (delayWriteIndex_ + delayBufferSize_ - delaySamples) % delayBufferSize_;
    float delayed = delayBuffer_[readIndex];

    float feedbackSample = input + delayed * delayFeedback_;
    delayBuffer_[delayWriteIndex_] = feedbackSample;

    delayWriteIndex_++;
    if (delayWriteIndex_ >= delayBufferSize_) {
        delayWriteIndex_ = 0;
    }

    return input * (1.0f - delayMix_) + delayed * delayMix_;
}

float SynthEngine::processChorus(float input, float sampleRate) {
    if (!chorusEnabled_ || chorusBuffer_.empty()) {
        return input;
    }

    float mod1 = std::sin(2.0f * kPI * chorusPhase1_);
    float mod2 = std::sin(2.0f * kPI * chorusPhase2_);

    float baseDelayMs = 12.0f;
    float depthMs = 8.0f * chorusDepth_;

    auto readChorus = [&](float mod) {
        float delayMs = baseDelayMs + depthMs * mod;
        float delaySamples = delayMs * sampleRate / 1000.0f;
        delaySamples = std::max(1.0f, std::min(delaySamples, static_cast<float>(chorusBufferSize_ - 1)));

        float readPos = static_cast<float>(chorusWriteIndex_) - delaySamples;
        while (readPos < 0.0f) {
            readPos += static_cast<float>(chorusBufferSize_);
        }

        size_t indexA = static_cast<size_t>(readPos) % chorusBufferSize_;
        size_t indexB = (indexA + 1) % chorusBufferSize_;
        float frac = readPos - std::floor(readPos);

        return chorusBuffer_[indexA] * (1.0f - frac) + chorusBuffer_[indexB] * frac;
    };

    float delayed1 = readChorus(mod1);
    float delayed2 = readChorus(mod2);
    float wet = 0.5f * (delayed1 + delayed2);

    chorusBuffer_[chorusWriteIndex_] = input;
    chorusWriteIndex_++;
    if (chorusWriteIndex_ >= chorusBufferSize_) {
        chorusWriteIndex_ = 0;
    }

    chorusPhase1_ += chorusRate_ / sampleRate;
    chorusPhase2_ += chorusRate_ / sampleRate;
    if (chorusPhase1_ >= 1.0f) chorusPhase1_ -= 1.0f;
    if (chorusPhase2_ >= 1.0f) chorusPhase2_ -= 1.0f;

    return input * (1.0f - chorusMix_) + wet * chorusMix_;
}

float SynthEngine::processReverb(float input, float sampleRate) {
    if (!reverbEnabled_ || reverbCombs_.empty() || reverbAllpasses_.empty()) {
        return input;
    }

    float sizeScale = 0.3f + 0.7f * reverbSize_;
    float damp = 0.2f + 0.75f * reverbDamping_;
    float feedback = 0.7f * sizeScale;

    float combSum = 0.0f;
    for (auto& comb : reverbCombs_) {
        float delayed = comb.buffer[comb.index];
        comb.filterStore = delayed * (1.0f - damp) + comb.filterStore * damp;
        comb.buffer[comb.index] = input + comb.filterStore * feedback;

        comb.index++;
        if (comb.index >= comb.buffer.size()) {
            comb.index = 0;
        }

        combSum += delayed;
    }

    float wet = combSum / static_cast<float>(reverbCombs_.size());

    for (auto& allpass : reverbAllpasses_) {
        float bufOut = allpass.buffer[allpass.index];
        float y = -wet + bufOut;
        allpass.buffer[allpass.index] = wet + bufOut * 0.5f;

        allpass.index++;
        if (allpass.index >= allpass.buffer.size()) {
            allpass.index = 0;
        }

        wet = y;
    }

    return input * (1.0f - reverbMix_) + wet * reverbMix_;
}

void SynthEngine::initializeEffects(float sampleRate) {
    delayBufferSize_ = static_cast<size_t>(sampleRate * 2.0f);
    delayBuffer_.assign(delayBufferSize_, 0.0f);
    delayWriteIndex_ = 0;

    chorusBufferSize_ = static_cast<size_t>(sampleRate * 2.0f);
    chorusBuffer_.assign(chorusBufferSize_, 0.0f);
    chorusWriteIndex_ = 0;
    chorusPhase1_ = 0.0f;
    chorusPhase2_ = 0.25f;

    reverbCombs_.clear();
    reverbAllpasses_.clear();

    std::vector<float> combTimes = {0.0297f, 0.0371f, 0.0411f, 0.0437f};
    for (float time : combTimes) {
        size_t length = static_cast<size_t>(time * sampleRate);
        length = std::max<size_t>(1, length);
        reverbCombs_.push_back({std::vector<float>(length, 0.0f), 0, 0.0f});
    }

    std::vector<float> allpassTimes = {0.005f, 0.0017f};
    for (float time : allpassTimes) {
        size_t length = static_cast<size_t>(time * sampleRate);
        length = std::max<size_t>(1, length);
        reverbAllpasses_.push_back({std::vector<float>(length, 0.0f), 0});
    }
}


Voice* SynthEngine::findFreeVoice() {
    // First, look for a truly free voice (no note assigned and envelope idle)
    for (auto& voice : voices_) {
        if (!voice.isNoteActive() && voice.getMidiNote() == -1) {
            return &voice;
        }
    }
    
    // Next, look for a voice that is fully inactive at the DSP level
    for (auto& voice : voices_) {
        if (!voice.isActive()) {
            return &voice;
        }
    }
    
    // All voices are active: steal the quietest one (smallest amp envelope level)
    Voice* quietest = &voices_[0];
    float minLevel = quietest->getAmpLevel();
    for (auto& voice : voices_) {
        float lvl = voice.getAmpLevel();
        if (lvl < minLevel) {
            minLevel = lvl;
            quietest = &voice;
        }
    }
    return quietest;
}


Voice* SynthEngine::findVoiceForNote(int midiNote) {
    for (auto& voice : voices_) {
        if (voice.getMidiNote() == midiNote /* && voice.isNoteActive() */) {
            return &voice;
        }
    }
    return nullptr;
}
