package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.ports.UnitOutputPort;
import com.unimi.lim.hmi.synthetizer.WaveForm;

public class Pwm extends Lfo {

    public final UnitOutputPort output;

    private int depth;

    public Pwm() {
        this(0, 0);
    }

    /**
     * Constructor
     *
     * @param frequency hertz
     * @param depth     between 0% and 100%
     */
    public Pwm(double frequency, int depth) {
        super(frequency, WaveForm.TRIANGLE);

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
        this.depth = depth;
        setAmplitude((double) depth / 100);
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
