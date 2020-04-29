package com.unimi.lim.hmi;

import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.util.NoteUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class NoteUtilsUnitTest {

    private final static double FUZZ_FACTOR = 0.01;

    @Test
    public void testCalculateNoteByOffset_zero() {
        double actual = NoteUtils.calculateNoteByOffset(Note.C4.getFrequency(), 0);
        Assert.assertEquals(Note.C4.getFrequency(), actual, FUZZ_FACTOR);
    }

    @Test
    public void testCalculateNoteByOffset_plusOne() {
        double actual = NoteUtils.calculateNoteByOffset(Note.C4.getFrequency(), 1);
        Assert.assertEquals(Note.Cd4.getFrequency(), actual, FUZZ_FACTOR);
    }

    @Test
    public void testCalculateNoteByOffset_minusOne() {
        double actual = NoteUtils.calculateNoteByOffset(Note.C4.getFrequency(), -1);
        Assert.assertEquals(Note.B3.getFrequency(), actual, FUZZ_FACTOR);
    }

    @Test
    public void testGetNoteByOffset_plusOne() {
        Note actual = NoteUtils.getNoteByOffset(Note.C4, 1);
        Assert.assertEquals(Note.Cd4, actual);
    }

    @Test
    public void testGetNoteByOffset_minusOne() {
        Note actual = NoteUtils.getNoteByOffset(Note.C4, -1);
        Assert.assertEquals(Note.B3, actual);
    }

    @Test
    public void testPerformance() {
        Random r = new Random();
        int n = 1000000;

        // Prepare data
        Note[] data = new Note[n];
        for (int i = 0; i < n; i++) {
            data[i] = Note.values()[r.nextInt(Note.values().length - 2)];
        }

        // getNoteByOffset
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            NoteUtils.getNoteByOffset(data[i], 1);
        }
        System.out.println("getNoteByOffset " + (System.currentTimeMillis() - start));

        // calculateNoteByOffset
        start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            NoteUtils.calculateNoteByOffset(data[i].getFrequency(), 1);
        }
        System.out.println("calculateNoteByOffset " + (System.currentTimeMillis() - start));
    }


}
