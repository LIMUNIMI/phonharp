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

    static ASensorManager* getSensorManager();
    void initSensorEventQueue();
    void enableSensor();
    void pauseSensor();
    void getSensorEvent();

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

    //Sensor vars
    const ASensor *rotationSensor;
    ASensorEventQueue *rotationSensorEventQueue;
    ALooper *looper;
    const int LOOPER_ID_USER = 3;
    static const int SENSOR_REFRESH_RATE_HZ = 100;
    static constexpr int32_t SENSOR_REFRESH_PERIOD_US = int32_t(1000000 / SENSOR_REFRESH_RATE_HZ);
    const float SENSOR_FILTER_ALPHA = 0.1f;
    float prev_val = 0.0f;
    float prev_act_val = 0.0f;
};
#endif //HMI_OBOESINEPLAYER_H
