#include <logging_macros.h>
#include "OboeSinePlayer.h"

int32_t OboeSinePlayer::initEngine(){
    std::lock_guard <std::mutex> lock(mLock);

    ampMul = std::make_shared<SmoothedAmpParameter>();
    smoothedFrequency = std::make_shared<SmoothedFrequency>(400.0f, 0.0f, kSampleRate);
    vibratoLFO = std::make_shared<LFO>();
    vibratoLFO->setDepth(20.0f);
    oscillator = std::make_unique<DynamicOscillator>();

    oscillator->setLFO(vibratoLFO);
    oscillator->setSmoothedFreq(smoothedFrequency);
    oscillator->setSampleRate(kSampleRate);

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
        auto osc = oscillator->getNextSample();
        float sampleValue = kAmplitude * osc * ampMul->smoothed();
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
    smoothedFrequency->setTargetFrequency(frequency);
    kFrequency.store(frequency);
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
    //pitchBendDelta = deltaPitch*4;
    oscillator->setPitchShift(log2lin(deltaPitch, kFrequency));
}

void OboeSinePlayer::controlReset() {
    oscillator->setPitchShift(0);
    smoothedFrequency->reset(kFrequency);
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

float OboeSinePlayer::log2lin(float semitonesDelta, float baseFreq) {
    //TODO: optimize
    return exp((logf(2)*(semitonesDelta + 12 * logf(baseFreq)))/12);
}

void OboeSinePlayer::setVibrato(float frequency, float depth) {
    LOGD("Vibratofreq: %f", frequency);
    vibratoLFO->setFrequency(frequency);
    vibratoLFO->setDepth(depth);
}

void OboeSinePlayer::controlVibrato(float deltaDepth) {
    vibratoLFO->deltaDepth(deltaDepth);
}
