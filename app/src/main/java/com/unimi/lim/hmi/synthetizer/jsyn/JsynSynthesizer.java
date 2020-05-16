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
import com.unimi.lim.hmi.synthetizer.jsyn.module.Clipper;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Equalizer;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Pwm;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Tremolo;
import com.unimi.lim.hmi.synthetizer.jsyn.module.Vibrato;
import com.unimi.lim.hmi.util.ConversionUtils;
import com.unimi.lim.hmi.util.NoteUtils;
import com.unimi.lim.hmi.util.TimbreUtils;

import static com.unimi.lim.hmi.util.ConversionUtils.percentageToDecimal;

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

    private final static double MAX_HARMONICS = 0.95;

    // Jsyn modules and synth properties
    private final com.jsyn.Synthesizer synth;
    private final LineOut lineOut;
    private final PulseOscillator osc;
    private final UnitInputPort volumeController;
    private final UnitInputPort pitchController;
    private final UnitInputPort harmonicsController;
    private final Tremolo tremolo;
    private final Vibrato vibrato;
    private final Pwm pwm;
    private final Equalizer equalizer;
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
    private int pwmDepth;

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

        // Harmonics mixers: harmMix1=envelop+pwm, harmMix2=controller+harmMix1, harmClip=minimum(harmMix2, 0.95)
        Add harmMix1;
        Add harmMix2;
        Clipper harmClip;

        // Add generators to synthesizer
        synth.add(lineOut = new LineOut());
        synth.add(osc = new PulseOscillator());
        synth.add(volMix1 = new Multiply());
        synth.add(volMix2 = new Multiply());
        synth.add(pitchMix1 = new Add());
        synth.add(pitchMix2 = new Add());
        synth.add(harmMix1 = new Add());
        synth.add(harmMix2 = new Add());
        synth.add(harmClip = new Clipper(-MAX_HARMONICS, MAX_HARMONICS));
        synth.add(equalizer = new Equalizer());
        synth.add(tremolo = new Tremolo());
        synth.add(vibrato = new Vibrato());
        synth.add(pwm = new Pwm());
        synth.add(volumeEnvelop = new Asr());
        synth.add(pitchEnvelop = new Asr());
        synth.add(harmonicsEnvelop = new Asr());

        // Controlled values
        volumeController = volMix2.inputA;
        pitchController = pitchMix2.inputA;
        harmonicsController = harmMix2.inputA;
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

        // Harmonics mixers: harmMix1=envelop+pwm, harmMix2=controller+harmMix1, harmClip=minimum(harmMix2, 0.95)
        harmonicsEnvelop.output.connect(harmMix1.inputA);
        pwm.output.connect(harmMix1.inputB);
        harmMix1.output.connect(harmMix2.inputB);
        harmMix2.output.connect(harmClip.input);
        harmClip.output.connect(osc.width);

        // Equalizer
        osc.output.connect(equalizer.input);

        // Line out
        equalizer.output.connect(0, lineOut.input, 0);
        equalizer.output.connect(0, lineOut.input, 1);

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
        volume = percentageToDecimal(timbre.getVolume());
        // 1 minus because stored value goes from 0 (all harmonics) to 100 (odd harmonics) but jsyn values goes from 0 (odd harmonics) to 1 (all harmonics).
        harmonics = 1f - percentageToDecimal(timbre.getHarmonics());

        // Equalizer gain, note that timbre dB values are converted to absolute value
        equalizer.setLowShelfGain(ConversionUtils.dBtoAbsoluteValue(TimbreUtils.safeEqLowShelfGain(timbre.getEqualizer())));
        equalizer.setHighShelfGain(ConversionUtils.dBtoAbsoluteValue(TimbreUtils.safeEqHighShelfGain(timbre.getEqualizer())));

        // LFO
        tremoloDepth = TimbreUtils.safeLfoDepth(timbre.getTremolo());
        vibratoDepth = TimbreUtils.safeLfoDepth(timbre.getVibrato());
        pwmDepth = TimbreUtils.safeLfoDepth(timbre.getPwm());
        tremolo.setFrequency(TimbreUtils.safeLfoRate(timbre.getTremolo()));
        tremolo.setDepth(tremoloDepth);
        vibrato.setFrequency(TimbreUtils.safeLfoRate(timbre.getVibrato()));
        vibrato.setDepth(vibratoDepth);
        pwm.setFrequency(TimbreUtils.safeLfoRate(timbre.getPwm()));
        pwm.setDepth(pwmDepth);

        // ASR
        // Note that values are divided by 100 because ui and stored ranges are 0-100 but jsyn range is 0-1
        volumeEnvelop.update(
                percentageToDecimal((int) TimbreUtils.safeAsrInitialValue(timbre.getVolumeAsr())),
                TimbreUtils.safeAsrAttackTime(timbre.getVolumeAsr()),
                volume,
                TimbreUtils.safeAsrReleaseTime(timbre.getVolumeAsr()),
                percentageToDecimal((int) TimbreUtils.safeAsrFinalValue(timbre.getVolumeAsr())));
        // Note that pitch envelop values depend on played note and are set on press method
        pitchAsrInitialSemitoneOffset = (int) TimbreUtils.safeAsrInitialValue(timbre.getPitchAsr());
        pitchAsrFinalSemitoneOffset = (int) TimbreUtils.safeAsrFinalValue(timbre.getPitchAsr());
        pitchEnvelop.update(
                0,
                TimbreUtils.safeAsrAttackTime(timbre.getPitchAsr()),
                0,
                TimbreUtils.safeAsrReleaseTime(timbre.getPitchAsr()),
                0,
                timbre.getPortamento());
        // Note that values are divided by 100 because ui and stored ranges are 0-100 but jsyn range is 0-1
        // If harmonicsEnvelopAsr is not configured (is null) then initial and final values are set to harmonics
        // 1 minus because stored value goes from 0 (all harmonics) to 100 (odd harmonics)
        harmonicsEnvelop.update(
                (1f - percentageToDecimal((int) TimbreUtils.safeAsrInitialValue(timbre.getHarmonicsAsr(), timbre.getHarmonics()))),
                TimbreUtils.safeAsrAttackTime(timbre.getHarmonicsAsr()),
                harmonics,
                TimbreUtils.safeAsrReleaseTime(timbre.getHarmonicsAsr()),
                (1f - percentageToDecimal((int) TimbreUtils.safeAsrFinalValue(timbre.getHarmonicsAsr(), timbre.getHarmonics()))));
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
            // Enqueue attack events to envelops
            volumeEnvelop.press();
            pitchEnvelop.press();
            harmonicsEnvelop.press();
        } else {
            // In case of legato hols sustain. Note that pitch envelop may reaches new sustain value after portamento delay
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
        pwm.setDepth(pwmDepth);
    }

    /**
     * Control volume, specified delta value is added to current volume value (volume range is 0-1)
     *
     * @param delta volume delta, percentage
     */
    @Override
    public void controlVolume(float delta) {
        double value = volumeController.get() + percentageToDecimal(delta);
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
     * @param delta harmonics delta, percentage
     */
    @Override
    public void controlHarmonics(float delta) {
        double value = harmonicsController.getValue() + percentageToDecimal(delta);
        Log.d(getClass().getName(), "Harmonics controller value " + value);
        harmonicsController.set(value);
    }

    /**
     * Control tremolo depth, specified delta is added to current depth (depth range is 0-100)
     *
     * @param delta tremolo depth delta, percentage
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
     * @param delta vibrato depth delta, percentage
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

    /**
     * Control PWM depth, specified delta is added to current depth (depth range is 0-100)
     *
     * @param delta vibrato depth delta, percentage
     */
    @Override
    public void controlPwmDepth(float delta) {
        double value = pwm.getDepth() + delta;
        if (value > 100 || value < 0) {
            return;
        }
        Log.d(getClass().getName(), "PWM controller value " + value);
        pwm.setDepth((int) value);
    }
}
