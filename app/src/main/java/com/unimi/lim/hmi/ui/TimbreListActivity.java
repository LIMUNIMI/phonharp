package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.jsyn.JsynSynthesizer;
import com.unimi.lim.hmi.ui.fragment.TimbreListFragment;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;
import com.unimi.lim.hmi.util.AndroidPropertyUtils;
import com.unimi.lim.hmi.util.TimbreUtils;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Settings.SELECTED_TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.System.APP_SHARE_URL;
import static com.unimi.lim.hmi.util.ConversionUtils.secondsToMillis;

public class TimbreListActivity extends AppCompatActivity implements TimbreListFragment.OnTimbreListListener, View.OnClickListener {

    private final static int REQUEST_CODE = 0;
    private final static int SUSTAIN_SAMPLE_TIME = 500;

    private Synthesizer synthesizer;
    private final Handler releaseSoundHandler = new Handler();
    private boolean playingSound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_list);

        // Setup synthesizer, timbre configuration is update when a timbre is selected
        synthesizer = new JsynSynthesizer.Builder()
                .androidAudioDeviceManager(AndroidPropertyUtils.framesPerBuffer(getBaseContext()))
                .outputSampleRate(AndroidPropertyUtils.outputSampleRate(getBaseContext()))
                .build();

        // When activity is created for the first time setup fragment data
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_timbre_list_select_container, TimbreListFragment.newInstance())
                    .commitNow();
        }

        // Show action bar up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set "Add" button click listener
        ImageButton addButton = findViewById(R.id.add_timbre);
        addButton.setOnClickListener(this);
    }

    /**
     * Startup the synth and reload timbre list
     */
    @Override
    public void onResume() {
        super.onResume();
        synthesizer.start();

        TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
        viewModel.reloadAll();
    }

    /**
     * Release and stop the synth
     */
    @Override
    public void onPause() {
        super.onPause();
        synthesizer.release();
        synthesizer.stop();
    }

    /**
     * Select timbre, invoked when radio button is clicked. Also play a sample
     *
     * @param timbre clicked timbre on timbre list
     */
    @Override
    public void onSelect(Timbre timbre) {
        // Store selected timbre id on system preferences
        Log.d(getClass().getName(), "Selected timbre " + timbre.getId());
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(SELECTED_TIMBRE_ID, timbre.getId());
        editor.commit();

        // Play audio sample
        if (!playingSound) {
            playTimbreSample(timbre);
        } else {
            if (timbre.getId().equalsIgnoreCase(synthesizer.getTimbreId())) {
                // Select the timbre again to stop audio sample
                synthesizer.release();
                playingSound = false;
            } else {
                // Another timbre was selected but current audio sample is not yes finished
                // Play the new timbre
                playTimbreSample(timbre);
            }
        }
    }

    /**
     * Play provided timbre and schedule stop action after sample time + timbre envelop time
     *
     * @param timbre timbre
     */
    private void playTimbreSample(Timbre timbre) {
        // Play sample of provided timbre
        synthesizer.updateSynthesizerCfg(timbre);
        synthesizer.press(Note.C3.getFrequency());
        playingSound = true;

        // A delayed function is invoked to stop the playing audio sample.
        // This function is executed after sample time + timbre envelop time
        float stopAfter = secondsToMillis(TimbreUtils.maxAsrAttackTime(timbre)) + secondsToMillis(TimbreUtils.maxAsrReleaseTime(timbre)) + SUSTAIN_SAMPLE_TIME;
        releaseSoundHandler.postDelayed(() -> {
            // Release only the owned audio sample
            if (playingSound && timbre.getId().equals(synthesizer.getTimbreId())) {
                synthesizer.release();
                playingSound = false;
            }
        }, (long) stopAfter);
    }

    /**
     * Open edit timbre activity, invoked when an item from timbre list is clicked
     *
     * @param item clicked timbre on timbre list
     */
    @Override
    public void onEdit(Timbre item) {
        Log.d(getClass().getName(), "Edit timbre " + item.getId());
        startTimbreDetailActivity(item.getId());
    }

    /**
     * Share timbre configuration
     *
     * @param item
     */
    @Override
    public void onShare(Timbre item) {
        // Clone current item to remove ID
        item = SerializationUtils.clone(item);
        item.setId(null);

        // Setup timbre URL
        String enc = TimbreUtils.toBase64UrlEncoded(item);
        if (StringUtils.isEmpty(enc)) {
            showShareAlert();
        }
        String url = APP_SHARE_URL + "/" + enc;

        // Share timbre URL
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    /**
     * Handles "add" timbre button click
     *
     * @param view view
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add_timbre) {
            startTimbreDetailActivity(null);
        }
    }

    /**
     * Launch timbre detail activity, timbre id is passed to the activity
     *
     * @param timbreId selected timbre id
     */
    private void startTimbreDetailActivity(String timbreId) {
        Intent intent = new Intent(this, TimbreDetailActivity.class);
        intent.putExtra(TIMBRE_ID, timbreId);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * Show import error message
     */
    private void showShareAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.cannot_share_alert_message));
        alert.setNeutralButton(getResources().getString(R.string.cannot_share_alert_ok), (dialog, which) -> dialog.dismiss());
        alert.show();
    }

}
