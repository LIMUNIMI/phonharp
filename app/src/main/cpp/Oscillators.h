#ifndef HMI_OSCILLATORS_H
#define HMI_OSCILLATORS_H

#include <math.h>
#include "SmoothedFrequency.h"
#include "SampleGenerator.h"
#include <list>

class NaiveOscillator : public SampleGenerator{
public:
    enum Waves {Sine = 0, Triangular = 1, Square = 2};

    NaiveOscillator() = default;
    virtual ~NaiveOscillator() = default;

    void setFrequency(const double frequency) {
        mFrequency = frequency;
        //LOGD("NaiveOscillator: setting freq %f", frequency);
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
        mPhase = fmod(mPhase, 1.0f);
    }

    void setWaveType(int type){
        waveType = type;
    }

    virtual void setSampleRate(float sampleRate){
        mSampleRate = sampleRate;
        updatePhaseIncrement();
    }

protected:

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
        //LOGD("DutyCycleOsc: (%f > %f = %d) - (%f < %f = %d)  = %d", mPhase, dutyCycle.load(), mPhase > dutyCycle, mPhase, dutyCycle.load(), mPhase < dutyCycle, ((mPhase > dutyCycle) - (mPhase < dutyCycle)));
        return mAmplitude * ((mPhase > dutyCycle) - (mPhase < dutyCycle));
    }

    void setDutyCycle(const float amount){
        dutyCycle.store(amount);
    }

protected:
    std::atomic<float> dutyCycle{0.5f};
};


class ModulatedSignal : public SampleGenerator{
public:

    ModulatedSignal(){};

    ModulatedSignal(SampleGenerator*  signal, const float amount){
        setSignal(signal);
        setModAmount(amount);
    }

    virtual float getNextSample() override {
        float sample = baseOffset + (mSignal->getNextSample() * (modAmount+mDelta));
        //return baseOffset + (mSignal->getNextSample() * (modAmount+mDelta));
        //LOGD("ModulatedSignal: sample from signal is %f", sample);
        //sample = sample * (modAmount+mDelta);
        //LOGD("ModulatedSignal: sample scaled is %f", sample);
        return sample;
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

    void setBaseOffset(const float value){
        baseOffset.store(value);
    }

protected:
    SampleGenerator* mSignal;
    std::atomic<float> modAmount{1.0f};
    std::atomic<float> mDelta{0.0f};
    std::atomic<float> baseOffset{0.0f};
};

class Mix : public SampleGenerator{
public:
    enum MixMode {Sum = 0, Mul = 1};

    Mix(){

    }

    void addSignal(SampleGenerator*  signal, const float modAmount){
        signals.push_back(new ModulatedSignal(signal, modAmount));
    }

    void addSignal(ModulatedSignal *modulatedSignal){
        signals.push_back(modulatedSignal);
    }

    float getNextSample() override {
        switch (mode) {
            case MixMode::Mul:
                ret = 1.0f;
                break;
            case MixMode::Sum:
                ret = 0.0f;
                break;
        }

        //LOGD("Mix: getting sample");
        for(itr = signals.begin(); itr != signals.end(); ++itr){
            //LOGD("Mix: cycling sources");
            switch (mode) {
                case MixMode::Mul:
                    ret *= (*itr)->getNextSample();
                    break;
                case MixMode::Sum:
                    ret += (*itr)->getNextSample();
                    break;
            }
            //LOGD("Mix: mix from sources %f", ret);
        }
        //LOGD("Mix: mix from sources %f", ret);
        return ret;
    }

    void setMixMode(MixMode type){
        mode = type;
    }

protected:
    std::list<ModulatedSignal*> signals;
    std::list<ModulatedSignal*>::iterator itr;

    MixMode mode = MixMode::Sum;

    float ret = 0.0f;
};

#endif //HMI_OSCILLATORS_H
