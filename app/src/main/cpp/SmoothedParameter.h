#ifndef HMI_SMOOTHEDPARAMETER_H
#define HMI_SMOOTHEDPARAMETER_H

#include "logging_macros.h"

class SmoothedParameter {

public:

    SmoothedParameter(const float targetValue, const float alpha = 0.5f, const float startValue = 0.0f) :
    currentValue(startValue), prevRawValue(startValue), targetValue(targetValue), alpha(alpha){

    }

    SmoothedParameter(){
        alpha = 0.5f;
        prevRawValue = 0.0f;
        currentValue = 1.0f;
    };

    virtual float smoothed(){
        //LOGD("Smoothing\n currentValue: %f\n prevRawValue: %f\n alpha: %f\n targetValue: %f\n", currentValue, prevRawValue, alpha, targetValue);
        currentValue = prevRawValue + alpha * (targetValue - currentValue);
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
        prevRawValue = rawPrevVal;
    }

    virtual void setAlpha(float a){
        alpha = a;
    }

    virtual void setTargetValue(float target){
        prevRawValue = targetValue;
        targetValue = target;
    }

    float getTargetValue(){
        return targetValue;
    }

    virtual ~SmoothedParameter() {

    }

protected:
    float currentValue;
    float prevRawValue;
    float targetValue;
    float alpha;
};


#endif //HMI_SMOOTHEDPARAMETER_H
