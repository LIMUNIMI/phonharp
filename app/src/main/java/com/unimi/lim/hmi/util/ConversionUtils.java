package com.unimi.lim.hmi.util;

public class ConversionUtils {

    private ConversionUtils() {
    }

    /**
     * Convert decibel value to absolute value, abs = 10^(dB/10)
     *
     * @param dB decibel value
     * @return absolute value
     */
    public static double dBtoAbsoluteValue(int dB) {
        return Math.pow(10, (float) dB / 10);
    }

    /**
     * Convert absolute value to decibel value, dB = 10*Log(abs)
     *
     * @param abs absolute value
     * @return decibel value
     */
    public static int absoluteValueTodB(double abs) {
        return (int) (10 * Math.log10(abs));
    }

    /**
     * Convert percentage value to decimal value
     *
     * @param percentage percentave value
     * @return percentage / 100
     */
    public static float percentageToDecimal(float percentage) {
        return percentage / 100;
    }

    /**
     * Convert seconds to milliseconds
     *
     * @param seconds seconds value
     * @return milliseconds value
     */
    public static long secondsToMillis(float seconds) {
        return (long) (seconds * 1000);
    }

}
