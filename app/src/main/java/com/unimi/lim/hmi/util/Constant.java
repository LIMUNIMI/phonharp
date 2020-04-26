package com.unimi.lim.hmi.util;

import com.unimi.lim.hmi.synthetizer.Synthesizer;

public class Constant {

    private Constant() {
    }

    public static class System {
        private System(){}

        public static final String TIMBRE_FILE_NAME = "timbre-list.json";
    }

    public static class Settings {
        private Settings() {
        }

        // Timbre
        public static final String SELECTED_TIMBRE_ID = "selectedTimbreId";
        public static final String DEFAULT_TIMBRE_ID = "tid0";

        // Keyboard
        public static final String HALF_TONE = "halftone";
        public static final String HANDEDNESS = "handedness";
        public static final String RIGHT_HANDED = "right-handed";
        public static final String LEFT_HANDED = "left-handed";

        // Tuning
        public static final String SCALE_TYPE = "scaleType";
        public static final String NOTE = "note";
        public static final String OCTAVE = "octave";
        public static final String OFFSET = "offset";
    }

    public static class Context {
        private Context() {
        }

        public static final String TIMBRE_ID = "timbreId";
        public static final String IS_NEW_ITEM = "isNewItem";
        public static final String RELOAD_TIMBRE_LIST = "reloadTimbreList";
    }

}
