package com.unimi.lim.hmi.music;

public class Scale {

    public enum Type {
        MAJOR,
        MINOR
    }

    private final Type type;
    private Integer startIdx;

    /**
     * Setup scale instance and calculate initial note array index depending on specified scale note (eg. C4)
     *
     * @param type      major or minor
     * @param startNote scale note
     */
    public Scale(Type type, Note startNote) {
        this.type = type;

        for (int i = 0; i < Note.values().length; i++) {
            if (Note.values()[i] == startNote) {
                startIdx = i;
                break;
            }
        }
        if (startIdx == null) {
            throw new IllegalArgumentException("Unknown start note value [" + startNote + "], must be one of available note frequencies, see Note enum.");
        }
    }

    /**
     * Retrieve the note related to specified note number in current scale.
     *
     * @param noteNum the number of the note on the scale
     * @return return the note related to specified note number in current scale.
     */
    public Note getNote(int noteNum) {
        return getNote(noteNum, 0);
    }

    /**
     * Retrieve the note related to specified note number in current scale. A modifier can be applied to gets notes that are not on the scale (eg. +1 to get half note upper)
     *
     * @param noteNum  the number of the note on the scale
     * @param modifier modifier to be applied on specified note number (eg. +1 to get the half note upper from specified note number)
     * @return return the note related to specified note number in current scale. A modifier can be applied to gets notes that are not on the scale (eg. +1 to get half note upper)
     */
    public Note getNote(int noteNum, int modifier) {
        int octaveNum = noteNum / 7;
        int noteNumInOctave = noteNum % 7;

        int noteIdx = noteNum * 2 - octaveNum * 2;
        if (type == Type.MAJOR) {
            noteIdx -= (noteNumInOctave > 2 ? 1 : 0);
        } else if (type == Type.MINOR) {
            noteIdx -= (noteNumInOctave > 1 ? 1 : 0);
            noteIdx -= (noteNumInOctave > 4 ? 1 : 0);
        }

        // Calculate resulting index; in case of index out of bound always plays the last available note
        int idx = startIdx + noteIdx + modifier;
        idx = idx < Note.values().length ? idx : Note.values().length - 1;
        return Note.values()[idx];
    }

}
