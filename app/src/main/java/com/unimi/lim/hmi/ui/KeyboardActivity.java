package com.unimi.lim.hmi.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.google.common.collect.ImmutableBiMap;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.PanMode;
import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.KeyHandler;
import com.unimi.lim.hmi.synthetizer.OboeSynth;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;
import com.unimi.lim.hmi.util.TimbreUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import static com.unimi.lim.hmi.util.Constant.Settings.DEFAULT_TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Settings.HALF_TONE;
import static com.unimi.lim.hmi.util.Constant.Settings.HANDEDNESS;
import static com.unimi.lim.hmi.util.Constant.Settings.NOTE;
import static com.unimi.lim.hmi.util.Constant.Settings.OCTAVE;
import static com.unimi.lim.hmi.util.Constant.Settings.OFFSET;
import static com.unimi.lim.hmi.util.Constant.Settings.PAN;
import static com.unimi.lim.hmi.util.Constant.Settings.RIGHT_HANDED;
import static com.unimi.lim.hmi.util.Constant.Settings.SCALE_TYPE;
import static com.unimi.lim.hmi.util.Constant.Settings.SELECTED_TIMBRE_ID;

public class KeyboardActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private Synthesizer synth;
    private KeyHandler keyHandler;

    protected SensorManager sensorManager;
    protected SensorEventListener gameRotationListener;

    private final static List<Integer> playableKeyIds = Arrays.asList(R.id.key_frst, R.id.key_scnd, R.id.key_thrd, R.id.key_frth);

    // Mapping between scale enum and scale array description idx
    private final ImmutableBiMap<Scale.Type, Integer> SCALE_BIMAP = new ImmutableBiMap.Builder<Scale.Type, Integer>()
            .put(Scale.Type.MAJOR, 0)
            .put(Scale.Type.MINOR, 1)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);

        // Instance SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Handle preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        applyKeyboardPreferences(sharedPreferences);
        String selectedScaleType = sharedPreferences.getString(SCALE_TYPE, Scale.Type.MAJOR.name());
        String selectedNote = sharedPreferences.getString(NOTE, "C");
        String selectedOctave = sharedPreferences.getString(OCTAVE, "3");
        String selectedOffset = sharedPreferences.getString(OFFSET, "0");
        String selectedPanMode = sharedPreferences.getString(PAN, PanMode.CENTER.name());

        String timbreId = sharedPreferences.getString(SELECTED_TIMBRE_ID, DEFAULT_TIMBRE_ID);
        TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
        Timbre timbre = viewModel.select(timbreId).getValue();

        // Initialize services
        Scale.Type scaleType = Scale.Type.valueOf(selectedScaleType.toUpperCase());
        Note note = Note.valueOf(selectedNote.replace('#', 'd').concat(selectedOctave));
        Scale scale = new Scale(scaleType, note);

        // Setup tuning and timbre descriptions
        String tuningDesc = String.format(Locale.getDefault(), "%s %s %s, %s %s", note.name(), getResources().getStringArray(R.array.scale_type_names)[SCALE_BIMAP.get(scaleType)], getResources().getString(R.string.scale_desc), getResources().getString(R.string.offset_desc), selectedOffset);
        String timbreNameDesc = String.format(Locale.getDefault(), "%s %s", StringUtils.defaultIfEmpty(timbre.getName(), ""), getResources().getString(R.string.timbre_desc));
        ((TextView) findViewById(R.id.tuning_key)).setText(tuningDesc);
        ((TextView) findViewById(R.id.timbre_name_key)).setText(timbreNameDesc);
        ((TextView) findViewById(R.id.timbre_desc_key)).setText(TimbreUtils.buildDescription(timbre));

        // Initialize synthesizer and key handler

        /*
        synth = new JsynSynthesizer.Builder()
                .androidAudioDeviceManager(AndroidPropertyUtils.framesPerBuffer(getBaseContext()))
                .panMode(PanMode.valueOf(selectedPanMode))
                .timbreCfg(timbre)
                .outputSampleRate(AndroidPropertyUtils.outputSampleRate(getBaseContext()))
                .build();
        */


        synth = new OboeSynth(this);
        synth.start();

        keyHandler = new KeyHandler(synth, scale, Integer.valueOf(selectedOffset), timbre);

        // Setup keyboard listener
        KeyListener keyListener = new KeyListener();
        playableKeyIds.forEach(kid -> findViewById(kid).setOnTouchListener(keyListener));

        // Setup half tone key listener
        findViewById(R.id.key_modifier).setOnTouchListener(new HalfToneKeyListener());

        // Setup Rotation Listener
        gameRotationListener = new GameRotationListener(synth);


        // TODO Remove code below, used just for debug system property values
        boolean hasLowLatencyFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
        boolean hasProFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO);
        Log.d(this.getClass().getName(), " >> LOW_LATENCY: " + hasLowLatencyFeature + ", PRO: " + hasProFeature);
    }

    // *********************************************************************************************
    // LIFECYCLE

    @Override
    public void onResume() {
        synth.start();

        // register listener for sensor
        Sensor gameRotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if(gameRotationSensor != null){
            sensorManager.registerListener(gameRotationListener, gameRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        synth.stop();

        // Unregister sensor to avoid wasting battery
        sensorManager.unregisterListener(gameRotationListener);

        super.onPause();
    }

    // *********************************************************************************************
    // MENU

    public void showMenuPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.main_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Log.d(getClass().getName(), "Clicked menu item " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.menu_quit:
                finish();
                return true;
            default:
                return false;
        }
    }

    // *********************************************************************************************
    // PREFERENCES

    private void applyKeyboardPreferences(SharedPreferences sharedPreferences) {

        // Half-tone button
        boolean showHalfTone = sharedPreferences.getBoolean(HALF_TONE, true);
        findViewById(R.id.key_modifier).setVisibility(showHalfTone ? View.VISIBLE : View.GONE);
        Log.d(getClass().getName(), "Halftone button enabled: " + showHalfTone);

        // Handedness
        ConstraintLayout layout = findViewById(R.id.layout_keyboard);
        ConstraintSet constraint = new ConstraintSet();
        constraint.clone(layout);

        String handedness = sharedPreferences.getString(HANDEDNESS, RIGHT_HANDED);
        if (RIGHT_HANDED.equals(handedness)) {
            playableKeyIds.forEach(kid -> flipRight(constraint, kid));
            flipLeft(constraint, R.id.menu_button);
            flipLeft(constraint, R.id.key_modifier);
            flipRight(constraint, R.id.toolbar_title);
            constraint.connect(R.id.key_modifier, ConstraintSet.END, R.id.key_scnd, ConstraintSet.START);
            constraint.connect(R.id.toolbar_title, ConstraintSet.START, R.id.menu_button, ConstraintSet.END);
        } else {
            playableKeyIds.forEach(kid -> flipLeft(constraint, kid));
            flipRight(constraint, R.id.menu_button);
            flipRight(constraint, R.id.key_modifier);
            flipLeft(constraint, R.id.toolbar_title);
            constraint.connect(R.id.key_modifier, ConstraintSet.START, R.id.key_scnd, ConstraintSet.END);
            constraint.connect(R.id.toolbar_title, ConstraintSet.END, R.id.menu_button, ConstraintSet.START);
        }
        constraint.applyTo(layout);
        Log.d(getClass().getName(), "Right-handed: " + showHalfTone);
    }

    private void flipRight(ConstraintSet constraint, int keyId) {
        constraint.connect(keyId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraint.clear(keyId, ConstraintSet.START);
    }

    private void flipLeft(ConstraintSet constraint, int keyId) {
        constraint.connect(keyId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraint.clear(keyId, ConstraintSet.END);
    }

    // *********************************************************************************************
    // LISTENERS

    /**
     * Playing keys handler
     */
    private class KeyListener implements View.OnTouchListener {

        // Distance in pixels a touch can wander before we think the user is moving
        private final int touchSlop;

        // Holds fingers coordinates on specific key. Map key is the key id (0 to 3), Map value is the coordinate.
        private final Map<Integer, Float> xCoords = new HashMap<>();
        private final Map<Integer, Float> yCoords = new HashMap<>();

        KeyListener() {
            ViewConfiguration vc = ViewConfiguration.get(getApplicationContext());
            touchSlop = vc.getScaledTouchSlop();
            Log.d(getClass().getName(), "TouchSlop " + touchSlop);
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int keyNum;
            switch (view.getId()) {
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
                    throw new IllegalArgumentException("Unhandled view " + view.getId() + ", KeyListener can handle only playable keys");
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setAlpha(0.5f);
                    setCoords(keyNum, event);
                    keyHandler.keyPressed(keyNum);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setAlpha(1.0f);
                    resetCoords(keyNum);
                    keyHandler.keyReleased(keyNum);
                    break;
                case MotionEvent.ACTION_MOVE:
                    handleCoords(xCoords, event.getX(), keyNum, step -> keyHandler.control1(step));
                    handleCoords(yCoords, event.getY(), keyNum, step -> keyHandler.control2(step));
            }
            return true;
        }

        /**
         * Setup finger coordinate
         *
         * @param keyNum number of pressed key
         * @param event  motion event
         */
        private void setCoords(int keyNum, MotionEvent event) {
            xCoords.put(keyNum, event.getX());
            yCoords.put(keyNum, event.getY());
        }

        /**
         * Reset finger coordinate
         *
         * @param keyNum number of released key
         */
        private void resetCoords(int keyNum) {
            xCoords.remove(keyNum);
            yCoords.remove(keyNum);
        }

        /**
         * Check if a finger has moved for more than touchSlop pixels in order to invoke the swipe controller.
         * The number of steps, passed to the controller consumer, are given by pixel delta / touch slope.
         * If more than one key is pressed then the step is divided by the number of pressed keys.
         *
         * @param coords             previous x or y coordinate map
         * @param coord              actual x or y coordinate
         * @param keyNum             number pressed key where the finger is moving
         * @param controllerConsumer controller that should be invoked if pixel delta is greater than touch slop
         */
        private void handleCoords(Map<Integer, Float> coords, float coord, int keyNum, Consumer<Float> controllerConsumer) {
            Float previous = coords.get(keyNum);
            if (previous == null) {
                return;
            }
            float diff = previous - coord;
            if (Math.abs(diff) > touchSlop) {
                coords.put(keyNum, coord);
                Float step = diff / touchSlop / coords.size();
                controllerConsumer.accept(step);
                String info = String.format(" - diff=%f, step=%f", diff, step);
                Log.d(getClass().getName(), "Moving key " + keyNum + info);
            }
        }
    }

    /**
     * Half tone button handler
     */
    private class HalfToneKeyListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                keyHandler.halfToneKeyPressed();
                v.setAlpha(0.5f);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                keyHandler.halfToneKeyReleased();
                v.setAlpha(1.0f);
            }
            return true;
        }
    }

    private class GameRotationListener implements SensorEventListener {

        Synthesizer synthesizer;

        GameRotationListener(Synthesizer synthesizer){
            this.synthesizer = synthesizer;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                //System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
                //TODO: modifica amp su synth
                //Log.d(getClass().getName(), "setting : "+ Arrays.toString(sensorEvent.values));
                synthesizer.controlVolume(-sensorEvent.values[0]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            // Do something here if sensor accuracy changes.
            // You must implement this callback in your code.

        }
    }
}
