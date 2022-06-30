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

#include "OboeSinePlayer.h"
#include "SoundBoardEngine.h"

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
    auto  *engine = new OboeSinePlayer();

    if (engine->initEngine() != 0) {
        LOGE("Failed to start OboeSinePlayer Engine");
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
    auto engine = reinterpret_cast<OboeSinePlayer*>(jEngineHandle);
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
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->noteOff(noteIndex);
        engine->stopAudio();
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_noteOn(JNIEnv *env, jobject thiz,
                                                         jlong engine_handle,
                                                    jfloat freq/*jint noteIndex */) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->noteOn(noteIndex);
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
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
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
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        engine->controlPitch(delta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlVibrato(JNIEnv *env, jobject thiz,
                                                            jlong engine_handle, jfloat depthDelta) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->noteOn(noteIndex);
        engine->controlVibrato(depthDelta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlReset(JNIEnv *env, jobject thiz,
                                                        jlong engine_handle) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
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
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //harmonics goes from -1 to +1
        //engine->controlHarmonics(delta);
        //TODO: control harmonics delta function, look for values passed, should be an envelope
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlTremolo(JNIEnv *env, jobject thiz,
                                                            jlong engine_handle,
                                                            jfloat delta) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->controlTremolo(delta);
        //TODO: control tremolo function with delta, look for values passed
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_controlPWM(JNIEnv *env, jobject thiz,
                                                        jlong engine_handle,
                                                        jfloat delta) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->controlPWM(delta);
        //TODO: control PWM function with delta, look for values passed
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

//SETTINGS:

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setPortamento(JNIEnv *env, jobject thiz,
                                                              jlong engine_handle, jfloat seconds) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->noteOn(noteIndex);
        engine->setPortamento(seconds);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setVibrato(JNIEnv *env, jobject thiz,
                                                           jlong engine_handle, jfloat frequency, jfloat depth) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->noteOn(noteIndex);
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
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->noteOn(noteIndex);
        engine->setPitchAdsr(attackTime, attackDelta, releaseTime, releaseDelta);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setHarmonics(JNIEnv *env, jobject thiz,
                                                              jlong engine_handle,
                                                              jfloat percentage) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //harmonics goes from -1 to +1
        //engine->setHarmonics(percentage);
        //TODO: set harmonics function with base settings
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setTremolo(JNIEnv *env, jobject thiz,
                                                        jlong engine_handle, jfloat frequency, jfloat depth) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->setTremolo(frequency, depth);
        //TODO: set tremolo function with base tremolo settings
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_setPWM(JNIEnv *env, jobject thiz,
                                                        jlong engine_handle, jfloat frequency, jfloat depth) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->setPWM(frequency, depth);
        //TODO: set PWM function with base PWM settings
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

} // extern "C"
