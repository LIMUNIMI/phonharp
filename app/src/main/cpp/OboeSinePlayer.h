#ifndef HMI_OBOESINEPLAYER_H
#define HMI_OBOESINEPLAYER_H

#include <oboe/Oboe.h>
#include <math.h>
#include <android/sensor.h>
#include "SmoothedAmpParameter.h"
#include "SmoothedFrequency.h"
#include "Oscillators.h"
#include "EnvelopeGenerator.h"
#include "Filters.h"

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

    static float constexpr kHarmonicsBaseLevelScale = 0.5f;
    static float constexpr kHarmonicsShiftSmoothingTime = 0.1f;
    static float constexpr kPwmScaling = 0.1f;
    static float constexpr kPitchShiftSmoothingTime = 0.1f;
    static float constexpr kPitchShiftScaling = 3.0f;
    static float constexpr kVibratoScaling = 0.5f;
    static float constexpr kAmpControlSmoothingTime = 0.1f;
    static float constexpr kTremoloScaling = 0.5f;
    static float constexpr kLowShelfFreq = 10.0f;
    static float constexpr kHighShelfFreq = 10000.0f;
    static float constexpr kShelfQ = 1.0f;

    std::atomic<float> kFrequency;

    //Oscillator stuff
    DutyCycleOsc* oscillator;

    //Harmonics stuff
    EnvelopeGenerator* harmonicsEnvelope;
    StaticSignal* harmonicsBaseLevel; //between 0.0 and 0.5
    SmoothedParameter* harmonicsShift;
    NaiveOscillator* pwmOsc;
    ModulatedSignal* scaledPwmOsc;
    Mix* harmMix;

    //Frequency stuff
    SmoothedFrequency* smoothedFrequency;
    SmoothedParameter* pitchShift; //can be a StaticSignal too, but it was choppy.
    NaiveOscillator* vibratoLFO;
    ModulatedSignal* scaledVibrato;
    EnvelopeGenerator* pitchEnvelope;
    Mix* freqMix; //Mix is in semitones

    //Amp stuff
    NaiveOscillator* tremoloLFO;
    ModulatedSignal* scaledTremolo;
    SmoothedAmpParameter* ampMul; //controlled by volume shift with fingers or accellerometer
    EnvelopeGenerator* volumeEnvelope;
    ModulatedSignal* scaledVolumeEnvelope;
    Mix* ampMix;

    //Eq stuff
    Shelf* lowShelf;
    Shelf* highShelf;
};
#endif //HMI_OBOESINEPLAYER_H
