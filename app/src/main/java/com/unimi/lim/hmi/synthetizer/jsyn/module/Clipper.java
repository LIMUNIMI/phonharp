package com.unimi.lim.hmi.synthetizer.jsyn.module;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.Maximum;
import com.jsyn.unitgen.Minimum;

public class Clipper extends Circuit {

    public UnitInputPort input;
    public UnitOutputPort output;

    /**
     * Clip signal between provided minimum and maximum values
     *
     * @param min minimum value
     * @param max maximum value
     */
    public Clipper(double min, double max) {
        Minimum minimum = new Minimum();
        Maximum maximum = new Maximum();
        add(minimum);
        add(maximum);

        minimum.inputB.set(max);
        minimum.output.connect(maximum.inputA);
        maximum.inputB.set(min);

        input = minimum.inputA;
        output = maximum.output;
    }
}
