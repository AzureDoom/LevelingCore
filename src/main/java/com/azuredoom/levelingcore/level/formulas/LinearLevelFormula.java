package com.azuredoom.levelingcore.level.formulas;

/**
 * Implementation of the LevelFormula interface that calculates experience points (XP) and level values using a linear
 * formula. In this model, the XP required for each level progression grows at a constant rate defined by the XP per
 * level parameter.
 */
public class LinearLevelFormula implements LevelFormula {

    private final long xpPerLevel;

    /**
     * Constructs an instance of the LinearLevelFormula class, which calculates XP and level values using a linear
     * formula. The XP progression grows at a constant rate based on the provided XP per level value.
     *
     * @param xpPerLevel The number of experience points required for each level progression. Must be greater than 0.
     * @throws IllegalArgumentException If xpPerLevel is less than or equal to 0.
     */
    public LinearLevelFormula(long xpPerLevel) {
        if (xpPerLevel <= 0) {
            throw new IllegalArgumentException("xpPerLevel must be > 0");
        }
        this.xpPerLevel = xpPerLevel;
    }

    /**
     * Calculates the total experience points (XP) required to reach a specific level using a linear formula. The XP
     * requirement increases linearly based on the configured XP per level value.
     *
     * @param level The level for which to calculate the required XP. Must be greater than or equal to 1.
     * @return The total XP required to reach the specified level. If the calculated XP exceeds {@code Long.MAX_VALUE},
     *         the method returns {@code Long.MAX_VALUE}.
     * @throws IllegalArgumentException If the level is less than 1.
     */
    @Override
    public long getXpForLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level must be >= 1");
        }

        if (level == 1) {
            return 0L;
        }

        long value = xpPerLevel * (long) (level - 1);

        if (value < 0 || value >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }

        return value;
    }

    /**
     * Determines the level corresponding to the given number of experience points (XP) using a linear formula.
     *
     * @param xp The total experience points for which the corresponding level is to be determined. Must be
     *           non-negative.
     * @return The level corresponding to the specified XP value. The level will always be at least 1. If the computed
     *         level exceeds {@link Integer#MAX_VALUE}, the method will return {@link Integer#MAX_VALUE}.
     * @throws IllegalArgumentException If the xp value is negative.
     */
    @Override
    public int getLevelForXp(long xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("xp must be >= 0");
        }

        long level = (xp / xpPerLevel) + 1;

        if (level >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) level;
    }
}
