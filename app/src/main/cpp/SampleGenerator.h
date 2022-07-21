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

    int id = 0;

protected:
    static int32_t constexpr kDefaultSampleRate = 48000;

    int32_t mSampleRate = kDefaultSampleRate;
};

class StaticSignal : public SampleGenerator{
public:
    virtual float getNextSample() override {
        return value;
    };

    void setValue(float val){
        LOGD("StaticSignal: setting sample %f", val);
        value.store(val);
    }

    void reset(){
        value.store(0.0f);
    }

protected:
    std::atomic<float> value{0.0f};
};


#endif //HMI_SAMPLEGENERATOR_H
