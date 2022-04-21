package com.unimi.lim.hmi.synthetizer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.unimi.lim.hmi.entity.Timbre;

import static java.lang.Math.min;

public class OboeSynth implements Synthesizer{
    private final String TAG = OboeSynth.class.toString();
    private static long mEngineHandle = 0;

    private native long startEngine(int[] cpuIds);
    private native void stopEngine(long engineHandle);
    //private native void noteOn(long engineHandle, int noteIndex);
    //private native void noteOff(long engineHandle, int noteIndex);
    private native void noteOn(long engineHandle, float freq);
    private native void noteOff(long engineHandle);

    private static native void native_setDefaultStreamValues(int sampleRate, int framesPerBurst);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("soundboard");
    }

    public OboeSynth(Context context){
        setDefaultStreamValues(context);
    }

    // Obtain CPU cores which are reserved for the foreground app. The audio thread can be
    // bound to these cores to avoids the risk of it being migrated to slower or more contended
    // core(s).
    private int[] getExclusiveCores(){
        int[] exclusiveCores = {};

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(TAG, "getExclusiveCores() not supported. Only available on API " +
                    Build.VERSION_CODES.N + "+");
        } else {
            try {
                exclusiveCores = android.os.Process.getExclusiveCores();
            } catch (RuntimeException e){
                Log.w(TAG, "getExclusiveCores() is not supported on this device.");
            }
        }
        return exclusiveCores;
    }

    static void setDefaultStreamValues(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
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
        return "Default";
    }

    @Override
    public void start() {
        mEngineHandle = startEngine(getExclusiveCores());
    }

    @Override
    public void stop() {
        stopEngine(mEngineHandle);
    }

    @Override
    public void updateSynthesizerCfg(Timbre timbre) {

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

    }

    @Override
    public void controlVolume(float delta) {

    }

    @Override
    public void controlPitch(float delta) {

    }

    @Override
    public void controlHarmonics(float delta) {

    }

    @Override
    public void controlTremoloDepth(float delta) {

    }

    @Override
    public void controlVibratoDepth(float delta) {

    }

    @Override
    public void controlPwmDepth(float delta) {

    }
}
