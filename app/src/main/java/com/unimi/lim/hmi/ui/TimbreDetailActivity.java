package com.unimi.lim.hmi.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.ui.fragment.TimbreDetailFragment;

import static com.unimi.lim.hmi.util.Constant.Context.TIMBRE_ID;

public class TimbreDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, TimbreDetailFragment.newInstance(getIntent().getStringExtra(TIMBRE_ID)))
                    .commitNow();
        }
    }
}
