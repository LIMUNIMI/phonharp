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

        setCurrentValue(getSampleInc(startFreq));
        setRawPrevValue(getSampleInc(startFreq));
        setTargetFrequency(startFreq);
    }
    float getSampleInc(float frequency){
        return (frequency * kTwoPi) / sampleRate;
    }

    void setTargetFrequency(const float targetFreq){
        setRawPrevValue(getTargetValue());
        setTargetValue(getSampleInc(targetFreq));
    }
    void setPortamento(const float seconds){
        portamento = seconds;
        setAlpha(exp(-1.0f/(seconds*sampleRate)));
        //setAlpha(0.1f);
    }

    float smoothed() override{
        //LOGD("SMOO: targetValue: %f", targetValue);
        if(portamento == 0.0f){
            return getTargetValue();
        } else {

            float smoothed = SmoothedParameter::smoothed();
            //LOGD("\n    SMOO:  smoothedValue: %f", smoothed);
            return smoothed;
        }
    }


private:
    float portamento;
    int sampleRate;

    static float constexpr kPI = M_PI;
    static float constexpr kTwoPi = kPI * 2;
};

#endif //HMI_SMOOTHEDPHASEPARAMETER_H
