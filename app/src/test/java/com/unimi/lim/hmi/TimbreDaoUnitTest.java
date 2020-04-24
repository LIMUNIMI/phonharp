package com.unimi.lim.hmi;

import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.Timbre;

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
        List<Timbre> timbres = timbreDao.selectAll();
        int count = timbres.size();

        Timbre t = newTimbre();
        timbreDao.save(t);

        timbres = timbreDao.selectAll();
        Assert.assertEquals(count + 1, timbres.size());

        Optional<Timbre> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(selected.isPresent());
        Assert.assertEquals(t.getId(), selected.get().getId());
    }

    @Test
    public void testSaveExisting() {
        Timbre t = newTimbre();
        timbreDao.save(t);

        List<Timbre> timbres = timbreDao.selectAll();
        int count = timbres.size();

        t.setName("CT2");
        timbreDao.save(t);

        timbres = timbreDao.selectAll();
        Assert.assertEquals(count, timbres.size());

        Optional<Timbre> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(selected.isPresent());
        Assert.assertEquals("CT2", selected.get().getName());
    }

    @Test
    public void testSelect() {
        Timbre t = newTimbre();
        timbreDao.save(t);

        Optional<Timbre> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(selected.isPresent());
        Assert.assertEquals(t.getId(), selected.get().getId());
        Assert.assertEquals(t.getName(), selected.get().getName());
    }

    @Test
    public void testDelete() {
        Timbre t = newTimbre();
        timbreDao.save(t);

        List<Timbre> timbres = timbreDao.selectAll();
        int count = timbres.size();

        timbreDao.delete(t.getId());

        timbres = timbreDao.selectAll();
        Assert.assertEquals(count - 1, timbres.size());

        Optional<Timbre> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(!selected.isPresent());
    }

    private Timbre newTimbre() {
        Timbre t = new Timbre();
        t.setId(UUID.randomUUID().toString());
        t.setName("CT1");
        return t;
    }

}
