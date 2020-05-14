package com.unimi.lim.hmi.synthetizer;

import android.os.Handler;
import android.util.Log;

import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.Scale;

import static com.unimi.lim.hmi.entity.Timbre.Controller.NONE;

public class KeyHandler {

    // Constant values, can be moved to configurations
    private final static int HALF_TONE_SEMITONES = -1;

    // Scaling factors between units and controller units
    private final static float CONTROL_VOLUME_FACTOR = 1f / 10;
    private final static float CONTROL_PITCH_FACTOR = 1f / 8;
    private final static float CONTROL_HARMONICS_FACTOR = 1f / 50;
    private final static float CONTROL_TREMOLO_FACTOR = 2.5f;
    private final static float CONTROL_VIBRATO_FACTOR = 2.5f;
    private final static float CONTROL_PWM_FACTOR = 2.5f;

    // Key handler configurations
    private final Synthesizer synth;
    private final Scale scale;
    private final int keyOffset;
    private final Timbre timbre;
    private final Handler delayedPlayer;

    // Instance data
    private boolean delayedPlayInvoked;
    private int noteNum = -1; // note number, from 0 to 2^4 - 1
    private int halfTone = 0; // 0 when released, -1 when pressed

    public KeyHandler(Synthesizer synth, Scale scale, int keyOffset, Timbre timbre) {
        this.scale = scale;
        this.synth = synth;
        this.keyOffset = keyOffset;
        this.delayedPlayer = new Handler();
        this.timbre = timbre;
    }

    /**
     * Handle key press
     *
     * @param keyNum number of pressed key, from 1 to 4
     */
    public void keyPressed(int keyNum) {
        // Reset controller values
        synth.controlReset();

        // Play current note
        noteNum += keyNumToWeight(keyNum);
        play();
    }

    /**
     * Handle key release
     *
     * @param keyNum number of released key, from 1 to 4
     */
    public void keyReleased(int keyNum) {
        noteNum -= keyNumToWeight(keyNum);
        play();
    }

    /**
     * Handle halftone key press
     */
    public void halfToneKeyPressed() {
        halfTone = HALF_TONE_SEMITONES;
        play();
    }

    /**
     * Handle halftone key release
     */
    public void halfToneKeyReleased() {
        halfTone = 0;
        play();
    }

    /**
     * Handle controller 1
     *
     * @param delta delta units
     */
    public void control1(float delta) {
        control(delta, timbre.getController1());
    }

    /**
     * Handle controller 2
     *
     * @param delta delta units
     */
    public void control2(float delta) {
        control(delta, timbre.getController2());
    }

    /**
     * Depending on timbre configuration invokes specific synthesizer controller
     *
     * @param delta      delta units
     * @param controller configured controller
     */
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
            case PWM:
                synth.controlPwmDepth(delta * CONTROL_PWM_FACTOR);
                break;
        }
    }

    /**
     * Provides to invoke synthesizer press or release functions. Note that synth is invoked after hysteresis time, or immediately if hysteresis time is 0
     */
    private void play() {
        if (timbre.getTapHysteresis() == 0) {
            invokeSynth();
        } else if (!delayedPlayInvoked) {
            long delay = (long) (timbre.getTapHysteresis() * 1000);
            Log.d(getClass().getName(), "Delay millis " + delay);
            delayedPlayer.postDelayed(() -> {
                invokeSynth();
                delayedPlayInvoked = false;
            }, delay);
            delayedPlayInvoked = true;
        }
    }

    /**
     * Invoke synthesizer press or release function
     */
    private void invokeSynth() {
        long start = System.currentTimeMillis();
        if (noteNum < 0) {
            synth.release();
        } else {
            // Calculate the note on the scale
            Note note = scale.getNote(noteNum + keyOffset, halfTone);
            synth.press(note.getFrequency());
        }
    }

    /**
     * Calculate key weight
     *
     * @param keyNum key number, from 1 to 4
     * @return 2^kn
     */
    private int keyNumToWeight(int keyNum) {
        return (int) Math.pow(2, keyNum);
    }

}
