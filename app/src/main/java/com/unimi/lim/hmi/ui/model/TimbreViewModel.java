package com.unimi.lim.hmi.ui.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.Timbre;

import org.apache.commons.lang3.SerializationUtils;

import java.util.List;

public class TimbreViewModel extends AndroidViewModel {

    private MutableLiveData<List<Timbre>> all;
    private MutableLiveData<Timbre> selected;
    private MutableLiveData<Timbre> working;

    private boolean itemChanged = false;

    public TimbreViewModel(@NonNull Application application) {
        super(application);
    }

    public TimbreViewModel selectAll() {
        Log.d(getClass().getName(), "Selecting all timbre");
        if (all == null) {
            List<Timbre> timbres = TimbreDao.getInstance(getApplication().getApplicationContext()).selectAll();
            all = new MutableLiveData<>();
            all.setValue(timbres);
        }
        return this;
    }

    public TimbreViewModel reloadAll() {
        Log.d(getClass().getName(), "Reloading timbre list");
        if (all != null) {
            List<Timbre> timbres = TimbreDao.getInstance(getApplication().getApplicationContext()).selectAll();
            all.setValue(timbres);
        }
        return this;
    }

    public LiveData<List<Timbre>> getAll() {
        Log.d(getClass().getName(), "Retrieve all timbre");
        if (all == null) {
            throw new IllegalStateException("All timbre is null, invoke selectAll before");
        }
        return all;
    }

    public TimbreViewModel select(String id) {
        Log.d(getClass().getName(), "Select timbre with id " + id);
        if (selected == null) {
            selected = new MutableLiveData<>();
            Timbre timbre = TimbreDao.getInstance(getApplication().getApplicationContext()).selectById(id).orElseGet(() -> {
                Log.d(getClass().getName(), "Unable to find timbre by provided id, create new empty timbre");
                return new Timbre();
            });
            selected.setValue(timbre);
        }
        return this;
    }

    public LiveData<Timbre> getSelected() {
        Log.d(getClass().getName(), "Retrieve selected timbre");
        if (selected == null) {
            throw new IllegalStateException("Selected timbre is null, invoke select before");
        }
        return selected;
    }

    public TimbreViewModel createWorking() {
        if (working != null) {
            throw new IllegalStateException("Working timbre can be created only one time per view model instance");
        }
        Log.d(getClass().getName(), "Create new timbre");
        working = new MutableLiveData<>();
        working.setValue(new Timbre());
        return this;
    }

    public TimbreViewModel createWorkingFromSelected() {
        if (working != null) {
            throw new IllegalStateException("Working timbre can be created only one time per view model instance");
        }
        if (selected == null) {
            throw new IllegalStateException("Selected timbre is null, invoke select before");
        }
        Log.d(getClass().getName(), "Create new timbre from selected");
        Timbre cloned = SerializationUtils.clone(selected.getValue());
        working = new MutableLiveData<>();
        working.setValue(cloned);
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
        itemChanged = true;
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
            itemChanged = true;
            // TODO show notification if save fails
            TimbreDao.getInstance(getApplication().getApplicationContext()).delete(working.getValue().getId());
        }
        return this;
    }

    public boolean isItemChanged() {
        return itemChanged;
    }

    public void setItemChanged(boolean itemChanged) {
        this.itemChanged = itemChanged;
    }

}

