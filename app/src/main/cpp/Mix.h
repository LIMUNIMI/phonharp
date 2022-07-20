#ifndef HMI_MIX_H
#define HMI_MIX_H

#include "SampleGenerator.h"
#include <list>


class ModulatedSignal : public SampleGenerator{
public:

    ModulatedSignal(){};

    ModulatedSignal(std::shared_ptr<SampleGenerator> & signal, const float amount){
        setSignal(signal);
        setModAmount(amount);
    }

    float getNextSample() override {
        return modAmount * mSignal->getNextSample();
    }

    void setModAmount(const float amount){
        modAmount.store(amount);
    }

    void setSignal(std::shared_ptr<SampleGenerator> & signal){
        mSignal = signal;
    }

protected:
    std::shared_ptr<SampleGenerator> mSignal;
    std::atomic<float> modAmount{1.0f};
};

class Mix : public SampleGenerator{
public:
    Mix(){

    }

    void addSignal(std::shared_ptr<SampleGenerator> & signal, const float modAmount){
        signals.push_back(new ModulatedSignal(signal, modAmount));
    }

    float getNextSample() override {
        for(itr = signals.begin(); itr != signals.end(); ++itr){
            ret += (*itr)->getNextSample();
        }

        return ret;
    }

protected:
    std::list<ModulatedSignal*> signals;
    std::list<ModulatedSignal*>::iterator itr;

    float ret = 0.0f;
};

#endif //HMI_MIX_H
