package com.unimi.lim.hmi.synthetizer.jsyn;

import com.jsyn.JSyn;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.PulseOscillator;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.jsyn.device.JSynAndroidAudioDevice;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Asr;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Tremolo;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Vibrato;
import com.unimi.lim.hmi.util.NoteUtils;
import com.unimi.lim.hmi.util.TimbreUtils;

public class JsynSynthesizer implements Synthesizer {

    public static class Builder {

        private Timbre timbre;
        private AudioDeviceManager audioDeviceManager;

        public Builder timbreCfg(Timbre timbre) {
            this.timbre = timbre;
            return this;
        }

        public Builder defaultAudioDeviceManager() {
            this.audioDeviceManager = new JSynAndroidAudioDevice();
            return this;
        }

        public Builder audioDeviceManager(AudioDeviceManager audioDeviceManager) {
            this.audioDeviceManager = audioDeviceManager;
            return this;
        }

        public JsynSynthesizer build() {
            return new JsynSynthesizer(audioDeviceManager, timbre);
        }
    }

    private final com.jsyn.Synthesizer synth;
    private final LineOut lineOut;
    private final PulseOscillator osc;
    private final UnitInputPort volumeController;
    private final UnitInputPort pitchController;
    private final UnitInputPort harmonicsController;
    private final Tremolo tremolo;
    private final Vibrato vibrato;
    private final Asr volumeEnvelop;
    private final Asr pitchEnvelop;
    private final Asr harmonicsEnvelop;
    private final Timbre timbre;

    private JsynSynthesizer(AudioDeviceManager audioDeviceManager, Timbre timbre) {
        this.synth = audioDeviceManager != null ? JSyn.createSynthesizer(audioDeviceManager) : JSyn.createSynthesizer();
        timbre = timbre != null ? timbre : new Timbre();
        this.timbre = timbre;

        // Volume mixers: volMix1=envelop*tremolo, volMix2=controller*volMix1
        Multiply volMix1;
        Multiply volMix2;

        // Pitch mixers: pitchMix1=envelop+vibrato, pitchMix2=controller+pitchMix1
        Add pitchMix1;
        Add pitchMix2;

        // Harmonics mixers: harmMix=envelop+controller
        Add harmMix;

        // Add generators to synthesizer
        synth.add(lineOut = new LineOut());
        synth.add(osc = new PulseOscillator());
        synth.add(volMix1 = new Multiply());
        synth.add(volMix2 = new Multiply());
        synth.add(pitchMix1 = new Add());
        synth.add(pitchMix2 = new Add());
        synth.add(harmMix = new Add());
        synth.add(tremolo = new Tremolo());
        synth.add(vibrato = new Vibrato());
        synth.add(volumeEnvelop = new Asr(TimbreUtils.safeAsrAttackTime(timbre.getVolumeAsr()), TimbreUtils.safeAsrReleaseTime(timbre.getVolumeAsr())));
        synth.add(pitchEnvelop = new Asr(TimbreUtils.safeAsrAttackTime(timbre.getPitchAsr()), TimbreUtils.safeAsrReleaseTime(timbre.getPitchAsr())));
        synth.add(harmonicsEnvelop = new Asr(TimbreUtils.safeAsrAttackTime(timbre.getHarmonicsAsr()), TimbreUtils.safeAsrReleaseTime(timbre.getHarmonicsAsr())));

        // Controlled values
        volumeController = volMix2.inputA;
        pitchController = pitchMix2.inputA;
        harmonicsController = harmMix.inputB;
        // Default value because is part of multiply operation
        volumeController.set(1);

        // Connect modules together
        // Volume mixers: volMix1=envelop*tremolo, volMix2=controller*volMix1
        volumeEnvelop.output.connect(volMix1.inputA);
        tremolo.output.connect(volMix1.inputB);
        volMix1.output.connect(volMix2.inputB);
        volMix2.output.connect(osc.amplitude);

        // Pitch mixers: pitchMix1=envelop+vibrato, pitchMix2=controller+pitchMix1
        pitchEnvelop.output.connect(pitchMix1.inputA);
        vibrato.output.connect(pitchMix1.inputB);
        pitchMix1.output.connect(pitchMix2.inputB);
        pitchMix2.output.connect(osc.frequency);

        // Harmonics mixers: harmMix=envelop+harmonics
        harmonicsEnvelop.output.connect(harmMix.inputA);
        harmMix.output.connect(osc.width);

        // Line out
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        updateTimbreCfg(timbre);
    }

    @Override
    public void start() {
        synth.start();
        lineOut.start();
        volumeEnvelop.start();
        pitchEnvelop.start();
        harmonicsEnvelop.start();
    }

    @Override
    public void stop() {
        volumeEnvelop.stop();
        pitchEnvelop.stop();
        harmonicsEnvelop.stop();
        lineOut.stop();
        synth.stop();
    }

    @Override
    public void updateTimbreCfg(Timbre timbre) {
        // Values are divided by 100 because ui and stored ranges are 0-100 but jsyn range is 0-1
        tremolo.setFrequency(TimbreUtils.safeLfoRate(timbre.getTremolo()));
        tremolo.setDepth(TimbreUtils.safeLfoDepth(timbre.getTremolo()));
        vibrato.setFrequency(TimbreUtils.safeLfoRate(timbre.getVibrato()));
        vibrato.setDepth(TimbreUtils.safeLfoDepth(timbre.getVibrato()));
        volumeEnvelop.updateValues(
                (double) TimbreUtils.safeAsrInitialValue(timbre.getVolumeAsr()) / 100,
                (double) timbre.getVolume() / 100,
                (double) TimbreUtils.safeAsrFinalValue(timbre.getVolumeAsr()) / 100);
        harmonicsEnvelop.updateValues(
                (double) TimbreUtils.safeAsrInitialValue(timbre.getHarmonicsAsr()) / 100 - (double) timbre.getHarmonics() / 100,
                (double) timbre.getHarmonics() / 100,
                (double) TimbreUtils.safeAsrFinalValue(timbre.getHarmonicsAsr()) / 100 - (double) timbre.getHarmonics() / 100);
    }

    @Override
    public void press(double frequency) {
        // Update pitch envelop sustain to played note
        pitchEnvelop.updateValues(
                NoteUtils.calculateNoteByOffset(frequency, (int) TimbreUtils.safeAsrInitialValue(timbre.getPitchAsr())),
                frequency,
                NoteUtils.calculateNoteByOffset(frequency, (int) TimbreUtils.safeAsrFinalValue(timbre.getPitchAsr()))
        );
        // Enqueue attack and sustain events to envelops
        volumeEnvelop.press();
        pitchEnvelop.press();
        harmonicsEnvelop.press();
    }

    @Override
    public void release() {
        volumeEnvelop.release();
        pitchEnvelop.release();
        harmonicsEnvelop.release();
    }

    @Override
    public void controlVolume(double delta) {
        // TODO
    }

    @Override
    public void controlPitch(double delta) {
        // TODO
    }

    @Override
    public void controlHarmonics(double delta) {
        // TODO
    }

    @Override
    public void controlTremoloDepth(double delta) {
        // TODO
    }

    @Override
    public void controlVibratoDepth(double delta) {
        // TODO
    }
}
