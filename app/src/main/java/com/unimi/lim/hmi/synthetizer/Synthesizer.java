package com.unimi.lim.hmi.synthetizer;

import com.unimi.lim.hmi.entity.Timbre;

public interface Synthesizer {

    void start();

    void stop();

    void updateSynthesizerCfg(Timbre timbre);

    void press(double frequency);

    void release();

    void controlReset();

    void controlVolume(float delta);

    void controlPitch(float delta);

    void controlHarmonics(float delta);

    void controlTremoloDepth(float delta);

    void controlVibratoDepth(float delta);

    void controlPwmDepth(float delta);

    String getTimbreId();

}
