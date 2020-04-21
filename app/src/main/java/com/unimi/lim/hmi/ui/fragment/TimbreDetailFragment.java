package com.unimi.lim.hmi.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Context.IS_NEW_ITEM;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;

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
            viewModel.create();
        } else {
            viewModel.select(timbreId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timbre_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO remove
        TextInputEditText timbreContentView = view.findViewById(R.id.timbre_content);
        viewModel.getSelected().observe(getViewLifecycleOwner(), timbre -> {
            Log.d(getClass().getName(), "Change -> timbre: " + timbre.toString());
            timbreContentView.setText(timbre.getName());
            timbreContentView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(getClass().getName(), " --> afterTextChanged: " + s.toString());
                    timbre.setName(s.toString());
                }
            });
        });

        // TODO refactor
        setupSwitchListener(view, R.id.swipe_control_enabled, R.id.swipe_control_container);
        setupSwitchListener(view, R.id.tremolo_enabled, R.id.tremolo_container);
        setupSwitchListener(view, R.id.vibrato_enabled, R.id.vibrato_container);
        setupSwitchListener(view, R.id.ampl_asr_enabled, R.id.ampl_asr_container);
        setupSwitchListener(view, R.id.pitch_asr_enabled, R.id.pitch_asr_container);
        setupSwitchListener(view, R.id.pwidth_asr_enabled, R.id.pwidth_asr_container);

    }

    private void setupSwitchListener(View view, int switchId, int containerId) {
        Switch switchView = view.findViewById(switchId);
        View containerView = view.findViewById(containerId);
        containerView.setVisibility(View.GONE);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    containerView.setVisibility(View.VISIBLE);
                } else {
                    containerView.setVisibility(View.GONE);
                }
            }
        });
    }

}
