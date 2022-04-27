#ifndef HMI_OBOESINEPLAYER_H
#define HMI_OBOESINEPLAYER_H

#include <oboe/Oboe.h>
#include <math.h>
#include <android/sensor.h>

using namespace oboe;

class OboeSinePlayer : public AudioStreamDataCallback {
public:
    virtual ~OboeSinePlayer() = default;

    int32_t initEngine();
    int32_t startAudio(float freq);
    void closeEngine();
    void stopAudio();
    void updatePhaseInc();
    DataCallbackResult onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;
    void setAmpMul(float amp);
    void deltaAmpMul(float deltaAmp);

private:
    std::mutex mLock;
    std::shared_ptr<oboe::AudioStream> mStream;

    static int constexpr kChannelCount = 2;
    static int constexpr kSampleRate = 48000;

    static float constexpr kAmplitude = 0.5f;
    static float constexpr kBaseFrequency = 200;
    static float constexpr kPI = M_PI;
    static float constexpr kTwoPi = kPI * 2;

    float kFrequency = kBaseFrequency;

    double mPhaseIncrement = kFrequency * kTwoPi / (double) kSampleRate;

    float mPhase = 0.0;

    std::atomic<float> kAmpMul;
};
#endif //HMI_OBOESINEPLAYER_H
