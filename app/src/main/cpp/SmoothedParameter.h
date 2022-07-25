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
            //setTargetValue(currentValue);
        }

        return currentValue;
    }

    void setNextValue(){
        if(kMultiplicative){
            currentValue.store(currentValue * step);
        } else {
            currentValue.store(currentValue + step);
        }
    }

    bool isSmoothing() const noexcept { return kCountDown > 0; }

    float getCurrentValue(){
        return currentValue;
    }

    void setCurrentValue(float curValue){
        currentValue.store(curValue);
    }

    // Doesn't update leftover steps, call it before setTargetValue or use setTargetWithSeconds. Use resetCountDown to set the new steps.
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
        resetCountDown();
        targetValue.store(target);
        updateStep();
    }

    float getTargetValue(){
        return targetValue;
    }

    virtual float getNextSample() override {
        return smoothed();
    }

    virtual void reset(const float base){
        // Set starting values
        LOGD("SmoothedParameter: resetting to %f", base);
        setCurrentValue(base);
        setTargetValue(base);

        //resetCountDown();
        // Reset steps countdown
        kCountDown = 0;
    }

    // Sets the countdown to the amount of steps calculated for the time and samplingFrequency. Doesn't recalculate the steps, just resets it.
    void resetCountDown(){
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
    std::atomic<float> currentValue {0.0f};
    std::atomic<float> targetValue {0.0f};

    float kRampLengthInSeconds = 0.0f;
    int kStepsToTarget = 1;
    int kCountDown = 1;
    bool kMultiplicative = false;
    std::atomic<float> step{0.0f};

    //float alpha = 0.5f;
};


#endif //HMI_SMOOTHEDPARAMETER_H
