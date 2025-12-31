package com.azuredoom.levelingcore.level.formulas;

/**
 * Interface for defining the XP-to-level relationship and calculations in a leveling system. Provides methods to
 * determine the total XP required for a specific level as well as the level corresponding to a given amount of total
 * XP.
 */
public interface LevelFormula {

    /**
     * Returns the total XP required to reach a specific level.
     */
    long getXpForLevel(int level);

    /**
     * Converts total XP into a level.
     */
    int getLevelForXp(long xp);
}
