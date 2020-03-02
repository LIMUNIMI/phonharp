package com.unimi.lim.hmi.music;

public class Scale {

    public enum Type {
        MAJOR,
        MINOR
    }

    private Type type;
    private Integer startIdx;

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

    public Note getNote(int noteNum) {
        int octaveNum = noteNum / 7;
        int noteNumInOctave = noteNum % 7;

        int noteIdx = noteNum * 2 - octaveNum * 2;
        if (type == Type.MAJOR) {
            noteIdx -= (noteNumInOctave > 2 ? 1 : 0);
        } else if (type == Type.MINOR) {
            noteIdx -= (noteNumInOctave > 1 ? 1 : 0);
            noteIdx -= (noteNumInOctave > 4 ? 1 : 0);
        }

        // FIXME checks for out of bound
        return Note.values()[startIdx + noteIdx];
    }

}
