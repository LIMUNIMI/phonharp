#ifndef HMI_OSCILLATORS_H
#define HMI_OSCILLATORS_H

#include <math.h>
#include "SmoothedFrequency.h"
#include "SampleGenerator.h"
#include "PitchEnvelope.h"
#include <list>

class NaiveOscillator : public SampleGenerator{
public:
    NaiveOscillator() = default;
    virtual ~NaiveOscillator() = default;

    void setFrequency(const double frequency) {
        mFrequency = frequency;
        LOGD("NaiveOscillator: setting freq %f", frequency);
        updatePhaseIncrement();
    };


    inline void setAmplitude(const float amplitude) {
        mAmplitude.store(amplitude);
    };

    virtual float getSineWaveSample(){
        //LOGD("NaiveOscillator: id %d, returning a sample %f, mAmp %f, mPhase %f, sinPhase %f, phaseInc %f", id, mAmplitude * sinf(mPhase * kTwoPi), mAmplitude.load(), mPhase, sinf(mPhase * kTwoPi), mPhaseIncrement.load());
        return mAmplitude * sinf(mPhase * kTwoPi);
    }

    virtual float getTriangularWaveSample(){
        return mAmplitude * (4.0f * abs(mPhase - 0.5f) -1.0f);
    }

    virtual float getSquareWaveSample(){
        return mAmplitude * ((mPhase > 0.5f) - (mPhase < 0.5f));
    }

    virtual float getNextSample() {
        //LOGD("NaiveOscillator: getting next sample");
        switch (waveType) {
            case Waves::Sine:
                ret = getSineWaveSample();
                break;
            case Waves::Triangular:
                ret = getTriangularWaveSample();
                break;
            case Waves::Square:
                ret = getSquareWaveSample();
                break;
            default:
                ret = getSineWaveSample();
                break;
        }
        //LOGD("NaiveOscillator: updateing phase");
        updatePhase();
        //LOGD("NaiveOscillator: returning %f", ret);
        return ret;
    };

    void updatePhase(){
        mPhase += mPhaseIncrement;
    }

    void setWaveType(int type){
        waveType = type;
    }

    virtual void setSampleRate(float sampleRate){
        mSampleRate = sampleRate;
        updatePhaseIncrement();
    }

    int id = 0;

protected:
    enum Waves {Sine = 0, Triangular = 1, Square = 2};

    int waveType = Waves::Sine;

    static double constexpr kDefaultFrequency = 440.0;
    static double constexpr kPi = M_PI;
    static double constexpr kTwoPi = kPi * 2;

    float mPhase = 0.0;
    std::atomic<float> mAmplitude{1.0f};
    std::atomic<double> mPhaseIncrement{0.0};

    double mFrequency = kDefaultFrequency;
    double mSampleRate = 48000.0f;

    float ret = 0.0f;

    void updatePhaseIncrement() {
        //LOGD("NaiveOscillator: id %d, setting phase inc %f", id, mFrequency/mSampleRate);
        mPhaseIncrement.store(mFrequency / mSampleRate);
        //LOGD("NaiveOscillator: phase inc is %f", mPhaseIncrement.load());
    };
};

class DutyCycleOsc : public NaiveOscillator{
public:
    DutyCycleOsc(){
        setWaveType(Waves::Square);
    }

    float getSquareWaveSample() override {
        //TODO: FIX
        return mAmplitude * ((mPhase > dutyCycle) - (mPhase < dutyCycle));
    }

    void setDutyCycle(const float amount){
        dutyCycle.store(amount);
    }

protected:
    std::atomic<float> dutyCycle{0.5f};
};

/*

class PWMOsc : public NaiveOscillator{
public:
    PWMOsc(){
        waveType = Waves::Square;
        triangleModulator.setWaveType(Waves::Triangular);
        triangleModulator.setDepth(0.05f);
        currentDutyCycle.setSecondsToTarget(0.50f);

    }

    void setHarmonicsEnvelope(std::shared_ptr<DeltaEnvelopeGenerator> & harmonicsEnvelope){
        mHarmonicsEnvelope = harmonicsEnvelope;
    }

    float getSquareWaveSample() override {
        float threshold = currentDutyCycle.getNextSample() + triangleModulator.getNextSample();
        if(mHarmonicsEnvelope != nullptr ){
            threshold += mHarmonicsEnvelope->getNextSample();
        }
        threshold = threshold >= upperBound ? upperBound : threshold;
        threshold = threshold <= lowerBound ? lowerBound : threshold;
        //LOGD("Harmonics threshold: %f, currentDutyCycle %f, baseDutyCycle %f", threshold, currentDutyCycle.getCurrentValue(), baseDutyCycle);
        threshold = threshold * kTwoPi;
        if(mPhase > threshold){
            return -mAmplitude;
        } else {
            return mAmplitude;
        }
    }

    void setDutyCycle(float dutyCycle){
        LOGD("PWMOsc::setDutyCycle: base duty cycle harmonics %f", dutyCycle);
        baseDutyCycle = dutyCycle;
    }

    float getCurrentDutyCycle(){
        return currentDutyCycle.getCurrentValue();
    }

    void resetDutyCycle(){
        currentDutyCycle.reset(baseDutyCycle);
    }

    void deltaDutyCycle(const float delta){
        currentDutyCycle.setTargetValue(baseDutyCycle + delta);
    }

    void setSampleRate(float sampleRate) override{
        triangleModulator.setSampleRate(sampleRate);
        NaiveOscillator::setSampleRate(sampleRate);
    }

    LFO triangleModulator;

protected:
    const float lowerBound = 0.1f;
    const float upperBound = 0.9999999f;
    float baseDutyCycle = 0.5f; //between 0 and 1
    SmoothedParameter currentDutyCycle;
    std::shared_ptr<DeltaEnvelopeGenerator> mHarmonicsEnvelope;
    //std::atomic<float> currentDutyCycle{0.5f};
};

// Oscillator with frequency controlled by a smoothed value, by a vibrato LFO, by pitch shift
class DynamicOscillator : public PWMOsc{
public:
    DynamicOscillator(){
        PWMOsc();
    };
    DynamicOscillator(std::shared_ptr<SmoothedFrequency> & smoothedFrequency, std::shared_ptr<LFO> & oscillator){
        setSmoothedFreq(smoothedFrequency);
        setLFO(oscillator);
        PWMOsc();
    }
    ~DynamicOscillator() = default;

    void setSmoothedFreq(std::shared_ptr<SmoothedFrequency> & smoothedFrequency){
        mSmoothedFrequency = smoothedFrequency;
    }

    void setPitchShift(const float shift){
        pitchShift.store(shift);
    }

    void setLFO(std::shared_ptr<LFO> & oscillator){
        mLFO = oscillator;
    }

    void setPitchEnvelope(std::shared_ptr<PitchEnvelope> & pitchEnvelope){
        mPitchEnvelope = pitchEnvelope;
    }

    void updateFreq(){
        //CAREFUL, breaks sound
        //LOGD("Oscillators::updateFreq: current smoothed freq: %f", mSmoothedFrequency->getCurrentValue());
        setFrequency(
                mSmoothedFrequency->smoothed()
              + mLFO->getNextSample()
              + pitchShift
              + mPitchEnvelope->getNextSample() // goes from a delta to freq to a delta
        );
    }

    float getNextSample() override{
        updateFreq();
        return NaiveOscillator::getNextSample();
    }

private:
    std::shared_ptr<SmoothedFrequency> mSmoothedFrequency;
    std::shared_ptr<LFO> mLFO;
    std::shared_ptr<PitchEnvelope> mPitchEnvelope;
    std::atomic<float> pitchShift {0.0f};
};
*/

class ModulatedSignal : public SampleGenerator{
public:

    ModulatedSignal(){};

    ModulatedSignal(SampleGenerator*  signal, const float amount){
        setSignal(signal);
        setModAmount(amount);
    }

    virtual float getNextSample() override {
        //float sample = mSignal->getNextSample() * (modAmount+mDelta);
        return mSignal->getNextSample() * (modAmount+mDelta);
        //LOGD("ModulatedSignal: sample from signal is %f", sample);
        //sample = sample * (modAmount+mDelta);
        //LOGD("ModulatedSignal: sample scaled is %f", sample);
        //return sample;
    }

    void setModAmount(const float amount){
        modAmount.store(amount);
    }

    void setSignal(SampleGenerator* signal){
        mSignal = signal;
    }

    void reset(){
        setModDelta(0.0f);
    }

    void setModDelta(const float delta){
        mDelta.store(delta);
    }

protected:
    SampleGenerator* mSignal;
    std::atomic<float> modAmount{1.0f};
    std::atomic<float> mDelta{0.0f};
};

class Mix : public SampleGenerator{
public:
    Mix(){

    }

    void addSignal(SampleGenerator*  signal, const float modAmount){
        signals.push_back(new ModulatedSignal(signal, modAmount));
    }

    void addSignal(ModulatedSignal *modulatedSignal){
        signals.push_back(modulatedSignal);
    }

    float getNextSample() override {
        ret = 0.0f;
        //LOGD("Mix: getting sample");
        for(itr = signals.begin(); itr != signals.end(); ++itr){
            //LOGD("Mix: cycling sources");
            ret += (*itr)->getNextSample();
            //LOGD("Mix: mix from sources %f", ret);
        }
        //LOGD("Mix: mix from sources %f", ret);
        return ret;
    }

protected:
    std::list<ModulatedSignal*> signals;
    std::list<ModulatedSignal*>::iterator itr;

    float ret = 0.0f;
};

#endif //HMI_OSCILLATORS_H
