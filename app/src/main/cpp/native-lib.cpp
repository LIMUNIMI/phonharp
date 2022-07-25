/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <string>
#include <vector>

#include "OboeSynthMain.h"

extern "C" {
/**
 * Start the audio engine
 *
 * @param env
 * @param instance
 * @param jCpuIds - CPU core IDs which the audio process should affine to
 * @return a pointer to the audio engine. This should be passed to other methods
 */
JNIEXPORT jlong JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_startEngine(JNIEnv *env, jobject /*unused*/,
         jint jNumSignals) {
    LOGD("numSignals : %d", static_cast<int>(jNumSignals));
    auto  *engine = new OboeSynthMain();

    if (engine->initEngine() != 0) {
        LOGE("Failed to start OboeSynthMain Engine");
        delete engine;
        engine = nullptr;
    } else  {
        LOGD("Engine Started");
    }
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_stopEngine(JNIEnv *env, jobject instance,
        jlong jEngineHandle) {
    auto engine = reinterpret_cast<OboeSynthMain*>(jEngineHandle);
    if (engine) {
        engine->closeEngine();
        delete engine;
    } else {
        LOGD("Engine invalid, call startEngine() to create");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_native_1setDefaultStreamValues(JNIEnv *env,
                                                                            jclass type,
                                                                            jint sampleRate,
                                                                            jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_noteOff(JNIEnv *env, jobject thiz,
                                                         jlong engine_handle/*, jint noteIndex */) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->stopAudio();
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_noteOn(JNIEnv *env, jobject thiz,
                                                         jlong engine_handle,
                                                    jfloat freq/*jint noteIndex */) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->startAudio(freq);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

//REAL TIME CONTROLS

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlAmpMul(JNIEnv *env, jobject thiz,
                                                    jlong engine_handle,
                                                    jfloat delta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->controlAmpMul(delta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlPitch(JNIEnv *env, jobject thiz,
                                                         jlong engine_handle,
                                                         jfloat delta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->controlPitch(delta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlVibrato(JNIEnv *env, jobject thiz,
                                                            jlong engine_handle, jfloat depthDelta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->controlVibrato(depthDelta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlReset(JNIEnv *env, jobject thiz,
                                                        jlong engine_handle) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->controlReset();
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlHarmonics(JNIEnv *env, jobject thiz,
                                                              jlong engine_handle,
                                                              jfloat delta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->controlHarmonics(delta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlTremolo(JNIEnv *env, jobject thiz,
                                                            jlong engine_handle,
                                                            jfloat delta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->controlTremolo(delta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlPWM(JNIEnv *env, jobject thiz,
                                                        jlong engine_handle,
                                                        jfloat delta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->controlPWM(delta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

//SETTINGS:

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setEQ(JNIEnv *env, jobject thiz,
                                                           jlong engine_handle, jfloat lowDb, jfloat highDb) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setEq(lowDb, highDb);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setPortamento(JNIEnv *env, jobject thiz,
                                                              jlong engine_handle, jfloat seconds) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setPortamento(seconds);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setVibrato(JNIEnv *env, jobject thiz,
                                                           jlong engine_handle, jfloat frequency, jfloat depth) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setVibrato(frequency, depth);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setPitchAdsr(JNIEnv *env, jobject thiz,
                                                            jlong engine_handle,
                                                            jfloat attackTime,
                                                            jfloat attackDelta,
                                                            jfloat releaseTime,
                                                            jfloat releaseDelta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setPitchAdsr(attackTime, attackDelta, releaseTime, releaseDelta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setVolume(JNIEnv *env, jobject thiz,
                                                          jlong engine_handle,
                                                          jfloat volume) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setVolume(volume);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setVolumeAdsr(JNIEnv *env, jobject thiz,
                                                           jlong engine_handle,
                                                           jfloat attackTime,
                                                           jfloat attackDelta,
                                                           jfloat releaseTime,
                                                           jfloat releaseDelta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setVolumeAdsr(attackTime, attackDelta, releaseTime, releaseDelta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setHarmonics(JNIEnv *env, jobject thiz,
                                                              jlong engine_handle,
                                                              jfloat percentage) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        //harmonics goes from 0 to 100
        engine->setHarmonics(percentage);
        //TODO: set harmonics function with base settings
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setTremolo(JNIEnv *env, jobject thiz,
                                                        jlong engine_handle, jfloat frequency, jfloat depth) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setTremolo(frequency, depth);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setPWM(JNIEnv *env, jobject thiz,
                                                        jlong engine_handle, jfloat frequency, jfloat depth) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setPWM(frequency, depth);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setEq(JNIEnv *env, jobject thiz,
                                                    jlong engine_handle, jfloat highGain, jfloat lowGain) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setEq(highGain, lowGain);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setHarmonicsAdsr(JNIEnv *env, jobject thiz,
                                                           jlong engine_handle,
                                                           jfloat attackTime,
                                                           jfloat attackDelta,
                                                           jfloat releaseTime,
                                                           jfloat releaseDelta) {
    auto *engine = reinterpret_cast<OboeSynthMain*>(engine_handle);
    if (engine) {
        engine->setHarmonicsAdsr(attackTime, attackDelta, releaseTime, releaseDelta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

} // extern "C"
