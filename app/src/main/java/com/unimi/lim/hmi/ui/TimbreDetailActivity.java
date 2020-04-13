package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.ui.fragment.TimbreDetailFragment;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Context.IS_NEW_ITEM;
import static com.unimi.lim.hmi.util.Constant.Context.RELOAD_TIMBRE_LIST;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;

@RequiresApi(api = Build.VERSION_CODES.N)
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

        // Setup save and cancel button click listener
        findViewById(R.id.timbre_save).setOnClickListener(this);
        findViewById(R.id.timbre_cancel).setOnClickListener(this);
    }

    /**
     * Handles save and cancel buttons; also tells to parent activity if item list must be reloaded
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
        if (view.getId() == R.id.timbre_save) {
            viewModel.saveSelected();
        }
        Intent intent = new Intent();
        intent.putExtra(RELOAD_TIMBRE_LIST, viewModel.isItemChanged());
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
