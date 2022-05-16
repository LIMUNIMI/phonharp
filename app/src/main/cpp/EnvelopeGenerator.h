#ifndef HMI_ENVELOPEGENERATOR_H
#define HMI_ENVELOPEGENERATOR_H

#include "SmoothedFrequency.h"

class EnvelopeGenerator : public SmoothedParameter{
public:
    EnvelopeGenerator() {}

    virtual ~EnvelopeGenerator() {}

protected:
    // Attack, decay, release
    float stageTimes[] = {0.2f, 0.1f, 0.2f}
    float stageLevels[] = {1.0f, 0.9f, 0.0f};

    //std::atomic<float> mAmplitude{1.0f};
};


#endif //HMI_ENVELOPEGENERATOR_H
