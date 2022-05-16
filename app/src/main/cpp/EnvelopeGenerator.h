#ifndef HMI_ENVELOPEGENERATOR_H
#define HMI_ENVELOPEGENERATOR_H

#include "SmoothedFrequency.h"

class EnvelopeGenerator : public SmoothedParameter{
public:
    enum EnvelopeStage {
        ENVELOPE_STAGE_OFF = 0,
        ENVELOPE_STAGE_ATTACK,
        ENVELOPE_STAGE_DECAY,
        ENVELOPE_STAGE_SUSTAIN,
        ENVELOPE_STAGE_RELEASE
    };

    EnvelopeGenerator() {
        setStageTimes(0.2f, 0.1f, 0.2f);
        setStageLevels(1.0f, 0.9f, 0.0f);
    }

    virtual ~EnvelopeGenerator() {}

    virtual float on(){
        active.store(true);
    }

    virtual float off(){
        active.store(false);
    }

    virtual float getNextSample() override {
        switch (currentStage) {
            case ENVELOPE_STAGE_OFF:
                if(active){
                    enterStage(ENVELOPE_STAGE_ATTACK);
                }
                break;
            case ENVELOPE_STAGE_ATTACK:
                if(getCurrentValue() >= stageLevels[0]){
                    enterStage(ENVELOPE_STAGE_DECAY);
                }
                break;
            case ENVELOPE_STAGE_DECAY:
                if(getCurrentValue() <= stageLevels[1]){
                    enterStage(ENVELOPE_STAGE_SUSTAIN);
                }
                break;
            case ENVELOPE_STAGE_SUSTAIN:
                if(!active){
                    enterStage(ENVELOPE_STAGE_RELEASE);
                }
                break;
            case ENVELOPE_STAGE_RELEASE:
                if(getCurrentValue() <= stageLevels[2]){
                    enterStage(ENVELOPE_STAGE_OFF);
                }
                break;
        }

        return smoothed();
    }

    virtual void enterStage(EnvelopeStage stage){
        currentStage = stage;
        switch (stage) {
            case ENVELOPE_STAGE_OFF:
                setTargetValue(stageLevels[2]);
                setCurrentValue(stageLevels[2]);
                setRawPrevValue(stageLevels[2]);
                break;
            case ENVELOPE_STAGE_ATTACK:
                setTargetValue(stageLevels[0]);
                setAlphaFromSeconds(stageTimes[0]);
                break;
            case ENVELOPE_STAGE_DECAY:
                setTargetValue(stageLevels[1]);
                setAlphaFromSeconds(stageTimes[1]);
                break;
            case ENVELOPE_STAGE_SUSTAIN:
                setTargetValue(stageLevels[1]);
                setCurrentValue(stageLevels[1]);
                setRawPrevValue(stageLevels[1]);
                break;
            case ENVELOPE_STAGE_RELEASE:
                setTargetValue(stageLevels[2]);
                setAlphaFromSeconds(stageTimes[2]);
                break;
        }
    }

    void setStageTimes(const float attack, const float decay, const float release){
        stageTimes[0] = attack;
        stageTimes[1] = decay;
        stageTimes[2] = release;
    }

    void setStageLevels(const float attack, const float sustain, const float release){
        stageLevels[0] = attack;
        stageLevels[1] = sustain;
        stageLevels[2] = release;
    }
protected:
    EnvelopeStage currentStage = ENVELOPE_STAGE_OFF;

    std::atomic<bool> active {false};

    // Attack, decay, release
    float stageTimes[3];// = {0.2f, 0.1f, 0.2f}
    float stageLevels[3];// = {1.0f, 0.9f, 0.0f};
};


#endif //HMI_ENVELOPEGENERATOR_H
