#ifndef HMI_OSCILLATORS_H
#define HMI_OSCILLATORS_H

#include <math.h>
#include "SmoothedFrequency.h"

class NaiveOscillator {
public:
    NaiveOscillator() = default;
    virtual ~NaiveOscillator() = default;

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

protected:

    static double constexpr kDefaultFrequency = 440.0;
    static int32_t constexpr kDefaultSampleRate = 48000;
    static double constexpr kPi = M_PI;
    static double constexpr kTwoPi = kPi * 2;

    float mPhase = 0.0;
    std::atomic<float> mAmplitude{1.0f};
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
        setFrequency(5.0f);
        setDepth(1.0f);
    };
    ~LFO() = default;

    void setDepth(const float depth){
        setAmplitude(depth);
    }

    void deltaDepth(const float delta){
        setAmplitude(mAmplitude + delta);
    }
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

    void updateFreq(){
        setFrequency(mSmoothedFrequency->smoothed() + mLFO->getNextSample() + pitchShift);
    }

    float getNextSample() override{
        updateFreq();
        return NaiveOscillator::getNextSample();
    }

private:
    std::shared_ptr<SmoothedFrequency> mSmoothedFrequency;
    std::shared_ptr<LFO> mLFO;
    std::atomic<float> pitchShift {0.0f};
};


#endif //HMI_OSCILLATORS_H
