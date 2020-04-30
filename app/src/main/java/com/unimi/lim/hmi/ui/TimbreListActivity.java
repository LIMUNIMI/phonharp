package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
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

import static com.unimi.lim.hmi.util.Constant.Context.IS_NEW_ITEM;
import static com.unimi.lim.hmi.util.Constant.Context.RELOAD_TIMBRE_LIST;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Settings.SELECTED_TIMBRE_ID;

public class TimbreListActivity extends AppCompatActivity implements TimbreListFragment.OnTimbreListListener, View.OnClickListener {

    private final static int REQUEST_CODE = 0;

    private Synthesizer synthesizer;
    private boolean playingSound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_list);

        // Setup synthesizer, timbre configuration is update when a timbre is selected
        synthesizer = new JsynSynthesizer.Builder().androidAudioDeviceManager().build();

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

    @Override
    public void onResume() {
        super.onResume();
        synthesizer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        synthesizer.release();
        synthesizer.stop();
    }

    /**
     * Select timbre, invoked when radio button is clicked
     *
     * @param timbre clicked timbre on timbre list
     */
    @Override
    public void onSelect(Timbre timbre) {
        Log.d(getClass().getName(), "Selected timbre " + timbre.getId());
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(SELECTED_TIMBRE_ID, timbre.getId());
        editor.commit();
        if (!playingSound) {
            synthesizer.updateTimbreCfg(timbre);
            synthesizer.press(Note.C3.getFrequency());
            playingSound = true;
        } else {
            synthesizer.release();
            playingSound = false;
        }
    }

    /**
     * Open edit timbre activity, invoked when an item from timbre list is clicked
     *
     * @param item clicked timbre on timbre list
     */
    @Override
    public void onEdit(Timbre item) {
        Log.d(getClass().getName(), "Edit timbre " + item.getId());
        startTimbreDetailActivity(item.getId(), false);
    }

    /**
     * Handles "add" timbre button click
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add_timbre) {
            startTimbreDetailActivity(null, true);
        }
    }

    /**
     * Handles return code from timbre detail activity: if the timbre has been saved then timbre list must be refreshed
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Reload timbre list in order to to refresh timbre fragment list values
        if (data != null && data.getBooleanExtra(RELOAD_TIMBRE_LIST, false)) {
            Log.d(getClass().getName(), "Reload timbre list");
            TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
            viewModel.reloadAll();
        }
    }

    /**
     * Launch timbre detail activity, timbre id is passed to the activity
     *
     * @param timbreId selected timbre id
     */
    private void startTimbreDetailActivity(String timbreId, boolean isNewItem) {
        Intent intent = new Intent(this, TimbreDetailActivity.class);
        intent.putExtra(TIMBRE_ID, timbreId);
        intent.putExtra(IS_NEW_ITEM, isNewItem);
        startActivityForResult(intent, REQUEST_CODE);
    }

}
