package com.unimi.lim.hmi.keyboard;

import android.os.Handler;

import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

import java.util.concurrent.locks.ReentrantLock;

public class KeyHandler {

    private final static int DELAY = 30;

    private int noteNum = -1;
    private Synthesizer sinth;
    private Scale scale;

    private final ReentrantLock lock = new ReentrantLock();
    private Handler delayedPlayer = new Handler();
    private Player player = new Player();
    private boolean playQueued;

    public KeyHandler(Synthesizer synthetizer, Scale scale) {
        this.sinth = synthetizer;
        this.scale = scale;

    }

    public void keyPressed(int keyNum) {
        lock.lock();
        try {
            noteNum += keyNumToWeight(keyNum);
            if(!playQueued) {
                delayedPlayer.postDelayed(player, DELAY);
                playQueued = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public void keyReleased(int keyNum) {
        lock.lock();
        try {
            noteNum -= keyNumToWeight(keyNum);
            if(!playQueued) {
                delayedPlayer.postDelayed(player, DELAY);
                playQueued = true;
            }
        } finally {
            lock.unlock();
        }
    }

    private int keyNumToWeight(int keyNum) {
        return (int) Math.pow(2, keyNum);
    }

    private class Player implements Runnable {

        @Override
        public void run() {
            lock.lock();
            try {
                if (noteNum < 0) {
                    sinth.release();
                } else {
                    sinth.press(scale.getNote(noteNum).getFrequency());
                }
            } finally {
                playQueued = false;
                lock.unlock();
            }
        }

    }
}
