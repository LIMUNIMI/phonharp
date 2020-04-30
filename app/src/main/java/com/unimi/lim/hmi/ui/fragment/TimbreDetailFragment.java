package com.unimi.lim.hmi.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TimbreDetailFragment extends Fragment {

    private final Function<Integer, String> MILLIS_TO_SECONDS_DESCRIPTION = val -> String.format("%.2f%s", seekToModel(val), getResources().getString(R.string.seconds));
    private final Function<Integer, String> PERCENTAGE_DESCRIPTION = val -> String.format("%d%s", val, getResources().getString(R.string.percentage));
    private final Function<Integer, String> SEMITONE_DESCRIPTION = val -> String.format("%.0f %s", semitoneSeekToModel(val), getResources().getString(R.string.semitone));
    private final Function<Integer, String> HERTZ_DESCRIPTION = val -> String.format("%.1f%s", seekToModel(val), getResources().getString(R.string.hertz));

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

        // Notify the synthesizer on TimbreDetailActivity that timbre was modified
        Runnable onChange = () -> viewModel.workingChanged(timbre);

        // Volume and Harmonics seek bar
        setupSeek(view, R.id.volume_seek, R.id.volume_text, 1, PERCENTAGE_DESCRIPTION, () -> timbre.getVolume(), val -> timbre.setVolume(val), onChange);
        setupSeek(view, R.id.harmonics_seek, R.id.harmonics_text, 1, PERCENTAGE_DESCRIPTION, () -> timbre.getHarmonics(), val -> timbre.setHarmonics(val), onChange);

        // Switch
        setupSwitch(view, R.id.tremolo_enabled, R.id.tremolo_container,
                timbre.getTremolo() != null,
                () -> {
                    Timbre.Lfo lfo = timbre.getTremolo() != null ? timbre.getTremolo() : new Timbre.Lfo();
                    timbre.setTremolo(lfo);
                    setupSeek(view, R.id.tremolo_rate_seek, R.id.tremolo_rate_text, 100, HERTZ_DESCRIPTION, () -> modelToSeek(timbre.getTremolo().getRate()), val -> timbre.getTremolo().setRate(seekToModel(val)), onChange);
                    setupSeek(view, R.id.tremolo_depth_seek, R.id.tremolo_depth_text, 1, PERCENTAGE_DESCRIPTION, () -> timbre.getTremolo().getDepth(), val -> timbre.getTremolo().setDepth(val), onChange);
                },
                () -> timbre.setTremolo(null), onChange);
        setupSwitch(view, R.id.vibrato_enabled, R.id.vibrato_container, timbre.getVibrato() != null, () -> {
                    Timbre.Lfo lfo = timbre.getVibrato() != null ? timbre.getVibrato() : new Timbre.Lfo();
                    timbre.setVibrato(lfo);
                    setupSeek(view, R.id.vibrato_rate_seek, R.id.vibrato_rate_text, 100, HERTZ_DESCRIPTION, () -> modelToSeek(timbre.getVibrato().getRate()), val -> timbre.getVibrato().setRate(seekToModel(val)), onChange);
                    setupSeek(view, R.id.vibrato_depth_seek, R.id.vibrato_depth_text, 1, PERCENTAGE_DESCRIPTION, () -> timbre.getVibrato().getDepth(), val -> timbre.getVibrato().setDepth(val), onChange);
                },
                () -> timbre.setVibrato(null), onChange);
        setupSwitch(view, R.id.volume_asr_enabled, R.id.volume_asr_container, timbre.getVolumeAsr() != null, () -> {
                    Timbre.Asr asr = timbre.getVolumeAsr() != null ? timbre.getVolumeAsr() : new Timbre.Asr();
                    timbre.setVolumeAsr(asr);
                    setupSeek(view, R.id.volume_asr_attack_seek, R.id.volume_asr_attack_text, 50, MILLIS_TO_SECONDS_DESCRIPTION, () -> modelToSeek(timbre.getVolumeAsr().getAttackTime()), val -> timbre.getVolumeAsr().setAttackTime(seekToModel(val)), onChange);
                    setupSeek(view, R.id.volume_asr_release_seek, R.id.volume_asr_release_text, 50, MILLIS_TO_SECONDS_DESCRIPTION, () -> modelToSeek(timbre.getVolumeAsr().getReleaseTime()), val -> timbre.getVolumeAsr().setReleaseTime(seekToModel(val)), onChange);
                },
                () -> timbre.setVolumeAsr(null), onChange);
        setupSwitch(view, R.id.pitch_asr_enabled, R.id.pitch_asr_container, timbre.getPitchAsr() != null, () -> {
                    Timbre.Asr asr = timbre.getPitchAsr() != null ? timbre.getPitchAsr() : new Timbre.Asr();
                    timbre.setPitchAsr(asr);
                    setupSeek(view, R.id.pitch_asr_attack_seek, R.id.pitch_asr_attack_text, 50, MILLIS_TO_SECONDS_DESCRIPTION, () -> modelToSeek(timbre.getPitchAsr().getAttackTime()), val -> timbre.getPitchAsr().setAttackTime(seekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_release_seek, R.id.pitch_asr_release_text, 50, MILLIS_TO_SECONDS_DESCRIPTION, () -> modelToSeek(timbre.getPitchAsr().getReleaseTime()), val -> timbre.getPitchAsr().setReleaseTime(seekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_init_seek, R.id.pitch_asr_init_text, 1, SEMITONE_DESCRIPTION, () -> semitoneModelToSeek(timbre.getPitchAsr().getInitialValue()), val -> timbre.getPitchAsr().setInitialValue(semitoneSeekToModel(val)), onChange);
                    setupSeek(view, R.id.pitch_asr_final_seek, R.id.pitch_asr_final_text, 1, SEMITONE_DESCRIPTION, () -> semitoneModelToSeek(timbre.getPitchAsr().getFinalValue()), val -> timbre.getPitchAsr().setFinalValue(semitoneSeekToModel(val)), onChange);
                },
                () -> timbre.setPitchAsr(null), onChange);
        setupSwitch(view, R.id.harmonics_asr_enabled, R.id.harmonics_asr_container, timbre.getHarmonicsAsr() != null, () -> {
                    Timbre.Asr asr = timbre.getHarmonicsAsr() != null ? timbre.getHarmonicsAsr() : new Timbre.Asr();
                    timbre.setHarmonicsAsr(asr);
                    setupSeek(view, R.id.harmonics_asr_attack_seek, R.id.harmonics_asr_attack_text, 50, MILLIS_TO_SECONDS_DESCRIPTION, () -> modelToSeek(timbre.getHarmonicsAsr().getAttackTime()), val -> timbre.getHarmonicsAsr().setAttackTime(seekToModel(val)), onChange);
                    setupSeek(view, R.id.harmonics_asr_release_seek, R.id.harmonics_asr_release_text, 50, MILLIS_TO_SECONDS_DESCRIPTION, () -> modelToSeek(timbre.getHarmonicsAsr().getReleaseTime()), val -> timbre.getHarmonicsAsr().setReleaseTime(seekToModel(val)), onChange);
                    setupSeek(view, R.id.harmonics_asr_init_seek, R.id.harmonics_asr_init_text, 1, PERCENTAGE_DESCRIPTION, () -> (int) timbre.getHarmonicsAsr().getInitialValue(), val -> timbre.getHarmonicsAsr().setInitialValue(val), onChange);
                    setupSeek(view, R.id.harmonics_asr_final_seek, R.id.harmonics_asr_final_text, 1, PERCENTAGE_DESCRIPTION, () -> (int) timbre.getHarmonicsAsr().getFinalValue(), val -> timbre.getHarmonicsAsr().setFinalValue(val), onChange);
                },
                () -> timbre.setHarmonicsAsr(null), onChange);
        setupSwitch(view, R.id.swipe_control_enabled, R.id.swipe_control_container, timbre.getController1() != null || timbre.getController2() != null, () -> {
                    // TODO setup swipe controller
                },
                () -> {
                    // TODO
                }, () -> {
                    // TODO
                });
    }

    private void setupSeek(View view, int seekId, int textViewId, int seekStep, Function<Integer, String> text, Supplier<Integer> supplier, Consumer<Integer> consumer, Runnable onChange) {
        SeekBar seekBar = view.findViewById(seekId);
        TextView textView = view.findViewById(textViewId);
        seekBar.setProgress(supplier.get());
        textView.setText(text.apply(seekBar.getProgress()));
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

    private void setupSwitch(View view, int switchId, int containerId, boolean isEnabled, Runnable onEnable, Runnable onDisable, Runnable onChange) {
        Switch aSwitch = view.findViewById(switchId);
        View containerView = view.findViewById(containerId);
        aSwitch.setChecked(isEnabled);
        handleSwitchContainerVisibility(containerView, isEnabled, onEnable, onDisable);
        aSwitch.setOnCheckedChangeListener((switchView, isChecked) -> {
            handleSwitchContainerVisibility(containerView, isChecked, onEnable, onDisable);
            onChange.run();
        });
    }

    private void handleSwitchContainerVisibility(View containerView, boolean isEnabled, Runnable onEnable, Runnable onDisable) {
        if (isEnabled) {
            containerView.setVisibility(View.VISIBLE);
            onEnable.run();
        } else {
            containerView.setVisibility(View.GONE);
            onDisable.run();
        }
    }

    private float seekToModel(int value) {
        return (float) value / 1000;
    }

    private int modelToSeek(float value) {
        return (int) (value * 1000);
    }

    private float semitoneSeekToModel(int value) {
        return (float) value - 24;
    }

    private int semitoneModelToSeek(float value) {
        return (int) value + 24;
    }

}
