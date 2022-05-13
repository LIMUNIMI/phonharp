#ifndef HMI_OSCILLATORWRAPPER_H
#define HMI_OSCILLATORWRAPPER_H

#include <math.h>
#include <Oscillator.h>

class OscillatorWrapper : public Oscillator{
public:
    OscillatorWrapper(const float sampleRate, SmoothedParameter *frequency, Oscillator *vibratoLFO) : oSampleRate(sampleRate){
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

        return oPrevAmplitude;
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


#endif //HMI_OSCILLATORWRAPPER_H
