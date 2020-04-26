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

    private boolean itemChanged = false;

    public TimbreViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Timbre>> selectAll() {
        Log.d(getClass().getName(), "Selecting all timbre");
        if (all == null) {
            List<Timbre> timbres = TimbreDao.getInstance(getApplication().getApplicationContext()).selectAll();
            all = new MutableLiveData<>();
            all.setValue(timbres);
        }
        return all;
    }

    public void reloadAll() {
        Log.d(getClass().getName(), "Reloading timbre list");
        if (all != null) {
            List<Timbre> timbres = TimbreDao.getInstance(getApplication().getApplicationContext()).selectAll();
            all.setValue(timbres);
        }
    }

    public LiveData<Timbre> select(String id) {
        Log.d(getClass().getName(), "Select timbre with id " + id);
        if (selected == null) {
            selected = new MutableLiveData<>();
            Timbre timbre = TimbreDao.getInstance(getApplication().getApplicationContext()).selectById(id).orElseThrow(() -> new IllegalArgumentException("Timbre with id " + id + " was not found"));
            selected.setValue(timbre);
        }
        return selected;
    }

    public LiveData<Timbre> create() {
        Log.d(getClass().getName(), "Create new timbre");
        if (selected == null) {
            selected = new MutableLiveData<>();
            selected.setValue(new Timbre());
        }
        return selected;
    }

    public LiveData<Timbre> getSelected() {
        Log.d(getClass().getName(), "Retrieve selected timbre");
        if (selected == null) {
            throw new IllegalStateException("Selected timbre is null, invoke select before");
        }
        return selected;
    }

    public void saveSelected() {
        if (selected == null) {
            throw new IllegalStateException("Unable to save selected timbre, selected timbre is null, invoke select before");
        }
        itemChanged = true;
        Log.d(getClass().getName(), "Saving timbre " + selected.getValue());
        // TODO show notification if save fails
        TimbreDao.getInstance(getApplication().getApplicationContext()).save(selected.getValue());
    }

    public boolean isItemChanged() {
        return itemChanged;
    }

    public void setItemChanged(boolean itemChanged) {
        this.itemChanged = itemChanged;
    }
}

