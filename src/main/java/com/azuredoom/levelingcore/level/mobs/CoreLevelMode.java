package com.azuredoom.levelingcore.level.mobs;

import java.util.Optional;

/**
 * Represents an enumeration of modes used for determining the core level in a system. These modes define different
 * strategies or contexts for calculating or assigning levels.
 * <ul>
 * <li>{@code SPAWN_ONLY} - Level is determined based solely on spawn conditions.</li>
 * <li>{@code BIOME} - Level is determined based on the biome where the entity is located.</li>
 * <li>{@code ZONE} - Level is determined based on specific zones or regions.</li>
 * <li>{@code NEARBY_PLAYERS_MEAN} - Level is determined based on the average level of nearby players.</li>
 * <li>{@code ENVIRONMENT} - Level is determined based on environmental factors or settings.</li>
 * <li>{@code INSTANCE} - Level is determined based on specific instances or individual contexts.</li>
 * </ul>
 */
public enum CoreLevelMode {

    SPAWN_ONLY,
    BIOME,
    ZONE,
    NEARBY_PLAYERS_MEAN,
    ENVIRONMENT,
    INSTANCE;

    /**
     * Returns the identifier of this enum constant, which is the enum's name.
     *
     * @return A string representing the name of the enum constant.
     */
    public String getId() {
        return name();
    }

    /**
     * Converts a string representation of a {@code CoreLevelMode} to its corresponding enum value, if valid. The input
     * is case-insensitive and will be converted to upper case for matching. If the input is null or does not match any
     * of the enum values, an empty {@code Optional} is returned.
     *
     * @param value The string value to be converted to a {@code CoreLevelMode}.
     * @return An {@code Optional} containing the corresponding {@code CoreLevelMode} if the input is valid; otherwise,
     *         an empty {@code Optional}.
     */
    public static Optional<CoreLevelMode> fromString(String value) {
        if (value == null)
            return Optional.empty();
        try {
            return Optional.of(CoreLevelMode.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
