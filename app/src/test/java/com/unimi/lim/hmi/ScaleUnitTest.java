package com.unimi.lim.hmi;

import com.unimi.lim.hmi.music.Note;
import com.unimi.lim.hmi.music.Scale;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScaleUnitTest {

    private Scale majScale;
    private Scale minScale;

    @Before
    public void setup() {
        majScale = new Scale(Scale.Type.MAJOR, Note.C4);
        minScale = new Scale(Scale.Type.MINOR, Note.A3);
    }

    // ************************************************************************
    // MAJOR SCALE

    @Test
    public void majScaleNote0_isCorrect() {
        assertEquals(Note.C4, majScale.getNote(0));
    }

    @Test
    public void majScaleNote05_isCorrect() {
        assertEquals(Note.Cd4, majScale.getNote(0, 1));
    }

    @Test
    public void majScaleNote1_isCorrect() {
        assertEquals(Note.D4, majScale.getNote(1));
    }

    @Test
    public void majScaleNote2_isCorrect() {
        assertEquals(Note.E4, majScale.getNote(2));
    }

    @Test
    public void majScaleNote3_isCorrect() {
        assertEquals(Note.F4, majScale.getNote(3));
    }

    @Test
    public void majScaleNote4_isCorrect() {
        assertEquals(Note.G4, majScale.getNote(4));
    }

    @Test
    public void majScaleNote5_isCorrect() {
        assertEquals(Note.A4, majScale.getNote(5));
    }

    @Test
    public void majScaleNote6_isCorrect() {
        assertEquals(Note.B4, majScale.getNote(6));
    }

    @Test
    public void majScaleNote7_isCorrect() {
        assertEquals(Note.C5, majScale.getNote(7));
    }

    @Test
    public void majScaleNote8_isCorrect() {
        assertEquals(Note.D5, majScale.getNote(8));
    }

    @Test
    public void majScaleNote12_isCorrect() {
        assertEquals(Note.A5, majScale.getNote(12));
    }

    // ************************************************************************
    // MINOR SCALE

    @Test
    public void minScaleNote0_isCorrect() {
        assertEquals(Note.A3, minScale.getNote(0));
    }

    @Test
    public void minScaleNote05_isCorrect() {
        assertEquals(Note.Ad3, minScale.getNote(0, 1));
    }

    @Test
    public void minScaleNote1_isCorrect() {
        assertEquals(Note.B3, minScale.getNote(1));
    }

    @Test
    public void minScaleNote2_isCorrect() {
        assertEquals(Note.C4, minScale.getNote(2));
    }

    @Test
    public void minScaleNote3_isCorrect() {
        assertEquals(Note.D4, minScale.getNote(3));
    }

    @Test
    public void minScaleNote4_isCorrect() {
        assertEquals(Note.E4, minScale.getNote(4));
    }

    @Test
    public void minScaleNote5_isCorrect() {
        assertEquals(Note.F4, minScale.getNote(5));
    }

    @Test
    public void minScaleNote6_isCorrect() {
        assertEquals(Note.G4, minScale.getNote(6));
    }

    @Test
    public void minScaleNote7_isCorrect() {
        assertEquals(Note.A4, minScale.getNote(7));
    }

    @Test
    public void minScaleNote9_isCorrect() {
        assertEquals(Note.C5, minScale.getNote(9));
    }

    @Test
    public void minScaleNote12_isCorrect() {
        assertEquals(Note.F5, minScale.getNote(12));
    }


    // ************************************************************************
    // OTHER

    @Test
    public void outOfBound_isCorrect() {
        assertEquals(Note.B6, minScale.getNote(120));
    }
}