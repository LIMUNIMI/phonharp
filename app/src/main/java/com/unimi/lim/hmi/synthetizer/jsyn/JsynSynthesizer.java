package com.unimi.lim.hmi.synthetizer.jsyn;

import com.jsyn.JSyn;
import com.jsyn.devices.AudioDeviceManager;
import com.unimi.lim.hmi.entity.TimbreCfg;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.jsyn.device.JSynAndroidAudioDevice;

public class JsynSynthesizer implements Synthesizer {

    public static class JsynSynthesizerBuilder {

        private TimbreCfg timbre;
        private AudioDeviceManager audioDeviceManager;

        public JsynSynthesizerBuilder timbre(TimbreCfg timbre) {
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
    private final TimbreCfg timbreCfg;

    private JsynSynthesizer(AudioDeviceManager audioDeviceManager, TimbreCfg timbreCfg) {
        this.synth = audioDeviceManager != null ? JSyn.createSynthesizer(audioDeviceManager) : JSyn.createSynthesizer();
        this.timbreCfg = timbreCfg;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void press(double frequency) {

    }

    @Override
    public void release() {

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
