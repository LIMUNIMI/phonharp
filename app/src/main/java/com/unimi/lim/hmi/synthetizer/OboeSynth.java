package com.unimi.lim.hmi.synthetizer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.unimi.lim.hmi.entity.Timbre;

import static java.lang.Math.min;

public class OboeSynth implements Synthesizer {
    private final String TAG = OboeSynth.class.toString();
    private static long mEngineHandle = 0;
    private Timbre timbre;

    // Internal Oboe stuff
    private native long startEngine(int[] cpuIds);

    private native void stopEngine(long engineHandle);

    private static native void native_setDefaultStreamValues(int sampleRate, int framesPerBurst);

    // Real time controls
    private native void noteOn(long engineHandle, float freq);

    private native void noteOff(long engineHandle);

    private native void controlAmpMul(long engineHandle, float delta);

    private native void controlPitch(long engineHandle, float delta);

    private native void controlVibrato(long engineHandle, float deltaDepth);

    private native void controlReset(long engineHandle);

    private native void controlHarmonics(long engineHandle, float delta);

    private native void controlTremolo(long engineHandle, float delta);

    private native void controlPWM(long engineHandle, float delta);

    // Settings
    private native void setPortamento(long engineHandle, float seconds);

    private native void setVibrato(long engineHandle, float frequency, float depth);

    private native void setHarmonics(long engineHandle, float harmonics);

    private native void setTremolo(long engineHandle, float frequency, float depth);

    private native void setPWM(long engineHandle, float frequency, float depth);

    private native void setPitchAdsr(long engineHandle, float attackTime, float attackDelta, float releaseTime, float releaseDelta);

    private native void setVolumeAdsr(long engineHandle, float attackTime, float attackDelta, float releaseTime, float releaseDelta);

    private native void setHarmonicsAdsr(long engineHandle, float attackTime, float attackDelta, float releaseTime, float releaseDelta);

    private native void setEq(long engineHandle, float highGain, float lowGain);

    private native void setVolume(long enginHandle, float volume);


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("OboeSynthMain");
    }

    public OboeSynth(Context context, Timbre timbre) {
        this.timbre = timbre;
        setDefaultStreamValues(context);
    }

    // Obtain CPU cores which are reserved for the foreground app. The audio thread can be
    // bound to these cores to avoids the risk of it being migrated to slower or more contended
    // core(s).
    private int[] getExclusiveCores() {
        int[] exclusiveCores = {};

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(TAG, "getExclusiveCores() not supported. Only available on API " +
                    Build.VERSION_CODES.N + "+");
        } else {
            try {
                exclusiveCores = android.os.Process.getExclusiveCores();
            } catch (RuntimeException e) {
                Log.w(TAG, "getExclusiveCores() is not supported on this device.");
            }
        }
        return exclusiveCores;
    }

    static void setDefaultStreamValues(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            AudioManager myAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            String sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            int defaultSampleRate = Integer.parseInt(sampleRateStr);
            String framesPerBurstStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
            int defaultFramesPerBurst = Integer.parseInt(framesPerBurstStr);

            native_setDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst);
        }
    }

    @Override
    public String getTimbreId() {
        return timbre.getId();
    }

    @Override
    public void start() {
        mEngineHandle = startEngine(getExclusiveCores());
        updateSynthesizerCfg(timbre);
    }

    @Override
    public void stop() {
        stopEngine(mEngineHandle);
    }

    @Override
    public void updateSynthesizerCfg(Timbre timbre) {
        this.timbre = timbre;

        // volume
        float volume = (float)timbre.getVolume()/100;
        setVolume(mEngineHandle, volume);

        //setHarmonics
        Log.d(TAG, "updateSynthesizerCfg: harmonics: " + timbre.getHarmonics());
        float harmonics = (float) timbre.getHarmonics() / 200;
        setHarmonics(mEngineHandle,harmonics); //ret int, percent
        // LO slider va da 0% a 50%, ma il valore passato va da 0 a 100
        // Provo a dividerlo per 2 e farlo diventare una percentuale su 1

        //setTremolo
        Timbre.Lfo tremolo = timbre.getTremolo(); //ret Lfo
        float tremoloDepth = tremolo != null ? (float) tremolo.getDepth() : 0; //ret int, percent
        float tremoloFreq =  tremolo != null ? tremolo.getRate() : 0; //ret float, frequency in Hz
        Log.d(TAG, "updateSynthesizerCfg: tremoloDepth: " + tremoloDepth + " tremoloFreq: " + tremoloFreq);
        setTremolo(mEngineHandle, tremoloFreq, tremoloDepth/100);

        //setVibrato
        Timbre.Lfo vibrato = timbre.getVibrato(); //ret Lfo
        float vibratoDepth = vibrato != null ? (float) vibrato.getDepth() : 0; //ret int, percent
        float vibratoFreq =  vibrato != null ? vibrato.getRate() : 0; //ret float, frequency in Hz
        Log.d(TAG, "updateSynthesizerCfg: vibratoDepth: " + vibratoDepth + " vibratoFreq: " + vibratoFreq);
        setVibrato(mEngineHandle, vibratoFreq, vibratoDepth/100);

        //setPwm
        Timbre.Lfo pwm = timbre.getPwm(); //ret Lfo
        float pwmDepth = pwm != null ? (float) pwm.getDepth() : 0; //ret int, percent
        float pwmFreq =  pwm != null ? pwm.getRate() : 0; //ret float, frequency in Hz
        Log.d(TAG, "updateSynthesizerCfg: pwmDepth: " + pwmDepth + " pwmFreq: " + pwmFreq);
        setPWM(mEngineHandle, pwmFreq, pwmDepth/100);

        //setEqualizer
        float highGain = 0;
        float lowGain = 0;
        Timbre.Equalizer eq = timbre.getEqualizer(); //ret Equalizer
        if(eq != null){
            highGain = eq.getHighShelfGain(); //ret int, dB
            lowGain = eq.getLowShelfGain(); //ret int, dB
        }

        setEq(mEngineHandle, highGain, lowGain);

        //setPortamento
        float portamentoSeconds = timbre.getPortamento(); //float set
        Log.d(TAG, "updateSynthesizerCfg: portamento: " + portamentoSeconds);
        setPortamento(mEngineHandle, portamentoSeconds);

        //setPitchEnvelope
        Timbre.Asr pitchEnvelope = timbre.getPitchAsr();
        if(pitchEnvelope != null){
            setPitchAdsr(mEngineHandle, pitchEnvelope.getAttackTime(), pitchEnvelope.getInitialValue(), pitchEnvelope.getReleaseTime(), pitchEnvelope.getFinalValue());
        } else {
            setPitchAdsr(mEngineHandle, 0, 0, 0, 0);
        }
        //Log.d(TAG, "updateSynthesizerCfg: pitchEnvelope: finalValue:" + pitchEnvelope.getFinalValue());

        //set volume envelope
        Timbre.Asr volumeEnvelope = timbre.getVolumeAsr();
        if(volumeEnvelope != null){
            setVolumeAdsr(mEngineHandle, volumeEnvelope.getAttackTime(), 0, volumeEnvelope.getReleaseTime(), 0);
        } else {
            setVolumeAdsr(mEngineHandle, 0, 0, 0, 0);
        }

        //set harmonics envelope
        Timbre.Asr harmonicsEnvelope = timbre.getHarmonicsAsr();
        if(harmonicsEnvelope != null){
            setHarmonicsAdsr(mEngineHandle, harmonicsEnvelope.getAttackTime(), harmonicsEnvelope.getInitialValue()/200-harmonics, harmonicsEnvelope.getReleaseTime(), harmonicsEnvelope.getFinalValue()/200-harmonics);
        } else {
            setHarmonicsAdsr(mEngineHandle, 0, 0, 0, 0);
        }
    }

    @Override
    public void press(double frequency) {
        //(int) Math.floor(frequency/440)
        noteOn(mEngineHandle, (float) frequency);
    }

    @Override
    public void release() {
        //stopEngine(mEngineHandle);
        noteOff(mEngineHandle);
    }

    @Override
    public void controlReset() {
        controlReset(mEngineHandle);
    }

    @Override
    public void controlVolume(float delta) {
        //Log.d(TAG, "controlVolume: delta "+delta);
        controlAmpMul(mEngineHandle, delta/100);
    }

    @Override
    public void setVolume(float volume){
        setVolume(mEngineHandle, volume);
    }

    @Override
    public void controlPitch(float delta) {
        Log.d(TAG, "controlPitch: " + delta);
        controlPitch(mEngineHandle, delta);
    }

    @Override
    public void controlHarmonics(float delta) {
        Log.d(TAG, "controlHarmonics: " + delta);
        controlHarmonics(mEngineHandle, delta/200);
    }

    @Override
    public void controlTremoloDepth(float delta) {
        Log.d(TAG, "controlTremoloDepth: " + delta);
        controlTremolo(mEngineHandle, delta/100);
    }

    @Override
    public void controlVibratoDepth(float delta) {
        Log.d(TAG, "controlVibratoDepth: " + delta);
        controlVibrato(mEngineHandle, delta/100);
    }

    @Override
    public void controlPwmDepth(float delta) {
        Log.d(TAG, "controlPwmDepth: " + delta);
        controlPWM(mEngineHandle, delta);
    }
}
