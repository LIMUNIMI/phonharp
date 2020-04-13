package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.fragment.TimbreListFragment;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Context.RELOAD_TIMBRE_LIST;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreListSelectActivity extends AppCompatActivity implements TimbreListFragment.OnTimbreListClickListener, View.OnClickListener {

    private final static int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_list_select);

        // Show action bar up button
        // Setup custom toolbar with save and cancel buttons
        Toolbar toolbar = findViewById(R.id.toolbar_timbre_list_select);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set "Edit" button click listener
        ImageButton edit = findViewById(R.id.timbre_edit);
        edit.setOnClickListener(this);
    }

    @Override
    public void onTimbreClicked(Timbre item) {
        // TODO set property
        Log.d(getClass().getName(), "Selected timbre " + item.getId());
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TimbreListEditActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
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

        Log.d(getClass().getName(), " --> YOTOOOH");

        // Reload timbre list in order to to refresh timbre fragment list values
        if (data != null && data.getBooleanExtra(RELOAD_TIMBRE_LIST, false)) {
            Log.d(getClass().getName(), "Reload timbre list");
            TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
            viewModel.reloadAll();
        }
    }

}
