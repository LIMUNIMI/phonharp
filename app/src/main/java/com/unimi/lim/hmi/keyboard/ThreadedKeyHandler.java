package com.unimi.lim.hmi.keyboard;

import android.os.Handler;
import android.os.HandlerThread;

import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

public class ThreadedKeyHandler extends KeyHandler {

    private static final String TAG = "ThreadedKeyHandler";

    private final static int DELAY = 50;
    private final static double[] ENVELOP = {
            0.001, 0.9, // attack
            0.001, 0.8, // decay
            0.001, 0.0  // release
    };

    private final HandlerThread handlerThread = new HandlerThread(TAG);
    private final Player player = new Player();
    private Handler delayedPlayer;


    public ThreadedKeyHandler(Synthesizer synth, Scale scale, int keyOffset) {
        super(synth, scale, keyOffset);
        synth.setEnvelopData(ENVELOP);
    }

    @Override
    public void onStart() {
        handlerThread.start();
        delayedPlayer = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onDestroy() {
        handlerThread.quit();
    }

    @Override
    protected void play() {
        delayedPlayer.postDelayed(player, DELAY);
    }

    // TODO this class is duplicated
    // FIXME implicit reference
    private class Player implements Runnable {

        @Override
        public void run() {
            invokeSynth();
        }

    }
}
