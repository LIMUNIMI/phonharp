#ifndef HMI_SMOOTHEDPARAMETER_H
#define HMI_SMOOTHEDPARAMETER_H

#include "logging_macros.h"
#include "SampleGenerator.h"
#include <math.h>

class SmoothedParameter : public SampleGenerator{

public:
    SmoothedParameter(){
    };

    virtual ~SmoothedParameter() {}

    virtual float smoothed(){
        if(!isSmoothing()){
            return targetValue;
        }

        --kCountDown;
        if(isSmoothing()){
            setNextValue();
        } else {
            setTargetValue(currentValue);
        }

        return currentValue;
    }

    void setNextValue(){
        if(kMultiplicative){
            currentValue *= step;
        } else {
            currentValue += step;
        }
    }

    bool isSmoothing() const noexcept { return kCountDown > 0; }

    float getCurrentValue(){
        return currentValue;
    }

    void setCurrentValue(float curValue){
        currentValue = curValue;
    }

    void setRawPrevValue(float rawPrevVal){
        prevRawValue.store(rawPrevVal);
    }

    // Doesn't update step, call it after setTargetValue or use setTargetWithSeconds
    void setSecondsToTarget(float seconds){
        kRampLengthInSeconds = seconds;
        kStepsToTarget = (int) floorf(kRampLengthInSeconds * mSampleRate);
    }

    // pass true to make it multiplicative, false will make it linear. Linear by default.
    void setSmoothingType(bool multiplicative){
        kMultiplicative = multiplicative;
    }

    virtual void setTargetWithSeconds(float target, float seconds){
        setSecondsToTarget(seconds);
        setTargetValue(target);
    }

    virtual void setTargetValue(float target){
        if(kMultiplicative && target == 0){
            target += 0.00001f;
        }
        targetValue.store(target);
        updateStep();
    }

    float getTargetValue(){
        return targetValue;
    }

    virtual float getNextSample() override {
        return smoothed();
    }

    void reset(const float base){
        // Set starting values
        setCurrentValue(base);
        setRawPrevValue(base);
        setTargetValue(base);

        // Reset steps countdown
        kCountDown = kStepsToTarget;
    }

    void updateStep(){
        if(kMultiplicative){
            step.store(exp ((log (abs (targetValue)) - log (abs (currentValue))) / (float) kCountDown));
        } else {
            // Linear
            step.store((targetValue - currentValue) / (float) kCountDown);
        }
    }


protected:
    float currentValue = 0.0f;
    std::atomic<float> prevRawValue {0.0f};
    std::atomic<float> targetValue {0.0f};

    float kRampLengthInSeconds = 0.0f;
    int kStepsToTarget = 1;
    int kCountDown = 0;
    bool kMultiplicative = false;
    std::atomic<float> step{0.0f};

    //float alpha = 0.5f;
};


#endif //HMI_SMOOTHEDPARAMETER_H
