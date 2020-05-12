package com.unimi.lim.hmi;

import com.jsyn.JSyn;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PulseOscillator;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Asr;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Equalizer;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Tremolo;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Vibrato;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SynthModuleUnitTest {

    private static final Note PLAY_NOTE = Note.C3;
    private static final double PLAY_DURATION = 3;

    private com.jsyn.Synthesizer synth;
    private PulseOscillator osc;
    private LineOut lineOut;

    @Before
    public void init() {
        synth = JSyn.createSynthesizer();
        osc = new PulseOscillator();
        lineOut = new LineOut();

        synth.add(osc);
        synth.add(lineOut);

        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        synth.start();
    }

    @After
    public void shutDown() {
        synth.stop();
    }


    @Test
    public void testTremolo() {
        System.out.println("Tremolo...");
        Tremolo tremolo = new Tremolo(6, 100);
        tremolo.output.connect(osc.amplitude);
        synth.add(tremolo);
        play(PLAY_NOTE);
    }

    @Test
    public void testVibrato() {
        System.out.println("Vibrato...");
        Vibrato vibrato = new Vibrato(6, 100);
        synth.add(vibrato);

        Add adder = new Add();
        adder.inputA.set(PLAY_NOTE.getFrequency());
        adder.inputB.connect(vibrato.output);
        adder.output.connect(osc.frequency);

        play();
    }

    @Test
    public void testAmplEnvelop() {
        Asr asr = new Asr(0, 2, 1, 1, 0);
        synth.add(asr);
        asr.output.connect(osc.amplitude);

        osc.frequency.set(PLAY_NOTE.getFrequency());

        lineOut.start();
        asr.start();

        // Simulate key pressed
        asr.press();
        sleep(3);
        // Simulate key released
        asr.release();
        sleep(3);

        asr.stop();
        lineOut.stop();
    }

    @Test
    public void testFreqEnvelop() {
        Asr asr = new Asr(-100, 2, 0, 2, 100);
        synth.add(asr);

        Add adder = new Add();
        adder.inputA.set(PLAY_NOTE.getFrequency());
        adder.inputB.connect(asr.output);

        adder.output.connect(osc.frequency);

        lineOut.start();
        asr.start();

        // Simulate key pressed
        asr.press();
        sleep(3);
        // Simulate key released
        asr.release();
        sleep(3);

        // Changes offset
        asr.updateValues(200, 0, 200);
        asr.press();
        sleep(3);
        asr.release();
        sleep(3);

        asr.stop();
        lineOut.stop();
    }

    @Test
    public void testEqualizer() {
        System.out.println("Without EQ");
//        play(Note.C4);

        osc.output.disconnect(0, lineOut.input, 0);
        osc.output.disconnect(0, lineOut.input, 1);

        Equalizer eq = new Equalizer(osc.output);
        synth.add(eq);

        eq.output.connect(0, lineOut.input, 0);
        eq.output.connect(0, lineOut.input, 1);

        System.out.println("EQ 0 0");
        eq.setLowShelfGain(0);
        eq.setHighShelfGain(0);
        play(Note.C4);

        double max = 16;

        System.out.println("EQ 0 4");
        eq.setLowShelfGain(0);
        eq.setHighShelfGain(max);
        play(Note.C4);

        System.out.println("EQ 4 0");
        eq.setLowShelfGain(max);
        eq.setHighShelfGain(0);
        play(Note.C4);

        System.out.println("EQ 4 4");
        eq.setLowShelfGain(0);
        eq.setHighShelfGain(max);
        play(Note.C4);

    }


    private void play() {
        play(null);
    }

    private void play(Note note) {
        note = note != null ? note : PLAY_NOTE;
        System.out.println("Playing note " + note);
        osc.frequency.set(note.getFrequency());
        lineOut.start();
        sleep(PLAY_DURATION);
        lineOut.stop();
    }

    private void sleep(double duration) {
        try {
            synth.sleepFor(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
