package com.azuredoom.levelingcore.api;

import java.util.Optional;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.level.LevelServiceImpl;

@SuppressWarnings("unused")
public final class LevelingCoreApi {

    private LevelingCoreApi() {}

    /**
     * Retrieves the {@link LevelServiceImpl} instance if it is available and returns it as an {@code Optional}. This
     * allows safely handling scenarios where the leveling service might not be initialized.
     *
     * @return an {@code Optional} containing the {@link LevelServiceImpl} instance if available; otherwise, an empty
     *         {@code Optional}.
     */
    public static Optional<LevelServiceImpl> getLevelServiceIfPresent() {
        return Optional.ofNullable(LevelingCore.getLevelService());
    }
}
