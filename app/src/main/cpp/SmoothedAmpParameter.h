#ifndef HMI_SMOOTHEDAMPPARAMETER_H
#define HMI_SMOOTHEDAMPPARAMETER_H

#include "SmoothedParameter.h"

class SmoothedAmpParameter : public SmoothedParameter {
public:
    SmoothedAmpParameter(const float seconds = 0.01, const float sampleRate = 48000): SmoothedParameter() {
        setSampleRate(sampleRate);
        setSecondsToTarget(seconds);
    };

    //TODO: rename function because it is no longer a delta of anything. Using absolutes now.
    void applyDeltaToTarget(float delta){
        float val = delta*5.0f; //getCurrentValue()+
        if(val >= 1) setTargetValue(1);
        else if(val <= 0) setTargetValue(0);
        else setTargetValue(val);
    }


    float smoothed() override {
        /*
        setCurrentValue(getTargetValue());
        return getCurrentValue();
         */
        return SmoothedParameter::smoothed();
    }
};

#endif //HMI_SMOOTHEDAMPPARAMETER_H
