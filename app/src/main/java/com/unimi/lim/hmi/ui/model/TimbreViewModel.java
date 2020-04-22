package com.unimi.lim.hmi.ui.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.TimbreCfg;

import java.util.List;

public class TimbreViewModel extends ViewModel {

    private MutableLiveData<List<TimbreCfg>> all;
    private MutableLiveData<TimbreCfg> selected;

    private boolean itemChanged = false;

    public LiveData<List<TimbreCfg>> selectAll() {
        Log.d(getClass().getName(), "Selecting all timbre");
        if (all == null) {
            List<TimbreCfg> timbres = TimbreDao.getInstance().selectAll();
            all = new MutableLiveData<>();
            all.setValue(timbres);
        }
        return all;
    }

    public void reloadAll() {
        Log.d(getClass().getName(), "Reloading timbre list");
        if (all != null) {
            List<TimbreCfg> timbres = TimbreDao.getInstance().selectAll();
            all.setValue(timbres);
        }
    }

    public LiveData<TimbreCfg> select(String id) {
        Log.d(getClass().getName(), "Select timbre with id " + id);
        if (selected == null) {
            selected = new MutableLiveData<>();
            TimbreCfg timbre = TimbreDao.getInstance().selectById(id).orElseThrow(() -> new IllegalArgumentException("TimbreCfg with id " + id + " was not found"));
            selected.setValue(timbre);
        }
        return selected;
    }

    public LiveData<TimbreCfg> create() {
        Log.d(getClass().getName(), "Create new timbre");
        if (selected == null) {
            selected = new MutableLiveData<>();
            selected.setValue(new TimbreCfg());
        }
        return selected;
    }

    public LiveData<TimbreCfg> getSelected() {
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
        TimbreDao.getInstance().save(selected.getValue());
    }

    public boolean isItemChanged() {
        return itemChanged;
    }

    public void setItemChanged(boolean itemChanged) {
        this.itemChanged = itemChanged;
    }
}

