package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.jsyn.JsynSynthesizer;
import com.unimi.lim.hmi.ui.fragment.TimbreDetailFragment;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Context.IS_NEW_ITEM;
import static com.unimi.lim.hmi.util.Constant.Context.RELOAD_TIMBRE_LIST;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Settings.DEFAULT_TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Settings.SELECTED_TIMBRE_ID;

public class TimbreDetailActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private Synthesizer synthesizer;
    private String detailTimbreId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_detail);

        // Setup working timbre on the view model, used by TimbreDetailFragment
        detailTimbreId = getIntent().getStringExtra(TIMBRE_ID);
        Boolean isNewItem = getIntent().getBooleanExtra(IS_NEW_ITEM, true);
        TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
        if (isNewItem) {
            viewModel.createWorking();
        } else {
            viewModel.createWorkingFrom(detailTimbreId);
        }

        // Setup synthesizer and update its configuration each time the timbre is modified
        synthesizer = new JsynSynthesizer.Builder().androidAudioDeviceManager().build();
        viewModel.getWorking().observe(this, timbre -> {
            Log.d(getClass().getName(), "Updating timbre configuration");
            synthesizer.updateSynthesizerCfg(timbre);
        });

        // When activity is created for the first time inject timbre detail fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_timbre_detail_container, TimbreDetailFragment.newInstance())
                    .commitNow();
        }

        // Setup custom toolbar with save and cancel buttons
        Toolbar toolbar = findViewById(R.id.toolbar_timbre_detail);
        setSupportActionBar(toolbar);

        // Setup save, cancel and delete button click listener
        findViewById(R.id.timbre_save).setOnClickListener(this);
        findViewById(R.id.timbre_cancel).setOnClickListener(this);
        View deleteButton = findViewById(R.id.timbre_delete);
        deleteButton.setOnClickListener(this);

        // Delete button hide for new timbre, nothing to delete
        deleteButton.setVisibility(isNewItem ? View.GONE : View.VISIBLE);

        // Play button touch listener
        FloatingActionButton play = findViewById(R.id.timbre_play_floating);
        play.setOnTouchListener(this);
    }

    /**
     * Startup the synth
     */
    @Override
    public void onResume() {
        super.onResume();
        synthesizer.start();
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
     * Handle play button touch to reproduce timbre audio sample
     *
     * @param view
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            synthesizer.press(Note.C3.getFrequency());
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            synthesizer.release();
        }
        return true;
    }

    /**
     * Handles save, cancel and delete buttons; also tells to parent activity if item list must be reloaded
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
        switch (view.getId()) {
            case R.id.timbre_save:
                viewModel.saveWorking();
                toParentActivity(true);
                break;
            case R.id.timbre_cancel:
                toParentActivity(false);
                break;
            case R.id.timbre_delete:
                // Check if timbre is deletable: selected timbre cannot be delete
                String selectedTimbreId = PreferenceManager.getDefaultSharedPreferences(this).getString(SELECTED_TIMBRE_ID, DEFAULT_TIMBRE_ID);
                boolean canDelete = !selectedTimbreId.equals(detailTimbreId);
                if (canDelete) {
                    showDeleteAlert(viewModel);
                } else {
                    showCannotDeleteAlert();
                }
                break;
            default:
                throw new IllegalArgumentException("Unable to handle onClick event on view " + view.getId());
        }
    }

    /**
     * Show delete alert popup
     *
     * @param viewModel timbre view model
     */
    private void showDeleteAlert(TimbreViewModel viewModel) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.delete_alert_message));
        alert.setPositiveButton(getResources().getString(R.string.delete_alert_yes), (dialog, which) -> {
            viewModel.deleteWorking();
            dialog.dismiss();
            toParentActivity(true);
        });
        alert.setNegativeButton(getResources().getString(R.string.delete_alert_no), (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    /**
     * Show cannot delete message
     */
    private void showCannotDeleteAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.cannot_delete_alert_message));
        alert.setNeutralButton(getResources().getString(R.string.cannot_delete_alert_cancel), (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    /**
     * Back to parent activity, also notify if timbre list must be reloaded
     *
     * @param reloadTimbreList true to tell parent activity to reload timbre list, false otherwise
     */
    private void toParentActivity(boolean reloadTimbreList) {
        Intent intent = new Intent();
        intent.putExtra(RELOAD_TIMBRE_LIST, reloadTimbreList);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

}
