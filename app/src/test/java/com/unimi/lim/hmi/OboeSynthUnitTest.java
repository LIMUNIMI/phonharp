package com.unimi.lim.hmi;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unimi.lim.hmi.entity.Timbre;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.OboeSynth;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.jsyn.JsynSynthesizer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class OboeSynthUnitTest {

    private static final Note NOTE = Note.C3;
    private static final long TIME_PLAY = 2000;
    private static final long TIME_RELEASE = 1000;

    private Synthesizer synth;
    private Timbre timbre;

    @Before
    public void init() {
        timbre = new Timbre();
    }

    @After
    public void shutDown() {
        synth.stop();
    }

    @Test
    public void testDefault() {
        buildAndStart(null);
        play();
    }

    @Test
    public void testVolume() {
        timbre.setVolume(100);
        buildAndStart(timbre);
        play();

        timbre.setVolume(50);
        synth.updateSynthesizerCfg(timbre);
        play();

        timbre.setVolume(10);
        synth.updateSynthesizerCfg(timbre);
        play();
    }

    @Test
    public void testHarmonics() {
        buildAndStart(timbre);
        timbre.setHarmonics(0);
        synth.updateSynthesizerCfg(timbre);
        play();

        timbre.setHarmonics(45);
        synth.updateSynthesizerCfg(timbre);
        play();

        timbre.setHarmonics(90);
        synth.updateSynthesizerCfg(timbre);
        play();

    }

    @Test
    public void testTremolo() {
        timbre.setTremolo(new Timbre.Lfo(6, 75));
        buildAndStart(timbre);
        play();
        // Changes tremolo config on the fly
        timbre.setTremolo(new Timbre.Lfo(20, 50));
        synth.updateSynthesizerCfg(timbre);
        play();
    }

    @Test
    public void testVibrato() {
        buildAndStart(timbre);
        for (int i = 1; i <= 3; i++) {
            int depth = i * 30;
            System.out.println("Depth " + depth + "%");
            timbre.setVibrato(new Timbre.Lfo(6, depth));
            synth.updateSynthesizerCfg(timbre);
            play();
        }
    }

    @Test
    public void testPwm() {
        timbre.setHarmonics(100);
        timbre.setPwm(new Timbre.Lfo(5, 10));
        buildAndStart(timbre);
        play();
        // Changes pwm config on the fly
        timbre.setHarmonics(100);
        timbre.setPwm(new Timbre.Lfo(15, 10));
        synth.updateSynthesizerCfg(timbre);
        play();
    }

    @Test
    public void testVolumeEnvelop() {
        timbre.setVolumeAsr(new Timbre.Asr(0, 0, 1, 1));
        buildAndStart(timbre);
        play();
    }

    @Test
    public void testPitchEnvelop() {
        timbre.setVolumeAsr(new Timbre.Asr(1, 0, 0, 1));
        timbre.setPitchAsr(new Timbre.Asr(-3, -3, 0.5f, 0.5f));
        buildAndStart(timbre);
        play();
    }

    @Test
    public void testHarmonicsEnvelop() {
        timbre.setVolumeAsr(new Timbre.Asr(1, 0, 0, 1));
        timbre.setHarmonicsAsr(new Timbre.Asr(-50, -50, 0.5f, 0.5f));
        buildAndStart(timbre);
        play();
    }

    @Test
    public void testEq() {
        timbre.setHarmonics(100);
        timbre.setEqualizer(new Timbre.Equalizer(0, 0));
        buildAndStart(timbre);

        System.out.println("Clean");
        play();

        System.out.println("Low gain");
        timbre.getEqualizer().setLowShelfGain(15);
        timbre.getEqualizer().setHighShelfGain(0);
        synth.updateSynthesizerCfg(timbre);
        play();

        System.out.println("High gain");
        timbre.getEqualizer().setLowShelfGain(0);
        timbre.getEqualizer().setHighShelfGain(15);
        synth.updateSynthesizerCfg(timbre);
        play();
    }

    @Test
    public void testStaccato() {
        timbre.setVolumeAsr(new Timbre.Asr(0, 0, 0.15f, 0.15f));
        timbre.setPitchAsr(new Timbre.Asr(-50, -50, 0.15f, 0.15f));
        timbre.setHarmonicsAsr(new Timbre.Asr(-3, -3, 0.15f, 0.15f));
        buildAndStart(timbre);

        sleep(500);

        Scale c3m = new Scale(Scale.Type.MAJOR, Note.C3);
        for (int i = 0; i < 8; i++) {
            System.out.println("Playing note " + c3m.getNote(i));
            synth.press(c3m.getNote(i).getFrequency());
            sleep(500);
            System.out.println("Release...");
            synth.release();
            sleep(200);
        }
        System.out.println("Stop.");
    }

    @Test
    public void testLegato() {
        timbre.setVolumeAsr(new Timbre.Asr(0, 0, 0.15f, 0.15f));
        timbre.setPitchAsr(new Timbre.Asr(-50, -50, 0.15f, 0.15f));
        timbre.setHarmonicsAsr(new Timbre.Asr(-3, -3, 0.15f, 0.15f));
        buildAndStart(timbre);

        sleep(500);

        Scale c3m = new Scale(Scale.Type.MAJOR, Note.C3);
        for (int i = 0; i < 8; i++) {
            System.out.println("Playing note " + c3m.getNote(i));
            synth.press(c3m.getNote(i).getFrequency());
            sleep(500);
            sleep(200);
        }
        System.out.println("Release...");
        synth.release();
        sleep(200);
        System.out.println("Stop.");
    }

    private void buildAndStart(Timbre cfg) {
        synth = new OboeSynth(this.getContext(), cfg);
        synth.start();
    }

    private void play() {
        System.out.println("Press...");
        synth.press(NOTE.getFrequency());
        sleep(TIME_PLAY);
        System.out.println("Release...");
        synth.release();
        sleep(TIME_RELEASE);
        System.out.println("Stop.");
    }

    private void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Context getContext(){
        //        return ApplicationProvider.getApplicationContext();
        return null;
    }

}
