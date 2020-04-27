package com.unimi.lim.hmi.keyboard;

import android.os.Handler;

import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

// TODO refactor, move everything to keyhandler
public class DelayedKeyHandler extends KeyHandler {

    // TODO move to configuration
    private final static int DELAY = 50;

    private final Handler delayedPlayer = new Handler();
    private final Player player = new Player();
    private boolean delayedPlayInvoked;

    public DelayedKeyHandler(Synthesizer synth, Scale scale, int keyOffset) {
        super(synth, scale, keyOffset);
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
