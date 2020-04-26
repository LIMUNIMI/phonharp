package com.unimi.lim.hmi.dao;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unimi.lim.hmi.entity.Timbre;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.unimi.lim.hmi.util.Constant.System.TIMBRE_FILE_NAME;

/**
 * Timbre list data access service.
 */
public class TimbreDao {

    // Singleton instance
    private static TimbreDao timbreDao;

    private final Gson gson;
    private final Optional<Context> applicationContext;
    private final static Type TIMBRE_LIST_TYPE = new TypeToken<ArrayList<Timbre>>() {
    }.getType();

    // In memory timbre list to minimize local storage access
    private List<Timbre> timbres;

    /**
     * Constructor
     *
     * @param applicationContext if present then timbre file is read from application assets or from internal storage. Empty optional can be used for test purposes.
     */
    private TimbreDao(Optional<Context> applicationContext) {
        this.applicationContext = applicationContext;
        this.gson = new Gson();
        if (!applicationContext.isPresent()) {
            Log.w(getClass().getName(), "Application context is not present");
        }
    }

    /**
     * Return timbre dao singleton instance
     *
     * @param applicationContext if present then timbre file is read from application assets or from internal storage. Empty optional can be used for test purposes.
     * @return singleton instance
     */
    public static synchronized TimbreDao getInstance(Optional<Context> applicationContext) {
        if (timbreDao == null) {
            timbreDao = new TimbreDao(applicationContext);
            timbreDao.reload();
        }
        return timbreDao;
    }

    /**
     * Select all timbres from memory
     *
     * @return all timbres
     */
    public synchronized List<Timbre> selectAll() {
        Log.d(getClass().getName(), "SelectAll");
        return timbres.stream().collect(Collectors.toList());
    }

    /**
     * Select timbre by timbre id
     *
     * @param id timbre id
     * @return selected timbre or empty optional if timbre with provided was not found
     */
    public synchronized Optional<Timbre> selectById(String id) {
        Log.d(getClass().getName(), "SelectById " + id);
        return timbres.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst();
    }

    /**
     * Save provided timbre to timbre list
     *
     * @param timbre timbre to be saved
     * @return true if save succeeded, false otherwise
     */
    public synchronized boolean save(Timbre timbre) {
        if (timbre == null) {
            Log.w(getClass().getName(), "Save invoked with null timbre");
            return false;
        }
        if (StringUtils.isEmpty(timbre.getId())) {
            timbre.setId(UUID.randomUUID().toString());
        } else {
            timbres.removeIf(t -> t.getId().equalsIgnoreCase(timbre.getId()));
        }
        Log.d(getClass().getName(), "Saving timbre " + timbre);
        timbres.add(timbre);
        return store();
    }

    /**
     * Delete timbre by id
     *
     * @param id timbre to be deleted
     * @return true if delete succeeded, false otherwise
     */
    public synchronized boolean delete(String id) {
        Log.d(getClass().getName(), "Deleting timbre with id " + id);
        boolean found = timbres.removeIf(t -> t.getId().equalsIgnoreCase(id));
        if (found) {
            return store();
        } else {
            Log.w(getClass().getName(), "Unable to delete timbre with id " + id + ", specified id not found on timbre list");
            return false;
        }
    }

    /**
     * Delete all timbre
     *
     * @return true if delete succeeded, false otherwise
     */
    public synchronized boolean deleteAll() {
        Log.d(getClass().getName(), "Deleting all timbre");
        timbres = new ArrayList<>();
        return store();
    }

    /**
     * Reload timbre list from local storage
     *
     * @return true if reload succeeded, false otherwise
     */
    public synchronized boolean reload() {
        Log.d(getClass().getName(), "Reloading timbres from local storage");
        try (FileReader reader = getFileReader()) {
            timbres = gson.fromJson(reader, TIMBRE_LIST_TYPE);
            return true;
        } catch (IOException e) {
            Log.e(getClass().getName(), "Unable to load timbre file from disk", e);
            timbres = new ArrayList<>();
            return false;
        }
    }

    /**
     * Store timbre list to internal storage
     *
     * @return true if store succeeded, false otherwise
     */
    private boolean store() {
        Log.d(getClass().getName(), "Saving timbres to local storage");
        try (FileWriter writer = getFileWriter()) {
            gson.toJson(timbres, TIMBRE_LIST_TYPE, writer);
            return true;
        } catch (IOException e) {
            Log.e(getClass().getName(), "Unable to save timbre file to disk", e);
            return false;
        }
    }

    /**
     * Retrieve timbre list file writer
     *
     * @return timbre list file writer
     * @throws IOException
     */
    private FileWriter getFileWriter() throws IOException {
        if (!applicationContext.isPresent()) {
            // This branch should be taken only during tests
            return new FileWriter(TIMBRE_FILE_NAME);
        }
        return new FileWriter(new File(applicationContext.get().getFilesDir(), TIMBRE_FILE_NAME));
    }

    /**
     * Retrieve timbre list file reader.
     *
     * @return timbre list file reader
     * @throws IOException
     */
    private FileReader getFileReader() throws IOException {
        if (!applicationContext.isPresent()) {
            // This branch should be taken only during tests
            return new FileReader(TIMBRE_FILE_NAME);
        }

        // Try to open timbre file from internal storage.
        // On the first invocation (after application installation) timbre file is copied from
        // assets directory to internal storage directory.
        FileReader fileReader;
        try {
            fileReader = new FileReader(new File(applicationContext.get().getFilesDir(), TIMBRE_FILE_NAME));
        } catch (FileNotFoundException e) {
            // Try to open timbre file from assets directory
            Log.i(getClass().getName(), "Timbre list file not found on local storage, copy from asset");
            InputStream initialTimbreList = applicationContext.get().getAssets().open(TIMBRE_FILE_NAME);
            timbres = gson.fromJson(new InputStreamReader(initialTimbreList), TIMBRE_LIST_TYPE);
            // Store timbre list to internal storage directory
            store();
            fileReader = new FileReader(new File(applicationContext.get().getFilesDir(), TIMBRE_FILE_NAME));
        }
        return fileReader;
    }

}
