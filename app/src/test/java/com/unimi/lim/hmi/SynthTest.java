package com.unimi.lim.hmi;

import com.unimi.lim.hmi.entity.Timbre;
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
        timbre.setVolume(1);
        buildAndStart(timbre);
        play();

        timbre.setVolume(0.25f);
        synth.updateTimbreCfg(timbre);
        play();
    }

    @Test
    public void testHarmonics() {
        buildAndStart(timbre);
        timbre.setHarmonics(0.0f);
        synth.updateTimbreCfg(timbre);
        play();

        timbre.setHarmonics(0.45f);
        synth.updateTimbreCfg(timbre);
        play();

        timbre.setHarmonics(0.9f);
        synth.updateTimbreCfg(timbre);
        play();

    }

    @Test
    public void testTremolo() {
        timbre.setTremolo(new Timbre.Lfo(6, 75));
        buildAndStart(timbre);
        play();
        // Changes tremolo config on the fly
        timbre.setTremolo(new Timbre.Lfo(20, 50));
        synth.updateTimbreCfg(timbre);
        play();
    }

    @Test
    public void testVibrato() {
        timbre.setVibrato(new Timbre.Lfo(6, 100));
        buildAndStart(timbre);
        play();
        // Changes vibrato config on the fly
        timbre.setVibrato(new Timbre.Lfo(20, 100));
        synth.updateTimbreCfg(timbre);
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
        timbre.setPitchAsr(new Timbre.Asr(-50, -50, 1, 1));
        buildAndStart(timbre);
        play();
    }

    @Test
    public void testHarmonicsEnvelop() {
        timbre.setVolumeAsr(new Timbre.Asr(1, 0, 0, 1));
        timbre.setHarmonicsAsr(new Timbre.Asr(-0.5f, -0.5f, 1, 1));
        buildAndStart(timbre);
        play();
    }

    @Test
    public void testStaccato() {
        timbre.setVolumeAsr(new Timbre.Asr(0, 0, 0.15f, 0.15f));
        timbre.setPitchAsr(new Timbre.Asr(-50, -50, 0.15f, 0.15f));
        timbre.setHarmonicsAsr(new Timbre.Asr(-0.9f, -0.9f, 0.15f, 0.15f));
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
        timbre.setHarmonicsAsr(new Timbre.Asr(-0.9f, -0.9f, 0.15f, 0.15f));
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
