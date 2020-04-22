package com.unimi.lim.hmi.synthetizer.jsyn;

import com.jsyn.JSyn;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.PulseOscillator;
import com.unimi.lim.hmi.entity.TimbreCfg;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.jsyn.device.JSynAndroidAudioDevice;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Asr;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Tremolo;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Vibrato;

public class JsynSynthesizer implements Synthesizer {

    public static class JsynSynthesizerBuilder {

        private TimbreCfg timbre;
        private AudioDeviceManager audioDeviceManager;

        public JsynSynthesizerBuilder timbreCfg(TimbreCfg timbre) {
            this.timbre = timbre;
            return this;
        }

        public JsynSynthesizerBuilder defaultAudioDeviceManager() {
            this.audioDeviceManager = new JSynAndroidAudioDevice();
            return this;
        }

        public JsynSynthesizerBuilder audioDeviceManager(AudioDeviceManager audioDeviceManager) {
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

    private JsynSynthesizer(AudioDeviceManager audioDeviceManager, TimbreCfg timbreCfg) {
        this.synth = audioDeviceManager != null ? JSyn.createSynthesizer(audioDeviceManager) : JSyn.createSynthesizer();
        timbreCfg = timbreCfg != null ? timbreCfg : new TimbreCfg();

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
        synth.add(volumeEnvelop = new Asr(timbreCfg.getVolumeEnv().getAttackTime(), timbreCfg.getVolumeEnv().getReleaseTime()));
        synth.add(pitchEnvelop = new Asr(timbreCfg.getPitchEnv().getAttackTime(), timbreCfg.getPitchEnv().getReleaseTime()));
        synth.add(harmonicsEnvelop = new Asr(timbreCfg.getHarmonicsEnv().getAttackTime(), timbreCfg.getHarmonicsEnv().getReleaseTime()));

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
        pitch3.output.connect(osc.amplitude);

        // Harmonics mixers: harmonics1=harmonics+envelop, harmonics2=harmonics1+controller
        harmonicsEnvelop.output.connect(harmonics1.inputB);
        harmonics1.output.connect(harmonics2.inputB);
        harmonics2.output.connect(osc.width);

        // Line out
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        updateTimbreCfg(timbreCfg);
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
    public void updateTimbreCfg(TimbreCfg timbreCfg) {
        volume.set(timbreCfg.getVolume());
        harmonics.set(timbreCfg.getHarmonics());
        tremolo.setFrequency(timbreCfg.getTremolo().getRate());
        tremolo.setDepth(timbreCfg.getTremolo().getDepth());
        vibrato.setFrequency(timbreCfg.getVibrato().getRate());
        vibrato.setDepth(timbreCfg.getVibrato().getDepth());
        volumeEnvelop.updateValues(timbreCfg.getVolumeEnv().getInitialValue(), 1, timbreCfg.getVolumeEnv().getFinalValue());
        pitchEnvelop.updateValues(timbreCfg.getPitchEnv().getInitialValue(), 0, timbreCfg.getPitchEnv().getFinalValue());
        harmonicsEnvelop.updateValues(timbreCfg.getHarmonicsEnv().getInitialValue(), 0, timbreCfg.getHarmonicsEnv().getFinalValue());
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

    }

    @Override
    public void controlPitch(double delta) {

    }

    @Override
    public void controlHarmonics(double delta) {

    }

    @Override
    public void controlTremoloDepth(double delta) {

    }

    @Override
    public void controlVibratoDepth(double delta) {

    }
}
