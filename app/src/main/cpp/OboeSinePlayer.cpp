#include <logging_macros.h>
#include "OboeSinePlayer.h"

int32_t OboeSinePlayer::initEngine(){
    std::lock_guard <std::mutex> lock(mLock);

    ampMul = new SmoothedAmpParameter(1.0f, kAmpMulAlpha, 1.0f);
    smoothedFrequency = std::make_unique<SmoothedFrequency>(400.0f, 0.0f, kSampleRate);

    oscillator = std::make_unique<Oscillator>(kSampleRate, 400.0f);

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
        float sampleValue = kAmplitude * oscillator->getNextSample() * ampMul->smoothed();
        for (int j = 0; j < kChannelCount; j++) {
            floatData[i * kChannelCount + j] = sampleValue;
        }
    }
    return oboe::DataCallbackResult::Continue;
}

int32_t OboeSinePlayer::startAudio(float freq) {
    std::lock_guard <std::mutex> lock(mLock);
    Result result = Result::ErrorInternal;
    // Typically, start the stream after querying some stream information, as well as some input from the user
    setFrequency(freq);
    if (mStream) {
        result = mStream->requestStart();
    }
    return (int32_t) result;
}

void OboeSinePlayer::setFrequency(float frequency) {
    kFrequency = frequency;
    smoothedFrequency->setTargetFrequency(frequency);
}

void OboeSinePlayer::deltaAmpMul(float deltaAmp){
    ampMul->applyDeltaToTarget(deltaAmp);
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

void OboeSinePlayer::controlPitch(float deltaPitch) {
    //TODO: log2lin
    pitchBendDelta = deltaPitch*4;
}

void OboeSinePlayer::controlReset() {
    pitchBendDelta = 0;
    smoothedFrequency->setTargetFrequency(kFrequency);
    smoothedFrequency->setRawPrevValue(kFrequency);
}

void OboeSinePlayer::setPortamento(float seconds) {
    smoothedFrequency->setPortamento(seconds);
}

OboeSinePlayer::~OboeSinePlayer() {
    if (mStream) {
        LOGE("OboeSynth destructed without closing stream. Resource leak.");
        closeEngine();
    }
}
