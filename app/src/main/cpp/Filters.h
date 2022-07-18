#ifndef HMI_FILTERS_H
#define HMI_FILTERS_H

class Filter{
    virtual float filterSample(float currentSample) = 0;
    virtual void resetFilter() = 0;
};

#endif //HMI_FILTERS_H
