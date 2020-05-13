package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.FilterHighShelf;
import com.jsyn.unitgen.FilterLowShelf;
import com.jsyn.unitgen.PassThrough;

public class Equalizer extends Circuit {

    public UnitOutputPort output;
    public UnitInputPort input;

    // Default values
    private final static double LOW_SHELF_FREQ = 80;
    private final static double HIGH_SHELF_FREQ = 12000;
    private final static double SLOPE = 1;

    // EQ filters
    private final FilterLowShelf lowShelf;
    private final FilterHighShelf highShelf;

    public Equalizer() {
        this(LOW_SHELF_FREQ, HIGH_SHELF_FREQ, SLOPE, SLOPE);
    }

    public Equalizer(double lowShelfFreq, double highShelfFreq, double lowShelfSlope, double highShelfSlope) {
        add(lowShelf = new FilterLowShelf());
        add(highShelf = new FilterHighShelf());

        // Filter setup
        lowShelf.frequency.set(lowShelfFreq);
        lowShelf.slope.set(lowShelfSlope);
        highShelf.frequency.set(highShelfFreq);
        highShelf.slope.set(highShelfSlope);

        // Filter chain
        input = lowShelf.input;
        lowShelf.output.connect(highShelf.input);
        output = highShelf.output;
    }

    /**
     * Set low shelf gain
     *
     * @param gain gain absolute value; from dB to absolute value: abs = 10^(DB/10)
     */
    public void setLowShelfGain(double gain) {
        lowShelf.gain.set(gain);
    }

    /**
     * Set high shelf gain
     *
     * @param gain gain absolute value; from dB to absolute value: abs = 10^(DB/10)
     */
    public void setHighShelfGain(double gain) {
        highShelf.gain.set(gain);
    }

}
