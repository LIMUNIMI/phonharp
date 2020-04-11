package com.unimi.lim.hmi.dao;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.unimi.lim.hmi.entity.Timbre;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimbreDao {

    private static TimbreDao timbreDao;
    private List<Timbre> timbres;

    private TimbreDao() {
    }

    public static synchronized TimbreDao getInstance() {
        if (timbreDao == null) {
            timbreDao = new TimbreDao();
            timbreDao.reload();
        }
        return timbreDao;
    }

    public synchronized List<Timbre> selectAll() {
        return timbres.stream().collect(Collectors.toList());
    }

    public synchronized Optional<Timbre> selectById(String id) {
        return timbres.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst();
    }

    public synchronized void save(Timbre timbre) {
        if (timbre == null) {
            return;
        }
        if (StringUtils.isEmpty(timbre.getId())) {
            timbre.setId(UUID.randomUUID().toString());
        } else {
            timbres.removeIf(t -> t.getId().equalsIgnoreCase(timbre.getId()));
        }
        timbres.add(timbre);
        // TODO save to json
    }

    public synchronized void delete(String id) {
        boolean removed = timbres.removeIf(t -> t.getId().equalsIgnoreCase(id));
        if (removed) {
            // TODO save to json
        }
    }

    private void reload() {
        // TODO load from json
        timbres = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Timbre t = new Timbre();
            t.setId("" + i);
            t.setContent("Content " + i);
            t.setDetails("Details " + i);
            timbres.add(t);
        }
    }

}
