package com.unimi.lim.hmi.ui.model;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModel;

import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.Timbre;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreDetailViewModel extends ViewModel {

    private Timbre timbre;

    public void selectTimbre(String id) {
        this.timbre = TimbreDao.getInstance().selectById(id).orElse(new Timbre());
    }

    public String getTimbreId() {
        return timbre.getId();
    }

    public String getTimbreContent() {
        return timbre.getContent();
    }

    public String getTimbreDetails() {
        return timbre.getDetails();
    }

    public void setTimbreContent(String content) {
        timbre.setContent(content);
    }
}

