package com.unimi.lim.hmi;

import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.TimbreCfg;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TimbreDaoUnitTest {

    private TimbreDao timbreDao;

    @Before
    public void init() {
        timbreDao = TimbreDao.getInstance();
    }

    @Test
    public void testSaveNew() {
        List<TimbreCfg> timbres = timbreDao.selectAll();
        int count = timbres.size();

        TimbreCfg t = newTimbre();
        timbreDao.save(t);

        timbres = timbreDao.selectAll();
        Assert.assertEquals(count + 1, timbres.size());

        Optional<TimbreCfg> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(selected.isPresent());
        Assert.assertEquals(t.getId(), selected.get().getId());
    }

    @Test
    public void testSaveExisting() {
        TimbreCfg t = newTimbre();
        timbreDao.save(t);

        List<TimbreCfg> timbres = timbreDao.selectAll();
        int count = timbres.size();

        t.setName("CT2");
        timbreDao.save(t);

        timbres = timbreDao.selectAll();
        Assert.assertEquals(count, timbres.size());

        Optional<TimbreCfg> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(selected.isPresent());
        Assert.assertEquals("CT2", selected.get().getName());
    }

    @Test
    public void testSelect() {
        TimbreCfg t = newTimbre();
        timbreDao.save(t);

        Optional<TimbreCfg> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(selected.isPresent());
        Assert.assertEquals(t.getId(), selected.get().getId());
        Assert.assertEquals(t.getName(), selected.get().getName());
    }

    @Test
    public void testDelete() {
        TimbreCfg t = newTimbre();
        timbreDao.save(t);

        List<TimbreCfg> timbres = timbreDao.selectAll();
        int count = timbres.size();

        timbreDao.delete(t.getId());

        timbres = timbreDao.selectAll();
        Assert.assertEquals(count - 1, timbres.size());

        Optional<TimbreCfg> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(!selected.isPresent());
    }

    private TimbreCfg newTimbre() {
        TimbreCfg t = new TimbreCfg();
        t.setId(UUID.randomUUID().toString());
        t.setName("CT1");
        return t;
    }

}
