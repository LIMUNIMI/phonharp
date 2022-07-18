#ifndef HMI_FILTERS_H
#define HMI_FILTERS_H

class Filter{
    virtual float filterSample(float currentSample) = 0;
    virtual void resetFilter() = 0;
};

class LowPassFilter: public Filter{
    float filterSample(float currentSample){
        return 0;
    }

    void resetFilter(){
        oldFilteredSample = 0.0f;
        oldRawSample = 0.0f;
    }

protected:
    float oldRawSample = 0.0f;
    float oldFilteredSample = 0.0f;
};


class HighPassFilter: public Filter{
    float filterSample(float currentSample){
        return 0;
    }

    void resetFilter(){
        oldFilteredSample = 0.0f;
        oldRawSample = 0.0f;
    }

protected:
    float oldRawSample = 0.0f;
    float oldFilteredSample = 0.0f;
};

#endif //HMI_FILTERS_H
