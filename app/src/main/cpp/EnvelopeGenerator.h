#ifndef HMI_ENVELOPEGENERATOR_H
#define HMI_ENVELOPEGENERATOR_H

#include "SmoothedFrequency.h"

/**
 * Very simple. Three stages, attack, sustain, relase
 * Attack, starts from a value called attackLevel sustainLevel
 * Once sustainLevel is reached, we will be in sustain stage. Will stay there until the off() method
 * is called, and then the release stage will kick in. Release stage can kick in also from the attack if off is called.
 * Release goes from whatever level it is in to the releaseLevel.
 *
 * We have attackTime and releaseTime to regulate the length of these stages.
 */
class EnvelopeGenerator : public SmoothedParameter{
public:
    enum EnvelopeStage {
        ENVELOPE_STAGE_OFF = 0,
        ENVELOPE_STAGE_ATTACK,
        ENVELOPE_STAGE_SUSTAIN,
        ENVELOPE_STAGE_RELEASE
    };

    EnvelopeGenerator() {
        setStageTimes(0.2f, 0.2f);
        setStageLevels(0.0f, 1.0f, 0.0f);
        //setSmoothingType(false);
    }

    virtual ~EnvelopeGenerator() {}

    virtual void on(){
        //enterStage(ENVELOPE_STAGE_ATTACK);
        LOGD("EnvelopeGenerator::on: targetValue %f, currentValue %f", getTargetValue(), getCurrentValue());
        active.store(true);
    }

    virtual void off(){
        active.store(false);
    }

    EnvelopeStage getCurrentStage(){
        return currentStage;
    }

    virtual float getNextSample() override {
        //LOGD("EnvelopeGenerator::getNextSample: current value %f", getCurrentValue());
        switch (currentStage) {
            case ENVELOPE_STAGE_OFF:
                if(active){
                    enterStage(ENVELOPE_STAGE_ATTACK);
                }
                break;
            case ENVELOPE_STAGE_ATTACK:
                //if( ((stageLevels[0] >= stageLevels[1]) && (getCurrentValue() >= stageLevels[0]) ) ||
                //( (stageLevels[0] <= stageLevels[1]) && (getCurrentValue() <= stageLevels[0])) )
                if(kCountDown <= 0)
                {
                    enterStage(ENVELOPE_STAGE_SUSTAIN);
                }
                if(!active){
                    enterStage(ENVELOPE_STAGE_RELEASE);
                }
                break;
            case ENVELOPE_STAGE_SUSTAIN:
                if(!active){
                    enterStage(ENVELOPE_STAGE_RELEASE);
                }
                break;
            case ENVELOPE_STAGE_RELEASE:
                //if( ((stageLevels[2] < stageLevels[1]) && (getCurrentValue() <= stageLevels[2]) ) ||
                //    ( (stageLevels[2] > stageLevels[1]) && (getCurrentValue() >= stageLevels[0])) )
                if(kCountDown <= 0)
                {
                    enterStage(ENVELOPE_STAGE_OFF);
                }
                break;
        }

        return smoothed();
    }

    virtual void enterStage(EnvelopeStage stage){
        LOGD("EnvelopeGenerator::enterStage: id %d, Entering stage %d, currentValue %f, leftover steps %d, target %f",id, stage, getCurrentValue(), kCountDown, getTargetValue());
        currentStage = stage;
        switch (stage) {
            case ENVELOPE_STAGE_OFF:
                setStaticLevel(attackLevel);
                break;
            case ENVELOPE_STAGE_ATTACK:
                setStaticLevel(attackLevel);
                setTargetWithSeconds(sustainLevel, attackTime);
                break;
            case ENVELOPE_STAGE_SUSTAIN:
                setStaticLevel(sustainLevel);
                break;
            case ENVELOPE_STAGE_RELEASE:
                setTargetWithSeconds(releaseLevel, releaseTime);
                break;
        }
        LOGD("EnvelopeGenerator::enterStage: id %d, Entered stage %d, currentValue %f, leftover steps %d, target %f", id, stage, getCurrentValue(), kCountDown, getTargetValue());
    }

    void setStaticLevel(const float level){
        //setTargetValue(level);
        //setCurrentValue(level);
        //kCountDown = 0;
        reset(level);
    }

    void setStageTimes(const float attack, const float release){
        attackTime = attack;
        releaseTime = release;
        LOGD("EnvelopeGenerator::setStageTimes: id %d attack %f, release %f",id, attack, release);
    }

    void setStageLevels(const float attack, const float sustain, const float release){
        attackLevel = attack;
        sustainLevel = sustain;
        releaseLevel = release;
        LOGD("EnvelopeGenerator::setStageLevels: id %d, attack %f, sustain %f, release %f", id, attack, sustain, release);
    }
protected:
    EnvelopeStage currentStage = ENVELOPE_STAGE_OFF;

    std::atomic<bool> active {false};

    float attackTime = 0.0f;
    float releaseTime = 0.0f;

    float attackLevel = 0.0f;
    float releaseLevel = 0.0f;
    float sustainLevel = 0.0f;
};


class DeltaEnvelopeGenerator : public EnvelopeGenerator{
public:
    DeltaEnvelopeGenerator() {}

    virtual ~DeltaEnvelopeGenerator() {

    }

    virtual void onWithBaseValue(const float base){
        setStageLevels(base-attackDelta, 0, base-releaseDelta);
        EnvelopeGenerator::on();
    }

    void setAttackDelta(const float delta){
        attackDelta = delta;
    }

    void setReleaseDelta(const float delta){
        releaseDelta = delta;
    }

protected:
    float attackDelta = 0.0f;
    float releaseDelta = 0.0f;
};

#endif //HMI_ENVELOPEGENERATOR_H
