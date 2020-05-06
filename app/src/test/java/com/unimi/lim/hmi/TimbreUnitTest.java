package com.unimi.lim.hmi;

import com.jsyn.JSyn;
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.CrossFade;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.PulseOscillator;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.unimi.lim.hmi.music.Note;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimbreUnitTest {

    private static final Note PLAY_NOTE = Note.C3;
    private static final double PLAY_DURATION = 3;

    private com.jsyn.Synthesizer synth;
    private PulseOscillator osc;
    private LineOut lineOut;

    private Multiply amplitude;

    // Amplitude envelop
    private VariableRateMonoReader amplEnvPlayer;
    private SegmentedEnvelope amplEnvelope;

    // Frequency envelop
    private VariableRateMonoReader freqEnvPlayer;
    private SegmentedEnvelope freqEnvelope;

    @Before
    public void setup() {
        synth = JSyn.createSynthesizer();
        osc = new PulseOscillator();
        lineOut = new LineOut();
        amplitude = new Multiply();

        synth.add(osc);
        synth.add(lineOut);
        synth.add(amplitude);

        amplitude.inputA.set(1);
        amplitude.inputB.set(1);
        amplitude.output.connect(osc.amplitude);

        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        osc.width.set(0.5);

        synth.start();
    }

    @After
    public void shutDown() {
        synth.stop();
    }

    @Test
    public void aa_freqModVibrato() {
        UnitOscillator modOsc = setupFreqMod(0, 0);
//        double freqs[] = {0.03, 0.3, 0.4, 1.6, 6, 25};
//        for (double freq : freqs) {
//            System.out.println("Vibrato f = " + freq + "Hz, M = 2");
//            modOsc.frequency.set(freq);
//            modOsc.amplitude.set(freq * 2);
//            play(PLAY_NOTE);
//        }
        modOsc.frequency.set(6);
        for (int i = 2; i <= 10; i += 2) {
            double ampl = (double) 6 / 10 * i;
            double depth = ampl / 6 * 100;
            modOsc.amplitude.set(ampl);
            System.out.println("Vibrato f = 6Hz, Depth = " + (int) depth);
            play(PLAY_NOTE);
        }
    }

    @Test
    public void ab_freqModCommon() {
        UnitOscillator modOsc = setupFreqMod(0, 0);
        double common_rates[] = {0.25, 0.5, 1, 1.5, 2, 3, 3.5, 6};
        for (double rate : common_rates) {
            System.out.println("Frequency modifier with rate = " + rate + ", M = 2");
            modOsc.frequency.set(PLAY_NOTE.getFrequency() * rate);
            modOsc.amplitude.set(PLAY_NOTE.getFrequency() * 2);
            play(PLAY_NOTE);
        }
    }

    @Test
    public void ac_FreqModVibratoPlusTimbre() {
        double vbrAm = 6 * 2;
        double vbrRt = 6;
        double tmbAm = PLAY_NOTE.getFrequency() * 2;
        double tmbRt = PLAY_NOTE.getFrequency() * 6;

        System.out.println("Normal");
        setupFreqMod(tmbAm, tmbRt);
        play(PLAY_NOTE);

        System.out.println("Vibrato");
        UnitOscillator vibrato = new SineOscillator();
        vibrato.frequency.set(vbrRt);
        vibrato.amplitude.set(vbrAm);

        UnitOscillator timbre = new SineOscillator();
        timbre.frequency.set(tmbRt);
        timbre.amplitude.set(tmbAm);

        Add mix = new Add();
        mix.inputA.connect(vibrato.output);
        mix.inputB.connect(timbre.output);

        Add mod = new Add();
        mod.inputA = new UnitInputPort("Carrier freq", PLAY_NOTE.getFrequency());
        mod.inputB.connect(mix.output);

        UnitOscillator out = new SineOscillator();
        out.amplitude.set(1);
        out.frequency.connect(mod.output);

        synth.add(vibrato);
        synth.add(timbre);
        synth.add(mix);
        synth.add(mod);
        synth.add(out);

        play(PLAY_NOTE, out.output);
    }


    @Test
    public void ba_AmplModTremolo() {
        UnitOscillator modOsc = setupAmplMod(0.5, 0.5, 0);
        //double freqs[] = {0.03, 0.3, 0.4, 1.6, 6, 25};
//        double freqs[] = {0.1, 1, 6, 10, 15, 20};
//        for (double freq : freqs) {
//            System.out.println("Tremolo f = " + freq + "Hz, M = 1");
//            modOsc.frequency.set(freq);
//            play(PLAY_NOTE);
//        }

        // Test different depths
//        modOsc.frequency.set(6);
//        for (int i = 2; i <= 10; i += 2) {
//            double ampl = 0.5 / 10 * (double) i;
//            double depth = ampl / 0.5 * 100;
//            modOsc.amplitude.set(ampl);
//            System.out.println("Tremolo f = 6Hz, Depth = " + (int) depth);
//            play(PLAY_NOTE);
//        }

        modOsc = setupAmplMod(0.6666666666666666, 0.33333333333333337, 6);
        play(PLAY_NOTE);
    }

    @Test
    public void c_AmplModWithMultiplier() {
        double amount = 0.5;
        double dcOffset = 0.5;
        double rate = 6;

        // Usual ampl modifier
        System.out.println("Usual ampl lfoOsc");
        setupAmplMod(amount, dcOffset, rate);
        play(PLAY_NOTE);

        // Amplitude modulator by multiplying two oscillators
        System.out.println("Mult ampl lfoOsc");

        // Carrier
        SineOscillator carOsc = new SineOscillator();
        carOsc.frequency.set(PLAY_NOTE.getFrequency());
        carOsc.amplitude.set(1);

        // Modulator
        SineOscillator modOsc = new SineOscillator();
        modOsc.frequency.set(rate);
        modOsc.amplitude.set(amount);
        Add mod = new Add();
        mod.inputA = new UnitInputPort("DC offset", dcOffset);
        mod.inputB.connect(modOsc.output);

        // Multiplied signal
        Multiply mult = new Multiply();
        mult.inputA.connect(carOsc.output);
        mult.inputB.connect(mod.output);

        synth.add(carOsc);
        synth.add(modOsc);
        synth.add(mod);
        synth.add(mult);

        play(PLAY_NOTE, mult.output);
    }

    @Test
    public void z_TestVari() {
//        System.out.println("Normal f=" + PLAY_NOTE.getFrequency());
//        play(PLAY_NOTE);
//
//        double cutoffFreq = PLAY_NOTE.getFrequency() / 2;
//        System.out.println("LowPass filter cf=" + cutoffFreq);
//        FilterLowPass flp = new FilterLowPass();
//        flp.frequency.set(cutoffFreq);
//        flp.amplitude.set(1);
//        flp.input.connect(osc.output);
//
//        synth.add(flp);
//        play(PLAY_NOTE, flp.output);
        CrossFade crossFade = new CrossFade();
        crossFade.generate();
        play(PLAY_NOTE);
    }


    private UnitOscillator setupFreqMod(double amount, double rate) {
        // Define unit
        Add adder = new Add();
        UnitOscillator modOsc = new SineOscillator();

        // Add to synth
        synth.add(adder);
        synth.add(modOsc);

        // Setup
        modOsc.amplitude.set(amount);
        modOsc.frequency.set(rate);
        adder.inputA.set(PLAY_NOTE.getFrequency());

        // Connect
        modOsc.output.connect(adder.inputB);
        adder.output.connect(osc.frequency);
        return modOsc;
    }

    private UnitOscillator setupAmplMod(double amount, double dcOffset, double rate) {
        // Define unit
        Add adder = new Add();
        UnitOscillator modOsc = new TriangleOscillator();

        // Add to synth
        synth.add(adder);
        synth.add(modOsc);

        // Setup
        modOsc.amplitude.set(amount);
        modOsc.frequency.set(rate);
        adder.inputA.set(dcOffset);

        // Connections
        adder.inputB.connect(modOsc.output);
        adder.output.connect(amplitude.inputA);
        return modOsc;
    }

    private void setupAmplEnvelop() {
        amplEnvelope = new SegmentedEnvelope(new double[]{
                0.5, 1.0,     // attack
                1, 0.5,       // decay
                1, 0          // release
        });
        // Hang at end of decay segment to provide a "sustain" segment.
        amplEnvelope.setSustainBegin(1);
        amplEnvelope.setSustainEnd(1);

        amplEnvPlayer = new VariableRateMonoReader();
        synth.add(amplEnvPlayer);
        amplEnvPlayer.dataQueue.clear();
        amplEnvPlayer.dataQueue.queue(amplEnvelope, 0, amplEnvelope.getNumFrames());
        amplEnvPlayer.output.connect(amplitude.inputB);
        amplEnvPlayer.start();
    }

    private void setupFreqEnvelop() {
        freqEnvelope = new SegmentedEnvelope(new double[]{
                0.1, PLAY_NOTE.getFrequency(),     // attack
                0.5, PLAY_NOTE.getFrequency() / 1.5,       // decay
                2, PLAY_NOTE.getFrequency() / 2          // release
        });
        // Hang at end of decay segment to provide a "sustain" segment.
        freqEnvelope.setSustainBegin(1);
        freqEnvelope.setSustainEnd(1);

        freqEnvPlayer = new VariableRateMonoReader();
        synth.add(freqEnvPlayer);
        freqEnvPlayer.dataQueue.clear();
        freqEnvPlayer.dataQueue.queue(freqEnvelope, 0, freqEnvelope.getNumFrames());
        freqEnvPlayer.output.connect(osc.frequency);
        freqEnvPlayer.start();
    }


    private void play(Note note) {
        osc.frequency.set(note.getFrequency());

        lineOut.start();
        if (amplEnvPlayer != null) {
            amplEnvPlayer.dataQueue.clear();
            amplEnvPlayer.dataQueue.queue(amplEnvelope, 0, 1);
            amplEnvPlayer.dataQueue.queueLoop(amplEnvelope, 1, 1);
            amplEnvPlayer.dataQueue.queue(amplEnvelope, 2, 1);
        }
        if (freqEnvPlayer != null) {
            freqEnvPlayer.dataQueue.clear();
            freqEnvPlayer.dataQueue.queue(freqEnvelope, 0, 1);
            freqEnvPlayer.dataQueue.queueLoop(freqEnvelope, 1, 1);
            freqEnvPlayer.dataQueue.queue(freqEnvelope, 2, 1);
        }
        try {
            synth.sleepFor(PLAY_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lineOut.stop();
    }

    private void play(Note note, UnitOutputPort outputPort) {
        osc.output.disconnect(0, lineOut.input, 0);
        osc.output.disconnect(0, lineOut.input, 1);

        outputPort.connect(0, lineOut.input, 0);
        outputPort.connect(0, lineOut.input, 1);

        lineOut.start();
        try {
            synth.sleepFor(PLAY_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lineOut.stop();
    }

}
