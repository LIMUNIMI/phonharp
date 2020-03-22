package com.unimi.lim.hmi;

import com.jsyn.JSyn;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.unimi.lim.hmi.music.Note;

import org.junit.Before;
import org.junit.Test;

public class LFOUnitTest {

    private static final double PLAY_DURATION = 1;

    private com.jsyn.Synthesizer synth;
    private UnitOscillator osc;
    private UnitOscillator modOsc;
    private LineOut lineOut;

    @Before
    public void setup() {
        synth = JSyn.createSynthesizer();
        osc = new SineOscillator();
        modOsc = new TriangleOscillator();
        lineOut = new LineOut();

        synth.add(osc);
        synth.add(modOsc);
        synth.add(lineOut);

        osc.amplitude.set(1);
        osc.frequency.set(Note.C4.getFrequency());

        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);
    }

    @Test
    public void freqLFO() {
        // Define unit
        Add adder = new Add();

        // Add to synth
        synth.add(adder);

        // Setup
        modOsc.amplitude.set(100);
        modOsc.frequency.set(20);
        adder.inputA = new UnitInputPort("Centre freq", Note.C4.getFrequency());

        // Connect
        modOsc.output.connect(adder.inputB);
        adder.output.connect(osc.frequency);

        // Start
        play();
    }

    @Test
    public void amplLFO() {
        // Setup
        modOsc.amplitude.set(0.5);
        modOsc.frequency.set(20);

        // Connections
        modOsc.output.connect(osc.amplitude);

        // Play
        play();
    }


    private void play() {
        synth.start();
        lineOut.start();
        try {
            synth.sleepFor(PLAY_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lineOut.stop();
        synth.stop();
    }

}
