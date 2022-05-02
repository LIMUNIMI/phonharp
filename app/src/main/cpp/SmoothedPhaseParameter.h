#ifndef HMI_SMOOTHEDPHASEPARAMETER_H
#define HMI_SMOOTHEDPHASEPARAMETER_H

#include "SmoothedParameter.h"
#include <math.h>

class SmoothedPhaseParameter: public SmoothedParameter{
public:
    SmoothedPhaseParameter(const float startFreq, const float portamento,
                           const int sampleFreq = 48000) :
                           SmoothedParameter(){
        setPortamento(portamento);

        sampleRate = sampleFreq;

        setCurrentValue(startFreq);
        setRawPrevValue(startFreq);
        setTargetFrequency(startFreq);
    }
    void setTargetFrequency(const float targetFreq){
        setTargetValue((targetFreq * kTwoPi) / sampleRate);
    }
    void setPortamento(const float seconds){
        portamento = seconds;
        setAlpha(exp(-1.0f/(seconds*sampleRate)));
    }

    float smoothed() override{
        if(portamento == 0.0f){
            return getTargetValue();
        } else {
            return SmoothedParameter::smoothed();
        }
    }


private:
    float portamento;
    int sampleRate;

    static float constexpr kPI = M_PI;
    static float constexpr kTwoPi = kPI * 2;
};

#endif //HMI_SMOOTHEDPHASEPARAMETER_H
