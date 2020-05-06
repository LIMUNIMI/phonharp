package com.unimi.lim.hmi.util;

import com.unimi.lim.hmi.music.Note;

public class NoteUtils {

    private final static double CONST = Math.pow(2, (double) 1 / 12);

    private NoteUtils() {

    }

    public static Note getNoteByOffset(Note startNote, int semitones) {
        int newOrdinal = startNote.ordinal() + semitones;
        return Note.values()[newOrdinal];
    }

    public static double calculateNoteByOffset(double startNote, float semitones) {
        return startNote * Math.pow(CONST, semitones);
    }

}
