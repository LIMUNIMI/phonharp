package com.unimi.lim.hmi.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.unimi.lim.hmi.entity.Timbre.DEFAULT_TAP_HYSTERESIS;

public class TimbreDetailFragment extends Fragment {

    // Utility functions to handle descriptions
    private final Function<Integer, String> MILLIS_DESCRIPTION = val -> String.format("%d%s", val, getResources().getString(R.string.milliseconds));
    private final Function<Integer, String> ASR_TIME_DESCRIPTION = val -> String.format("%.2f%s", asrTimeSeekToModel(val), getResources().getString(R.string.seconds));
    private final Function<Integer, String> PERCENTAGE_DESCRIPTION = val -> String.format("%d%s", val, getResources().getString(R.string.percentage));
    private final Function<Integer, String> SEMITONE_DESCRIPTION = val -> String.format("%.0f %s", asrPitchSeekToModel(val), getResources().getString(R.string.semitone));
    private final Function<Integer, String> HERTZ_DESCRIPTION = val -> String.format("%.1f%s", seekToModel(val), getResources().getString(R.string.hertz));
    private final Function<Integer, String> HARMONICS_DESCRIPTION = val ->
            val == 100 ? String.format("%s", getResources().getString(R.string.harmonics_odd)) :
                    (val == 0 ? String.format("%s", getResources().getString(R.string.harmonics_all)) :
                            String.format("%s: %d%s ", getResources().getString(R.string.harmonics_all_to_odd), val / 2, getResources().getString(R.string.percentage)));
    // Mapping between controller enum and controller spinner IDs
    private final BiMap<Integer, Timbre.Controller> CONTROLLER_BIMAP;

    public static Fragment newInstance() {
        return new TimbreDetailFragment();
    }

    public TimbreDetailFragment() {
        // Mapping between controller spinner IDs and controller enum
        CONTROLLER_BIMAP = HashBiMap.create();
        CONTROLLER_BIMAP.put(0, Timbre.Controller.NONE);
        CONTROLLER_BIMAP.put(1, Timbre.Controller.VOLUME);
        CONTROLLER_BIMAP.put(2, Timbre.Controller.HARMONICS);
        CONTROLLER_BIMAP.put(3, Timbre.Controller.PITCH);
        CONTROLLER_BIMAP.put(4, Timbre.Controller.TREMOLO);
        CONTROLLER_BIMAP.put(5, Timbre.Controller.VIBRATO);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timbre_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve current timbre; use getValue instead of observe because timbre will be modified by the user but we dont want to setup view element each time
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
        setupSeek(view, R.id.volume_seek, R.id.volume_text, 1, PERCENTAGE_DESCRIPTION, () -> timbre.getVolume(), val -> timbre.setVolume(val), onChange);
        setupSeek(view, R.id.harmonics_seek, R.id.harmonics_text, 1, HARMONICS_DESCRIPTION, () -> timbre.getHarmonics(), val -> timbre.setHarmonics(val), onChange);

        // Tremolo Switch
        setupSwitch(view, R.id.tremolo_enabled, R.id.tremolo_container,
                timbre.getTremolo() != null,
                () -> {
                    // Setup Tremolo Seek bars
                    Timbre.Lfo lfo = timbre.getTremolo() != null ? timbre.getTremolo() : new Timbre.Lfo();
                    timbre.setTremolo(lfo);
                    setupSeek(view, R.id.tremolo_rate_seek, R.id.tremolo_rate_text, 100, HERTZ_DESCRIPTION, () -> modelToSeek(timbre.getTremolo().getRate()), val -> timbre.getTremolo().setRate(seekToModel(val)), onChange);
                    setupSeek(view, R.id.tremolo_depth_seek, R.id.tremolo_depth_text, 1, PERCENTAGE_DESCRIPTION, () -> timbre.getTremolo().getDepth(), val -> timbre.getTremolo().setDepth(val), onChange);
                },
                () -> timbre.setTremolo(null), onChange);

        // Vibrato Switch
        setupSwitch(view, R.id.vibrato_enabled, R.id.vibrato_container, timbre.getVibrato() != null, () -> {
                    // Setup Vibrato Seek bars
                    Timbre.Lfo lfo = timbre.getVibrato() != null ? timbre.getVibrato() : new Timbre.Lfo();
                    timbre.setVibrato(lfo);
                    setupSeek(view, R.id.vibrato_rate_seek, R.id.vibrato_rate_text, 100, HERTZ_DESCRIPTION, () -> modelToSeek(timbre.getVibrato().getRate()), val -> timbre.getVibrato().setRate(seekToModel(val)), onChange);
                    setupSeek(view, R.id.vibrato_depth_seek, R.id.vibrato_depth_text, 1, PERCENTAGE_DESCRIPTION, () -> timbre.getVibrato().getDepth(), val -> timbre.getVibrato().setDepth(val), onChange);
                },
                () -> timbre.setVibrato(null), onChange);

        // Volume ASR Switch
        setupSwitch(view, R.id.volume_asr_enabled, R.id.volume_asr_container, timbre.getVolumeAsr() != null, () -> {
                    // Setup Volume ASR Seek bars
                    Timbre.Asr asr = timbre.getVolumeAsr() != null ? timbre.getVolumeAsr() : new Timbre.Asr();
                    timbre.setVolumeAsr(asr);
                    setupSeek(view, R.id.volume_asr_attack_seek, R.id.volume_asr_attack_text, 1, ASR_TIME_DESCRIPTION, () -> asrTimeModelToSeek(timbre.getVolumeAsr().getAttackTime()), val -> timbre.getVolumeAsr().setAttackTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.volume_asr_release_seek, R.id.volume_asr_release_text, 1, ASR_TIME_DESCRIPTION, () -> asrTimeModelToSeek(timbre.getVolumeAsr().getReleaseTime()), val -> timbre.getVolumeAsr().setReleaseTime(asrTimeSeekToModel(val)), onChange);
                },
                () -> timbre.setVolumeAsr(null), onChange);

        // Pitch ASR Switch
        setupSwitch(view, R.id.pitch_asr_enabled, R.id.pitch_asr_container, timbre.getPitchAsr() != null, () -> {
                    // Setup Pitch ASR Seek bars
                    Timbre.Asr asr = timbre.getPitchAsr() != null ? timbre.getPitchAsr() : new Timbre.Asr();
                    timbre.setPitchAsr(asr);
                    setupSeek(view, R.id.pitch_asr_attack_seek, R.id.pitch_asr_attack_text, 1, ASR_TIME_DESCRIPTION, () -> asrTimeModelToSeek(timbre.getPitchAsr().getAttackTime()), val -> timbre.getPitchAsr().setAttackTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_release_seek, R.id.pitch_asr_release_text, 1, ASR_TIME_DESCRIPTION, () -> asrTimeModelToSeek(timbre.getPitchAsr().getReleaseTime()), val -> timbre.getPitchAsr().setReleaseTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_init_seek, R.id.pitch_asr_init_text, 1, SEMITONE_DESCRIPTION, () -> asrPitchModelToSeek(timbre.getPitchAsr().getInitialValue()), val -> timbre.getPitchAsr().setInitialValue(asrPitchSeekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_final_seek, R.id.pitch_asr_final_text, 1, SEMITONE_DESCRIPTION, () -> asrPitchModelToSeek(timbre.getPitchAsr().getFinalValue()), val -> timbre.getPitchAsr().setFinalValue(asrPitchSeekToModel(val)), onChange);
                },
                () -> timbre.setPitchAsr(null), onChange);

        // Harmonics ASR Switch
        setupSwitch(view, R.id.harmonics_asr_enabled, R.id.harmonics_asr_container, timbre.getHarmonicsAsr() != null, () -> {
                    // Setup Harmonics ASR Seek bars
                    Timbre.Asr asr = timbre.getHarmonicsAsr() != null ? timbre.getHarmonicsAsr() : new Timbre.Asr();
                    timbre.setHarmonicsAsr(asr);
                    setupSeek(view, R.id.harmonics_asr_attack_seek, R.id.harmonics_asr_attack_text, 1, ASR_TIME_DESCRIPTION, () -> asrTimeModelToSeek(timbre.getHarmonicsAsr().getAttackTime()), val -> timbre.getHarmonicsAsr().setAttackTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.harmonics_asr_release_seek, R.id.harmonics_asr_release_text, 1, ASR_TIME_DESCRIPTION, () -> asrTimeModelToSeek(timbre.getHarmonicsAsr().getReleaseTime()), val -> timbre.getHarmonicsAsr().setReleaseTime(asrTimeSeekToModel(val)), onChange);
                    setupSeek(view, R.id.harmonics_asr_init_seek, R.id.harmonics_asr_init_text, 1, HARMONICS_DESCRIPTION, () -> (int) timbre.getHarmonicsAsr().getInitialValue(), val -> timbre.getHarmonicsAsr().setInitialValue(val), onChange);
                    setupSeek(view, R.id.harmonics_asr_final_seek, R.id.harmonics_asr_final_text, 1, HARMONICS_DESCRIPTION, () -> (int) timbre.getHarmonicsAsr().getFinalValue(), val -> timbre.getHarmonicsAsr().setFinalValue(val), onChange);
                },
                () -> timbre.setHarmonicsAsr(null), onChange);

        // Swipe controller Switch
        setupSwitch(view, R.id.swipe_control_enabled, R.id.swipe_control_container, timbre.getController1() != null || timbre.getController2() != null, () -> {
                    // Setup Swipe controller Spinner
                    setupSwipeControllerSpinner(view, R.id.swipe_horiz_spinner, timbre.getController1(), pos -> timbre.setController1(CONTROLLER_BIMAP.get(pos)), onChange);
                    setupSwipeControllerSpinner(view, R.id.swipe_vert_spinner, timbre.getController2(), pos -> timbre.setController2(CONTROLLER_BIMAP.get(pos)), onChange);
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
                    setupSeek(view, R.id.tap_hysteresis_seek, R.id.tap_hysteresis_text, 1, MILLIS_DESCRIPTION, () -> modelToSeek(timbre.getTapHysteresis()), val -> timbre.setTapHysteresis(seekToModel(val)), onChange);
                },
                () -> timbre.setTapHysteresis(0f), onChange);
    }

    /**
     * Common method to setup spinners
     *
     * @param view       main view
     * @param seekId     seek id
     * @param textViewId text view that show seek value
     * @param seekStep   seek stp
     * @param text       text to be shown on specidied text view
     * @param supplier   to setup initial seek value
     * @param consumer   invoked when seek value changed
     * @param onChange   invoked when seek value changed
     */
    private void setupSeek(View view, int seekId, int textViewId, int seekStep, Function<Integer, String> text, Supplier<Integer> supplier, Consumer<Integer> consumer, Runnable onChange) {
        // Setup initial values
        SeekBar seekBar = view.findViewById(seekId);
        TextView textView = view.findViewById(textViewId);
        seekBar.setProgress(supplier.get());
        textView.setText(text.apply(seekBar.getProgress()));

        // Setup seek change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / seekStep;
                progress = progress * seekStep;
                consumer.accept(progress);
                textView.setText(text.apply(progress));
                onChange.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

    }

    /**
     * Common method to setup swipe controller spinner
     *
     * @param view       main view
     * @param spinnerId  spinner id
     * @param controller initial spinner value
     * @param consumer   invoked when spinner item is clicked
     * @param onChange   invoked when spinner item is clicked
     */
    private void setupSwipeControllerSpinner(View view, int spinnerId, Timbre.Controller controller, Consumer<Integer> consumer, Runnable onChange) {
        // Setup initial item
        controller = controller != null ? controller : Timbre.Controller.NONE;
        Spinner spinner = view.findViewById(spinnerId);
        spinner.setSelection(CONTROLLER_BIMAP.inverse().get(controller));

        // Setup item click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                consumer.accept(position);
                onChange.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    /**
     * Common method to setup switch
     *
     * @param view        main view
     * @param switchId    switch id
     * @param containerId container id, hide or shown when switch is disabled or enabled
     * @param isEnabled   initial switch status
     * @param onEnable    invoked when the switch is enabled
     * @param onDisable   invoked when the switch is disabled
     * @param onChange    invoked when switch status changes
     */
    private void setupSwitch(View view, int switchId, int containerId, boolean isEnabled, Runnable onEnable, Runnable onDisable, Runnable onChange) {
        // Setup initial status
        Switch aSwitch = view.findViewById(switchId);
        View containerView = view.findViewById(containerId);
        aSwitch.setChecked(isEnabled);
        handleSwitchContainerVisibility(containerView, isEnabled, onEnable, onDisable);

        // Setup change listener
        aSwitch.setOnCheckedChangeListener((switchView, isChecked) -> {
            handleSwitchContainerVisibility(containerView, isChecked, onEnable, onDisable);
            onChange.run();
        });
    }

    /**
     * Show or hide provided container
     *
     * @param containerView container view
     * @param isEnabled     true if enabled, false otherwise
     * @param onEnable      invoked when the switch is enabled
     * @param onDisable     invoked when the switch is disabled
     */
    private void handleSwitchContainerVisibility(View containerView, boolean isEnabled, Runnable onEnable, Runnable onDisable) {
        if (isEnabled) {
            containerView.setVisibility(View.VISIBLE);
            onEnable.run();
        } else {
            containerView.setVisibility(View.GONE);
            onDisable.run();
        }
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
     * Convert asr time from seek to model: m = (s/1000)^(3/2) * 5, where 1000 is seek bar max value and 5 is time max value in seconds (5 seconds)
     *
     * @param value seek value
     * @return model value
     */
    private float asrTimeSeekToModel(int value) {
        return (float) Math.pow((double) value / 10000, 3f / 2f) * 5;
    }

    /**
     * Convert asr time value from model to seek. s = (m / 5)^(2/3) * 1000, where 1000 is seek bar max value and 5 is time max value in seconds (5 seconds)
     *
     * @param value
     * @return
     */
    private int asrTimeModelToSeek(float value) {
        return (int) (Math.pow(value / 5, 2f / 3f) * 10000);
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

}
