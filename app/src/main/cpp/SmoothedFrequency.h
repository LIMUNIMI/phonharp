#ifndef HMI_SMOOTHEDFREQUENCY_H
#define HMI_SMOOTHEDFREQUENCY_H

#include "SmoothedParameter.h"
#include <math.h>

class SmoothedFrequency: public SmoothedParameter{
public:
    SmoothedFrequency(const float portamento) : SmoothedParameter(){

        setPortamento(portamento);
    }

    void setTargetFrequency(const float targetFreq){
        setTargetValue(targetFreq);
    }

    void setPortamento(const float seconds){
        setSecondsToTarget(seconds);
    }

    float getStep(){
        return step;
    }

    float smoothed() override{
        //LOGD("Smoothing %f to %f, leftover steps %d, increment %f", currentValue, getTargetValue(), kCountDown, getStep());
        return SmoothedParameter::smoothed();
    }
};

#endif //HMI_SMOOTHEDFREQUENCY_H
