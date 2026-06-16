package com.azuredoom.levelingcore.level.formulas;

import com.azuredoom.levelingcore.exceptions.LevelingCoreException;

/**
 * Implementation of the LevelFormula interface that calculates experience points (XP) and level values using a
 * polynomial formula. The XP required to reach a given level is computed as the sum of a configurable set of polynomial
 * terms evaluated at {@code (level - 1)}, allowing flexible progression curves from simple quadratic growth to
 * higher-degree polynomials.
 * <p>
 * Each coefficient {@code c[i]} in the provided array contributes the term {@code c[i] * (level - 1)^i} to the total XP
 * threshold, so {@code coefficients[0]} is a flat offset, {@code coefficients[1]} is the linear rate,
 * {@code coefficients[2]} drives quadratic growth, and so on.
 * <p>
 * Example — classic quadratic curve ({@code 100 * (level-1)^2}):
 *
 * <pre>{@code
 * new PolynomialLevelFormula(new double[] { 0, 0, 100 }, 100);
 * }</pre>
 */
public class PolynomialLevelFormula implements LevelFormula {

    private final double[] coefficients;

    private final int maxLevel;

    /**
     * Constructs a PolynomialLevelFormula with the given coefficients and maximum level.
     * <p>
     * The XP required for {@code level} is computed as:
     *
     * <pre>
     *   xp = sum(coefficients[i] * (level - 1)^i)  for i in [0, coefficients.length)
     * </pre>
     *
     * @param coefficients The polynomial coefficients ordered from degree 0 to degree N. Must be non-null, non-empty,
     *                     and contain at least one non-zero value with a positive index (i.e. the polynomial must
     *                     actually grow with level). All values must be finite.
     * @param maxLevel     The maximum level supported by this formula. Must be greater than or equal to 1.
     * @throws IllegalArgumentException If {@code coefficients} is null, empty, contains non-finite values, or
     *                                  represents a polynomial that does not grow (all coefficients at index >= 1 are
     *                                  zero or negative).
     * @throws LevelingCoreException    If {@code maxLevel} is less than 1.
     */
    public PolynomialLevelFormula(double[] coefficients, int maxLevel) {
        if (coefficients == null || coefficients.length == 0) {
            throw new IllegalArgumentException("coefficients must be non-null and non-empty");
        }
        for (var i = 0; i < coefficients.length; i++) {
            if (!Double.isFinite(coefficients[i])) {
                throw new IllegalArgumentException("coefficients[" + i + "] must be finite");
            }
        }
        var hasPositiveGrowth = false;
        for (var i = 1; i < coefficients.length; i++) {
            if (coefficients[i] > 0) {
                hasPositiveGrowth = true;
                break;
            }
        }
        if (!hasPositiveGrowth) {
            throw new IllegalArgumentException("polynomial must have at least one positive coefficient at degree >= 1");
        }
        if (maxLevel < 1) {
            throw new LevelingCoreException("maxLevel must be >= 1");
        }
        this.coefficients = coefficients.clone();
        this.maxLevel = maxLevel;
    }

    /**
     * Calculates the total XP required to reach a specific level using the configured polynomial formula.
     *
     * @param level The level for which to calculate the required XP. Must be greater than or equal to 1.
     * @return The total XP required to reach the specified level. Returns {@link Long#MAX_VALUE} if the computed value
     *         overflows or is non-finite.
     * @throws IllegalArgumentException If {@code level} is less than 1.
     */
    @Override
    public long getXpForLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level must be >= 1");
        }

        if (level == 1) {
            return 0L;
        }

        var x = level - 1D;
        var value = 0.0D;
        var power = 1.0D;

        for (var coefficient : coefficients) {
            value += coefficient * power;
            power *= x;
        }

        if (!Double.isFinite(value) || value >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }

        return (long) Math.ceil(value);
    }

    /**
     * Determines the level corresponding to the given total XP using a binary search over the polynomial curve.
     *
     * @param xp The total experience points. Must be non-negative.
     * @return The highest level whose XP threshold does not exceed {@code xp}, capped at {@code maxLevel} and floored
     *         at 1.
     * @throws IllegalArgumentException If {@code xp} is negative.
     */
    @Override
    public int getLevelForXp(long xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("xp must be >= 0");
        }

        if (getXpForLevel(maxLevel) <= xp) {
            return maxLevel;
        }

        var lo = 1;
        var hi = maxLevel;

        while (lo < hi - 1) {
            var mid = lo + (hi - lo) / 2;
            if (getXpForLevel(mid) <= xp) {
                lo = mid;
            } else {
                hi = mid;
            }
        }

        return Math.max(lo, 1);
    }
}
