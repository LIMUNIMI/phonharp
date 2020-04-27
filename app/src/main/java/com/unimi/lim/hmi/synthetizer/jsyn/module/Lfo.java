package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.unimi.lim.hmi.synthetizer.WaveForm;

public class Lfo extends Circuit {

    protected final UnitOscillator lfoOsc;

    public Lfo(WaveForm waveForm) {
        this(0, 0, waveForm);
    }

    /**
     * Constructor
     *
     * @param frequency modulator frequency
     * @param waveForm  modulator wave form
     */
    public Lfo(double frequency, WaveForm waveForm) {
        this(frequency, 0, waveForm);
    }

    /**
     * Constructor
     *
     * @param frequency modulator frequency
     * @param amplitude (note that volume value range is between 0 and 1)
     * @param waveForm  modulator wave form
     */
    public Lfo(double frequency, double amplitude, WaveForm waveForm) {
        switch (waveForm) {
            case SQUARE:
                lfoOsc = new SquareOscillator();
                break;
            case SINE:
                lfoOsc = new SineOscillator();
                break;
            case TRIANGLE:
                lfoOsc = new TriangleOscillator();
                break;
            default:
                throw new IllegalArgumentException("Unexpected wave form type, waveform " + waveForm + " is not supported");
        }
        setFrequency(frequency);
        setAmplitude(amplitude);
        add(lfoOsc);
    }

    public void setAmplitude(double amplitude) {
        lfoOsc.amplitude.set(amplitude);
    }

    public void setFrequency(double frequency) {
        lfoOsc.frequency.set(frequency);
    }

    public double getAmplitude() {
        return lfoOsc.amplitude.get();
    }

    public double getFrequency() {
        return lfoOsc.frequency.get();
    }
}
