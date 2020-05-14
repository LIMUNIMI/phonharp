package com.unimi.lim.hmi.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.google.common.collect.ImmutableBiMap;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import java.util.function.Function;

import static com.unimi.lim.hmi.entity.Timbre.DEFAULT_PORTAMENTO_TIME;
import static com.unimi.lim.hmi.entity.Timbre.DEFAULT_TAP_HYSTERESIS;
import static com.unimi.lim.hmi.ui.common.SetupUtils.setupSeek;
import static com.unimi.lim.hmi.ui.common.SetupUtils.setupSpinner;
import static com.unimi.lim.hmi.ui.common.SetupUtils.setupSwitch;

public class TimbreDetailFragment extends Fragment {

    private final static float SEEK_TO_MODEL_EXP = 3f / 2f;
    private final static float MODEL_TO_SEEK_EXP = 2f / 3f;

    // Utility functions to handle descriptions
    private final Function<Integer, String> GAIN_DESCRIPTION = val -> String.format("%d%s", eqSeekToModel(val), getResources().getString(R.string.decibel));
    private final Function<Integer, String> HYSTERESOS_DESCRIPTION = val -> String.format("%d%s", val, getResources().getString(R.string.milliseconds));
    private final Function<Integer, String> PORTAMENTO_DESCRIPTION = val -> String.format("%.0f%s", portamentoSeekToModel(val) * 1000, getResources().getString(R.string.milliseconds));
    private final Function<Integer, String> ASR_TIME_DESCRIPTION = val -> String.format("%.2f%s", asrTimeSeekToModel(val), getResources().getString(R.string.seconds));
    private final Function<Integer, String> PERCENTAGE_DESCRIPTION = val -> String.format("%d%s", val, getResources().getString(R.string.percentage));
    private final Function<Integer, String> SEMITONE_DESCRIPTION = val -> String.format("%.0f %s", asrPitchSeekToModel(val), getResources().getString(R.string.semitone));
    private final Function<Integer, String> HERTZ_DESCRIPTION = val -> String.format("%.1f%s", seekToModel(val), getResources().getString(R.string.hertz));
    private final Function<Integer, String> HARMONICS_DESCRIPTION = val ->
            val == 100 ? String.format("%s", getResources().getString(R.string.harmonics_odd)) :
                    (val == 0 ? String.format("%s", getResources().getString(R.string.harmonics_all)) :
                            String.format("%s: %d%s ", getResources().getString(R.string.harmonics_all_to_odd), val / 2, getResources().getString(R.string.percentage)));
    // Mapping between controller enum and controller spinner IDs
    private final ImmutableBiMap<Integer, Timbre.Controller> CONTROLLER_BIMAP = new ImmutableBiMap.Builder<Integer, Timbre.Controller>()
            .put(0, Timbre.Controller.NONE)
            .put(1, Timbre.Controller.VOLUME)
            .put(2, Timbre.Controller.HARMONICS)
            .put(3, Timbre.Controller.PITCH)
            .put(4, Timbre.Controller.TREMOLO)
            .put(5, Timbre.Controller.VIBRATO)
            .put(6, Timbre.Controller.PWM)
            .build();

    public static Fragment newInstance() {
        return new TimbreDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timbre_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve current timbre; use getValue instead of observe because timbre will be modified by the user but we dont want to setup view element each time timbre is modified
        TimbreViewModel viewModel = ViewModelProviders.of(getActivity()).get(TimbreViewModel.class);
        Timbre timbre = viewModel.getWorking().getValue();
        Log.d(getClass().getName(), "Editing timbre: " + timbre.toString());

        // Timbre name
        TextInputEditText timbreName = view.findViewById(R.id.timbre_name);
        timbreName.setText(timbre.getName());
        timbreName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                timbre.setName(s.toString());
            }
        });

        // Notify TimbreDetailActivity that timbre is changed: sample synth is updated with new timbre configuration (see TimbreDetailActivity)
        Runnable onChange = () -> viewModel.workingChanged(timbre);

        // Volume and Harmonics seek bar
        setupSeek(view, R.id.volume_seek, R.id.volume_text, 1, PERCENTAGE_DESCRIPTION, timbre.getVolume(), val -> timbre.setVolume(val), onChange);
        setupSeek(view, R.id.harmonics_seek, R.id.harmonics_text, 1, HARMONICS_DESCRIPTION, timbre.getHarmonics(), val -> timbre.setHarmonics(val), onChange);

        // Tremolo Switch
        setupSwitch(view, R.id.tremolo_enabled, R.id.tremolo_container,
                timbre.getTremolo() != null,
                () -> {
                    // Setup Tremolo Seek bars
                    Timbre.Lfo lfo = timbre.getTremolo() != null ? timbre.getTremolo() : new Timbre.Lfo();
                    timbre.setTremolo(lfo);
                    setupSeek(view, R.id.tremolo_rate_seek, R.id.tremolo_rate_text, 100, HERTZ_DESCRIPTION, modelToSeek(timbre.getTremolo().getRate()), val -> timbre.getTremolo().setRate(seekToModel(val)), onChange);
                    setupSeek(view, R.id.tremolo_depth_seek, R.id.tremolo_depth_text, 1, PERCENTAGE_DESCRIPTION, timbre.getTremolo().getDepth(), val -> timbre.getTremolo().setDepth(val), onChange);
                },
                () -> timbre.setTremolo(null), onChange);

        // Vibrato Switch
        setupSwitch(view, R.id.vibrato_enabled, R.id.vibrato_container, timbre.getVibrato() != null, () -> {
                    // Setup Vibrato Seek bars
                    Timbre.Lfo lfo = timbre.getVibrato() != null ? timbre.getVibrato() : new Timbre.Lfo();
                    timbre.setVibrato(lfo);
                    setupSeek(view, R.id.vibrato_rate_seek, R.id.vibrato_rate_text, 100, HERTZ_DESCRIPTION, modelToSeek(timbre.getVibrato().getRate()), val -> timbre.getVibrato().setRate(seekToModel(val)), onChange);
                    setupSeek(view, R.id.vibrato_depth_seek, R.id.vibrato_depth_text, 1, PERCENTAGE_DESCRIPTION, timbre.getVibrato().getDepth(), val -> timbre.getVibrato().setDepth(val), onChange);
                },
                () -> timbre.setVibrato(null), onChange);

        // PWM Switch
        setupSwitch(view, R.id.pwm_enabled, R.id.pwm_container, timbre.getPwm() != null, () -> {
                    // Setup Pwm Seek bars
                    Timbre.Lfo lfo = timbre.getPwm() != null ? timbre.getPwm() : new Timbre.Lfo();
                    timbre.setPwm(lfo);
                    setupSeek(view, R.id.pwm_rate_seek, R.id.pwm_rate_text, 100, HERTZ_DESCRIPTION, modelToSeek(timbre.getPwm().getRate()), val -> timbre.getPwm().setRate(seekToModel(val)), onChange);
                    setupSeek(view, R.id.pwm_depth_seek, R.id.pwm_depth_text, 1, PERCENTAGE_DESCRIPTION, timbre.getPwm().getDepth(), val -> timbre.getPwm().setDepth(val), onChange);
                },
                () -> timbre.setPwm(null), onChange);

        // Volume ASR Switch
        setupSwitch(view, R.id.volume_asr_enabled, R.id.volume_asr_container, timbre.getVolumeAsr() != null, () -> {
                    // Setup Volume ASR Seek bars
                    Timbre.Asr asr = timbre.getVolumeAsr() != null ? timbre.getVolumeAsr() : new Timbre.Asr();
                    timbre.setVolumeAsr(asr);
                    setupSeek(view, R.id.volume_asr_attack_seek, R.id.volume_asr_attack_text, 1, ASR_TIME_DESCRIPTION, asrTimeModelToSeek(timbre.getVolumeAsr().getAttackTime()), val -> timbre.getVolumeAsr().setAttackTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.volume_asr_release_seek, R.id.volume_asr_release_text, 1, ASR_TIME_DESCRIPTION, asrTimeModelToSeek(timbre.getVolumeAsr().getReleaseTime()), val -> timbre.getVolumeAsr().setReleaseTime(asrTimeSeekToModel(val)), onChange);
                },
                () -> timbre.setVolumeAsr(null), onChange);

        // Pitch ASR Switch
        setupSwitch(view, R.id.pitch_asr_enabled, R.id.pitch_asr_container, timbre.getPitchAsr() != null, () -> {
                    // Setup Pitch ASR Seek bars
                    Timbre.Asr asr = timbre.getPitchAsr() != null ? timbre.getPitchAsr() : new Timbre.Asr();
                    timbre.setPitchAsr(asr);
                    setupSeek(view, R.id.pitch_asr_attack_seek, R.id.pitch_asr_attack_text, 1, ASR_TIME_DESCRIPTION, asrTimeModelToSeek(timbre.getPitchAsr().getAttackTime()), val -> timbre.getPitchAsr().setAttackTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_release_seek, R.id.pitch_asr_release_text, 1, ASR_TIME_DESCRIPTION, asrTimeModelToSeek(timbre.getPitchAsr().getReleaseTime()), val -> timbre.getPitchAsr().setReleaseTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_init_seek, R.id.pitch_asr_init_text, 1, SEMITONE_DESCRIPTION, asrPitchModelToSeek(timbre.getPitchAsr().getInitialValue()), val -> timbre.getPitchAsr().setInitialValue(asrPitchSeekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_final_seek, R.id.pitch_asr_final_text, 1, SEMITONE_DESCRIPTION, asrPitchModelToSeek(timbre.getPitchAsr().getFinalValue()), val -> timbre.getPitchAsr().setFinalValue(asrPitchSeekToModel(val)), onChange);
                },
                () -> timbre.setPitchAsr(null), onChange);

        // Harmonics ASR Switch
        setupSwitch(view, R.id.harmonics_asr_enabled, R.id.harmonics_asr_container, timbre.getHarmonicsAsr() != null, () -> {
                    // Setup Harmonics ASR Seek bars
                    Timbre.Asr asr = timbre.getHarmonicsAsr() != null ? timbre.getHarmonicsAsr() : new Timbre.Asr();
                    timbre.setHarmonicsAsr(asr);
                    setupSeek(view, R.id.harmonics_asr_attack_seek, R.id.harmonics_asr_attack_text, 1, ASR_TIME_DESCRIPTION, asrTimeModelToSeek(timbre.getHarmonicsAsr().getAttackTime()), val -> timbre.getHarmonicsAsr().setAttackTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.harmonics_asr_release_seek, R.id.harmonics_asr_release_text, 1, ASR_TIME_DESCRIPTION, asrTimeModelToSeek(timbre.getHarmonicsAsr().getReleaseTime()), val -> timbre.getHarmonicsAsr().setReleaseTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.harmonics_asr_init_seek, R.id.harmonics_asr_init_text, 1, HARMONICS_DESCRIPTION, (int) timbre.getHarmonicsAsr().getInitialValue(), val -> timbre.getHarmonicsAsr().setInitialValue(val), onChange);
                    setupSeek(view, R.id.harmonics_asr_final_seek, R.id.harmonics_asr_final_text, 1, HARMONICS_DESCRIPTION, (int) timbre.getHarmonicsAsr().getFinalValue(), val -> timbre.getHarmonicsAsr().setFinalValue(val), onChange);
                },
                () -> timbre.setHarmonicsAsr(null), onChange);

        // Swipe controller Switch
        setupSwitch(view, R.id.swipe_control_enabled, R.id.swipe_control_container, timbre.getController1() != null || timbre.getController2() != null, () -> {
                    // Setup Swipe controller Spinner
                    setupSpinner(view, R.id.swipe_horiz_spinner, timbre.getController1() != null ? timbre.getController1() : Timbre.Controller.NONE, CONTROLLER_BIMAP, pos -> timbre.setController1(CONTROLLER_BIMAP.get(pos)), onChange);
                    setupSpinner(view, R.id.swipe_vert_spinner, timbre.getController2() != null ? timbre.getController2() : Timbre.Controller.NONE, CONTROLLER_BIMAP, pos -> timbre.setController2(CONTROLLER_BIMAP.get(pos)), onChange);
                },
                () -> {
                    timbre.setController1(null);
                    timbre.setController2(null);
                }, onChange);

        // Hysteresis Switch
        setupSwitch(view, R.id.tap_hysteresis_enabled, R.id.tap_hysteresis_container, timbre.getTapHysteresis() != 0, () -> {
                    // Setup Hysteresis Seek bar
                    float tapHysteresis = timbre.getTapHysteresis() != 0 ? timbre.getTapHysteresis() : DEFAULT_TAP_HYSTERESIS;
                    timbre.setTapHysteresis(tapHysteresis);
                    setupSeek(view, R.id.tap_hysteresis_seek, R.id.tap_hysteresis_text, 1, HYSTERESOS_DESCRIPTION, modelToSeek(timbre.getTapHysteresis()), val -> timbre.setTapHysteresis(seekToModel(val)), onChange);
                },
                () -> timbre.setTapHysteresis(0f), onChange);

        // Portamento Switch
        setupSwitch(view, R.id.portamento_enabled, R.id.portamento_container, timbre.getPortamento() != 0, () -> {
                    // Setup Hysteresis Seek bar
                    float portamento = timbre.getPortamento() != 0 ? timbre.getPortamento() : DEFAULT_PORTAMENTO_TIME;
                    timbre.setPortamento(portamento);
                    setupSeek(view, R.id.portamento_seek, R.id.portamento_text, 1, PORTAMENTO_DESCRIPTION, portamentoModelToSeek(timbre.getPortamento()), val -> timbre.setPortamento(portamentoSeekToModel(val)), onChange);
                },
                () -> timbre.setPortamento(0f), onChange);

        // Equalizer Switch
        setupSwitch(view, R.id.equalizer_enabled, R.id.equalizer_container,
                timbre.getEqualizer() != null,
                () -> {
                    // Setup Equalizer Seek bars
                    Timbre.Equalizer eq = timbre.getEqualizer() != null ? timbre.getEqualizer() : new Timbre.Equalizer();
                    timbre.setEqualizer(eq);
                    setupSeek(view, R.id.equalizer_low_shelf_seek, R.id.equalizer_low_shelf_text, 1, GAIN_DESCRIPTION, eqModelToSeek(timbre.getEqualizer().getLowShelfGain()), val -> timbre.getEqualizer().setLowShelfGain(eqSeekToModel(val)), onChange);
                    setupSeek(view, R.id.equalizer_high_shelf_seek, R.id.equalizer_high_shelf_text, 1, GAIN_DESCRIPTION, eqModelToSeek(timbre.getEqualizer().getHighShelfGain()), val -> timbre.getEqualizer().setHighShelfGain(eqSeekToModel(val)), onChange);
                },
                () -> timbre.setEqualizer(null), onChange);
    }


    /**
     * Convert seek value to model value
     *
     * @param value seek value
     * @return model value
     */
    private float seekToModel(int value) {
        return (float) value / 1000;
    }

    /**
     * Convert model value to seek value
     *
     * @param value model value
     * @return seek value
     */
    private int modelToSeek(float value) {
        return (int) (value * 1000);
    }

    /**
     * Convert asr time from seek to model: m = (s/1000)^(3/2) * 5, where 10000 is seek bar max value and 5 is time max value in seconds (5 seconds)
     *
     * @param value seek value
     * @return model value
     */
    private float asrTimeSeekToModel(int value) {
        return (float) Math.pow((double) value / 10000, SEEK_TO_MODEL_EXP) * 5;
    }

    /**
     * Convert asr time value from model to seek. s = (m / 5)^(2/3) * 1000, where 1000 is seek bar max value and 5 is time max value in seconds (5 seconds)
     *
     * @param value
     * @return
     */
    private int asrTimeModelToSeek(float value) {
        return (int) (Math.pow(value / 5, MODEL_TO_SEEK_EXP) * 10000);
    }

    /**
     * Convert asr time value from model to seek. s = (m)^(2/3) * 10000, where 10000 is seek bar max value and 5 is time max value in seconds (5 seconds)
     *
     * @param value
     * @return
     */
    private int portamentoModelToSeek(float value) {
        return (int) (Math.pow(value, MODEL_TO_SEEK_EXP) * 10000);
    }

    /**
     * Convert asr time from seek to model: m = (s/1000)^(3/2), where 1000 is seek bar max value and 5 is time max value in seconds (5 seconds)
     *
     * @param value seek value
     * @return model value
     */
    private float portamentoSeekToModel(int value) {
        return (float) Math.pow((double) value / 10000, SEEK_TO_MODEL_EXP);
    }

    /**
     * Convert semitones value from seek to model. Seek range is 0/48, model range is -24/+24.
     *
     * @param value seek semitone value
     * @return model semitone value
     */
    private float asrPitchSeekToModel(int value) {
        return (float) value - 24;
    }

    /**
     * Convert semitones value from model to seek. Seek range is 0/48, model range is -24/+24.
     *
     * @param value model semitone value
     * @return seek semitone value
     */
    private int asrPitchModelToSeek(float value) {
        return (int) value + 24;
    }

    /**
     * Convert eq gain from seek to model. Seek range is 0/30, model range is -15/+15
     *
     * @param value seek dB gain value
     * @return model dB gain value
     */
    private int eqSeekToModel(int value) {
        return value - 15;
    }

    /**
     * Convert eq gain from model to seek. Seek range is 0/30, model range is -15/+15
     *
     * @param value model dB gain value
     * @return seek dB gain value
     */
    private int eqModelToSeek(int value) {
        return value + 15;
    }
}
