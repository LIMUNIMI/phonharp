#ifndef HMI_MIX_H
#define HMI_MIX_H

#include <map>
#include "SampleGenerator.h"

class Mix : public SampleGenerator{
public:
    Mix(){

    }

    void addElement(SampleGenerator element, const float modAmount){
        elements.insert(pair<SampleGenerator, modAmount>(element, modAmount));
    }

    float getNextSample() override {
        for(itr = elements.begin(); itr != elements.end(); ++itr){
            ret += itr->first.getNextSample() * itr->second;
        }

        return ret;
    }

protected:
    std::map<SampleGenerator, modAmount> elements;
    std::map<SampleGenerator, modAmount>::iterator itr;

    float ret = 0.0f;
};

#endif //HMI_MIX_H
