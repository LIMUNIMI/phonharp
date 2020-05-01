package com.unimi.lim.hmi.util;

import com.unimi.lim.hmi.entity.Timbre;

public class TimbreUtils {

    private final static String SEPARATOR = " ";
    private final static String VOLUME = "VL";
    private final static String HARMONICS = "HR";
    private final static String SWIPE_HORIZONTAL = "SWH";
    private final static String SWIPE_VERTICAL = "SWV";
    private final static String VIBRATO = "VB";
    private final static String TREMOLO = "TR";
    private final static String VOL_ENV = "VENV";
    private final static String PITCH_ENV = "PENV";
    private final static String HARM_ENV = "HENV";


    private TimbreUtils() {
    }

    public static float safeLfoRate(Timbre.Lfo lfo) {
        return safeLfoRate(lfo, 0);
    }

    public static float safeLfoRate(Timbre.Lfo lfo, float defaultValue) {
        return lfo == null ? defaultValue : lfo.getRate();
    }

    public static int safeLfoDepth(Timbre.Lfo lfo) {
        return safeLfoDepth(lfo, 0);
    }

    public static int safeLfoDepth(Timbre.Lfo lfo, int defaultValue) {
        return lfo == null ? defaultValue : lfo.getDepth();
    }

    public static float safeAsrInitialValue(Timbre.Asr asr) {
        return safeAsrInitialValue(asr, 0);
    }

    public static float safeAsrInitialValue(Timbre.Asr asr, float defaultValue) {
        return asr == null ? defaultValue : asr.getInitialValue();
    }

    public static float safeAsrFinalValue(Timbre.Asr asr) {
        return safeAsrFinalValue(asr, 0);
    }

    public static float safeAsrFinalValue(Timbre.Asr asr, float defaultValue) {
        return asr == null ? defaultValue : asr.getFinalValue();
    }

    public static float safeAsrAttackTime(Timbre.Asr asr) {
        return safeAsrAttackTime(asr, 0);
    }

    public static float safeAsrAttackTime(Timbre.Asr asr, float defaultValue) {
        return asr == null ? defaultValue : asr.getAttackTime();
    }

    public static float safeAsrReleaseTime(Timbre.Asr asr) {
        return safeAsrReleaseTime(asr, 0);
    }

    public static float safeAsrReleaseTime(Timbre.Asr asr, float defaultValue) {
        return asr == null ? defaultValue : asr.getReleaseTime();
    }

    public static float maxAsrAttackTime(Timbre timbre) {
        return Math.max(safeAsrAttackTime(timbre.getHarmonicsAsr()), Math.max(safeAsrAttackTime(timbre.getVolumeAsr()), safeAsrAttackTime(timbre.getPitchAsr())));
    }

    public static float maxAsrReleaseTime(Timbre timbre) {
        return Math.max(safeAsrReleaseTime(timbre.getHarmonicsAsr()), Math.max(safeAsrReleaseTime(timbre.getVolumeAsr()), safeAsrReleaseTime(timbre.getPitchAsr())));
    }

    public static String buildDescription(Timbre timbre) {
        StringBuilder builder = new StringBuilder();
        builder.append(VOLUME).append(timbre.getVolume()).append(SEPARATOR)
                .append(HARMONICS).append(timbre.getHarmonics()).append(SEPARATOR);
        addControllerDescription(builder, SWIPE_HORIZONTAL, timbre.getController1());
        addControllerDescription(builder, SWIPE_VERTICAL, timbre.getController2());
        addLfoDescription(builder, VIBRATO, timbre.getVibrato());
        addLfoDescription(builder, TREMOLO, timbre.getTremolo());
        addAsrDescription(builder, VOL_ENV, timbre.getVolumeAsr());
        addAsrDescription(builder, PITCH_ENV, timbre.getPitchAsr());
        addAsrDescription(builder, HARM_ENV, timbre.getHarmonicsAsr());
        return builder.toString();
    }

    private static void addControllerDescription(StringBuilder builder, String ctrlName, Timbre.Controller ctrl) {
        if (ctrl == null) {
            return;
        }
        builder.append(ctrlName).append(SEPARATOR);
    }

    private static void addLfoDescription(StringBuilder builder, String lfoName, Timbre.Lfo lfo) {
        if (lfo == null) {
            return;
        }
        builder.append(lfoName).append(SEPARATOR);
    }

    private static void addAsrDescription(StringBuilder builder, String asrName, Timbre.Asr asr) {
        if (asr == null) {
            return;
        }
        builder.append(asrName).append(SEPARATOR);
    }
}
