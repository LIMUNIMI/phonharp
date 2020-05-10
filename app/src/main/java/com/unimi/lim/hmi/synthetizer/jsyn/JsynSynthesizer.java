package com.unimi.lim.hmi.synthetizer.jsyn;

import android.util.Log;

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

    /**
     * Builder with available configurations to create a new synth instance
     */
    public static class Builder {

        private Timbre timbre;
        private AudioDeviceManager audioDeviceManager;

        /**
         * Setup specified timbre configuration, if not used then default timbre wille be used
         *
         * @param timbre
         * @return Builder instance
         */
        public Builder timbreCfg(Timbre timbre) {
            this.timbre = timbre;
            return this;
        }

        /**
         * Setup android audio device manager, if not used then default audio device will be used
         *
         * @return Builder instance
         */
        public Builder androidAudioDeviceManager() {
            this.audioDeviceManager = new JSynAndroidAudioDevice();
            return this;
        }

        /**
         * Setup specified audio device manager, if not used then default audio device will be used
         *
         * @return Builder instance
         */
        public Builder audioDeviceManager(AudioDeviceManager audioDeviceManager) {
            this.audioDeviceManager = audioDeviceManager;
            return this;
        }

        /**
         * Return synth instance
         *
         * @return synth instance
         */
        public JsynSynthesizer build() {
            return new JsynSynthesizer(audioDeviceManager, timbre);
        }
    }

    // Jsyn modules and synth properties
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

    // Timbre configuration
    private Timbre timbre;

    // From timbre config, values are converted from stored values (timbre instance) to jsyn values
    private double volume;
    private double harmonics;
    private int pitchAsrInitialSemitoneOffset;
    private int pitchAsrFinalSemitoneOffset;
    private int tremoloDepth;
    private int vibratoDepth;

    // Played note
    private double noteFrequency;
    // True if keys are pressed
    private boolean pressing = false;

    private JsynSynthesizer(AudioDeviceManager audioDeviceManager, Timbre timbre) {
        this.synth = audioDeviceManager != null ? JSyn.createSynthesizer(audioDeviceManager) : JSyn.createSynthesizer();
        timbre = timbre != null ? timbre : new Timbre();

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
        synth.add(volumeEnvelop = new Asr());
        synth.add(pitchEnvelop = new Asr());
        synth.add(harmonicsEnvelop = new Asr());

        // Controlled values
        volumeController = volMix2.inputA;
        pitchController = pitchMix2.inputA;
        harmonicsController = harmMix.inputB;
        // Default value to 1 because is part of multiply operation
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

        updateSynthesizerCfg(timbre);
    }

    /**
     * Startup synth and synth modules
     */
    @Override
    public void start() {
        synth.start();
        lineOut.start();
        volumeEnvelop.start();
        pitchEnvelop.start();
        harmonicsEnvelop.start();
    }

    /**
     * Stop synth and synth modules
     */
    @Override
    public void stop() {
        volumeEnvelop.stop();
        pitchEnvelop.stop();
        harmonicsEnvelop.stop();
        lineOut.stop();
        synth.stop();
    }


    /**
     * Update timbre configuration
     *
     * @param timbre
     */
    @Override
    public void updateSynthesizerCfg(Timbre timbre) {
        this.timbre = timbre;
        // Note that values are divided by 100 because ui and stored ranges are 0-100 but jsyn range is 0-1
        volume = timbre.getVolume() / 100f;
        // 1 minus because stored value goes from 0 (all harmonics) to 100 (odd harmonics) but jsyn values goes from 0 (odd harmonics) to 1 (all harmonics)
        harmonics = 1f - timbre.getHarmonics() / 100f;
        tremoloDepth = TimbreUtils.safeLfoDepth(timbre.getTremolo());
        vibratoDepth = TimbreUtils.safeLfoDepth(timbre.getVibrato());

        tremolo.setFrequency(TimbreUtils.safeLfoRate(timbre.getTremolo()));
        tremolo.setDepth(tremoloDepth);
        vibrato.setFrequency(TimbreUtils.safeLfoRate(timbre.getVibrato()));
        vibrato.setDepth(vibratoDepth);
        // Note that values are divided by 100 because ui and stored ranges are 0-100 but jsyn range is 0-1
        volumeEnvelop.update(
                TimbreUtils.safeAsrInitialValue(timbre.getVolumeAsr()) / 100f,
                TimbreUtils.safeAsrAttackTime(timbre.getVolumeAsr()),
                volume,
                TimbreUtils.safeAsrReleaseTime(timbre.getVolumeAsr()),
                TimbreUtils.safeAsrFinalValue(timbre.getVolumeAsr()) / 100f);
        // Note that pitch envelop values depend on played note and are set on press method
        pitchAsrInitialSemitoneOffset = (int) TimbreUtils.safeAsrInitialValue(timbre.getPitchAsr());
        pitchAsrFinalSemitoneOffset = (int) TimbreUtils.safeAsrFinalValue(timbre.getPitchAsr());
        pitchEnvelop.update(
                0,
                TimbreUtils.safeAsrAttackTime(timbre.getPitchAsr()),
                0,
                TimbreUtils.safeAsrReleaseTime(timbre.getPitchAsr()),
                0);
        // Note that values are divided by 100 because ui and stored ranges are 0-100 but jsyn range is 0-1
        // If harmonicsEnvelopAsr is not configured (is null) then initial and final values are set to harmonics
        // 1 minus because stored value goes from 0 (all harmonics) to 100 (odd harmonics)
        harmonicsEnvelop.update(
                (1f - (timbre.getHarmonicsAsr() != null ? timbre.getHarmonicsAsr().getInitialValue() : timbre.getHarmonics()) / 100f),
                TimbreUtils.safeAsrAttackTime(timbre.getHarmonicsAsr()),
                harmonics,
                TimbreUtils.safeAsrReleaseTime(timbre.getHarmonicsAsr()),
                (1f - (timbre.getHarmonicsAsr() != null ? timbre.getHarmonicsAsr().getFinalValue() : timbre.getHarmonics()) / 100f));
    }

    /**
     * Configuration Id
     *
     * @return configuration id
     */
    @Override
    public String getTimbreId() {
        return timbre.getId();
    }

    /**
     * Play specified note frequency
     *
     * @param notefrequency
     */
    @Override
    public void press(double notefrequency) {
        this.noteFrequency = notefrequency;

        // Adjust vibrato depending on note frequency
        vibrato.update(notefrequency);

        // Update pitch envelop sustain to played note
        pitchEnvelop.updateValues(
                NoteUtils.calculateNoteByOffset(notefrequency, pitchAsrInitialSemitoneOffset),
                notefrequency,
                NoteUtils.calculateNoteByOffset(notefrequency, pitchAsrFinalSemitoneOffset)
        );

        if (!pressing) {
            // Enqueue attack and sustain events to envelops
            volumeEnvelop.press();
            pitchEnvelop.press();
            harmonicsEnvelop.press();
        } else {
            volumeEnvelop.sustain();
            pitchEnvelop.sustain();
            harmonicsEnvelop.sustain();
        }
        pressing = true;
    }

    /**
     * Release playing sound
     */
    @Override
    public void release() {
        volumeEnvelop.release();
        pitchEnvelop.release();
        harmonicsEnvelop.release();
        pressing = false;
    }

    /**
     * Reset dynamic controller state
     */
    @Override
    public void controlReset() {
        volumeController.set(1);
        pitchController.set(0);
        harmonicsController.set(0);
        tremolo.setDepth(tremoloDepth);
        vibrato.setDepth(vibratoDepth);
    }

    /**
     * Control volume, specified delta value is added to current volume value (volume range is 0-1)
     *
     * @param delta volume delta
     */
    @Override
    public void controlVolume(float delta) {
        double value = volumeController.get() + delta;
        if (volume * value >= 1) {
            // Lock maximum value to 1
            value = 1 / volume;
        } else if (value <= 0) {
            // Lock minimum value to 0
            value = 0;
        }
        Log.d(getClass().getName(), "Volume controller value " + value);
        volumeController.set(value);
    }

    /**
     * Control pitch, specified delta is added to current pitch
     *
     * @param delta in semitones
     */
    @Override
    public void controlPitch(float delta) {
        double pitchDelta = NoteUtils.calculateNoteByOffset(noteFrequency, delta) - noteFrequency;
        double value = pitchController.get() + pitchDelta;
        Log.d(getClass().getName(), "Pitch controller value " + value);
        pitchController.set(value);
    }

    /**
     * Control harmonics, specified delta is added to current harmonics value (harmonics range is -1/1)
     *
     * @param delta harmonics delta
     */
    @Override
    public void controlHarmonics(float delta) {
        double value = harmonicsController.getValue() + delta;
        Log.d(getClass().getName(), "Harmonics controller value " + value);
        harmonicsController.set(value);
    }

    /**
     * Control tremolo depth, specified delta is added to current depth (depth range is 0-100)
     *
     * @param delta tremolo depth delta
     */
    @Override
    public void controlTremoloDepth(float delta) {
        double value = tremolo.getDepth() + delta;
        if (value > 100 || value < 0) {
            return;
        }
        Log.d(getClass().getName(), "Tremolo controller value " + value);
        tremolo.setDepth((int) value);
    }

    /**
     * Control vibrato depth, specified delta is added to current depth (depth range is 0-100)
     *
     * @param delta vibrato depth delta
     */
    @Override
    public void controlVibratoDepth(float delta) {
        double value = vibrato.getDepth() + delta;
        if (value > 100 || value < 0) {
            return;
        }
        Log.d(getClass().getName(), "Vibrato controller value " + value);
        vibrato.setDepth((int) value);
        vibrato.update(noteFrequency);
    }
}
