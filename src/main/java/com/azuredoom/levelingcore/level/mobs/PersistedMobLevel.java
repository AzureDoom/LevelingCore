package com.azuredoom.levelingcore.level.mobs;

/**
 * Represents the persisted state of a mob's level configuration in the leveling system. This immutable record contains
 * information about the mob's spawn level, locking status, associated profile and tier identifiers, and various
 * multipliers that affect the mob's attributes such as health, damage, and experience points.
 *
 * @param spawnLevel       The level at which the mob spawns.
 * @param locked           A boolean indicating whether the mob's level is locked.
 * @param profileId        The identifier of the profile associated with the mob.
 * @param tierId           The identifier of the tier associated with the mob.
 * @param healthMultiplier The multiplier for the mob's health.
 * @param damageMultiplier The multiplier for the mob's damage.
 * @param xpMultiplier     The multiplier for the mob's experience points.
 */
public record PersistedMobLevel(
    int spawnLevel,
    boolean locked,
    String profileId,
    String tierId,
    float healthMultiplier,
    float damageMultiplier,
    float xpMultiplier
) {

    /**
     * Creates a legacy instance of {@link PersistedMobLevel} with predefined parameters.
     *
     * @param spawnLevel The level at which the mob spawns.
     * @param locked     A boolean indicating whether the mob's level is locked.
     * @return A new {@link PersistedMobLevel} instance representing the legacy mob level configuration.
     */
    public static PersistedMobLevel legacy(int spawnLevel, boolean locked) {
        return new PersistedMobLevel(
            spawnLevel,
            locked,
            "normal",
            "NORMAL",
            1.0f,
            1.0f,
            1.0f
        );
    }
}
