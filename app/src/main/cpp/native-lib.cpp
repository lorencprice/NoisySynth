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

} // extern "C"
