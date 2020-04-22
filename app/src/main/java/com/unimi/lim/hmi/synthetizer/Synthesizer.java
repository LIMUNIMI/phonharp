package com.unimi.lim.hmi.synthetizer;

public interface Synthesizer {

    void start();

    void stop();

    void press(double frequency);

    void release();

    void controlVolume(double delta);

    void controlPitch(double delta);

    void controlHarmonics(double delta);

    void controlTremoloDepth(double delta);

    void controlVibratoDepth(double delta);

}
