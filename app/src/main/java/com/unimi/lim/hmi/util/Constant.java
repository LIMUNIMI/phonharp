package com.unimi.lim.hmi.util;

@SuppressWarnings("unused")
public class Constant {

    private Constant() {
    }

    public static class System {
        private System() {
        }

        public static final String APP_HOST = "http://phoneharp.unimi.it";
        public static final String APP_SHARE_URL = APP_HOST + "/share";
        public static final String TIMBRE_FILE_NAME = "timbre-list.json";
    }

    public static class Settings {
        private Settings() {
        }

        // Timbre
        public static final String SELECTED_TIMBRE_ID = "selectedTimbreId";
        public static final String DEFAULT_TIMBRE_ID = "04b1be44-78b0-4a8a-84c5-12be11027e51";

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
    }

}
