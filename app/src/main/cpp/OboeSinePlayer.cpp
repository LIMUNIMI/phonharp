#include <logging_macros.h>
#include "OboeSinePlayer.h"

int32_t OboeSinePlayer::initEngine(){
    std::lock_guard <std::mutex> lock(mLock);

    oscillator = new DutyCycleOsc();
    oscillator->id = 21;
    oscillator->setSampleRate(kSampleRate);
    //oscillator->setWaveType(0);

    pwmOsc = new NaiveOscillator();
    pwmOsc->setWaveType(NaiveOscillator::Triangular);
    pwmOsc->setSampleRate(kSampleRate);

    harmonicsBaseLevel = new StaticSignal();
    harmonicsBaseLevel->setValue(0.5f);

    harmoncisEnvelope = new EnvelopeGenerator();
    harmoncisEnvelope->setSampleRate(kSampleRate);
    harmoncisEnvelope->id = 56;

    harmonicsShift = new SmoothedParameter();
    harmonicsShift->setSecondsToTarget(0.1f);
    harmonicsShift->setSampleRate(kSampleRate);

    scaledPwmOsc = new ModulatedSignal(pwmOsc, 0.1f);
    harmMix = new Mix();
    harmMix->addSignal(scaledPwmOsc, 1.0f);
    harmMix->addSignal(harmonicsBaseLevel, 1.0f);
    harmMix->addSignal(harmonicsShift, 1.0f);
    harmMix->addSignal(harmoncisEnvelope, 1.0f);


    smoothedFrequency = new SmoothedFrequency(0.0f);
    smoothedFrequency->setSampleRate(kSampleRate);
    smoothedFrequency->setSmoothingType(false);

    vibratoLFO = new NaiveOscillator();
    vibratoLFO->id = 12;
    vibratoLFO->setSampleRate(kSampleRate);

    pitchEnvelope = new EnvelopeGenerator();
    pitchEnvelope->setSampleRate(kSampleRate);

    pitchShift = new SmoothedParameter();
    pitchShift->setSampleRate(kSampleRate);
    pitchShift->setSecondsToTarget(0.1f);

    freqMix = new Mix();
    //Initialize ModulatedSignal here if you want to change the scaling later. If not, use AddSignal with two parameters and be happy with a static value
    scaledVibrato = new ModulatedSignal(vibratoLFO, 0.5f); //mod amount controlled in settings and real time
    freqMix->addSignal(smoothedFrequency, 1); //Already in semitones, doesn't need to be scaled
    freqMix->addSignal(scaledVibrato);
    freqMix->addSignal(pitchEnvelope, 1); //Already in semitones, doesn't need to be scaled
    freqMix->addSignal(pitchShift, 3.0f);



    tremoloLFO = new NaiveOscillator();
    tremoloLFO->id = 3;
    tremoloLFO->setSampleRate(kSampleRate);

    ampMul = new SmoothedAmpParameter();
    ampMul->setSampleRate(kSampleRate);
    ampMul->setSecondsToTarget(0.1f);

    volumeEnvelope = new EnvelopeGenerator(); //TODO: clipping after two notes. Consider adding smoothing
    volumeEnvelope->setSampleRate(kSampleRate);
    volumeEnvelope->id = 42;

    auto static1 = new StaticSignal();
    static1->setValue(1);

    ampMix = new Mix();
    ampMix->setMixMode(Mix::Mul);
    ampMix->addSignal(static1, 1.0f); //Static scaling is okay
    scaledTremolo = new ModulatedSignal(tremoloLFO, 0.5f);
    scaledTremolo->setBaseOffset(1.000001f);
    ampMix->addSignal(scaledTremolo);
    scaledVolumeEnvelope = new ModulatedSignal(volumeEnvelope, 1.0f);
    scaledVolumeEnvelope->setBaseOffset(1.0f);
    ampMix->addSignal(scaledVolumeEnvelope);

    lowShelf = new Shelf();
    lowShelf->setSampleRate(kSampleRate);
    lowShelf->configure(200, 1, 0);
    highShelf = new Shelf();
    highShelf->setSampleRate(kSampleRate);
    highShelf->configure(4000, 1, 0);

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
    //mStream->stop();
}

oboe::DataCallbackResult
OboeSinePlayer::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {
    //LOGD("onAudioReady: entered audio callback");
    auto *floatData = (float *) audioData;
    for (int i = 0; i < numFrames; ++i) {
        //LOGD("onAudioReady: cycling frame %d", i);
        if(pitchEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF
            &&
            volumeEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF
            &&
            harmoncisEnvelope->getCurrentStage() == EnvelopeGenerator::ENVELOPE_STAGE_OFF
        ){
            for (int j = 0; j < kChannelCount; j++) {
                floatData[i * kChannelCount + j] = 0.0f;
            }
        } else {
            //LOGD("onAudioReady: started making sample");
            float freq = log2lin(freqMix->getNextSample(), 16.35f);
            //LOGD("onAudioReady: got frequency from freqMix %f", freq);
            float dutyCycle = harmMix->getNextSample();

            //LOGD("onAudioReady: harmonics level %f", dutyCycle);

            oscillator->setFrequency(freq);
            oscillator->setDutyCycle(dutyCycle);
            //LOGD("onAudioReady: set frequency from freqMix");

            float osc = oscillator->getNextSample();
            //LOGD("onAudioReady: got sample from osc %f", osc);

            osc = lowShelf->getFilteredSample(osc);
            osc = highShelf->getFilteredSample(osc);
            //LOGD("onAudioReady: filtered sample from osc %f", osc);

            float volumeMix = ampMix->getNextSample() * 2;//ampMul->getNextSample();
            //LOGD("onAudioReady: got volume mix %f", volumeMix);
            //volumeMix = volumeMix <= 0.0000001f ? 0.0000001f : volumeMix;
            //volumeMix = volumeMix * ampMix->getNextSample();

            //LOGD("onAudioReady: capped volume mix %f", volumeMix);
            //volumeMix = log10f(volumeMix/0.0000001f);
            //LOGD("onAudioReady: DB volume mix %f", volumeMix);
            float sampleValue = kAmplitude * osc * volumeMix;// * volumeMix;
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
        pitchEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK); //Needs to be called here too
        volumeEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        harmoncisEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        isPlaying.store(true);
        LOGD("startAudio: stored start playing TRUE");
    } else {
        LOGD("startAudio: Smoothing, destFreq: %f, currentFreq: %f", freq, smoothedFrequency->getCurrentValue());
        smoothedFrequency->setTargetFrequency(freq);
    }
    // Typically, start the stream after querying some stream information, as well as some input from the user
    pitchEnvelope->on();
    volumeEnvelope->on();
    harmoncisEnvelope->on();
    //LOGD("startAudio: new Freq %d", isNewFreq);
    if(isNewFreq){
        pitchEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        volumeEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
        harmoncisEnvelope->enterStage(EnvelopeGenerator::ENVELOPE_STAGE_ATTACK);
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
    //ampMul->applyDeltaToTarget(deltaAmp);
    //AmpMul is being used as an absolute because in OboeSynth it takes the value from the gyro which is absolute.
    //Consider doing something similar to the pitchShift if you want to use the fingers.
    ampMul->setTargetValue(deltaAmp);
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
    pitchShift->setTargetValue(deltaPitch);
}

void OboeSinePlayer::controlReset() {
    LOGD("=============CONTROL RESET=============");
    //oscillator->setPitchShift(0);
    scaledVibrato->reset();
    smoothedFrequency->reset(kFrequency);
    pitchShift->reset(0.0f);
    scaledTremolo->reset();
    scaledPwmOsc->reset();
    scaledVolumeEnvelope->reset(); //doesn't need reset but whatever
    harmonicsShift->reset(0.0f);
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
    pitchEnvelope->setStageTimes(attackTime, releaseTime);
    pitchEnvelope->setStageLevels(attackDelta, 0.0f, releaseDelta);
}

void OboeSinePlayer::setTremolo(float frequency, float depth) {
    tremoloLFO->setFrequency(frequency);
    //LOGD("OboeSinePlayer: setting tremolo depth %f", depth);
    scaledVibrato->setModAmount(depth);
}

void OboeSinePlayer::setPWM(float frequency, float depth) {
    pwmOsc->setFrequency(frequency);
    scaledPwmOsc->setModDelta(depth);
}

void OboeSinePlayer::setHarmonics(float percent) {
    harmonicsBaseLevel->setValue(percent);
}

void OboeSinePlayer::controlTremolo(float deltaDepth) {
    scaledTremolo->setModDelta(deltaDepth);
}

void OboeSinePlayer::controlPWM(float deltaDepth) {
    //oscillator->triangleModulator.deltaDepth(deltaDepth/60);
}

void OboeSinePlayer::controlHarmonics(float delta) {
    harmonicsShift->setTargetValue(delta);
}

void OboeSinePlayer::setVolumeAdsr(float attackTime, float attackDelta, float releaseTime,
                                   float releaseDelta) {
    volumeEnvelope->setStageTimes(attackTime, releaseTime);
    //LOGD("OboeSinePlayers: id 42, volume ASR levels: attackDelta %f, releaseDelta %f", attackDelta, releaseDelta);
    volumeEnvelope->setStageLevels(attackDelta-1.0f, 0, releaseDelta-1.0f); //gets scaled up
}

void OboeSinePlayer::setEq(float highGain, float lowGain) {
    lowShelf->configure(10, 1, lowGain);
    lowShelf->configure(10000, 1, -highGain);
}

void OboeSinePlayer::setHarmonicsAdsr(float attackTime, float attackDelta, float releaseTime,
                                      float releaseDelta) {
    harmoncisEnvelope->setStageTimes(attackTime, releaseTime);
    LOGD("OboeSinePlayers: harmonics levels parameters: attackDelta %f, releaseDelta %f", attackDelta, releaseDelta);
    harmoncisEnvelope->setStageLevels(attackDelta, 0, releaseDelta);
}
