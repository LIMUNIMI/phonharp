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

import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.unimi.lim.hmi.util.Constant.Context.IS_NEW_ITEM;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Strings.PERCENTAGE;
import static com.unimi.lim.hmi.util.Constant.Strings.SECOND;

public class TimbreDetailFragment extends Fragment {

    private TimbreViewModel viewModel;

    public static Fragment newInstance(String timbreId, boolean isNewItem) {
        TimbreDetailFragment fragment = new TimbreDetailFragment();
        Bundle args = new Bundle();
        args.putString(TIMBRE_ID, timbreId);
        args.putBoolean(IS_NEW_ITEM, isNewItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup current timbre
        String timbreId = getArguments().getString(TIMBRE_ID);
        boolean isNewItem = getArguments().getBoolean(IS_NEW_ITEM);
        viewModel = ViewModelProviders.of(getActivity()).get(TimbreViewModel.class);
        if (isNewItem) {
            viewModel.createWorking();
        } else {
            viewModel.select(timbreId).createWorkingFromSelected();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timbre_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getWorking().observe(getViewLifecycleOwner(), timbre -> {
            Log.d(getClass().getName(), "Change -> timbre: " + timbre.toString());

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

            // Volume and Harmonics seek bar
            setupSeek(view, R.id.seek_volume, R.id.volume_value, val -> val + StringUtils.EMPTY, () -> timbre.getVolume(), val -> timbre.setVolume(val));
            setupSeek(view, R.id.seek_harmonics, R.id.harmonics_value, val -> val + PERCENTAGE, () -> timbre.getHarmonics(), val -> timbre.setHarmonics(val));

            // TODO setup other seek bar

            // TODO setup swipe controller

            Function<Integer, String> timeDescription = val -> ((float) val / 1000f) + SECOND;

            // Switch
            setupSwitch(view, R.id.tremolo_enabled, R.id.tremolo_container,
                    timbre.getTremolo() != null,
                    isEnabled -> {
                        if (isEnabled) {
                            timbre.setTremolo(new Timbre.Lfo());
                            // TODO fix
                            setupSeek(view, R.id.seek_volume_asr_attack, R.id.volume_asr_attack_value, timeDescription, () -> (int) timbre.getVolumeAsr().getAttackTime() * 1000, val -> timbre.getVolumeAsr().setAttackTime((float) val / 1000));
                            setupSeek(view, R.id.seek_volume_asr_release, R.id.volume_asr_release_value, timeDescription, () -> (int) timbre.getVolumeAsr().getReleaseTime() * 1000, val -> timbre.getVolumeAsr().setReleaseTime((float) val / 1000));
                        } else {
                            timbre.setTremolo(null);
                        }
                    });
            setupSwitch(view, R.id.vibrato_enabled, R.id.vibrato_container, timbre.getVibrato() != null, isEnabled -> timbre.setVibrato(isEnabled ? new Timbre.Lfo() : null));
            setupSwitch(view, R.id.volume_asr_enabled, R.id.volume_asr_container, timbre.getVolumeAsr() != null, isEnabled -> {
                if (isEnabled) {
                    Timbre.Asr asr = timbre.getVolumeAsr() != null ? timbre.getVolumeAsr() : new Timbre.Asr();
                    timbre.setVolumeAsr(asr);
                    setupSeek(view, R.id.seek_volume_asr_attack, R.id.volume_asr_attack_value, timeDescription, () -> (int) timbre.getVolumeAsr().getAttackTime() * 1000, val -> timbre.getVolumeAsr().setAttackTime((float) val / 1000));
                    setupSeek(view, R.id.seek_volume_asr_release, R.id.volume_asr_release_value, timeDescription, () -> (int) timbre.getVolumeAsr().getReleaseTime() * 1000, val -> timbre.getVolumeAsr().setReleaseTime((float) val / 1000));
                } else {
                    timbre.setVolumeAsr(null);
                }
            });
            setupSwitch(view, R.id.pitch_asr_enabled, R.id.pitch_asr_container, timbre.getPitchAsr() != null, isEnabled -> timbre.setPitchAsr(isEnabled ? new Timbre.Asr() : null));
            setupSwitch(view, R.id.harmonics_asr_enabled, R.id.harmonics_asr_container, timbre.getHarmonicsAsr() != null, isEnabled -> timbre.setHarmonicsAsr(isEnabled ? new Timbre.Asr() : null));
            setupSwitch(view, R.id.swipe_control_enabled, R.id.swipe_control_container, timbre.getController1() != null || timbre.getController2() != null, isEnabled -> {
            });

        });
    }

    private void setupSeek(View view, int seekId, int textViewId, Function<Integer, String> text, Supplier<Integer> supplier, Consumer<Integer> consumer) {
        SeekBar seekBar = view.findViewById(seekId);
        TextView textView = view.findViewById(textViewId);
        seekBar.setProgress(supplier.get());
        textView.setText(text.apply(seekBar.getProgress()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                consumer.accept(progress);
                textView.setText(text.apply(seekBar.getProgress()));
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

    private void setupSwitch(View view, int switchId, int containerId, boolean isEnabled, Consumer<Boolean> onChange) {
        Switch aSwitch = view.findViewById(switchId);
        View containerView = view.findViewById(containerId);
        containerView.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        aSwitch.setChecked(isEnabled);
        // To setup seek bars initial value
        onChange.accept(isEnabled);
        aSwitch.setOnCheckedChangeListener((switchView, isChecked) -> {
            onChange.accept(isChecked);
            if (isChecked) {
                containerView.setVisibility(View.VISIBLE);
            } else {
                containerView.setVisibility(View.GONE);
            }
        });
    }

}
