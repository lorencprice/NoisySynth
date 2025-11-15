#include <jni.h>
#include "SynthEngine.h"
#include <android/log.h>

#define LOG_TAG "NoisySynth-JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_noisysynth_SynthEngine_create(JNIEnv *env, jobject thiz) {
    auto *engine = new SynthEngine();
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_destroy(JNIEnv *env, jobject thiz, jlong engine_handle) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    delete engine;
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1noteOn(
    JNIEnv *env, jobject thiz, jlong engine_handle, jint midi_note) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->noteOn(static_cast<int>(midi_note));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1noteOff(
    JNIEnv *env, jobject thiz, jlong engine_handle, jint midi_note) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->noteOff(static_cast<int>(midi_note));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setWaveform(
    JNIEnv *env, jobject thiz, jlong engine_handle, jint waveform) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setWaveform(static_cast<int>(waveform));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setFilterCutoff(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat cutoff) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setFilterCutoff(static_cast<float>(cutoff));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setFilterResonance(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat resonance) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setFilterResonance(static_cast<float>(resonance));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setAttack(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat attack) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setAttack(static_cast<float>(attack));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setDecay(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat decay) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setDecay(static_cast<float>(decay));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setSustain(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat sustain) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setSustain(static_cast<float>(sustain));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setRelease(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat release) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setRelease(static_cast<float>(release));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setFilterAttack(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat attack) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setFilterAttack(static_cast<float>(attack));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setFilterDecay(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat decay) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setFilterDecay(static_cast<float>(decay));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setFilterSustain(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat sustain) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setFilterSustain(static_cast<float>(sustain));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setFilterRelease(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat release) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setFilterRelease(static_cast<float>(release));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setFilterEnvelopeAmount(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat amount) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setFilterEnvelopeAmount(static_cast<float>(amount));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setLFORate(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat rate) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setLFORate(static_cast<float>(rate));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setLFOAmount(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat amount) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setLFOAmount(static_cast<float>(amount));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setDelayEnabled(
    JNIEnv *env, jobject thiz, jlong engine_handle, jboolean enabled) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setDelayEnabled(static_cast<bool>(enabled));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setDelayTime(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat time) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setDelayTime(static_cast<float>(time));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setDelayFeedback(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat feedback) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setDelayFeedback(static_cast<float>(feedback));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setDelayMix(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat mix) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setDelayMix(static_cast<float>(mix));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setChorusEnabled(
    JNIEnv *env, jobject thiz, jlong engine_handle, jboolean enabled) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setChorusEnabled(static_cast<bool>(enabled));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setChorusRate(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat rate) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setChorusRate(static_cast<float>(rate));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setChorusDepth(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat depth) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setChorusDepth(static_cast<float>(depth));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setChorusMix(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat mix) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setChorusMix(static_cast<float>(mix));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setReverbEnabled(
    JNIEnv *env, jobject thiz, jlong engine_handle, jboolean enabled) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setReverbEnabled(static_cast<bool>(enabled));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setReverbSize(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat size) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setReverbSize(static_cast<float>(size));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setReverbDamping(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat damping) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setReverbDamping(static_cast<float>(damping));
}

JNIEXPORT void JNICALL
Java_com_example_noisysynth_SynthEngine_native_1setReverbMix(
    JNIEnv *env, jobject thiz, jlong engine_handle, jfloat mix) {
    auto *engine = reinterpret_cast<SynthEngine *>(engine_handle);
    engine->setReverbMix(static_cast<float>(mix));
}

} // extern "C"
