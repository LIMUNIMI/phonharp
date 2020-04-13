package com.unimi.lim.hmi.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Context.IS_NEW_ITEM;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;

@RequiresApi(api = Build.VERSION_CODES.N)
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

        // TODO implement real version
        EditText timbreIdView = view.findViewById(R.id.timbre_id);
        timbreIdView.setEnabled(false);
        TextInputEditText timbreContentView = view.findViewById(R.id.timbre_content);

        viewModel.getSelected().observe(getViewLifecycleOwner(), timbre -> {
            Log.d(getClass().getName(), "Change -> timbre: " + timbre.toString());
            timbreIdView.setText(timbre.getId());
            timbreContentView.setText(timbre.getContent());
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
                    timbre.setContent(s.toString());
                }
            });
        });
    }

}
