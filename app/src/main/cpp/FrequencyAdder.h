#ifndef HMI_FREQUENCYADDER_H
#define HMI_FREQUENCYADDER_H

#include "SmoothedFrequency.h"
#include "Oscillator.h"

class FrequencyAdder{
public:
    FrequencyAdder(SmoothedFrequency *smoothedFrequency, Oscillator *oscillator, float *pitchBend){
        aSmoothedFrequency = smoothedFrequency;
        aOscillator = oscillator;
        aPitchBend = pitchBend;
    }

    float getFrequency(){
        return aSmoothedFrequency->smoothed() + aOscillator->getNextSample() + *aPitchBend;
    }
private:
    SmoothedFrequency *aSmoothedFrequency;
    Oscillator *aOscillator;
    float *aPitchBend;
};

#endif //HMI_FREQUENCYADDER_H
