#ifndef HMI_SMOOTHEDAMPPARAMETER_H
#define HMI_SMOOTHEDAMPPARAMETER_H

#include "SmoothedParameter.h"

class SmoothedAmpParameter : public SmoothedParameter {
public:
    SmoothedAmpParameter(const float targetValue, const float alpha = 0.5f, const float startValue = 0.0f): SmoothedParameter(targetValue,
                                                                                                                alpha,
                                                                                                                startValue) {};
    void applyDeltaToTarget(float delta){
        float val = getCurrentValue()+delta;
        if(val >= 1) setTargetValue(1);
        else if(val <= 0) setTargetValue(0);
        else setTargetValue(val);
    }
};

#endif //HMI_SMOOTHEDAMPPARAMETER_H
