package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.ports.UnitOutputPort;
import com.unimi.lim.hmi.synthetizer.WaveForm;
import com.unimi.lim.hmi.util.NoteUtils;

public class Vibrato extends Lfo {

    public final UnitOutputPort output;

    // Maximum vibrato oscillator amplitude, in semitones (2 semitones)
    private final static int MAX_AMPLITUDE = 2;

    private int depth;

    public Vibrato() {
        this(0, 0);
    }

    /**
     * Constructor
     *
     * @param frequency frequency
     * @param depth     between 0% and 100%
     */
    public Vibrato(double frequency, int depth) {
        super(frequency, WaveForm.SINE);

        // Setup modulating wave amplitude
        setDepth(depth);

        // Provides output port to allow connections
        output = lfoOsc.output;
    }

    /**
     * Set vibrato oscillator amplitude depending on configured depth and carrier frequency
     *
     * @param carrierFrequency hertz
     */
    public void update(double carrierFrequency) {
        float depthToSemitone = (float) MAX_AMPLITUDE / 100 * depth;
        double amplitude = NoteUtils.calculateNoteByOffset(carrierFrequency, depthToSemitone) - carrierFrequency;
        setAmplitude(amplitude);
    }

    /**
     * Set depth value
     *
     * @param depth depth value between 0% and 100%
     */
    public void setDepth(int depth) {
        this.depth = depth;
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
