package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Add;
import com.unimi.lim.hmi.synthetizer.WaveForm;

public class Tremolo extends Lfo {

    public final UnitOutputPort output;

    private final Add modulator;
    private int depth;

    public Tremolo() {
        this(0, 0);
    }

    /**
     * Constructor
     *
     * @param frequency frequency
     * @param depth     between 0% and 100%
     */
    public Tremolo(double frequency, int depth) {
        super(frequency, WaveForm.SINE);

        // Setup modulating wave amplitude (oscillator + dcoffset)
        add(modulator = new Add());
        setDepth(depth);
        modulator.inputB.connect(lfoOsc.output);

        // Provides output port to allow connections
        output = modulator.output;
    }

    /**
     * Giving depth setup modulator amplitude and dcoffset
     *
     * @param depth depth value between 0% and 100%
     */
    public void setDepth(int depth) {
        this.depth = depth;

        // depth = amplitude / dcoffset * 100
        // Giving depth and considering that amplitude + dcoffset = 1 (max amplitude value)
        // calculates both amplitude and dcoffset
        double amplitude = (double) 1 - ((double) 100 / (depth + 100));
        double dcOffset = (double) 100 / (depth + 100);

        setAmplitude(amplitude);
        modulator.inputA.set(dcOffset);
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
