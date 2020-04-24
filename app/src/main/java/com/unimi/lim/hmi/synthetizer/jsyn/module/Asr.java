package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.VariableRateMonoReader;

public class Asr extends Circuit {

    public UnitOutputPort output;

    private VariableRateMonoReader envPlayer;
    private SegmentedEnvelope envData;

    public Asr(double attackTime, double releaseTime) {
        this(0, attackTime, 0, releaseTime, 0);
    }

    public Asr(double initialValue, double attackTime, double sunstainValue, double releaseTime, double finalValue) {
        // Envelop data
        envData = new SegmentedEnvelope(new double[]{
                // First frame is used to setup initial value, indeed first frame time is 0
                0, initialValue,
                // Attack frame
                attackTime, sunstainValue,
                // Release frame
                releaseTime, finalValue});

        // Envelop player
        add(envPlayer = new VariableRateMonoReader());
        envPlayer.dataQueue.clear();

        output = envPlayer.output;
    }

    public void start() {
        envPlayer.start();
    }

    public void stop() {
        envPlayer.stop();
    }

    public void updateValues(double initialValue, double sunstainValue, double finalValue) {
        envData.writeDouble(0, initialValue);
        envData.writeDouble(1, sunstainValue);
        envData.writeDouble(2, finalValue);
    }

    public void press() {
        // TODO comment or uncomment queue clear
        //envPlayer.dataQueue.clear();
        envPlayer.dataQueue.queue(envData, 0, 1);
        envPlayer.dataQueue.queue(envData, 1, 1);
    }

    public void release() {
        envPlayer.dataQueue.queue(envData, 2, 1);
    }

}
