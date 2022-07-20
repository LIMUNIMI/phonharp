#ifndef HMI_SMOOTHEDFREQUENCY_H
#define HMI_SMOOTHEDFREQUENCY_H

#include "SmoothedParameter.h"
#include <math.h>

class SmoothedFrequency: public SmoothedParameter{
public:
    SmoothedFrequency(const float portamento) : SmoothedParameter(){

        setPortamento(portamento);
        setSmoothingType(true);
    }

    void setTargetFrequency(const float targetFreq){
        setTargetValue(convertFreqToSemitones(targetFreq));
        LOGD("SmoothedFrequency::setTargetFrequency: Smoothing %f to %f, leftover steps %d, increment %f", getCurrentValue(), getTargetValue(), kCountDown, getStep());
    }

    void setPortamento(const float seconds){
        setSecondsToTarget(seconds);
    }

    float getStep(){
        return step;
    }

    float convertFreqToSemitones(const float freq){
        //16.35 è C0
        return 12.0f * log2f(freq / 16.35f);
    }

    float smoothed() override{
        //LOGD("SmoothedFrequency: Smoothing %f to %f, leftover steps %d, increment %f", getCurrentValue(), getTargetValue(), kCountDown, getStep());
        if(!isSmoothing()){
            //LOGD("SmoothedFrequency: reached stable frequency %f, leftover steps %d", getTargetValue(), kCountDown);
        }
        return SmoothedParameter::smoothed();
    }
};

#endif //HMI_SMOOTHEDFREQUENCY_H
