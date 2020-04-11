package com.unimi.lim.hmi.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.ui.fragment.TimbreListFragment;
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Context.RELOAD_TIMBRE_LIST;
import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreListActivity extends AppCompatActivity implements TimbreListFragment.OnTimbreListClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_list);

        Toolbar toolbar = findViewById(R.id.toolbar_timbre_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TimbreViewModel mViewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
        Log.d(getClass().getName(), " --> mViewModel " + mViewModel);

        FloatingActionButton fab = findViewById(R.id.add_timbre);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onTimbreClicked(Timbre item) {
        Intent intent = new Intent(this, TimbreDetailActivity.class);
        intent.putExtra(TIMBRE_ID, item.getId());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(getClass().getName(), " --> On activity result " + resultCode);
        if (resultCode == RESULT_OK && requestCode == 0) {
            boolean reload = data.getBooleanExtra(RELOAD_TIMBRE_LIST, false);
            Log.d(getClass().getName(), "Reload timbre " + reload);

            TimbreViewModel mViewModel = ViewModelProviders.of(this).get(TimbreViewModel.class);
            Log.d(getClass().getName(), " --> mViewModel " + mViewModel);
            mViewModel.reloadAll();
        }
    }
}
