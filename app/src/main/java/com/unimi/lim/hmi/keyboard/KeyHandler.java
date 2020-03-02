package com.unimi.lim.hmi.keyboard;

import android.util.Log;

import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;

public abstract class KeyHandler {

    protected final static String TAG = "KEY_HANDLER";

    protected int noteNum = -1;
    private int keyOffset;
    private Synthesizer synth;
    private Scale scale;

    public KeyHandler(Synthesizer synthetizer, Scale scale, int keyOffset) {
        this.scale = scale;
        this.synth = synthetizer;
        this.keyOffset = keyOffset;
    }

    public abstract void keyPressed(int keyNum);

    public abstract void keyReleased(int keyNum);

    protected int keyNumToWeight(int keyNum) {
        return (int) Math.pow(2, keyNum);
    }

    protected void play() {
        if (noteNum < 0) {
            Log.d(TAG, "Releasing note");
            synth.release();
        } else {
            Note note = scale.getNote(noteNum + keyOffset);
            Log.d(TAG, "Playing note " + note.toString());
            synth.press(note.getFrequency());
        }
    }

}
