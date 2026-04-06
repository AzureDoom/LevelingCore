package com.azuredoom.levelingcore.utils;

public final class PartyCompatUtil {

    private PartyCompatUtil() {}

    /**
     * Safely rounds a double value to a long value, ensuring the result stays within the bounds of the {@code long}
     * data type. If the input value is NaN or infinite, it returns 0. If the input value exceeds the bounds of
     * {@code long}, it caps the result to {@code Long.MIN_VALUE} or {@code Long.MAX_VALUE} accordingly.
     *
     * @param value the double value to be safely rounded to a long
     * @return the resulting long value, capped or rounded as necessary
     */
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
