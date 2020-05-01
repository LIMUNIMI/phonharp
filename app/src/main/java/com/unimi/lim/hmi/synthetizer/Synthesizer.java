package com.unimi.lim.hmi.synthetizer;

import com.unimi.lim.hmi.entity.Timbre;

public interface Synthesizer {

    void start();

    void stop();

    void updateTimbreCfg(Timbre timbre);

    void press(double frequency);

    void release();

    void controlVolume(double delta);

    void controlPitch(double delta);

    void controlHarmonics(double delta);

    void controlTremoloDepth(double delta);

    void controlVibratoDepth(double delta);

    String getTimbreId();

}
