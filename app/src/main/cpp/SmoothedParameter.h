#ifndef HMI_SMOOTHEDPARAMETER_H
#define HMI_SMOOTHEDPARAMETER_H


class SmoothedParameter {

public:

    SmoothedParameter(const float targetValue, const float alpha = 0.5f, const float startValue = 0.0f) :
    currentValue(startValue), prevRawValue(startValue), targetValue(targetValue), alpha(alpha){

    }

    float smoothed(){
        currentValue = prevRawValue + alpha * (targetValue - currentValue);
        return currentValue;
    }

    float getCurrentValue(){
        return currentValue;
    }

    void setAlpha(float a){
        alpha = a;
    }

    void setTargetValue(float target){
        targetValue = target;
    }

    virtual ~SmoothedParameter() {

    }

private:
    std::atomic<float> currentValue;
    std::atomic<float> prevRawValue;
    std::atomic<float> targetValue;
    std::atomic<float> alpha;
};


#endif //HMI_SMOOTHEDPARAMETER_H
