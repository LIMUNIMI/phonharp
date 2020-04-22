package com.unimi.lim.hmi.dao;

import android.util.Log;

import com.unimi.lim.hmi.entity.TimbreCfg;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TimbreDao {

    private static TimbreDao timbreDao;
    private List<TimbreCfg> timbres;

    private TimbreDao() {
    }

    public static synchronized TimbreDao getInstance() {
        if (timbreDao == null) {
            timbreDao = new TimbreDao();
            timbreDao.reload();
        }
        return timbreDao;
    }

    public synchronized List<TimbreCfg> selectAll() {
        Log.d(getClass().getName(), "SelectAll");
        return timbres.stream().collect(Collectors.toList());
    }

    public synchronized Optional<TimbreCfg> selectById(String id) {
        Log.d(getClass().getName(), "SelectById " + id);
        return timbres.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst();
    }

    public synchronized void save(TimbreCfg timbre) {
        if (timbre == null) {
            return;
        }
        if (StringUtils.isEmpty(timbre.getId())) {
            timbre.setId(UUID.randomUUID().toString());
        } else {
            timbres.removeIf(t -> t.getId().equalsIgnoreCase(timbre.getId()));
        }
        Log.d(getClass().getName(), "Saving timbre " + timbre);
        timbres.add(timbre);
        // TODO save to json
    }

    public synchronized void delete(String id) {
        Log.d(getClass().getName(), "Deleting timbre with id " + id);
        boolean removed = timbres.removeIf(t -> t.getId().equalsIgnoreCase(id));
        if (removed) {
            // TODO save to json
        }
    }

    private void reload() {
        Log.d(getClass().getName(), "Reload");
        // TODO load from json
        timbres = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            TimbreCfg t = new TimbreCfg();
            t.setId(i + "");
            t.setName("TimbreCfg " + i);
            timbres.add(t);
        }
    }

}
