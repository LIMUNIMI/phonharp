#ifndef HMI_OSCILLATORS_H
#define HMI_OSCILLATORS_H

#include <math.h>
#include "SmoothedFrequency.h"
#include "SampleGenerator.h"
#include "PitchEnvelope.h"

class NaiveOscillator : public SampleGenerator{
public:
    NaiveOscillator() = default;
    virtual ~NaiveOscillator() = default;

    void setFrequency(const double frequency) {
        mFrequency = frequency;
        updatePhaseIncrement();
    };


    inline void setAmplitude(const float amplitude) {
        mAmplitude.store(amplitude);
    };

    virtual float getSineWaveSample(){
        return sinf(mPhase) * mAmplitude;
    }

    virtual float getTriangularWaveSample(){
        return mAmplitude * (((-abs(mPhase - kPi) + kPi) * kOneOverPi) - 0.5f);
    }

    virtual float getSquareWaveSample(){
        if(mPhase <= kPi){
            return -mAmplitude;
        } else {
            return mAmplitude;
        }
    }

    virtual float getNextSample() {
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

        // Square wave
        /*
        if (mPhase <= kPi){
        audioData[i] = -mAmplitude;
        } else {
        audioData[i] = mAmplitude;
        }
        */
        updatePhase();
        return ret;
    };

    void updatePhase(){
        mPhase += mPhaseIncrement;
        if (mPhase > kTwoPi) mPhase -= kTwoPi;
    }

    void setWaveType(int type){
        waveType = type;
    }

    virtual void setSampleRate(float sampleRate){
        mSampleRate = sampleRate;
    }

protected:
    enum Waves {Sine = 0, Triangular = 1, Square = 2};

    int waveType = Waves::Sine;

    static double constexpr kDefaultFrequency = 440.0;
    static double constexpr kPi = M_PI;
    static double constexpr kTwoPi = kPi * 2;
    static double constexpr kOneOverPi = 1/kPi;

    float mPhase = 0.0;
    std::atomic<float> mAmplitude{1.0f};
    std::atomic<double> mPhaseIncrement{0.0};
    double mFrequency = kDefaultFrequency;
    double mSampleRate = 48000.0f;

    float ret = 0.0f;

    void updatePhaseIncrement() {
        mPhaseIncrement.store((kTwoPi * mFrequency) / mSampleRate);
    };
};

// simple wrapper with easy initialization for LFO
class LFO : public NaiveOscillator {
public:
    LFO(){
        setFrequency(5.0f);
        setDepth(1.0f);
    };
    ~LFO() = default;

    void setDepth(const float depth){
        kBaseDepth = depth;
        setAmplitude(depth);
    }

    void resetDepth(){
        setAmplitude(kBaseDepth);
    }

    void deltaDepth(const float delta){
        setAmplitude(mAmplitude + delta);
    }

protected:
    float kBaseDepth = 1.0f;
};

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

#endif //HMI_OSCILLATORS_H
