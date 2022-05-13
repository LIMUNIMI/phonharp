#ifndef HMI_OSCILLATORWRAPPER_H
#define HMI_OSCILLATORWRAPPER_H

#include <math.h>
#include "SmoothedFrequency.h"

constexpr double kDefaultFrequency = 440.0;
constexpr int32_t kDefaultSampleRate = 48000;
constexpr double kPi = M_PI;
constexpr double kTwoPi = kPi * 2;

class NaiveOscillator {
public:
    NaiveOscillator() = default;
    ~NaiveOscillator() = default;

    void setSampleRate(int32_t sampleRate) {
        mSampleRate = sampleRate;
        updatePhaseIncrement();
    };

    void setFrequency(const double frequency) {
        mFrequency = frequency;
        updatePhaseIncrement();
    };


    inline void setAmplitude(const float amplitude) {
        mAmplitude = amplitude;
    };

    virtual float getNextSample() {
        auto ret = sinf(mPhase) * mAmplitude;
        // Square wave
        /*
        if (mPhase <= kPi){
        audioData[i] = -mAmplitude;
        } else {
        audioData[i] = mAmplitude;
        }
        */
        mPhase += mPhaseIncrement;
        if (mPhase > kTwoPi) mPhase -= kTwoPi;
        return ret;
    };

private:
    float mPhase = 0.0;
    std::atomic<float> mAmplitude{0};
    std::atomic<double> mPhaseIncrement{0.0};
    double mFrequency = kDefaultFrequency;
    int32_t mSampleRate = kDefaultSampleRate;

    void updatePhaseIncrement() {
        mPhaseIncrement.store((kTwoPi * mFrequency) / static_cast<double>(mSampleRate));
    };
};

// simple wrapper with easy initialization for LFO
class LFO : public NaiveOscillator {
public:
    LFO(){
        setFrequency(2.0f);
        setDepth(1.0f);
    };
    ~LFO() = default;

    void setDepth(const float depth){
        setAmplitude(depth);
    }
};

// Oscillator with frequency controlled by a smoothed value, by a vibrato LFO, by pitch shift
class DynamicOscillator : public NaiveOscillator{
public:
    DynamicOscillator() = default;
    DynamicOscillator(SmoothedFrequency *smoothedFrequency, LFO *oscillator){
        setSmoothedFreq(smoothedFrequency);
        setLFO(oscillator);
    }
    ~DynamicOscillator() = default;

    void setSmoothedFreq(SmoothedFrequency *smoothedFrequency){
        mSmoothedFrequency = smoothedFrequency;
    }

    void setPitchShift(const float shift){
        pitchShift.store(shift);
    }

    void setLFO(LFO *oscillator){
        mLFO = oscillator;
    }

    void updateFreq(){
        setFrequency(mSmoothedFrequency->smoothed() + mLFO->getNextSample() + pitchShift);
    }

    float getNextSample() {
        updateFreq();

        return NaiveOscillator::getNextSample();
    }

private:
    SmoothedFrequency *mSmoothedFrequency;
    LFO *mLFO;
    std::atomic<float> pitchShift {0.0f};
};


#endif //HMI_OSCILLATORWRAPPER_H
