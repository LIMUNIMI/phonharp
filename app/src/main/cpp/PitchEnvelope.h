#ifndef HMI_PITCHENVELOPE_H
#define HMI_PITCHENVELOPE_H

#include "EnvelopeGenerator.h"

class PitchEnvelope : public EnvelopeGenerator{
public:
    PitchEnvelope() {}

    virtual ~PitchEnvelope() {

    }

    void onWithBaseFreq(float frequency) {
        setStageLevels(log2lin(attackDelta, frequency)-frequency, 0, log2lin(releaseDelta, frequency)-frequency);
        EnvelopeGenerator::on();
    }

    void setAttackDelta(float semitones){
        attackDelta = semitones;
    }

    void setReleaseDelta(float semitones){
        releaseDelta = semitones;
    }

    float log2lin(float semitonesDelta, float baseFreq) {
        //TODO: optimize
        return exp((logf(2)*(semitonesDelta + 12 * logf(baseFreq)))/12);
    }

private:
    float attackDelta = 1.0f;
    float releaseDelta = -1.0f;
};


#endif //HMI_PITCHENVELOPE_H
