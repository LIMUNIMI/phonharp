package com.unimi.lim.hmi.synthetizer;

import com.unimi.lim.hmi.entity.Timbre;

public interface Synthesizer {

    /**
     * Configuration Id
     *
     * @return configuration id
     */
    String getTimbreId();

    /**
     * Startup synth
     */
    void start();

    /**
     * Stop synth
     */
    void stop();

    /**
     * Update synth configuration
     *
     * @param timbre timbre configuration
     */
    void updateSynthesizerCfg(Timbre timbre);

    /**
     * Play specified note frequency
     *
     * @param frequency hertz
     */
    void press(double frequency);

    /**
     * Release playing note
     */
    void release();

    /**
     * Reset controller values
     */
    void controlReset();

    /**
     * Control volume, specified delta value is added to current volume value (volume range is 0-100)
     *
     * @param delta volume delta, percentage
     */
    void controlVolume(float delta);

    /**
     * Sets base volume (volume range is 0-1)
     *
     * @param volume , between 0 and 1
     */
    void setVolume(float volume);

    /**
     * Control pitch, specified delta is added to current pitch
     *
     * @param delta in semitones
     */
    void controlPitch(float delta);

    /**
     * Control harmonics, specified delta is added to current harmonics value (harmonics range is -1/1)
     *
     * @param delta harmonics delta, percentage
     */
    void controlHarmonics(float delta);

    /**
     * Control tremolo depth, specified delta is added to current depth (depth range is 0-100)
     *
     * @param delta tremolo depth delta, percentage
     */
    void controlTremoloDepth(float delta);

    /**
     * Control vibrato depth, specified delta is added to current depth (depth range is 0-100)
     *
     * @param delta vibrato depth delta, percentage
     */
    void controlVibratoDepth(float delta);

    /**
     * Control PWM depth, specified delta is added to current depth (depth range is 0-100)
     *
     * @param delta vibrato depth delta, percentage
     */
    void controlPwmDepth(float delta);

}
