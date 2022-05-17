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

    virtual void on(){
        active.store(true);
    }

    virtual void off(){
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
                if( ((stageLevels[0] >= stageLevels[1]) && (getCurrentValue() >= stageLevels[0]) ) ||
                ( (stageLevels[0] <= stageLevels[1]) && (getCurrentValue() <= stageLevels[0])) )
                {
                    enterStage(ENVELOPE_STAGE_DECAY);
                }
                break;
            case ENVELOPE_STAGE_DECAY:
                if( ((stageLevels[1] <= stageLevels[0]) && (getCurrentValue() <= stageLevels[1]) ) ||
                    ( (stageLevels[0] >= stageLevels[1]) && (getCurrentValue() >= stageLevels[1])) )
                {
                    enterStage(ENVELOPE_STAGE_SUSTAIN);
                }
                break;
            case ENVELOPE_STAGE_SUSTAIN:
                if(!active){
                    enterStage(ENVELOPE_STAGE_RELEASE);
                }
                break;
            case ENVELOPE_STAGE_RELEASE:
                if( ((stageLevels[2] < stageLevels[1]) && (getCurrentValue() <= stageLevels[2]) ) ||
                    ( (stageLevels[2] > stageLevels[1]) && (getCurrentValue() >= stageLevels[0])) )
                {
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
                setStaticLevel(stageLevels[2]);
                break;
            case ENVELOPE_STAGE_ATTACK:
                setTargetWithSeconds(stageLevels[0], stageTimes[0]);
                break;
            case ENVELOPE_STAGE_DECAY:
                setTargetWithSeconds(stageLevels[1], stageTimes[1]);
                break;
            case ENVELOPE_STAGE_SUSTAIN:
                setStaticLevel(stageLevels[1]);
                break;
            case ENVELOPE_STAGE_RELEASE:
                setTargetWithSeconds(stageLevels[2], stageTimes[2]);
                break;
        }
    }

    void setStaticLevel(const float level){
        setTargetValue(level);
        setCurrentValue(level);
        kCountDown = 0;
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
