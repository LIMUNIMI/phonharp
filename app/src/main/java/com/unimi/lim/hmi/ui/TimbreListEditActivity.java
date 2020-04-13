package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.fragment.TimbreListFragment;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Context.IS_NEW_ITEM;
import static com.unimi.lim.hmi.util.Constant.Context.RELOAD_TIMBRE_LIST;
import static com.unimi.lim.hmi.util.Constant.Context.SHOW_CHECK_MARKER;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreListEditActivity extends AppCompatActivity implements TimbreListFragment.OnTimbreListClickListener, View.OnClickListener {

    private final static int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_list_edit);

        // When activity is created for the first time setup fragment data
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_timbre_list_edit_container, TimbreListFragment.newInstance(false, null))
                    .commitNow();
        }

        // Show action bar up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set "Add" button click listener
        FloatingActionButton fab = findViewById(R.id.add_timbre);
        fab.setOnClickListener(this);
    }

    /**
     * Open timbre detail activity, invoked when an item from timbre list is clicked
     *
     * @param item clicked timbre on timbre list
     */
    @Override
    public void onTimbreClicked(Timbre item) {
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
            viewModel.setItemChanged(true);
        }
    }

    /**
     * Android back button
     */
    @Override
    public void onBackPressed() {
        sendItentToParentActivity();
    }

    /**
     * Toolbar back button
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() != android.R.id.home) {
            return super.onOptionsItemSelected(item);
        }
        sendItentToParentActivity();
        return true;
    }

    /**
     * Tells to parent activity if item list must be reloaded
     */
    private void sendItentToParentActivity() {
        TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
        Intent intent = new Intent();
        intent.putExtra(RELOAD_TIMBRE_LIST, viewModel.isItemChanged());
        setResult(RESULT_CANCELED, intent);
        finish();
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
