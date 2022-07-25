#ifndef HMI_SMOOTHEDAMPPARAMETER_H
#define HMI_SMOOTHEDAMPPARAMETER_H

#include "SmoothedParameter.h"

/**
 * Applies a lower bound
 */
class SmoothedAmpParameter : public SmoothedParameter {
public:
    void applyDeltaToTarget(float delta){
        //TODO: use it in a way to apply a delta, to use with the fingers control. Would need a new interface.
    }


    float smoothed() override {
        float val = SmoothedParameter::smoothed();
        return val <= 0.0000001f ? 0.0000001f : val;
    }
};

#endif //HMI_SMOOTHEDAMPPARAMETER_H
