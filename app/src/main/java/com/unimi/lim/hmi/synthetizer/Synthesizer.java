package com.unimi.lim.hmi.synthetizer;

public interface Synthesizer {

    void start();

    void stop();

    void press(double frequency);

    void release();

}
