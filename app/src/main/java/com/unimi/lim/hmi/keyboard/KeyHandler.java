package com.unimi.lim.hmi.keyboard;

import android.util.Log;

import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.SynthesizerOld;

public abstract class KeyHandler {

    protected int noteNum = -1;
    private int halfTone = 0;
    private int keyOffset;
    private SynthesizerOld synth;
    private Scale scale;

    public KeyHandler(SynthesizerOld synth, Scale scale, int keyOffset) {
        this.scale = scale;
        this.synth = synth;
        this.keyOffset = keyOffset;
    }

    public void onStart() {
    }

    public void onDestroy() {
    }

    public void keyPressed(int keyNum) {
        noteNum += keyNumToWeight(keyNum);
        play();
    }

    public void keyReleased(int keyNum) {
        noteNum -= keyNumToWeight(keyNum);
        play();
    }

    public void halfToneKeyPressed() {
        halfTone = -1;
        play();
    }

    public void halfToneKeyReleased() {
        halfTone = 0;
        play();
    }

    protected abstract void play();

    protected void invokeSynth() {
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
