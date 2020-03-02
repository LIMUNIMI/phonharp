package com.unimi.lim.hmi.keyboard;

import android.util.Log;

import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

public class BaseKeyHandler extends KeyHandler {

    private final static double[] ENVELOP = {
            0.01, 0.9, // attack
            0.01, 0.8, // decay
            0.01, 0.0 // release
    };

    public BaseKeyHandler(Synthesizer synth, Scale scale, int keyOffset) {
        super(synth, scale, keyOffset);
        synth.setEnvelopData(ENVELOP);
    }

    public synchronized void keyPressed(int keyNum) {
        noteNum += keyNumToWeight(keyNum);
        Log.d(TAG, "Pressed key " + keyNum + ", note number " + noteNum);
        play();
    }

    public synchronized void keyReleased(int keyNum) {
        noteNum -= keyNumToWeight(keyNum);
        Log.d(TAG, "Released key " + keyNum + ", note number " + noteNum);
        play();
    }

}
