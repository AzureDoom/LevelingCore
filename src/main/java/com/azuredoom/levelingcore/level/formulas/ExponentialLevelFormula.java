package com.azuredoom.levelingcore.level.formulas;

import com.azuredoom.levelingcore.exceptions.LevelingCoreException;

/**
 * Implementation of the LevelFormula interface that calculates XP and level values using an exponential formula. This
 * class models the XP progression required to reach higher levels, which grows exponentially based on the provided base
 * XP and exponent values.
 */
public class ExponentialLevelFormula implements LevelFormula {

    private final double baseXp;

    private final double exponent;

    private final int maxLevel;

    /**
     * Constructs an instance of the ExponentialLevelFormula class, which calculates XP and level values using an
     * exponential formula. The XP progression grows exponentially based on the provided base XP and exponent values.
     *
     * @param baseXp   The base number of experience points required for progression. Must be greater than 0.
     * @param exponent The exponent that determines the growth rate of the XP required for each level. Must be greater
     *                 than 0.
     * @param maxLevel The maximum level supported by this formula. Must be greater than or equal to 1.
     * @throws IllegalArgumentException If baseXp is less than or equal to 0.
     * @throws IllegalArgumentException If the exponent is less than or equal to 0.
     */
    public ExponentialLevelFormula(double baseXp, double exponent, int maxLevel) {
        if (baseXp <= 0) {
            throw new IllegalArgumentException("baseXp must be > 0");
        }
        if (exponent <= 0) {
            throw new IllegalArgumentException("exponent must be > 0");
        }
        if (maxLevel < 1) {
            throw new LevelingCoreException("maxLevel must be >= 1");
        }
        this.baseXp = baseXp;
        this.exponent = exponent;
        this.maxLevel = maxLevel;
    }

    /**
     * Calculates the total experience points (XP) required to reach a specific level using an exponential formula. The
     * XP requirement grows exponentially based on the configured base XP and exponent values.
     *
     * @param level The level for which to calculate the required XP. Must be greater than or equal to 1.
     * @return The total XP required to reach the specified level. If the resulting XP exceeds {@link Long#MAX_VALUE},
     *         the method returns {@link Long#MAX_VALUE}.
     * @throws IllegalArgumentException If the level is less than 1.
     */
    @Override
    public long getXpForLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level must be >= 1");
        }

        if (level == 1) {
            return 0;
        }

        var value = baseXp * Math.pow(level - 1, exponent);

        if (!Double.isFinite(value) || value >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }

        return (long) Math.ceil(value);
    }

    /**
     * Determines the level corresponding to the given number of experience points (XP) using a binary search over the
     * exponential curve. Returns the highest level whose XP threshold does not exceed {@code xp}, capped at
     * {@code maxLevel} and floored at 1.
     *
     * @param xp The total experience points for which the corresponding level is to be determined. Must be
     *           non-negative.
     * @return The level corresponding to the specified XP value. The level will always be at least 1.
     * @throws IllegalArgumentException If the xp value is negative.
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
