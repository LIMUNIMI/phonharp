#ifndef HMI_OBOESINEPLAYER_H
#define HMI_OBOESINEPLAYER_H

#include <oboe/Oboe.h>
#include <math.h>
#include <android/sensor.h>
#include "SmoothedAmpParameter.h"
#include "SmoothedFrequency.h"
#include "Oscillators.h"

using namespace oboe;

class OboeSinePlayer : public AudioStreamDataCallback {
public:
    virtual ~OboeSinePlayer();

    int32_t initEngine();
    int32_t startAudio(float freq);
    void closeEngine();
    void stopAudio();
    void setFrequency(float frequency);
    DataCallbackResult onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;
    void deltaAmpMul(float deltaAmp);
    void controlPitch(float deltaPitch);
    void controlReset();
    void setPortamento(float seconds);
    float log2lin(float semitonesDelta, float baseFreq);

private:
    std::mutex mLock;
    std::shared_ptr<oboe::AudioStream> mStream;

    static int constexpr kChannelCount = 2;
    static int constexpr kSampleRate = 48000;

    static float constexpr kAmplitude = 1.0f;

    std::atomic<float> kFrequency;

    std::shared_ptr<SmoothedFrequency> smoothedFrequency;
    std::shared_ptr<LFO> vibratoLFO;
    std::unique_ptr<DynamicOscillator> oscillator;
    std::shared_ptr<SmoothedAmpParameter> ampMul;
};
#endif //HMI_OBOESINEPLAYER_H
