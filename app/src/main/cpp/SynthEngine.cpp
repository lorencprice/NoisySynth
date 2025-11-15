#include "SynthEngine.h"
#include <android/log.h>

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
      filterEnvAmount_(0.5f) {
    
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
    
    // Process each frame
    for (int32_t i = 0; i < numFrames; i++) {
        float sample = 0.0f;
        
        // Generate LFO value
        float lfoValue = lfo_.process(sampleRate);
        
        // Mix all active voices
        int activeVoices = 0;
        for (auto& voice : voices_) {
            if (voice.isNoteActive()) {
                sample += voice.process(sampleRate, lfoValue);
                activeVoices++;
            }
        }
        
        // Normalize by active voice count to prevent clipping
        if (activeVoices > 0) {
            sample /= std::sqrt(static_cast<float>(activeVoices));
        }
        
        // Soft clipping / saturation
        sample = std::tanh(sample * 0.7f);
        
        // Final limiting
        sample = std::max(-1.0f, std::min(1.0f, sample));
        
        outputBuffer[i] = sample;
    }
    
    return oboe::DataCallbackResult::Continue;
}

void SynthEngine::noteOn(int midiNote) {
    // First check if this note is already playing - if so, retrigger it
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

Voice* SynthEngine::findFreeVoice() {
    // First, try to find a completely inactive voice (not playing any note)
    for (auto& voice : voices_) {
        if (!voice.isNoteActive() && voice.getMidiNote() == -1) {
            return &voice;
        }
    }
    
    // Second, try to find a voice that's releasing
    for (auto& voice : voices_) {
        if (!voice.isActive()) {
            return &voice;
        }
    }
    
    // If no free voice, steal the oldest one (first in array)
    return &voices_[0];
}

Voice* SynthEngine::findVoiceForNote(int midiNote) {
    for (auto& voice : voices_) {
        if (voice.getMidiNote() == midiNote && voice.isNoteActive()) {
            return &voice;
        }
    }
    return nullptr;
}
