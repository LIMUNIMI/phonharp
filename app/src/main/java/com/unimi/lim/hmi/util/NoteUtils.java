package com.unimi.lim.hmi.util;

import com.unimi.lim.hmi.music.Note;

public class NoteUtils {

    private final static double SEMITONE_TO_FREQ_CONST = Math.pow(2, (double) 1 / 12);

    private NoteUtils() {

    }

    /**
     * Return the note given by start note + semitones offset
     *
     * @param startNote start note
     * @param semitones semitone offset
     * @return the note given by start note + semitones offset
     */
    public static Note getNoteByOffset(Note startNote, int semitones) {
        int newOrdinal = startNote.ordinal() + semitones;
        return Note.values()[newOrdinal];
    }

    /**
     * Return the note frequency given by start note frequency + semitone offset
     *
     * @param startNote start note frequency
     * @param semitones semitone offset
     * @return note frequency given by start note + semitone offset
     */
    public static double calculateNoteByOffset(double startNote, float semitones) {
        return startNote * Math.pow(SEMITONE_TO_FREQ_CONST, semitones);
    }

}
