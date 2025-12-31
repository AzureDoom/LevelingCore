package com.azuredoom.levelingcore.level.formulas;

import java.util.Arrays;

/**
 * Implementation of the LevelFormula interface that uses a pre-defined table of XP values for each level to determine
 * XP-to-level relationships. This implementation allows for custom, non-linear XP progression by providing an array of
 * cumulative XP values indexed by level.
 */
public class TableLevelFormula implements LevelFormula {

    private final long[] xpByLevel;

    public TableLevelFormula(long[] xpByLevel) {
        if (xpByLevel == null || xpByLevel.length < 2) {
            throw new IllegalArgumentException("xpByLevel must include at least levels 0..1");
        }
        this.xpByLevel = xpByLevel;
        validate();
    }

    /**
     * Validates the XP progression defined by the xpByLevel array to ensure it adheres to expected constraints. This
     * method ensures that: - Level 1 requires exactly 0 XP. - The XP values for later levels are non-decreasing.
     *
     * @throws IllegalArgumentException if: - Level 1 does not require 0 XP. - XP values for any level are less than the
     *                                  XP values for the previous level, violating the non-decreasing constraint.
     */
    private void validate() {
        if (xpByLevel[1] != 0L) {
            throw new IllegalArgumentException("Level 1 must require 0 XP");
        }
        var prev = xpByLevel[1];
        for (var level = 2; level < xpByLevel.length; level++) {
            var xp = xpByLevel[level];
            if (xp < prev) {
                throw new IllegalArgumentException("XP must be non-decreasing (level " + level + ")");
            }
            prev = xp;
        }
    }

    /**
     * Retrieves the total experience points (XP) required to reach the specified level. This method uses a pre-defined
     * XP-to-level mapping to determine the XP requirements. If the requested level exceeds the maximum level defined by
     * the mapping, the XP for the highest level is returned.
     *
     * @param level The level for which to determine the required XP. Must be greater than or equal to 1.
     * @return The total XP required to reach the specified level. If the level exceeds the maximum defined level, the
     *         maximum XP value in the mapping is returned.
     * @throws IllegalArgumentException If the level is less than 1.
     */
    @Override
    public long getXpForLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level must be >= 1");
        }

        if (level >= xpByLevel.length) {
            return xpByLevel[xpByLevel.length - 1];
        }
        return xpByLevel[level];
    }

    /**
     * Determines the level corresponding to the given total experience points (XP) using a pre-defined XP-to-level
     * mapping. If the exact XP value exists in the mapping, the corresponding level is returned. If the exact XP value
     * is not found, the method calculates the level based on the appropriate insertion point in the XP progression.
     *
     * @param xp The total experience points for which the corresponding level is to be determined. Must be
     *           non-negative.
     * @return The level corresponding to the given experience points. The level will always be at least 1, and it will
     *         not exceed the maximum level defined by the XP mapping.
     * @throws IllegalArgumentException If the xp value is negative.
     */
    @Override
    public int getLevelForXp(long xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("xp must be >= 0");
        }

        var idx = Arrays.binarySearch(xpByLevel, xp);
        if (idx >= 0) {
            return Math.max(1, idx);
        }

        var insertionPoint = -(idx + 1);
        var level = insertionPoint - 1;
        return Math.max(1, Math.min(level, xpByLevel.length - 1));
    }
}
