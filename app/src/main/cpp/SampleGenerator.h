#ifndef HMI_SAMPLEGENERATOR_H
#define HMI_SAMPLEGENERATOR_H


class SampleGenerator {
public:
    SampleGenerator() {}

    virtual ~SampleGenerator() {}

    void setSampleRate(int32_t sampleRate) {
        mSampleRate = sampleRate;
    };

    virtual float getNextSample() = 0;

protected:
    static int32_t constexpr kDefaultSampleRate = 48000;

    int32_t mSampleRate = kDefaultSampleRate;
};


#endif //HMI_SAMPLEGENERATOR_H
