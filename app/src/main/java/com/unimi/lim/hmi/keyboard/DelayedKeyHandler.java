package com.unimi.lim.hmi.keyboard;

import android.os.Handler;

import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

public class DelayedKeyHandler extends KeyHandler {

    private final static int DELAY = 50;
    private final static double[] ENVELOP = {
            0.001, 0.9, // attack
            0.001, 0.8, // decay
            0.001, 0.0  // release
    };

    private Handler delayedPlayer = new Handler();
    private Player player = new Player();
    private boolean delayedPlayInvoked;

    public DelayedKeyHandler(Synthesizer synth, Scale scale, int keyOffset) {
        super(synth, scale, keyOffset);
        synth.setEnvelopData(ENVELOP);
    }

    @Override
    protected void play() {
        if (!delayedPlayInvoked) {
            delayedPlayer.postDelayed(player, DELAY);
            delayedPlayInvoked = true;
        }
    }

    private class Player implements Runnable {

        @Override
        public void run() {
            invokeSynth();
            delayedPlayInvoked = false;
        }

    }
}
