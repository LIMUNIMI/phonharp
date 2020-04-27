package com.unimi.lim.hmi.keyboard;

import android.os.Handler;
import android.os.HandlerThread;

import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

// TODO refactor, this class is probably useless
public class ThreadedKeyHandler extends KeyHandler {

    private static final String TAG = "ThreadedKeyHandler";

    private final static int DELAY = 50;

    private final HandlerThread handlerThread = new HandlerThread(TAG);
    private final Player player = new Player();
    private Handler delayedPlayer;


    public ThreadedKeyHandler(Synthesizer synth, Scale scale, int keyOffset) {
        super(synth, scale, keyOffset);
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
