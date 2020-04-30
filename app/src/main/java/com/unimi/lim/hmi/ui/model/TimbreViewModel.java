package com.unimi.lim.hmi.ui.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.Timbre;

import java.util.List;

public class TimbreViewModel extends AndroidViewModel {

    private MutableLiveData<List<Timbre>> all;
    private MutableLiveData<Timbre> selected;
    private MutableLiveData<Timbre> working;

    public TimbreViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Timbre>> selectAll() {
        Log.d(getClass().getName(), "Selecting all timbre");
        if (all == null) {
            all = new MutableLiveData<>();
            // If needed perform the asynchronously
            List<Timbre> timbres = TimbreDao.getInstance(getApplication().getApplicationContext()).selectAll();
            all.setValue(timbres);
        }
        return all;
    }

    public TimbreViewModel reloadAll() {
        Log.d(getClass().getName(), "Reloading timbre list");
        if (all != null) {
            // If needed perform select asynchronously
            List<Timbre> timbres = TimbreDao.getInstance(getApplication().getApplicationContext()).selectAll();
            all.setValue(timbres);
        }
        return this;
    }

    public LiveData<Timbre> select(String id) {
        Log.d(getClass().getName(), "Select timbre with id " + id);
        if (selected == null) {
            selected = new MutableLiveData<>();
            // If needed perform select asynchronously
            Timbre timbre = TimbreDao.getInstance(getApplication().getApplicationContext()).selectById(id).orElseGet(() -> {
                Log.w(getClass().getName(), "Unable to find timbre by provided id, create new empty timbre");
                return new Timbre();
            });
            selected.setValue(timbre);
        }
        return selected;
    }

    public TimbreViewModel createWorking() {
        if (working == null) {
            Log.d(getClass().getName(), "Create new timbre");
            working = new MutableLiveData<>();
            working.setValue(new Timbre());
        }
        return this;
    }

    public TimbreViewModel createWorkingFrom(String timbreId) {
        if (working == null) {
            Log.d(getClass().getName(), "Create new timbre from selected");
            working = new MutableLiveData<>();
            working.setValue(select(timbreId).getValue());
        }
        return this;
    }

    public LiveData<Timbre> getWorking() {
        Log.d(getClass().getName(), "Retrieve working timbre");
        if (working == null) {
            throw new IllegalStateException("Working timbre is null, invoke select before");
        }
        return working;
    }

    public TimbreViewModel saveWorking() {
        if (working == null) {
            throw new IllegalStateException("Unable to save working timbre, working timbre is null, invoke create working before");
        }
        Log.d(getClass().getName(), "Saving timbre " + working.getValue());
        // TODO show notification if save fails
        TimbreDao.getInstance(getApplication().getApplicationContext()).save(working.getValue());
        return this;
    }

    public TimbreViewModel deleteWorking() {
        if (working == null) {
            throw new IllegalStateException("Unable to save working timbre, working timbre is null, invoke create working before");
        }
        Log.d(getClass().getName(), "Deleting timbre " + working.getValue());
        if (working.getValue().getId() != null) {
            // TODO show notification if save fails
            TimbreDao.getInstance(getApplication().getApplicationContext()).delete(working.getValue().getId());
        }
        return this;
    }

}

