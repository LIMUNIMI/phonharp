package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.ports.UnitOutputPort;
import com.unimi.lim.hmi.synthetizer.WaveForm;

public class Vibrato extends Lfo {

    public UnitOutputPort output;

    private int depth;

    public Vibrato() {
        this(0, 0);
    }

    /**
     * Constructor
     *
     * @param frequency frequency
     * @param depth     between 0 and 100
     */
    public Vibrato(double frequency, int depth) {
        super(frequency, WaveForm.TRIANGLE);

        // Setup modulating wave amplitude
        setDepth(depth);

        // Provides output port to allow connections
        output = lfoOsc.output;
    }

    /**
     * Giving depth setup modulator amplitude and dcoffset
     *
     * @param depth depth value between 0 and 100
     */
    public void setDepth(int depth) {
        if (depth < 0 || depth > 100) {
            throw new IllegalArgumentException("Invalid depth value, expected bewtween 0 and 100 bus is " + depth);
        }
        this.depth = depth;

        // depth = amplitude / frequency * 100
        // Giving depth and frequency calculates amplitude
        // calculates both amplitude and dcoffset
        double amplitude = (double) depth * getFrequency() / 100;
        setAmplitude(amplitude);
    }

    /**
     * Return depth value
     *
     * @return depth value
     */
    public int getDepth() {
        return depth;
    }

}
