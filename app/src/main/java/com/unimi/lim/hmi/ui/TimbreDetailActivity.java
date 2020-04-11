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
import com.unimi.lim.hmi.ui.model.TimbreViewModel;

import static com.unimi.lim.hmi.util.Constant.Context.RELOAD_TIMBRE_LIST;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbre_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_timbre_detail);
        setSupportActionBar(toolbar);

        View saveButton = findViewById(R.id.timbre_save);
        View cancelButton = findViewById(R.id.timbre_cancel);

        AppCompatActivity activity = this;

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setResult(RESULT_CANCELED);
                activity.finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimbreViewModel mViewModel = ViewModelProviders.of(activity).get(TimbreViewModel.class);
                mViewModel.saveSelected();
                Intent intent = new Intent();
                intent.putExtra(RELOAD_TIMBRE_LIST, true);
                activity.setResult(RESULT_OK, intent);
                activity.finish();
            }
        });

    }


}
