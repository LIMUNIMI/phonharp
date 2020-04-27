package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.VariableRateMonoReader;

public class Asr extends Circuit {

    public UnitOutputPort output;

    private VariableRateMonoReader envPlayer;
    private SegmentedEnvelope envData;

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
        // Envelop data
        envData = new SegmentedEnvelope(new double[]{
                // First frame is used to setup initial value, indeed first frame time is 0
                0, initialValue,
                // Attack frame
                attackTime, sustainValue,
                // Release frame
                releaseTime, finalValue});

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
    }

    /**
     * Starting from initial value reaches sustain value after attack time seconds
     */
    public void press() {
        // TODO comment or uncomment queue clear
        envPlayer.dataQueue.clear();
        envPlayer.dataQueue.queue(envData, 0, 1);
        envPlayer.dataQueue.queue(envData, 1, 1);
    }

    /**
     * Starting from sustain value reaches final value after release time seconds
     */
    public void release() {
        envPlayer.dataQueue.queue(envData, 2, 1);
    }

}
