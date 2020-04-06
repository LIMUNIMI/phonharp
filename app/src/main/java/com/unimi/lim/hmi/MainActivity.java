package com.unimi.lim.hmi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import static com.unimi.lim.hmi.util.Constant.Context.NOTE;
import static com.unimi.lim.hmi.util.Constant.Context.OCTAVE;
import static com.unimi.lim.hmi.util.Constant.Context.OFFSET;
import static com.unimi.lim.hmi.util.Constant.Context.SCALE_TYPE;
import static com.unimi.lim.hmi.util.Constant.Context.WAVE_FORM;

public class MainActivity extends AppCompatActivity {

    // Data passed to keyboard activity
    private String selectedWaveForm;
    private String selectedScaleType;
    private String selectedNote;
    private String selectedOctave;
    private String selectedOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Attach listener on spinners
        Spinner waveForm = findViewById(R.id.main_select_wave);
        Spinner scaleType = findViewById(R.id.main_select_scale_type);
        Spinner note = findViewById(R.id.main_select_note);
        Spinner octave = findViewById(R.id.main_select_octave);
        Spinner offset = findViewById(R.id.main_select_offset);

        octave.setSelection(4);

        SpinnerListener spinnerListener = new SpinnerListener();
        waveForm.setOnItemSelectedListener(spinnerListener);
        scaleType.setOnItemSelectedListener(spinnerListener);
        note.setOnItemSelectedListener(spinnerListener);
        octave.setOnItemSelectedListener(spinnerListener);
        offset.setOnItemSelectedListener(spinnerListener);

        // Open settings button
        final Button settingsButton = findViewById(R.id.open_settings);
        settingsButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Open keyboard button, launch keyboard activity
        final Button keyboarButton = findViewById(R.id.open_keyboard);
        keyboarButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(this, KeyboardActivity.class);
            intent.putExtra(WAVE_FORM, selectedWaveForm);
            intent.putExtra(SCALE_TYPE, selectedScaleType);
            intent.putExtra(NOTE, selectedNote);
            intent.putExtra(OCTAVE, selectedOctave);
            intent.putExtra(OFFSET, selectedOffset);
            startActivity(intent);
        });
    }

    /**
     * Set data to be passed to keyboard activity
     */
    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selected = (String) parent.getItemAtPosition(position);
            switch (parent.getId()) {
                case R.id.main_select_wave:
                    selectedWaveForm = selected;
                    break;
                case R.id.main_select_scale_type:
                    selectedScaleType = selected;
                    break;
                case R.id.main_select_note:
                    selectedNote = selected;
                    break;
                case R.id.main_select_octave:
                    selectedOctave = selected;
                    break;
                case R.id.main_select_offset:
                    selectedOffset = selected;
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // empty
        }
    }

}
