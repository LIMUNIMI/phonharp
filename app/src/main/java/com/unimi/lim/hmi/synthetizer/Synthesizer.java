package com.unimi.lim.hmi.synthetizer;

public interface Synthesizer {

    void start();

    void stop();

    void setEnvelopData(double[] evenlopData);

    void press(double frequency);

    void release();

}
