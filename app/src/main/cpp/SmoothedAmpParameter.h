#ifndef HMI_SMOOTHEDAMPPARAMETER_H
#define HMI_SMOOTHEDAMPPARAMETER_H

#include "SmoothedParameter.h"

class SmoothedAmpParameter : public SmoothedParameter {
public:
    SmoothedAmpParameter(): SmoothedParameter() {};

    void applyDeltaToTarget(float delta){
        float val = getCurrentValue()+delta;
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
