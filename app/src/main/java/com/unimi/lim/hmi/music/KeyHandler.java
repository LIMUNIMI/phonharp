package com.unimi.lim.hmi.music;

import android.os.Handler;
import android.util.Log;

import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

import static com.unimi.lim.hmi.entity.Timbre.Controller.NONE;

public class KeyHandler {

    // Constant values, can be moved to configurations
    private final static int HALF_TONE_SEMITONES = -1;
    private final static int HYSTERESIS_DELAY = 50;
    private final static float CONTROL_VOLUME_FACTOR = 1f / 10;
    private final static float CONTROL_PITCH_FACTOR = 1f / 4;
    private final static float CONTROL_HARMONICS_FACTOR = 1f / 50;
    private final static float CONTROL_TREMOLO_FACTOR = 5;
    private final static float CONTROL_VIBRATO_FACTOR = 5;

    // Key handler data
    private final Synthesizer synth;
    private final Scale scale;
    private final int keyOffset;
    private final Timbre timbre;
    private final Handler delayedPlayer;

    private boolean delayedPlayInvoked;
    private int noteNum = -1;
    private int halfTone = 0;

    public KeyHandler(Synthesizer synth, Scale scale, int keyOffset, Timbre timbre) {
        this.scale = scale;
        this.synth = synth;
        this.keyOffset = keyOffset;
        this.delayedPlayer = new Handler();
        this.timbre = timbre;
    }

    public void keyPressed(int keyNum) {
        // Reset controller values
        synth.controlReset();

        // Play current note
        noteNum += keyNumToWeight(keyNum);
        play();
    }

    public void keyReleased(int keyNum) {
        noteNum -= keyNumToWeight(keyNum);
        play();
    }

    public void halfToneKeyPressed() {
        halfTone = HALF_TONE_SEMITONES;
        play();
    }

    public void halfToneKeyReleased() {
        halfTone = 0;
        play();
    }

    public void control1(float delta) {
        control(delta, timbre.getController1());
    }

    public void control2(float delta) {
        control(delta, timbre.getController2());
    }

    private void control(float delta, Timbre.Controller controller) {
        if (controller == null || controller == NONE) {
            // nothing to control here
            return;
        }
        switch (controller) {
            case VOLUME:
                synth.controlVolume(delta * CONTROL_VOLUME_FACTOR);
                break;
            case PITCH:
                synth.controlPitch(delta * CONTROL_PITCH_FACTOR);
                break;
            case HARMONICS:
                synth.controlHarmonics(delta * CONTROL_HARMONICS_FACTOR);
                break;
            case TREMOLO:
                synth.controlTremoloDepth(delta * CONTROL_TREMOLO_FACTOR);
                break;
            case VIBRATO:
                synth.controlVibratoDepth(delta * CONTROL_VIBRATO_FACTOR);
                break;
        }
    }

    private void play() {
        if (timbre.getTapHysteresis() == 0) {
            invokeSynth();
        } else if (!delayedPlayInvoked) {
            delayedPlayer.postDelayed(() -> {
                invokeSynth();
                delayedPlayInvoked = false;
            }, HYSTERESIS_DELAY);
            delayedPlayInvoked = true;
        }
    }

    private void invokeSynth() {
        long start = System.currentTimeMillis();
        if (noteNum < 0) {
            synth.release();
            Log.d(getClass().getName(), "Note released " + (System.currentTimeMillis() - start));
        } else {
            Note note = scale.getNote(noteNum + keyOffset, halfTone);
            synth.press(note.getFrequency());
            Log.d(getClass().getName(), "Playing note " + note.toString() + " " + (System.currentTimeMillis() - start));
        }
    }

    private int keyNumToWeight(int keyNum) {
        return (int) Math.pow(2, keyNum);
    }

}
