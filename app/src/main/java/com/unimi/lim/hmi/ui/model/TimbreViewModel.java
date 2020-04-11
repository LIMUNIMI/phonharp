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
        if (all == null) {
            Log.d(getClass().getName(), "Select all timbre");
            List<Timbre> timbres = TimbreDao.getInstance().selectAll();
            all = new MutableLiveData<>();
            all.setValue(timbres);
        }
        return all;
    }

    public LiveData<Timbre> select(String id) {
        if (selected == null) {
            Log.d(getClass().getName(), "Select timbre with id " + id);
            Timbre timbre = TimbreDao.getInstance().selectById(id).orElse(new Timbre());
            selected = new MutableLiveData<>();
            selected.setValue(timbre);
        }
        return selected;
    }

}

