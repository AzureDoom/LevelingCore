package com.azuredoom.levelingcore.utils;

public final class PartyCompatUtil {

    private PartyCompatUtil() {}

    public static long safeRoundToLong(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value))
            return 0L;
        if (value <= Long.MIN_VALUE)
            return Long.MIN_VALUE;
        if (value >= Long.MAX_VALUE)
            return Long.MAX_VALUE;
        return Math.round(value);
    }
}
