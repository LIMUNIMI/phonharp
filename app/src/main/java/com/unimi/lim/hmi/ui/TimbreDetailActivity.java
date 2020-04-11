package com.unimi.lim.hmi.ui;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.unimi.lim.hmi.R;
import com.unimi.lim.hmi.ui.fragment.TimbreDetailFragment;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_timbre_detail);
        setSupportActionBar(toolbar);
    }
}
