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
        mAmplitude = amplitude;
    };

    virtual float getSineWaveSample(){
        return sinf(mPhase) * mAmplitude;
    }

    virtual float getTriangularWaveSample(){
        return 4.0f * abs(mPhase - 0.5f) - 1.0f;
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

protected:
    enum Waves {Sine = 0, Triangular = 1, Square = 2};

    int waveType = Waves::Square;

    static double constexpr kDefaultFrequency = 440.0;
    static double constexpr kPi = M_PI;
    static double constexpr kTwoPi = kPi * 2;

    float mPhase = 0.0;
    std::atomic<float> mAmplitude{1.0f};
    std::atomic<double> mPhaseIncrement{0.0};
    double mFrequency = kDefaultFrequency;

    float ret = 0.0f;

    void updatePhaseIncrement() {
        mPhaseIncrement.store((kTwoPi * mFrequency) / static_cast<double>(mSampleRate));
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

// Oscillator with frequency controlled by a smoothed value, by a vibrato LFO, by pitch shift
class DynamicOscillator : public NaiveOscillator{
public:
    DynamicOscillator() = default;
    DynamicOscillator(std::shared_ptr<SmoothedFrequency> & smoothedFrequency, std::shared_ptr<LFO> & oscillator){
        setSmoothedFreq(smoothedFrequency);
        setLFO(oscillator);
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

class PWMOsc{

};

#endif //HMI_OSCILLATORS_H
