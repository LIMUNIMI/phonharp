#include "OboeSinePlayer.h"

int32_t OboeSinePlayer::initEngine(){
    initSensorEventQueue();
    enableSensor();

    std::lock_guard <std::mutex> lock(mLock);
    oboe::AudioStreamBuilder builder;
    // The builder set methods can be chained for convenience.
    Result result = builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setChannelCount(kChannelCount)
            ->setSampleRate(kSampleRate)
            ->setSampleRateConversionQuality(oboe::SampleRateConversionQuality::Medium)
            ->setFormat(oboe::AudioFormat::Float)
            ->setDataCallback(this)
            ->openStream(mStream);
    //if (result != Result::OK) return (int32_t) result;
    return (int32_t) result;
}

void OboeSinePlayer::stopAudio() {
    std::lock_guard <std::mutex> lock(mLock);
    if (mStream) {
        mStream->stop();
    }
}

oboe::DataCallbackResult
OboeSinePlayer::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {
    auto *floatData = (float *) audioData;
    for (int i = 0; i < numFrames; ++i) {
        float sampleValue = kAmplitude * sinf(mPhase);
        for (int j = 0; j < kChannelCount; j++) {
            floatData[i * kChannelCount + j] = sampleValue;
        }
        mPhase += mPhaseIncrement;
        if (mPhase >= kTwoPi) mPhase -= kTwoPi;
    }
    return oboe::DataCallbackResult::Continue;
}

int32_t OboeSinePlayer::startAudio(float freq) {
    std::lock_guard <std::mutex> lock(mLock);
    Result result = Result::ErrorInternal;
    // Typically, start the stream after querying some stream information, as well as some input from the user
    kFrequency = freq;
    updatePhaseInc();
    if (mStream) {
        result = mStream->requestStart();
    }
    return (int32_t) result;
}

void OboeSinePlayer::closeEngine() {
    // Stop, close and delete in case not already closed.
    std::lock_guard <std::mutex> lock(mLock);
    if (mStream) {
        mStream->stop();
        mStream->close();
        mStream.reset();
    }
}

void OboeSinePlayer::updatePhaseInc() {
    mPhaseIncrement = kFrequency * kTwoPi / (double) kSampleRate;
}

ASensorManager* OboeSinePlayer::getSensorManager() {
    ASensorManager* sensor_manager =
            ASensorManager_getInstance();
    if (!sensor_manager) {
        return nullptr;
    }
    return sensor_manager;
}

void OboeSinePlayer::initSensorEventQueue() {
    ASensorManager* sensorManager = getSensorManager();
    assert(sensorManager != nullptr);
    rotationSensor = ASensorManager_getDefaultSensor(sensorManager, ASENSOR_TYPE_GAME_ROTATION_VECTOR);
    looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    assert(looper != nullptr);
    rotationSensorEventQueue = ASensorManager_createEventQueue(sensorManager, looper,
                                                              LOOPER_ID_USER, NULL, NULL);
    assert(rotationSensorEventQueue != nullptr);
}

void OboeSinePlayer::enableSensor() {
    auto status = ASensorEventQueue_enableSensor(rotationSensorEventQueue,
                                                 rotationSensor);
    assert(status >= 0);
    status = ASensorEventQueue_setEventRate(rotationSensorEventQueue,
                                            rotationSensor,
                                            SENSOR_REFRESH_PERIOD_US);
    assert(status >= 0);
}

void OboeSinePlayer::pauseSensor() {
    ASensorEventQueue_disableSensor(rotationSensorEventQueue, rotationSensor);
}