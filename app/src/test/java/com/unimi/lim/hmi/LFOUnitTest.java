package com.unimi.lim.hmi;

import com.jsyn.JSyn;
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.unimi.lim.hmi.music.Note;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LFOUnitTest {

    private static final Note PLAY_NOTE = Note.C4;
    private static final double PLAY_DURATION = 3;

    private com.jsyn.Synthesizer synth;
    private UnitOscillator osc;
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
        osc = new SineOscillator();
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

        synth.start();
    }

    @After
    public void shutDown() {
        synth.stop();
    }

    @Test
    public void aa_freqModVibrato() {
        UnitOscillator modOsc = setupFreqMod(0, 0);
        double freqs[] = {0.03, 0.3, 0.4, 1.6, 6, 25};
        for (double freq : freqs) {
            System.out.println("Vibrato f = " + freq + "Hz, M = 2");
            modOsc.frequency.set(freq);
            modOsc.amplitude.set(freq * 2);
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
    public void ba_AmplModTremolo() {
        UnitOscillator modOsc = setupAmplMod(0.5, 0.5, 0);
        double freqs[] = {0.03, 0.3, 0.4, 1.6, 6, 25};
        for (double freq : freqs) {
            System.out.println("Tremolo f = " + freq + "Hz, M = 1");
            modOsc.frequency.set(freq);
            play(PLAY_NOTE);
        }
    }

    @Test
    public void b_TestVari() {
        setupAmplEnvelop();
        //setupFreqEnvelop();
        setupAmplMod(0.15, 0.5, 3);
        //setupFreqMod(PLAY_NOTE.getFrequency()*6, PLAY_NOTE.getFrequency()*2);
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
        UnitOscillator modOsc = new SineOscillator();

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
                0.5, PLAY_NOTE.getFrequency()/1.5,       // decay
                2, PLAY_NOTE.getFrequency()/2          // release
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

}
