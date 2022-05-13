#ifndef HMI_OBOESINEPLAYER_H
#define HMI_OBOESINEPLAYER_H

#include <oboe/Oboe.h>
#include <math.h>
#include <android/sensor.h>
#include "SmoothedAmpParameter.h"
#include "SmoothedFrequency.h"
#include "OscillatorWrapper.h"

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

private:
    std::mutex mLock;
    std::shared_ptr<oboe::AudioStream> mStream;

    static int constexpr kChannelCount = 2;
    static int constexpr kSampleRate = 48000;

    static float constexpr kAmplitude = 0.5f;
    static float constexpr kPI = M_PI;
    static float constexpr kTwoPi = kPI * 2;

    std::atomic<float> kFrequency;
    std::atomic<float> pitchBendDelta;

    std::unique_ptr<SmoothedFrequency> smoothedFrequency;

    std::unique_ptr<OscillatorWrapper> oscillator;

    float mPhase = 0.0;

    SmoothedAmpParameter *ampMul;
    const float kAmpMulAlpha = 0.9f;
};
#endif //HMI_OBOESINEPLAYER_H
