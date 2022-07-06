#include <logging_macros.h>
#include "OboeSinePlayer.h"

int32_t OboeSinePlayer::initEngine(){
    std::lock_guard <std::mutex> lock(mLock);

    ampMul = std::make_shared<SmoothedAmpParameter>();
    smoothedFrequency = std::make_shared<SmoothedFrequency>(0.0f);
    smoothedFrequency->setSampleRate(kSampleRate);
    smoothedFrequency->setSmoothingType(false);

    vibratoLFO = std::make_shared<LFO>();
    vibratoLFO->setDepth(20.0f);
    pitchEnvelope = std::make_shared<PitchEnvelope>();
    oscillator = std::make_unique<DynamicOscillator>();

    oscillator->setSampleRate(kSampleRate);
    oscillator->setLFO(vibratoLFO);
    oscillator->setSmoothedFreq(smoothedFrequency);
    oscillator->setPitchEnvelope(pitchEnvelope);

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
    pitchEnvelope->off();
    //ADSR envelope off

    //TODO: fare in modo che le successive istruzioni vengano eseguite solo quando tutti gli inviluppi sono esauriti

    isPlaying.store(false);

    /*
    if (mStream) {
        mStream->stop();
    }
     */
}

oboe::DataCallbackResult
OboeSinePlayer::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {
    auto *floatData = (float *) audioData;
    for (int i = 0; i < numFrames; ++i) {
        if(pitchEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF){
            for (int j = 0; j < kChannelCount; j++) {
                floatData[i * kChannelCount + j] = 0.0f;
            }

            if (mStream) {
                //mStream->stop();
                mStream->requestStop();
            }
        } else {
            float osc = oscillator->getNextSample();
            float sampleValue = kAmplitude * osc * ampMul->smoothed();
            for (int j = 0; j < kChannelCount; j++) {
                floatData[i * kChannelCount + j] = sampleValue;
            }
        }
    }
    return oboe::DataCallbackResult::Continue;
}

int32_t OboeSinePlayer::startAudio(float freq) {
    std::lock_guard <std::mutex> lock(mLock);
    Result result = Result::ErrorInternal;

    LOGD("Pressed note: %f", freq);
    bool isNewFreq = kFrequency != freq;
    kFrequency.store(freq);


    if(!isPlaying){
        LOGD("startAudio: Start playing, freq: %f", freq);
        smoothedFrequency->reset(freq);
        isPlaying.store(true);
    } else {
        LOGD("startAudio: Smoothing, destFreq: %f, currentFreq: %f", freq, smoothedFrequency->getCurrentValue());
        smoothedFrequency->setTargetFrequency(freq);
    }
    // Typically, start the stream after querying some stream information, as well as some input from the user
    pitchEnvelope->onWithBaseFreq(freq);
    if(isNewFreq){
        pitchEnvelope->enterStage(pitchEnvelope->ENVELOPE_STAGE_ATTACK);
    }
    if (mStream) {
        result = mStream->requestStart();
    }
    return (int32_t) result;
}

void OboeSinePlayer::setFrequency(float frequency) {
    smoothedFrequency->setTargetFrequency(frequency);
    kFrequency.store(frequency);
}

void OboeSinePlayer::controlAmpMul(float deltaAmp){
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
    LOGD("=============CONTROL RESET=============");
    oscillator->setPitchShift(0);
    vibratoLFO->resetDepth();
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
    //TODO: optimize, maybe remove (it's in the PitchEnvelope
    return exp((logf(2)*(semitonesDelta + 12 * logf(baseFreq)))/12);
}

void OboeSinePlayer::setVibrato(float frequency, float depth) {
    //LOGD("Vibratofreq: %f", frequency);
    vibratoLFO->setFrequency(frequency);
    vibratoLFO->setDepth(depth);
}

void OboeSinePlayer::controlVibrato(float deltaDepth) {
    vibratoLFO->deltaDepth(deltaDepth);
}

void OboeSinePlayer::setPitchAdsr(float attackTime, float attackDelta, float releaseTime,
                                  float releaseDelta) {
    pitchEnvelope->setStageTimes(attackTime, 0.1f, releaseTime);
    pitchEnvelope->setAttackDelta(attackDelta);
    pitchEnvelope->setReleaseDelta(releaseDelta);
}

void OboeSinePlayer::setTremolo(float frequency, float depth) {
    //TODO
}

void OboeSinePlayer::setPWM(float frequency, float depth) {
    //TODO
}

void OboeSinePlayer::setHarmonics(float percent) {
    //TODO
}

void OboeSinePlayer::controlTremolo(float deltaDepth) {
    //TODO
}

void OboeSinePlayer::controlPWM(float deltaDepth) {
    //TODO
}

void OboeSinePlayer::controlHarmonics(float delta) {
    //TODO
}
