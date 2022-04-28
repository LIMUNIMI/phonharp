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

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_deltaAmpMul(JNIEnv *env, jobject thiz,
                                                    jlong engine_handle,
                                                    jfloat deltaAmpMul) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->noteOn(noteIndex);
        engine->deltaAmpMul(deltaAmpMul);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT void JNICALL
Java_com_unimi_lim_hmi_synthetizer_OboeSynth_deltaPitch(JNIEnv *env, jobject thiz,
                                                         jlong engine_handle,
                                                         jfloat deltaPitch) {
    auto *engine = reinterpret_cast<OboeSinePlayer*>(engine_handle);
    if (engine) {
        //engine->noteOn(noteIndex);
        engine->controlPitch(deltaPitch);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

} // extern "C"
