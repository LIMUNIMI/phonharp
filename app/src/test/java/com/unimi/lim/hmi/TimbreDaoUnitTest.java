package com.unimi.lim.hmi;

import android.content.Context;
import android.content.res.AssetManager;

import com.unimi.lim.hmi.dao.TimbreDao;
import com.unimi.lim.hmi.entity.Timbre;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.unimi.lim.hmi.util.Constant.System.TIMBRE_FILE_NAME;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimbreDaoUnitTest {

    private final static String TEST_PATH = "/";
    private final static String TEST_FILE = TEST_PATH + TIMBRE_FILE_NAME;

    private TimbreDao timbreDao;

    @Mock
    Context mockContext;
    @Mock
    AssetManager mockAssetManager;

    @Before
    public void init() throws IOException {
        when(mockContext.getFilesDir()).thenReturn(new File(getClass().getResource(TEST_PATH).getFile()));
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open(TIMBRE_FILE_NAME)).thenReturn(new FileInputStream(getClass().getResource(TEST_FILE).getFile()));
        timbreDao = TimbreDao.getInstance(mockContext);
        timbreDao.deleteAll();
        for (int i = 0; i < 1; i++) {
            Timbre t = new Timbre();
            t.setId("tid" + i);
            timbreDao.save(t);
        }
    }

    @Test
    public void testSaveNew() {
        List<Timbre> timbres = timbreDao.selectAll();
        int count = timbres.size();

        Timbre t = newTimbre();
        timbreDao.save(t);
        timbreDao.reload();

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
        timbreDao.reload();

        List<Timbre> timbres = timbreDao.selectAll();
        int count = timbres.size();

        t.setName("CT2");
        timbreDao.save(t);
        timbreDao.reload();

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
        timbreDao.reload();

        Optional<Timbre> selected = timbreDao.selectById(t.getId());
        Assert.assertTrue(selected.isPresent());
        Assert.assertEquals(t.getId(), selected.get().getId());
        Assert.assertEquals(t.getName(), selected.get().getName());
    }

    @Test
    public void testDelete() {
        Timbre t = newTimbre();
        timbreDao.save(t);
        timbreDao.reload();

        List<Timbre> timbres = timbreDao.selectAll();
        int count = timbres.size();

        timbreDao.delete(t.getId());
        timbreDao.reload();

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
