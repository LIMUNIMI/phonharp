#ifndef HMI_SMOOTHEDPARAMETER_H
#define HMI_SMOOTHEDPARAMETER_H

#include "logging_macros.h"
#include <math.h>

class SmoothedParameter {

public:

    SmoothedParameter(const float targetValue, const float alpha = 0.5f, const float startValue = 0.0f) :
    currentValue(startValue), prevRawValue(startValue), targetValue(targetValue), alpha(alpha){

    }

    SmoothedParameter(){
    };

    virtual float smoothed(){
        //LOGD("Smoothing\n currentValue: %f\n prevRawValue: %f\n alpha: %f\n targetValue: %f\n", currentValue, prevRawValue, alpha, targetValue);
        currentValue = prevRawValue + alpha * (targetValue - currentValue);
        prevRawValue.store(targetValue);
        //LOGD("Smoothed value: %f\n", currentValue);
        return currentValue;
    }

    float getCurrentValue(){
        return currentValue;
    }

    void setCurrentValue(float curValue){
        currentValue = curValue;
    }

    void setRawPrevValue(float rawPrevVal){
        prevRawValue.store(rawPrevVal);
    }

    void setAlpha(float a){
        alpha = a;
    }

    void setAlphaFromSeconds(float s, float sampleRate){
        setAlpha(exp(-1.0f/(s*sampleRate)));
    }

    virtual void setTargetValue(float target){
        prevRawValue.store(targetValue);
        targetValue.store(target);
    }

    float getTargetValue(){
        return targetValue;
    }

    virtual ~SmoothedParameter() {

    }

protected:
    float currentValue = 0.0f;
    std::atomic<float> prevRawValue {0.0f};
    std::atomic<float> targetValue {0.0f};
    float alpha = 0.5f;
};


#endif //HMI_SMOOTHEDPARAMETER_H
