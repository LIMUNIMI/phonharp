#ifndef HMI_SAMPLEGENERATOR_H
#define HMI_SAMPLEGENERATOR_H


#include <list>

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
        //LOGD("StaticSignal: setting sample %f", val);
        value.store(val);
    }

    void reset(){
        value.store(0.0f);
    }

protected:
    std::atomic<float> value{0.0f};
};

class ModulatedSignal : public SampleGenerator{
public:

    ModulatedSignal(){};

    ModulatedSignal(SampleGenerator*  signal, const float amount){
        setSignal(signal);
        setModAmount(amount);
    }

    virtual float getNextSample() override {
        return baseOffset + (mSignal->getNextSample() * (modAmount+mDelta));
    }

    void setModAmount(const float amount){
        modAmount.store(amount);
    }

    void setSignal(SampleGenerator* signal){
        mSignal = signal;
    }

    void reset(){
        setModDelta(0.0f);
    }

    void setModDelta(const float delta){
        mDelta.store(delta);
    }

    void setBaseOffset(const float value){
        baseOffset.store(value);
    }

protected:
    SampleGenerator* mSignal;
    std::atomic<float> modAmount{1.0f};
    std::atomic<float> mDelta{0.0f};
    std::atomic<float> baseOffset{0.0f};
};

class Mix : public SampleGenerator{
public:
    enum MixMode {Sum = 0, Mul = 1};

    Mix(){

    }

    void addSignal(SampleGenerator*  signal, const float modAmount){
        signals.push_back(new ModulatedSignal(signal, modAmount));
    }

    void addSignal(ModulatedSignal *modulatedSignal){
        signals.push_back(modulatedSignal);
    }

    float getNextSample() override {
        switch (mode) {
            case MixMode::Mul:
                ret = 1.0f;
                break;
            case MixMode::Sum:
                ret = 0.0f;
                break;
        }

        //LOGD("Mix: getting sample");
        for(itr = signals.begin(); itr != signals.end(); ++itr){
            //LOGD("Mix: cycling sources");
            switch (mode) {
                case MixMode::Mul:
                    ret *= (*itr)->getNextSample();
                    break;
                case MixMode::Sum:
                    ret += (*itr)->getNextSample();
                    break;
            }
            //LOGD("Mix: mix from sources %f", ret);
        }
        //LOGD("Mix: mix from sources %f", ret);
        return ret;
    }

    void setMixMode(MixMode type){
        mode = type;
    }

protected:
    std::list<ModulatedSignal*> signals;
    std::list<ModulatedSignal*>::iterator itr;

    MixMode mode = MixMode::Sum;

    float ret = 0.0f;
};

#endif //HMI_SAMPLEGENERATOR_H
