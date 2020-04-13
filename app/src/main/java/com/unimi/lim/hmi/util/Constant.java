package com.unimi.lim.hmi.util;

public class Constant {

    private Constant() {
    }

    public static class Settings {
        private Settings() {
        }

        public static final String HALF_TONE = "halftone";
        public static final String HANDEDNESS = "handedness";
        public static final String RIGHT_HANDED = "right-handed";
        public static final String LEFT_HANDED = "left-handed";
    }

    public static class Context {
        private Context() {
        }

        public static final String WAVE_FORM = "waveForm";
        public static final String SCALE_TYPE = "scaleType";
        public static final String NOTE = "note";
        public static final String OCTAVE = "octave";
        public static final String OFFSET = "offset";

        public static final String TIMBRE_ID = "timbreId";
        public static final String IS_NEW_ITEM = "isNewItem";
        public static final String RELOAD_TIMBRE_LIST = "reloadTimbreList";
    }

}
