#include <logging_macros.h>
#include "OboeSynthMain.h"

int32_t OboeSynthMain::initEngine(){
    std::lock_guard <std::mutex> lock(mLock);

    oscillator = new DutyCycleOsc();
    oscillator->id = 21;
    oscillator->setSampleRate(kSampleRate);
    //oscillator->setWaveType(0);

    pwmOsc = new NaiveOscillator();
    pwmOsc->setWaveType(NaiveOscillator::Triangular);
    pwmOsc->setSampleRate(kSampleRate);

    harmonicsBaseLevel = new StaticSignal();
    harmonicsBaseLevel->setValue(kHarmonicsBaseLevelScale);

    harmonicsEnvelope = new EnvelopeGenerator();
    harmonicsEnvelope->setSampleRate(kSampleRate);
    harmonicsEnvelope->id = 56;

    harmonicsShift = new SmoothedParameter();
    harmonicsShift->setSecondsToTarget(kHarmonicsShiftSmoothingTime);
    harmonicsShift->setSampleRate(kSampleRate);

    scaledPwmOsc = new ModulatedSignal(pwmOsc, kPwmScaling);
    harmMix = new Mix();
    harmMix->addSignal(scaledPwmOsc, 1.0f);
    harmMix->addSignal(harmonicsBaseLevel, 1.0f);
    harmMix->addSignal(harmonicsShift, 1.0f);
    harmMix->addSignal(harmonicsEnvelope, 1.0f);


    smoothedFrequency = new SmoothedFrequency(0.0f);
    smoothedFrequency->setSampleRate(kSampleRate);
    smoothedFrequency->setSmoothingType(false);
    smoothedFrequency->setFreqBase(kBaseFreq);

    vibratoLFO = new NaiveOscillator();
    vibratoLFO->id = 12;
    vibratoLFO->setSampleRate(kSampleRate);

    pitchEnvelope = new EnvelopeGenerator();
    pitchEnvelope->setSampleRate(kSampleRate);

    pitchShift = new SmoothedParameter();
    pitchShift->setSampleRate(kSampleRate);
    pitchShift->setSecondsToTarget(kPitchShiftSmoothingTime);

    freqMix = new Mix();
    //Initialize ModulatedSignal here if you want to change the scaling later. If not, use AddSignal with two parameters and be happy with a static value
    scaledVibrato = new ModulatedSignal(vibratoLFO, kVibratoScaling); //mod amount controlled in settings and real time
    freqMix->addSignal(smoothedFrequency, 1); //Already in semitones, doesn't need to be scaled
    freqMix->addSignal(scaledVibrato);
    freqMix->addSignal(pitchEnvelope, 1); //Already in semitones, doesn't need to be scaled
    freqMix->addSignal(pitchShift, kPitchShiftScaling);



    tremoloLFO = new NaiveOscillator();
    tremoloLFO->id = 3;
    tremoloLFO->setSampleRate(kSampleRate);

    ampMul = new SmoothedAmpParameter();
    ampMul->setSampleRate(kSampleRate);
    ampMul->setSecondsToTarget(kAmpControlSmoothingTime);
    ampMul->reset(1.0f);

    volumeEnvelope = new EnvelopeGenerator(); //TODO: clipping after two notes. Consider adding smoothing
    volumeEnvelope->setSampleRate(kSampleRate);
    volumeEnvelope->id = 42;

    //auto static1 = new StaticSignal();
    //static1->setValue(1);

    ampMix = new Mix();
    ampMix->setMixMode(Mix::Mul);
    scaledAmpMul = new ModulatedSignal(ampMul, 1.0f);
    ampMix->addSignal(scaledAmpMul); //Static scaling is okay
    scaledTremolo = new ModulatedSignal(tremoloLFO, kTremoloScaling);
    scaledTremolo->setBaseOffset(1.000001f);
    ampMix->addSignal(scaledTremolo);
    scaledVolumeEnvelope = new ModulatedSignal(volumeEnvelope, 1.0f);
    scaledVolumeEnvelope->setBaseOffset(1.0f);
    ampMix->addSignal(scaledVolumeEnvelope);

    lowShelf = new Shelf();
    lowShelf->setSampleRate(kSampleRate);
    lowShelf->configure(kLowShelfFreq, kShelfQ, 0);
    highShelf = new Shelf();
    highShelf->setSampleRate(kSampleRate);
    highShelf->configure(kHighShelfFreq, kShelfQ, 0);

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

void OboeSynthMain::stopAudio() {
    std::lock_guard <std::mutex> lock(mLock);
    pitchEnvelope->off();
    volumeEnvelope->off();
    harmonicsEnvelope->off();
    //ADSR envelope off

    if (mStream && !isPlaying) {
        LOGD("OboeSynthMain:: STOPPED STREAM");
        mStream->stop();
    }

    isPlaying.store(false);
    //mStream->stop();
}

oboe::DataCallbackResult
OboeSynthMain::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {
    auto *floatData = (float *) audioData;
    for (int i = 0; i < numFrames; ++i) {
        if(pitchEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF && volumeEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF && harmonicsEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF){
            for (int j = 0; j < kChannelCount; j++) {
                floatData[i * kChannelCount + j] = 0.0f;
            }
        } else {
            float freq = log2lin(freqMix->getNextSample());
            float dutyCycle = harmMix->getNextSample();

            oscillator->setFrequency(freq);
            oscillator->setDutyCycle(dutyCycle);

            float osc = oscillator->getNextSample();

            osc = lowShelf->getFilteredSample(osc);
            osc = highShelf->getFilteredSample(osc);

            float volumeMix = ampMix->getNextSample();
            float sampleValue = kAmplitude * osc * volumeMix;
            for (int j = 0; j < kChannelCount; j++) {
                floatData[i * kChannelCount + j] = sampleValue;
            }
        }
    }
    return oboe::DataCallbackResult::Continue;
}

int32_t OboeSynthMain::startAudio(float freq) {
    std::lock_guard <std::mutex> lock(mLock);
    Result result = Result::ErrorInternal;

    //LOGD("Pressed note: %f", freq);
    bool isNewFreq = kFrequency != freq;
    kFrequency.store(freq);


    if(!isPlaying){
        //LOGD("startAudio: Start playing, freq: %f", freq);
        smoothedFrequency->reset(freq);
        //LOGD("startAudio: reset frequency");
        pitchEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK); //Needs to be called here too
        volumeEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        harmonicsEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        isPlaying.store(true);
        //LOGD("startAudio: stored start playing TRUE");
    } else {
        //LOGD("startAudio: Smoothing, destFreq: %f, currentFreq: %f", freq, smoothedFrequency->getCurrentValue());
        smoothedFrequency->setTargetFrequency(freq);
    }
    // Typically, start the stream after querying some stream information, as well as some input from the user
    pitchEnvelope->on();
    volumeEnvelope->on();
    harmonicsEnvelope->on();
    //LOGD("startAudio: new Freq %d", isNewFreq);
    if(isNewFreq){
        pitchEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        volumeEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        harmonicsEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
    }
    if (mStream) {
        //LOGD("startAudio: requesting stream start");
        result = mStream->requestStart();
        //LOGD("startAudio: requested stream start");
    }
    return (int32_t) result;
}

void OboeSynthMain::setFrequency(float frequency) {
    //TODO: consider removing
    smoothedFrequency->setTargetFrequency(frequency);
    kFrequency.store(frequency);
}

void OboeSynthMain::controlAmpMul(float deltaAmp){
    //LOGD("controlAmpMul: delta amp %f", deltaAmp);
    //ampMul->applyDeltaToTarget(deltaAmp);
    //AmpMul is being used as an absolute because in OboeSynth it takes the value from the gyro which is absolute.
    //Consider doing something similar to the pitchShift if you want to use the fingers.
    scaledAmpMul->setModDelta(deltaAmp);
}

void OboeSynthMain::closeEngine() {
    // Stop, close and delete in case not already closed.
    std::lock_guard <std::mutex> lock(mLock);
    if (mStream) {
        mStream->stop();
        mStream->close();
        mStream.reset();
    }
}

void OboeSynthMain::controlPitch(float deltaPitch) {
    pitchShift->setTargetValue(deltaPitch);
}

void OboeSynthMain::controlReset() {
    //LOGD("=============CONTROL RESET=============");
    scaledVibrato->reset();
    smoothedFrequency->reset(kFrequency);
    pitchShift->reset(0.0f);
    scaledTremolo->reset();
    scaledPwmOsc->reset();
    scaledVolumeEnvelope->reset(); //doesn't need reset but whatever
    harmonicsShift->reset(0.0f);
}

void OboeSynthMain::setPortamento(float seconds) {
    smoothedFrequency->setPortamento(seconds);
}

OboeSynthMain::~OboeSynthMain() {
    if (mStream) {
        LOGE("OboeSynth destructed without closing stream. Resource leak.");
        closeEngine();
    }
}

float OboeSynthMain::log2lin(float semitonesDelta) {
    return expf(semitonesDelta * (logf(2)/12) ) * kBaseFreq;
}

void OboeSynthMain::setVibrato(float frequency, float depth) {
    //LOGD("Vibratofreq: %f", frequency);
    vibratoLFO->setFrequency(frequency);
    scaledVibrato->setModAmount(depth);
    //vibratoLFO->setDepth(depth);
}

void OboeSynthMain::controlVibrato(float deltaDepth) {
   scaledVibrato->setModDelta(deltaDepth);
}

void OboeSynthMain::setPitchAdsr(float attackTime, float attackDelta, float releaseTime,
                                 float releaseDelta) {
    pitchEnvelope->setStageTimes(attackTime, releaseTime);
    pitchEnvelope->setStageLevels(attackDelta, 0.0f, releaseDelta);
}

void OboeSynthMain::setTremolo(float frequency, float depth) {
    tremoloLFO->setFrequency(frequency);
    //LOGD("OboeSynthMain: setting tremolo depth %f", depth);
    scaledVibrato->setModAmount(depth);
}

void OboeSynthMain::setPWM(float frequency, float depth) {
    pwmOsc->setFrequency(frequency);
    scaledPwmOsc->setModDelta(depth);
}

void OboeSynthMain::setHarmonics(float percent) {
    harmonicsBaseLevel->setValue(percent);
}

void OboeSynthMain::controlTremolo(float deltaDepth) {
    scaledTremolo->setModDelta(deltaDepth);
}

void OboeSynthMain::controlPWM(float deltaDepth) {
    scaledPwmOsc->setModDelta(deltaDepth);
}

void OboeSynthMain::controlHarmonics(float delta) {
    harmonicsShift->setTargetValue(delta);
}

void OboeSynthMain::setVolumeAdsr(float attackTime, float attackDelta, float releaseTime,
                                  float releaseDelta) {
    volumeEnvelope->setStageTimes(attackTime, releaseTime);
    //LOGD("OboeSinePlayers: id 42, volume ASR levels: attackDelta %f, releaseDelta %f", attackDelta, releaseDelta);
    volumeEnvelope->setStageLevels(attackDelta-1.0f, 0, releaseDelta-1.0f); //gets scaled up
}

void OboeSynthMain::setEq(float highGain, float lowGain) {
    lowShelf->configure(kLowShelfFreq, kShelfQ, lowGain);
    lowShelf->configure(kHighShelfFreq, kShelfQ, -highGain);
}

void OboeSynthMain::setHarmonicsAdsr(float attackTime, float attackDelta, float releaseTime,
                                     float releaseDelta) {
    harmonicsEnvelope->setStageTimes(attackTime, releaseTime);
    //LOGD("OboeSinePlayers: harmonics levels parameters: attackDelta %f, releaseDelta %f", attackDelta, releaseDelta);
    harmonicsEnvelope->setStageLevels(attackDelta, 0, releaseDelta);
}

void OboeSynthMain::setVolume(float volume) {
    ampMul->setTargetValue(volume);
    //kAmplitude = volume;
}
