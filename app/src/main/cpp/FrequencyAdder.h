#ifndef HMI_FREQUENCYADDER_H
#define HMI_FREQUENCYADDER_H

#include "SmoothedFrequency.h"

class FrequencyAdder{
public:
    FrequencyAdder(SmoothedFrequency *smoothedFrequency){
        //TODO getvibrato, get pointer of pitch delta
    }

    float getFrequency(){
        return 0.0f
    }
private:
    SmoothedFrequency *aSmoothedFrequency;
};

#endif //HMI_FREQUENCYADDER_H
