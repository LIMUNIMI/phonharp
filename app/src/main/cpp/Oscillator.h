#ifndef HMI_OSCILLATOR_H
#define HMI_OSCILLATOR_H

#include <math.h>

class Oscillator{
public:
    Oscillator(const float sampleRate, const float frequency = 200.0f) : oSampleRate(sampleRate){
        oFrequency = frequency;
    }

    void setFrequency(const float frequency){
        oFrequency = frequency;
        oPhaseInc = getPhaseInc();
    }

    float getNextSample(){
        oPrevAmplitude = oAmplitude;

        oPhase += oPhaseInc;
        if (oPhase >= oTwoPi) oPhase -= oTwoPi;
        oAmplitude = sinf(oPhase);

        return oPrevAmplitude
    }

private:

    float getPhaseInc(){
        return (oFrequency * oTwoPi) / oSampleRate;
    }

    float oSampleRate;
    std::atomic<float> oFrequency;
    std::atomic<float> oPhaseInc;
    float oPhase = 0;
    float oAmplitude = 0;
    float oPrevAmplitude;

    static float constexpr oPI = M_PI;
    static float constexpr oTwoPi = oPI * 2;
};


#endif //HMI_OSCILLATOR_H
