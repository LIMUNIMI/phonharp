package com.unimi.lim.hmi;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.unimi.lim.hmi.keyboard.KeyHandler;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.WaveForm;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.jsyn.JsynSynthesizer;

import static com.unimi.lim.hmi.util.Constant.Context.*;

public class KeyboardActivity extends AppCompatActivity {

    private Synthesizer synthesizer;
    private KeyHandler keyHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);

        // Read data passed by main activity
        String selectedWaveForm = getIntent().getStringExtra(WAVE_FORM);
        String selectedScaleType = getIntent().getStringExtra(SCALE_TYPE);
        String selectedNote = getIntent().getStringExtra(NOTE);
        String selectedOctave = getIntent().getStringExtra(OCTAVE);

        // Initialize services
        Scale.Type scaleType = Scale.Type.valueOf(selectedScaleType.toUpperCase());
        Note note = Note.valueOf(selectedNote.replace('#', 'd').concat(selectedOctave));
        Scale scale = new Scale(scaleType, note);

        synthesizer = new JsynSynthesizer(WaveForm.valueOf(selectedWaveForm.toUpperCase()));
        keyHandler = new KeyHandler(synthesizer, scale);

        // Setup keyboard listener
        KeyListener keyListener = new KeyListener();
        findViewById(R.id.key_frst).setOnTouchListener(keyListener);
        findViewById(R.id.key_scnd).setOnTouchListener(keyListener);
        findViewById(R.id.key_thrd).setOnTouchListener(keyListener);
        findViewById(R.id.key_frth).setOnTouchListener(keyListener);

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

    /**
     * Handles keys touches
     */
    private class KeyListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean clicked = false;
            int keyNum = 0;
            switch (v.getId()) {
                case R.id.key_frst:
                    keyNum = 0;
                    clicked = true;
                    break;
                case R.id.key_scnd:
                    keyNum = 1;
                    clicked = true;
                    break;
                case R.id.key_thrd:
                    keyNum = 2;
                    clicked = true;
                    break;
                case R.id.key_frth:
                    keyNum = 3;
                    clicked = true;
                    break;
            }
            if (clicked) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                    keyHandler.keyPressed(keyNum);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                    keyHandler.keyReleased(keyNum);
                }
            }
            return true;
        }
    }

}
