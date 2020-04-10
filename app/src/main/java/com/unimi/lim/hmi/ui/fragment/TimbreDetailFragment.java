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
import com.unimi.lim.hmi.ui.model.TimbreDetailViewModel;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreDetailFragment extends Fragment {

    private TimbreDetailViewModel mViewModel;
    private String timbreId;

    public static TimbreDetailFragment newInstance(String timbreId) {
        return new TimbreDetailFragment(timbreId);
    }

    public TimbreDetailFragment(String timbreId) {
        this.timbreId = timbreId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(getClass().getName(), " --> onCreateView");
        return inflater.inflate(R.layout.fragment_timbre_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(getClass().getName(), " --> onViewCreated");

        mViewModel = ViewModelProviders.of(this).get(TimbreDetailViewModel.class);
        mViewModel.selectTimbre(timbreId);

        EditText timbreIdView = view.findViewById(R.id.timbre_id);
        timbreIdView.setText(mViewModel.getTimbreId());
        timbreIdView.setEnabled(false);

        TextInputEditText timbreContentView = view.findViewById(R.id.timbre_content);
        timbreContentView.setText(mViewModel.getTimbreContent());
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
                mViewModel.setTimbreContent(s.toString());
            }
        });
    }
}
