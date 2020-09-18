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
import java.util.Objects;

public class TimbreViewModel extends AndroidViewModel {

    private MutableLiveData<List<Timbre>> all;
    private MutableLiveData<Timbre> selected;
    private MutableLiveData<Timbre> working;

    public TimbreViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Select all timbre, note that dao is invoked once per activity lifecycle
     *
     * @return all timbre
     */
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

    /**
     * Reload timbre, selectAll must be invoked fist
     */
    public void reloadAll() {
        Log.d(getClass().getName(), "Reloading timbre list");
        if (all != null) {
            // If needed perform select asynchronously
            List<Timbre> timbres = TimbreDao.getInstance(getApplication().getApplicationContext()).selectAll();
            all.setValue(timbres);
        }
    }

    /**
     * Select specified timbre, note that dao is invoked once per activity lifecycle
     *
     * @param id timbre id
     * @return timbre
     */
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

    /**
     * Create working timbre from existing timbre, note that timbre is created once per activity lifecycle
     *
     * @param timbreId timbre id
     */
    public void createWorkingFrom(String timbreId) {
        if (working == null) {
            Log.d(getClass().getName(), "Create new timbre from selected");
            working = new MutableLiveData<>();
            Timbre copy = timbreId != null ? SerializationUtils.clone(select(timbreId).getValue()) : new Timbre();
            working.setValue(copy);
        }
    }

    /**
     * Create working timbre from specified timbre
     *
     * @param timbre
     */
    public void createWorkingFrom(Timbre timbre) {
        if (working == null) {
            Log.d(getClass().getName(), "Create new timbre from selected");
            working = new MutableLiveData<>();
            Timbre copy = timbre != null ? SerializationUtils.clone(timbre) : new Timbre();
            working.setValue(copy);
        }
    }

    /**
     * Retrieve working timbre
     *
     * @return working timbre
     */
    public LiveData<Timbre> getWorking() {
        Log.d(getClass().getName(), "Retrieve working timbre");
        if (working == null) {
            throw new IllegalStateException("Working timbre is null, invoke create working before");
        }
        return working;
    }

    /**
     * Update working timbre, note that working timbre observers will be notified
     *
     * @param timbre working timbre
     */
    public void workingChanged(Timbre timbre) {
        if (working == null) {
            throw new IllegalStateException("Working timbre is null, invoke create working before");
        }
        working.setValue(timbre);
    }

    /**
     * Save working timbre, timbre dao is invoked
     */
    public void saveWorking() {
        if (working == null) {
            throw new IllegalStateException("Unable to save working timbre, working timbre is null, invoke create working before");
        }
        Log.d(getClass().getName(), "Saving timbre " + working.getValue());
        TimbreDao.getInstance(getApplication().getApplicationContext()).save(working.getValue());
    }

    /**
     * Delete working timbre, timbre dao is invoked
     */
    public void deleteWorking() {
        if (working == null) {
            throw new IllegalStateException("Unable to save working timbre, working timbre is null, invoke create working before");
        }
        Log.d(getClass().getName(), "Deleting timbre " + working.getValue());
        if (Objects.requireNonNull(working.getValue()).getId() != null) {
            TimbreDao.getInstance(getApplication().getApplicationContext()).delete(working.getValue().getId());
        }
    }

}

