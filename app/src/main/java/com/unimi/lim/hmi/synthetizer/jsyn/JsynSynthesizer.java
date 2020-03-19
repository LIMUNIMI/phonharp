package com.unimi.lim.hmi.synthetizer.jsyn;

import com.jsyn.JSyn;
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.WaveForm;

public class JsynSynthesizer implements Synthesizer {

    private final com.jsyn.Synthesizer synth;
    private final UnitOscillator osc;
    private final LineOut lineOut;
    private final VariableRateMonoReader envPlayer;
    private SegmentedEnvelope envelope;

    public JsynSynthesizer(WaveForm waveForm) {

        switch (waveForm) {
            case SINE:
                osc = new SineOscillator();
                break;
            case SQUARE:
                osc = new SquareOscillator();
                break;
            case TRIANGLE:
                osc = new TriangleOscillator();
                break;
            default:
                throw new IllegalArgumentException("Unknown wave form [" + waveForm + "]");
        }

        // Create a JSyn synthesizer that uses the Android output.
        synth = JSyn.createSynthesizer(new JSynAndroidAudioDevice());

        // Create the unit generators and add them to the synthesizer.
        synth.add(osc);
        synth.add(lineOut = new LineOut());

        // Connect an osc to each channel of the LineOut.
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Default envelop configuration
        setEnvelopData(new double[]{
                0, 1, // attack
                0, 1, // decay
                0, 0 // release
        });

        synth.add(envPlayer = new VariableRateMonoReader());

        // Connect envelope to osc amplitude
        envPlayer.output.connect(osc.amplitude);
    }

    @Override
    public void start() {
        synth.start();
        lineOut.start();
    }

    @Override
    public void stop() {
        lineOut.stop();
        synth.stop();
    }

    @Override
    public void setEnvelopData(double[] evenlopData) {
        envelope = new SegmentedEnvelope(evenlopData);
        // Hang at end of decay segment to provide a "sustain" segment.
        envelope.setSustainBegin(1);
        envelope.setSustainEnd(1);
    }

    @Override
    public void press(double frequency) {
        osc.frequency.set(frequency);
        envPlayer.dataQueue.clear();
        envPlayer.dataQueue.queue(envelope, 0, 1);
        envPlayer.dataQueue.queueLoop(envelope, 1, 1);
    }

    @Override
    public void release() {
        envPlayer.dataQueue.queue(envelope, 2, 1);
    }
}
