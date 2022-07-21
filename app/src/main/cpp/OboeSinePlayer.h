#ifndef HMI_OBOESINEPLAYER_H
#define HMI_OBOESINEPLAYER_H

#include <oboe/Oboe.h>
#include <math.h>
#include <android/sensor.h>
#include "SmoothedAmpParameter.h"
#include "SmoothedFrequency.h"
#include "Oscillators.h"
#include "PitchEnvelope.h"

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
    void setPitchAdsr(float attackTime, float attackDelta, float releaseTime, float releaseDelta);
    void setTremolo(float frequency, float depth);
    void setPWM(float frequency, float depth);
    void setHarmonics(float percent);
    void setVolumeAdsr(float attackTime, float attackDelta, float releaseTime, float releaseDelta);
    void setHarmonicsAdsr(float attackTime, float attackDelta, float releaseTime, float releaseDelta);
    void setEq(float highGain, float lowGain);

    // real time controls
    int32_t startAudio(float freq);
    void stopAudio();
    void controlAmpMul(float deltaAmp);
    void controlPitch(float deltaPitch);
    void controlVibrato(float deltaDepth);
    void controlTremolo(float deltaDepth);
    void controlPWM(float deltaDepth);
    void controlHarmonics(float delta);
    void controlReset();


private:
    std::mutex mLock;
    std::shared_ptr<oboe::AudioStream> mStream;
    std::atomic<bool> isPlaying{false};

    static int constexpr kChannelCount = 2;
    static int constexpr kSampleRate = 48000;

    static float constexpr kAmplitude = 1.0f;

    std::atomic<float> kFrequency;

    DutyCycleOsc* oscillator;
    SmoothedFrequency* smoothedFrequency;

    NaiveOscillator* vibratoLFO;
    ModulatedSignal* scaledVibrato;

    EnvelopeGenerator* pitchEnvelope;
    NaiveOscillator* tremoloLFO;
    SmoothedAmpParameter* ampMul;
    DeltaEnvelopeGenerator* volumeEnvelope;
    DeltaEnvelopeGenerator* harmoncisEnvelope;
    Mix* freqMix;
};
#endif //HMI_OBOESINEPLAYER_H
