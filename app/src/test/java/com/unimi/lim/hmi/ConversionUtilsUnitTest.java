package com.unimi.lim.hmi;

import com.unimi.lim.hmi.util.ConversionUtils;

import org.junit.Assert;
import org.junit.Test;

public class ConversionUtilsUnitTest {

    private final static double absValue[] = {0.001, 0.01, 0.1, 1, 10, 100, 1000};
    private final static int dBValue[] = {-30, -20, -10, 0, 10, 20, 30};

    @Test
    public void testDbToAbsolutValue() {
        for (int i = 0; i < dBValue.length; i++) {
            double abs = ConversionUtils.dBtoAbsoluteValue(dBValue[i]);
            Assert.assertEquals(absValue[i], abs, 0.0001);
        }
    }

    @Test
    public void testAbsoluteValueToDb() {
        for (int i = 0; i < absValue.length; i++) {
            int dB = ConversionUtils.absoluteValueTodB(absValue[i]);
            Assert.assertEquals(dBValue[i], dB);
        }
    }
}
