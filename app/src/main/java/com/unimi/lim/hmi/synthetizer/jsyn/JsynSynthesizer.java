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
    private final UnitInputPort volume;
    private final UnitInputPort pitch;
    private final UnitInputPort harmonics;
    private final UnitInputPort volumeController;
    private final UnitInputPort pitchController;
    private final UnitInputPort harmonicsController;
    private final Tremolo tremolo;
    private final Vibrato vibrato;
    private final Asr volumeEnvelop;
    private final Asr pitchEnvelop;
    private final Asr harmonicsEnvelop;

    private JsynSynthesizer(AudioDeviceManager audioDeviceManager, Timbre timbre) {
        this.synth = audioDeviceManager != null ? JSyn.createSynthesizer(audioDeviceManager) : JSyn.createSynthesizer();
        timbre = timbre != null ? timbre : new Timbre();

        // Volume mixers: volume1=volume*tremolo, volume2=envelop*volume1, volume3=controller+volume2
        Multiply volume1;
        Multiply volume2;
        Add volume3;

        // Pitch mixers: pitch1=pitch+vibrato, pitch2=envelop+pitch1, pitch3=controller+pitch2
        Add pitch1;
        Add pitch2;
        Add pitch3;

        // Harmonics mixers: harmonics1=harmonics+envelop, harmonics2=harmonics1+controller
        Add harmonics1;
        Add harmonics2;

        // Add generators to synthesizer
        synth.add(lineOut = new LineOut());
        synth.add(osc = new PulseOscillator());
        synth.add(volume1 = new Multiply());
        synth.add(volume2 = new Multiply());
        synth.add(volume3 = new Add());
        synth.add(pitch1 = new Add());
        synth.add(pitch2 = new Add());
        synth.add(pitch3 = new Add());
        synth.add(harmonics1 = new Add());
        synth.add(harmonics2 = new Add());
        synth.add(tremolo = new Tremolo());
        synth.add(vibrato = new Vibrato());
        synth.add(volumeEnvelop = new Asr(TimbreUtils.safeAsrAttackTime(timbre.getVolumeAsr()), TimbreUtils.safeAsrReleaseTime(timbre.getVolumeAsr())));
        synth.add(pitchEnvelop = new Asr(TimbreUtils.safeAsrAttackTime(timbre.getPitchAsr()), TimbreUtils.safeAsrReleaseTime(timbre.getPitchAsr())));
        synth.add(harmonicsEnvelop = new Asr(TimbreUtils.safeAsrAttackTime(timbre.getHarmonicsAsr()), TimbreUtils.safeAsrReleaseTime(timbre.getHarmonicsAsr())));

        // Controlled values
        volume = volume1.inputA;
        pitch = pitch1.inputA;
        harmonics = harmonics1.inputA;
        volumeController = volume3.inputA;
        pitchController = pitch3.inputA;
        harmonicsController = harmonics2.inputA;

        // Connect modules together
        // Volume mixers: volume1=volume*tremolo, volume2=envelop*volume1, volume3=controller+volume2
        tremolo.output.connect(volume1.inputB);
        volumeEnvelop.output.connect(volume2.inputA);
        volume1.output.connect(volume2.inputB);
        volume2.output.connect(volume3.inputB);
        volume3.output.connect(osc.amplitude);

        // Pitch mixers: pitch1=pitch+vibrato, pitch2=envelop+pitch1, pitch3=controller+pitch2
        vibrato.output.connect(pitch1.inputB);
        pitchEnvelop.output.connect(pitch2.inputA);
        pitch1.output.connect(pitch2.inputB);
        pitch2.output.connect(pitch3.inputB);
        pitch3.output.connect(osc.frequency);

        // Harmonics mixers: harmonics1=harmonics+envelop, harmonics2=harmonics1+controller
        harmonicsEnvelop.output.connect(harmonics1.inputB);
        harmonics1.output.connect(harmonics2.inputB);
        harmonics2.output.connect(osc.width);

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
        volume.set((double) timbre.getVolume() / 100);
        harmonics.set((double) timbre.getHarmonics() / 100);
        tremolo.setFrequency(TimbreUtils.safeLfoRate(timbre.getTremolo()));
        tremolo.setDepth(TimbreUtils.safeLfoDepth(timbre.getTremolo()));
        vibrato.setFrequency(TimbreUtils.safeLfoRate(timbre.getVibrato()));
        vibrato.setDepth(TimbreUtils.safeLfoDepth(timbre.getVibrato()));
        volumeEnvelop.updateValues((double) TimbreUtils.safeAsrInitialValue(timbre.getVolumeAsr()) / 100, 1, (double) TimbreUtils.safeAsrFinalValue(timbre.getVolumeAsr()) / 100);
        pitchEnvelop.updateValues(TimbreUtils.safeAsrInitialValue(timbre.getPitchAsr()), 0, TimbreUtils.safeAsrFinalValue(timbre.getPitchAsr()));
        harmonicsEnvelop.updateValues((double) TimbreUtils.safeAsrInitialValue(timbre.getHarmonicsAsr()) / 100, 0, (double) TimbreUtils.safeAsrFinalValue(timbre.getHarmonicsAsr()) / 100);
    }

    @Override
    public void press(double frequency) {
        pitch.set(frequency);
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
