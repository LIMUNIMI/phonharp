#include <logging_macros.h>
#include "OboeSinePlayer.h"

int32_t OboeSinePlayer::initEngine(){
    std::lock_guard <std::mutex> lock(mLock);

    ampMul = new SmoothedAmpParameter();
    smoothedFrequency = new SmoothedFrequency(0.0f);
    smoothedFrequency->setSampleRate(kSampleRate);
    smoothedFrequency->setSmoothingType(false);

    vibratoLFO = new NaiveOscillator();
    vibratoLFO->setSampleRate(kSampleRate);
    vibratoLFO->setFrequency(5.0f);
    pitchEnvelope = new PitchEnvelope();

    oscillator = new DutyCycleOsc();
    oscillator->setSampleRate(kSampleRate);
    oscillator->setWaveType(0); //TODO: remove

    freqMix = new Mix();
    scaledVibrato = new ModulatedSignal(vibratoLFO, 0.2f);
    freqMix->addSignal(smoothedFrequency, 1);
    freqMix->addSignal(scaledVibrato);
    //freqMix->addSignal(spPitchEnvelope, 1);

    tremoloLFO = new NaiveOscillator();
    tremoloLFO->setSampleRate(kSampleRate);

    volumeEnvelope = new DeltaEnvelopeGenerator();

    harmoncisEnvelope = new DeltaEnvelopeGenerator();
    //oscillator->setHarmonicsEnvelope(harmoncisEnvelope);

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
    volumeEnvelope->off();
    harmoncisEnvelope->off();
    //ADSR envelope off

    if (mStream && !isPlaying) {
        LOGD("OboeSinePlayer:: STOPPED STREAM");
        mStream->stop();
    }

    isPlaying.store(false);
    mStream->stop();
}

oboe::DataCallbackResult
OboeSinePlayer::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {
    //LOGD("onAudioReady: entered audio callback");
    auto *floatData = (float *) audioData;
    for (int i = 0; i < numFrames; ++i) {
        //LOGD("onAudioReady: cycling frame %d", i);
        if(!isPlaying
        /*
        (pitchEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF &&
        harmoncisEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF) ||
        volumeEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF
         */
        ){
            for (int j = 0; j < kChannelCount; j++) {
                floatData[i * kChannelCount + j] = 0.0f;
            }
        } else {
            //LOGD("onAudioReady: started making sample");
            float freq = log2lin(freqMix->getNextSample(), 16.35f);
            LOGD("onAudioReady: got frequency from freqMix %f", freq);
            oscillator->setFrequency(freq);
            //LOGD("onAudioReady: set frequency from freqMix");
            float osc = oscillator->getNextSample();
            //LOGD("onAudioReady: got sample from osc");
            float volumeMix = ampMul->smoothed(); //+ volumeEnvelope->getNextSample() + tremoloLFO->getNextSample();
            //LOGD("onAudioReady: got smoothed amp");
            if(ampMul->getCurrentValue() <= 0.00001f){
                //LOGD("controlAmpMul: ZERO");
                volumeMix = 0.0f;
            } //else {
                //LOGD("controlAmpMul: mix %f", ampMul->getCurrentValue());
            //}
            float sampleValue = kAmplitude * osc * 50;// * volumeMix;
            //LOGD("onAudioReady: sample %f", sampleValue);
            //TODO: applica i filtri
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
        LOGD("startAudio: reset frequency");
        //pitchEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK); //Needs to be called here too
        //volumeEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        //harmoncisEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        isPlaying.store(true);
        LOGD("startAudio: stored start playing TRUE");
    } else {
        LOGD("startAudio: Smoothing, destFreq: %f, currentFreq: %f", freq, smoothedFrequency->getCurrentValue());
        smoothedFrequency->setTargetFrequency(freq);
    }
    // Typically, start the stream after querying some stream information, as well as some input from the user
    //pitchEnvelope->onWithBaseFreq(freq);
    //volumeEnvelope->onWithBaseValue(kAmplitude);
    //harmonicsEnvelope->
    //LOGD("startAudio: new Freq %d", isNewFreq);
    if(isNewFreq){
        //pitchEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        //volumeEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        //harmoncisEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
    }
    if (mStream) {
        LOGD("startAudio: requesting stream start");
        result = mStream->requestStart();
        LOGD("startAudio: requested stream start");
    }
    return (int32_t) result;
}

void OboeSinePlayer::setFrequency(float frequency) {
    //TODO: consider removing
    smoothedFrequency->setTargetFrequency(frequency);
    kFrequency.store(frequency);
}

void OboeSinePlayer::controlAmpMul(float deltaAmp){
    //LOGD("controlAmpMul: delta amp %f", deltaAmp);
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
    //oscillator->setPitchShift(log2lin(deltaPitch, kFrequency));
}

void OboeSinePlayer::controlReset() {
    LOGD("=============CONTROL RESET=============");
    //oscillator->setPitchShift(0);
    scaledVibrato->reset();
    smoothedFrequency->reset(kFrequency);
    //oscillator->resetDutyCycle();
    //oscillator->triangleModulator.resetDepth();
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
    //return exp((logf(2)*(semitonesDelta + 12 * logf(baseFreq)))/12);
    //return expf(logf(baseFreq)-(logf(2.0f)*semitonesDelta)/12);
    //return expf(logf(baseFreq) + semitonesDelta/12);
    //return expf(((logf(2.0f)*semitonesDelta)/12) - logf(baseFreq));
    return expf(semitonesDelta * (logf(2)/12) ) * baseFreq;
}

void OboeSinePlayer::setVibrato(float frequency, float depth) {
    //LOGD("Vibratofreq: %f", frequency);
    vibratoLFO->setFrequency(frequency);
    scaledVibrato->setModAmount(depth);
    //vibratoLFO->setDepth(depth);
}

void OboeSinePlayer::controlVibrato(float deltaDepth) {
   scaledVibrato->setModDelta(deltaDepth);
}

void OboeSinePlayer::setPitchAdsr(float attackTime, float attackDelta, float releaseTime,
                                  float releaseDelta) {
    pitchEnvelope->setStageTimes(attackTime, 0.1f, releaseTime);
    pitchEnvelope->setAttackDelta(attackDelta);
    pitchEnvelope->setReleaseDelta(releaseDelta);
}

void OboeSinePlayer::setTremolo(float frequency, float depth) {
    //tremoloLFO->setDepth(depth);
    tremoloLFO->setFrequency(frequency);
}

void OboeSinePlayer::setPWM(float frequency, float depth) {
    //oscillator->triangleModulator.setFrequency(frequency);
    //oscillator->triangleModulator.setDepth(depth/150);
}

void OboeSinePlayer::setHarmonics(float percent) {
    oscillator->setDutyCycle(percent/200.0f);
}

void OboeSinePlayer::controlTremolo(float deltaDepth) {
    //tremoloLFO->deltaDepth(deltaDepth);
    //TODO: check for weird behavior
}

void OboeSinePlayer::controlPWM(float deltaDepth) {
    //oscillator->triangleModulator.deltaDepth(deltaDepth/60);
}

void OboeSinePlayer::controlHarmonics(float delta) {
    //oscillator->deltaDutyCycle(delta/5);
    //LOGD("OboeSinePlayer::controlHarmonics: delta %f, current duty cycle %f", delta/5, oscillator->getCurrentDutyCycle());
}

void OboeSinePlayer::setVolumeAdsr(float attackTime, float attackDelta, float releaseTime,
                                   float releaseDelta) {
    volumeEnvelope->setStageTimes(attackTime, 0.1f, releaseTime);
    volumeEnvelope->setAttackDelta(attackDelta);
    volumeEnvelope->setReleaseDelta(releaseDelta);
}

void OboeSinePlayer::setEq(float highGain, float lowGain) {
    //TODO: scrivere filtri.
}

void OboeSinePlayer::setHarmonicsAdsr(float attackTime, float attackDelta, float releaseTime,
                                      float releaseDelta) {
    harmoncisEnvelope->setStageTimes(attackTime, 0.1f, releaseTime);
    harmoncisEnvelope->setAttackDelta(attackDelta);
    harmoncisEnvelope->setReleaseDelta(releaseDelta);
}
