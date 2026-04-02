package com.azuredoom.levelingcore.level.mobs;

/**
 * Represents the tier classification of a mob in the leveling system. The tier determines the general strength and
 * rarity of the mob, with each tier representing a specific level of difficulty or importance.
 * <ul>
 * <li>{@code NORMAL} - Represents a standard or baseline mob tier.</li>
 * <li>{@code ELITE} - Represents a stronger and more challenging mob tier compared to {@code NORMAL}.</li>
 * <li>{@code BOSS} - Represents the highest and most challenging tier of mobs, typically serving as significant
 * challenges or major encounters.</li>
 * </ul>
 */
public enum MobTier {
    NORMAL,
    ELITE,
    BOSS
}
