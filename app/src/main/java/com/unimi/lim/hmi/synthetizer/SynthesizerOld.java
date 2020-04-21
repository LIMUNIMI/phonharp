package com.unimi.lim.hmi.synthetizer;

public interface SynthesizerOld {

    void start();

    void stop();

    void setEnvelopData(double[] evenlopData);

    void press(double frequency);

    void release();

}
