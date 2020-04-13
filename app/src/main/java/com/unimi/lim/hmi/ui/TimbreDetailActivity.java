package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
     * Handle save and cancel buttons; action result is returned to caller activity
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.timbre_cancel) {
            setResult(RESULT_CANCELED);
        } else if (view.getId() == R.id.timbre_save) {
            TimbreViewModel viewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
            viewModel.saveSelected();
            Intent intent = new Intent();
            intent.putExtra(RELOAD_TIMBRE_LIST, true);
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}
