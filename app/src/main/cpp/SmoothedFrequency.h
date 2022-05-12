#ifndef HMI_SMOOTHEDFREQUENCY_H
#define HMI_SMOOTHEDFREQUENCY_H

#include "SmoothedParameter.h"
#include <math.h>

class SmoothedFrequency: public SmoothedParameter{
public:
    SmoothedFrequency(const float startFreq, const float portamento,
                           const int sampleFreq = 48000) :
                           SmoothedParameter(){
        setPortamento(portamento);

        sampleRate = sampleFreq;

        setCurrentValue(startFreq);
        setRawPrevValue(startFreq);
        setTargetFrequency(startFreq);
    }
    void setTargetFrequency(const float targetFreq){
        setRawPrevValue(getTargetValue());
        setTargetValue(targetFreq);
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
};

#endif //HMI_SMOOTHEDFREQUENCY_H
