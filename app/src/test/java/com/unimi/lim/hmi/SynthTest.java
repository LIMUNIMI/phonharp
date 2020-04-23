package com.unimi.lim.hmi;

import com.unimi.lim.hmi.entity.TimbreCfg;
import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.Scale;
import com.unimi.lim.hmi.synthetizer.Synthesizer;
import com.unimi.lim.hmi.synthetizer.jsyn.JsynSynthesizer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SynthTest {

    private static final Note NOTE = Note.C3;
    private static final long TIME_PLAY = 2000;
    private static final long TIME_RELEASE = 1000;

    private Synthesizer synth;
    private TimbreCfg cfg;

    @Before
    public void init() {
        cfg = new TimbreCfg();
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
        cfg.setVolume(1);
        buildAndStart(cfg);
        play();

        cfg.setVolume(0.25f);
        synth.updateTimbreCfg(cfg);
        play();
    }

    @Test
    public void testHarmonics() {
        buildAndStart(cfg);
        cfg.setHarmonics(0.0f);
        synth.updateTimbreCfg(cfg);
        play();

        cfg.setHarmonics(0.45f);
        synth.updateTimbreCfg(cfg);
        play();

        cfg.setHarmonics(0.9f);
        synth.updateTimbreCfg(cfg);
        play();

    }

    @Test
    public void testTremolo() {
        cfg.setTremolo(new TimbreCfg.LfoCfg(6, 75));
        buildAndStart(cfg);
        play();
        // Changes tremolo config on the fly
        cfg.setTremolo(new TimbreCfg.LfoCfg(20, 50));
        synth.updateTimbreCfg(cfg);
        play();
    }

    @Test
    public void testVibrato() {
        cfg.setVibrato(new TimbreCfg.LfoCfg(6, 100));
        buildAndStart(cfg);
        play();
        // Changes vibrato config on the fly
        cfg.setVibrato(new TimbreCfg.LfoCfg(20, 100));
        synth.updateTimbreCfg(cfg);
        play();
    }

    @Test
    public void testVolumeEnvelop() {
        cfg.setVolumeEnv(new TimbreCfg.EnvelopCfg(0, 0, 1, 1));
        buildAndStart(cfg);
        play();
    }

    @Test
    public void testPitchEnvelop() {
        cfg.setVolumeEnv(new TimbreCfg.EnvelopCfg(1, 0, 0, 1));
        cfg.setPitchEnv(new TimbreCfg.EnvelopCfg(-50, -50, 1, 1));
        buildAndStart(cfg);
        play();
    }

    @Test
    public void testHarmonicsEnvelop() {
        cfg.setVolumeEnv(new TimbreCfg.EnvelopCfg(1, 0, 0, 1));
        cfg.setHarmonicsEnv(new TimbreCfg.EnvelopCfg(-0.5f, -0.5f, 1, 1));
        buildAndStart(cfg);
        play();
    }

    @Test
    public void testStaccato() {
        cfg.setVolumeEnv(new TimbreCfg.EnvelopCfg(0, 0, 0.15f, 0.15f));
        cfg.setPitchEnv(new TimbreCfg.EnvelopCfg(-50, -50, 0.15f, 0.15f));
        cfg.setHarmonicsEnv(new TimbreCfg.EnvelopCfg(-0.9f, -0.9f, 0.15f, 0.15f));
        buildAndStart(cfg);

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
        cfg.setVolumeEnv(new TimbreCfg.EnvelopCfg(0, 0, 0.15f, 0.15f));
        cfg.setPitchEnv(new TimbreCfg.EnvelopCfg(-50, -50, 0.15f, 0.15f));
        cfg.setHarmonicsEnv(new TimbreCfg.EnvelopCfg(-0.9f, -0.9f, 0.15f, 0.15f));
        buildAndStart(cfg);

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

    private void buildAndStart(TimbreCfg cfg) {
        synth = new JsynSynthesizer
                .Builder()
                .timbreCfg(cfg)
                .build();
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


}
