package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.ui.fragment.TimbreDetailFragment;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import org.apache.commons.lang3.StringUtils;

import static com.unimi.lim.hmi.util.Constant.Context.IS_NEW_ITEM;
import static com.unimi.lim.hmi.util.Constant.Context.RELOAD_TIMBRE_LIST;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Settings.DEFAULT_TIMBRE_ID;
import static com.unimi.lim.hmi.util.Constant.Settings.SELECTED_TIMBRE_ID;

public class TimbreDetailActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_detail);

        // When activity is created for the first time inject timbre detail fragment
        // Use newInstance to send parameters from the activity to the fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_timbre_detail_container, TimbreDetailFragment.newInstance(
                            getIntent().getStringExtra(TIMBRE_ID),
                            getIntent().getBooleanExtra(IS_NEW_ITEM, true)))
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
        deleteButton.setVisibility(StringUtils.isNotEmpty(getIntent().getStringExtra(TIMBRE_ID)) ? View.VISIBLE : View.GONE);
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
                boolean canDelete = !selectedTimbreId.equals(viewModel.getWorking().getValue().getId());
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
