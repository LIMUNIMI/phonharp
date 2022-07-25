#ifndef HMI_SMOOTHEDAMPPARAMETER_H
#define HMI_SMOOTHEDAMPPARAMETER_H

#include "SmoothedParameter.h"

/**
 * Applies a lower and upper bound
 */
class SmoothedAmpParameter : public SmoothedParameter {
public:
    float smoothed() override {
        float val = SmoothedParameter::smoothed();
        val = val <= 1.0f ? val : 1.0f;
        return val <= 0.0000001f ? 0.0000001f : val;
    }
};

#endif //HMI_SMOOTHEDAMPPARAMETER_H
