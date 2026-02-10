package com.azuredoom.levelingcore.level.mobs;

import java.util.Optional;

public enum CoreLevelMode implements LevelMode {

    SPAWN_ONLY,
    BIOME,
    ZONE,
    NEARBY_PLAYERS_MEAN,
    ENVIRONMENT,
    INSTANCE;

    @Override
    public String getId() {
        return name();
    }

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
