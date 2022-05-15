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

    // internal
    int32_t initEngine();
    void closeEngine();
    void setFrequency(float frequency);
    DataCallbackResult onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;
    float log2lin(float semitonesDelta, float baseFreq);

    // settings
    void setPortamento(float seconds);
    void setVibrato(float frequency, float depth);

    // real time controls
    int32_t startAudio(float freq);
    void stopAudio();
    void deltaAmpMul(float deltaAmp);
    void controlPitch(float deltaPitch);
    void controlVibrato(float deltaDepth);
    void controlReset();


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
