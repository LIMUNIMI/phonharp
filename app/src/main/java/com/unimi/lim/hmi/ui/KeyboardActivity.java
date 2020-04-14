package com.unimi.lim.hmi.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.preference.PreferenceManager;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.keyboard.KeyHandler;
import com.unimi.lim.hmi.keyboard.ThreadedKeyHandler;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.WaveForm;
import com.unimi.lim.hmi.synthetizer.jsyn.JsynSynthesizer;

import java.util.Arrays;
import java.util.List;

import static com.unimi.lim.hmi.util.Constant.Context.NOTE;
import static com.unimi.lim.hmi.util.Constant.Context.OCTAVE;
import static com.unimi.lim.hmi.util.Constant.Context.OFFSET;
import static com.unimi.lim.hmi.util.Constant.Context.SCALE_TYPE;
import static com.unimi.lim.hmi.util.Constant.Context.WAVE_FORM;
import static com.unimi.lim.hmi.util.Constant.Settings.HALF_TONE;
import static com.unimi.lim.hmi.util.Constant.Settings.HANDEDNESS;
import static com.unimi.lim.hmi.util.Constant.Settings.RIGHT_HANDED;

public class KeyboardActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "KEYBOARD_ACTIVITY";

    private Synthesizer synthesizer;
    private KeyHandler keyHandler;

    private List<Integer> playableKeyIds = Arrays.asList(R.id.key_frst, R.id.key_scnd, R.id.key_thrd, R.id.key_frth);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);

        // Register this activity as preferences change listener
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        applyPreferences();

        // Show action bar up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Read data passed by main activity
        String selectedWaveForm = getIntent().getStringExtra(WAVE_FORM);
        String selectedScaleType = getIntent().getStringExtra(SCALE_TYPE);
        String selectedNote = getIntent().getStringExtra(NOTE);
        String selectedOctave = getIntent().getStringExtra(OCTAVE);
        String selectedOffset = getIntent().getStringExtra(OFFSET);

        // Initialize services
        Scale.Type scaleType = Scale.Type.valueOf(selectedScaleType.toUpperCase());
        Note note = Note.valueOf(selectedNote.replace('#', 'd').concat(selectedOctave));
        Scale scale = new Scale(scaleType, note);

        synthesizer = new JsynSynthesizer(WaveForm.valueOf(selectedWaveForm.toUpperCase()));
        keyHandler = new ThreadedKeyHandler(synthesizer, scale, Integer.valueOf(selectedOffset));

        // Setup keyboard listener
        KeyListener keyListener = new KeyListener();
        playableKeyIds.forEach(kid -> findViewById(kid).setOnTouchListener(keyListener));

        // Note modifier listener
        ModifierListener modifierListener = new ModifierListener();
        findViewById(R.id.key_modifier).setOnTouchListener(modifierListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        keyHandler.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keyHandler.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        synthesizer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        synthesizer.stop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed, key: " + key);
        applyPreferences();
    }

    private void applyPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Half-tone button
        Boolean showHalfTone = sharedPreferences.getBoolean(HALF_TONE, false);
        findViewById(R.id.key_modifier).setVisibility(showHalfTone ? View.VISIBLE : View.GONE);
        Log.d(TAG, "Halftone button enabled: " + showHalfTone);


        // Handedness
        ConstraintLayout layout = findViewById(R.id.layout_keyboard);
        ConstraintSet constraint = new ConstraintSet();
        constraint.clone(layout);

        String handedness = sharedPreferences.getString(HANDEDNESS, RIGHT_HANDED);
        if (RIGHT_HANDED.equals(handedness)) {
            playableKeyIds.forEach(kid -> flipRight(constraint, kid));
            flipLeft(constraint, R.id.key_modifier);
            constraint.connect(R.id.key_modifier, ConstraintSet.END, R.id.key_scnd, ConstraintSet.START);
        } else {
            playableKeyIds.forEach(kid -> flipLeft(constraint, kid));
            flipRight(constraint, R.id.key_modifier);
            constraint.connect(R.id.key_modifier, ConstraintSet.START, R.id.key_scnd, ConstraintSet.END);
        }
        constraint.applyTo(layout);
        Log.d(TAG, "Right-handed: " + showHalfTone);
    }

    private void flipRight(ConstraintSet constraint, int keyId) {
        constraint.connect(keyId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraint.clear(keyId, ConstraintSet.START);
    }

    private void flipLeft(ConstraintSet constraint, int keyId) {
        constraint.connect(keyId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraint.clear(keyId, ConstraintSet.END);
    }

    /**
     * Handles keys touches
     */
    private class KeyListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean clicked = true;
            int keyNum = 0;
            switch (v.getId()) {
                case R.id.key_frst:
                    keyNum = 0;
                    break;
                case R.id.key_scnd:
                    keyNum = 1;
                    break;
                case R.id.key_thrd:
                    keyNum = 2;
                    break;
                case R.id.key_frth:
                    keyNum = 3;
                    break;
                default:
                    clicked = false;
            }
            if (clicked) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    keyHandler.keyPressed(keyNum);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    keyHandler.keyReleased(keyNum);
                }
            }
            return true;
        }
    }

    /**
     * Handles keys touches
     */
    private class ModifierListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                keyHandler.modifierPressed();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                keyHandler.modifierReleased();
            }
            return true;
        }
    }

}
