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
      release_(0.3f) {
    
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
        for (auto& voice : voices_) {
            if (voice.isActive() || voice.getEnvelope().isActive()) {
                sample += voice.process(sampleRate, lfoValue);
            }
        }
        
        // Simple limiting to prevent clipping
        sample = std::max(-1.0f, std::min(1.0f, sample * 0.3f));
        
        outputBuffer[i] = sample;
    }
    
    return oboe::DataCallbackResult::Continue;
}

void SynthEngine::noteOn(int midiNote) {
    Voice* voice = findFreeVoice();
    if (voice) {
        voice->noteOn(midiNote, currentWaveform_);
        voice->getEnvelope().setAttack(attack_);
        voice->getEnvelope().setDecay(decay_);
        voice->getEnvelope().setSustain(sustain_);
        voice->getEnvelope().setRelease(release_);
        voice->getFilter().setCutoff(filterCutoff_);
        voice->getFilter().setResonance(filterResonance_);
        LOGD("Note ON: %d", midiNote);
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
        voice.getEnvelope().setAttack(attack);
    }
}

void SynthEngine::setDecay(float decay) {
    decay_ = decay;
    for (auto& voice : voices_) {
        voice.getEnvelope().setDecay(decay);
    }
}

void SynthEngine::setSustain(float sustain) {
    sustain_ = sustain;
    for (auto& voice : voices_) {
        voice.getEnvelope().setSustain(sustain);
    }
}

void SynthEngine::setRelease(float release) {
    release_ = release;
    for (auto& voice : voices_) {
        voice.getEnvelope().setRelease(release);
    }
}

void SynthEngine::setLFORate(float rate) {
    lfo_.setRate(rate);
}

void SynthEngine::setLFOAmount(float amount) {
    lfo_.setAmount(amount);
}

Voice* SynthEngine::findFreeVoice() {
    // First, try to find a completely inactive voice
    for (auto& voice : voices_) {
        if (!voice.isActive() && !voice.getEnvelope().isActive()) {
            return &voice;
        }
    }
    
    // If no free voice, steal the oldest one (first in array)
    return &voices_[0];
}

Voice* SynthEngine::findVoiceForNote(int midiNote) {
    for (auto& voice : voices_) {
        if (voice.isActive() && voice.getMidiNote() == midiNote) {
            return &voice;
        }
    }
    return nullptr;
}
