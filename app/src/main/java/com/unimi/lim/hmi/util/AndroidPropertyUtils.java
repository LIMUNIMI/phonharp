package com.unimi.lim.hmi.util;

import android.content.Context;
import android.media.AudioManager;

import static com.unimi.lim.hmi.util.Constant.System.DEFAULT_FRAMES_PER_BUFFER;
import static com.unimi.lim.hmi.util.Constant.System.DEFAULT_SAMPLE_RATE;

public class AndroidPropertyUtils {

    private AndroidPropertyUtils() {
    }

    public static int outputSampleRate(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String sampleRateStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int sampleRate = Integer.parseInt(sampleRateStr);
        return sampleRate != 0 ? sampleRate : DEFAULT_SAMPLE_RATE;
    }

    public static int framesPerBuffer(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String framesPerBuffer = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int framesPerBufferInt = Integer.parseInt(framesPerBuffer);
        return framesPerBufferInt != 0 ? framesPerBufferInt : DEFAULT_FRAMES_PER_BUFFER;
    }


}
