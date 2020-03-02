package com.unimi.lim.hmi.keyboard;

import android.os.Handler;

import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

import java.util.concurrent.locks.ReentrantLock;

public class DelayedKeyHandler extends KeyHandler {

    private final static int DELAY = 40;
    private final static double[] ENVELOP = {
            0.001, 0.9, // attack
            0.01, 0.8, // decay
            0.001, 0.0 // release
    };

    private final ReentrantLock lock = new ReentrantLock();
    private Handler delayedPlayer = new Handler();
    private Player player = new Player();
    private boolean playQueued;

    public DelayedKeyHandler(Synthesizer synth, Scale scale, int keyOffset) {
        super(synth, scale, keyOffset);
        synth.setEnvelopData(ENVELOP);
    }

    @Override
    public void keyPressed(int keyNum) {
        lock.lock();
        try {
            noteNum += keyNumToWeight(keyNum);
            if (!playQueued) {
                delayedPlayer.postDelayed(player, DELAY);
                playQueued = true;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void keyReleased(int keyNum) {
        lock.lock();
        try {
            noteNum -= keyNumToWeight(keyNum);
            if (!playQueued) {
                delayedPlayer.postDelayed(player, DELAY);
                playQueued = true;
            }
        } finally {
            lock.unlock();
        }
    }

    private class Player implements Runnable {

        @Override
        public void run() {
            lock.lock();
            try {
                play();
            } finally {
                playQueued = false;
                lock.unlock();
            }
        }

    }
}
