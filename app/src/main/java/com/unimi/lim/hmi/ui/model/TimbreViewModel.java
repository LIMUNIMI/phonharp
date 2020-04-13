package com.unimi.lim.hmi.ui.model;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.Timbre;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreViewModel extends ViewModel {

    private MutableLiveData<List<Timbre>> all;
    private MutableLiveData<Timbre> selected;


    public LiveData<List<Timbre>> selectAll() {
        Log.d(getClass().getName(), "Selecting all timbre");
        if (all == null) {
            List<Timbre> timbres = TimbreDao.getInstance().selectAll();
            all = new MutableLiveData<>();
            all.setValue(timbres);
        }
        return all;
    }

    public void reloadAll() {
        Log.d(getClass().getName(), "Reloading timbre list");
        if (all != null) {
            List<Timbre> timbres = TimbreDao.getInstance().selectAll();
            all.setValue(timbres);
        }
    }

    public LiveData<Timbre> select(String id) {
        Log.d(getClass().getName(), "Select timbre with id " + id);
        if (selected == null) {
            selected = new MutableLiveData<>();
            Timbre timbre = TimbreDao.getInstance().selectById(id).orElseThrow(() -> new IllegalArgumentException("Timbre with id " + id + " was not found"));
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
        Log.d(getClass().getName(), "Saving timbre " + selected.getValue());
        TimbreDao.getInstance().save(selected.getValue());
    }

}

