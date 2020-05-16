package com.unimi.lim.hmi.ui.common;

import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.collect.ImmutableBiMap;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("ConstantConditions")
public class SetupUtils {

    private SetupUtils() {
    }

    /**
     * Common method to setup spinner
     *
     * @param view              main view
     * @param spinnerId         spinner id
     * @param initialModelValue initial model value
     * @param onSelect          invoked when spinner item is clicked
     * @param onChange          invoked when spinner item is clicked
     * @param <M>               model type
     */
    public static <M> void setupSpinner(View view, int spinnerId, M initialModelValue, ImmutableBiMap<Integer, M> spinnerIdToModelValue, Consumer<Integer> onSelect, Runnable onChange) {
        // Setup initial item
        Spinner spinner = view.findViewById(spinnerId);
        spinner.setSelection(spinnerIdToModelValue.inverse().get(initialModelValue));

        // Setup item click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSelect.accept(position);
                onChange.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }

    /**
     * Common method to setup seek bar with text to show bar progress
     *
     * @param view                main view
     * @param seekId              seek id
     * @param textViewId          text view that show seek value
     * @param seekStep            seek step
     * @param text                text to be shown on specified text view, given seek progress this function should return progress description
     * @param seekInitialProgress to setup initial seek value
     * @param onProgressChanged   invoked when seek value changed
     * @param onChange            invoked when seek value changed
     */
    public static void setupSeek(View view, int seekId, int textViewId, int seekStep, Function<Integer, String> text, Integer seekInitialProgress, Consumer<Integer> onProgressChanged, Runnable onChange) {
        // Setup initial values
        SeekBar seekBar = view.findViewById(seekId);
        TextView textView = view.findViewById(textViewId);
        seekBar.setProgress(seekInitialProgress);
        textView.setText(text.apply(seekBar.getProgress()));

        // Setup seek change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / seekStep;
                progress = progress * seekStep;
                onProgressChanged.accept(progress);
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
    public static void setupSwitch(View view, int switchId, int containerId, boolean isEnabled, Runnable onEnable, Runnable onDisable, Runnable onChange) {
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
    private static void handleSwitchContainerVisibility(View containerView, boolean isEnabled, Runnable onEnable, Runnable onDisable) {
        if (isEnabled) {
            containerView.setVisibility(View.VISIBLE);
            onEnable.run();
        } else {
            containerView.setVisibility(View.GONE);
            onDisable.run();
        }
    }
}
