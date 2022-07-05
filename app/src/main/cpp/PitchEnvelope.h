#ifndef HMI_PITCHENVELOPE_H
#define HMI_PITCHENVELOPE_H

#include "EnvelopeGenerator.h"

class PitchEnvelope : public EnvelopeGenerator{
public:
    PitchEnvelope() {}

    virtual ~PitchEnvelope() {

    }

    void onWithBaseFreq(float frequency) {
        LOGD("PitchEnvelope::onWithBaseFreq: (setStageLevels) attackDelta %f, releaseDelta %f, log2lin attack %f", attackDelta, releaseDelta, log2lin(attackDelta, frequency));
        setStageLevels(frequency-log2lin(attackDelta, frequency), 0, frequency-log2lin(releaseDelta, frequency));
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
        //return exp((logf(2)*(semitonesDelta + 12 * logf(baseFreq)))/12);
        return expf(logf(baseFreq)-(logf(2.0f)*semitonesDelta)/12);
    }

private:
    float attackDelta = 0.0f;
    float releaseDelta = 0.0f;
};


#endif //HMI_PITCHENVELOPE_H
