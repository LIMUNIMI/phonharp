package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.VariableRateMonoReader;

public class Asr extends Circuit {

    public final UnitOutputPort output;

    private final VariableRateMonoReader envPlayer;
    private final SegmentedEnvelope envData;
    private final double[] pairs = new double[8];

    /**
     * Asr constructor
     */
    public Asr() {
        this(0, 0, 0, 0, 0);
    }

    /**
     * Asr constructor
     *
     * @param attackTime  attack time in seconds
     * @param releaseTime release time in seconds
     */
    public Asr(double attackTime, double releaseTime) {
        this(0, attackTime, 0, releaseTime, 0);
    }

    /**
     * Asr constructor
     *
     * @param initialValue initial value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param attackTime   attack time in seconds
     * @param sustainValue sustain value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param releaseTime  release time in seconds
     * @param finalValue   final value (note that volume value range and pulse width value rage is between 0 and 1)
     */
    public Asr(double initialValue, double attackTime, double sustainValue, double releaseTime, double finalValue) {

        // Envelop data, provided constructor int is the number of frames (3 frames: attack, sustain, release)
        envData = new SegmentedEnvelope(pairs.length / 2);
        update(initialValue, attackTime, sustainValue, releaseTime, finalValue);

        // Envelop player
        add(envPlayer = new VariableRateMonoReader());
        envPlayer.dataQueue.clear();

        output = envPlayer.output;
    }

    /**
     * Start envelop player
     */
    public void start() {
        envPlayer.start();
    }

    /**
     * Stop envelop player
     */
    public void stop() {
        envPlayer.stop();
    }

    /**
     * Update envelop values
     *
     * @param initialValue initial value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param sustainValue sustain value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param finalValue   final value (note that volume value range and pulse width value rage is between 0 and 1)
     */
    public void updateValues(double initialValue, double sustainValue, double finalValue) {
        envData.writeDouble(0, initialValue);
        envData.writeDouble(1, sustainValue);
        envData.writeDouble(2, finalValue);
        envData.writeDouble(3, sustainValue);
    }

    /**
     * Update envelop values and time
     *
     * @param initialValue initial value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param attackTime   attack time in seconds
     * @param sustainValue sustain value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param releaseTime  release time in seconds
     * @param finalValue   final value (note that volume value range and pulse width value rage is between 0 and 1)
     */
    public void update(double initialValue, double attackTime, double sustainValue, double releaseTime, double finalValue) {
        update(initialValue, attackTime, sustainValue, releaseTime, finalValue, 0);
    }

    /**
     * Update envelop values and time
     *
     * @param initialValue   initial value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param attackTime     attack time in seconds
     * @param sustainValue   sustain value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param releaseTime    release time in seconds
     * @param finalValue     final value (note that volume value range and pulse width value rage is between 0 and 1)
     * @param portamentoTime portamento time in seconds
     */
    public void update(double initialValue, double attackTime, double sustainValue, double releaseTime, double finalValue, double portamentoTime) {
        // First frame is used to setup initial value, indeed first frame time is 0
        pairs[0] = 0;
        pairs[1] = initialValue;
        // Attack frame, reaches sustain value after attackTime
        pairs[2] = attackTime;
        pairs[3] = sustainValue;
        // Release frame, reaches final value after releaseTime
        pairs[4] = releaseTime;
        pairs[5] = finalValue;
        // Shortcut to sustain value, reaches sustain value after portamento time or immediately if portamento time was not specified
        pairs[6] = portamentoTime;
        pairs[7] = sustainValue;
        // Setup SegmentedEnvelope
        envData.write(pairs);
    }

    /**
     * Starting from initial value reaches sustain value after attack time seconds
     */
    public void press() {
        envPlayer.dataQueue.clear();
        envPlayer.dataQueue.queue(envData, 0, 1);
        envPlayer.dataQueue.queue(envData, 1, 1);
    }

    /**
     * Starting from sustain value reaches final value after release time seconds
     */
    public void release() {
        envPlayer.dataQueue.clear(); // clear to queue to release immediately; eventually stop attack phase
        envPlayer.dataQueue.queue(envData, 2, 1);
    }

    /**
     * Shortcut to sustain value, reaches sustain value after portamento time or immediately if portamento time was not specified
     */
    public void sustain() {
        envPlayer.dataQueue.queue(envData, 3, 1);
    }

}
