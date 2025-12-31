package com.azuredoom.levelingcore;

import com.azuredoom.levelingcore.config.ConfigBootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class Main {

    public static final ConfigBootstrap.Bootstrap bootstrap = ConfigBootstrap.bootstrap();

    public static final System.Logger LOGGER = System.getLogger(Main.class.getName());

    public static final Path configPath = Paths.get("./data/plugins/levelingcore/");

    static void main() {
        // TODO: Move to server startup so registration are auto handled
        var levelService = bootstrap.service();
        // TODO: Remove once hooks into the player/mob kill events are found and integrable.
        var testId = UUID.fromString("d3804858-4bb8-4026-ae21-386255ed467d");
        levelService.addXp(testId, 500);
        // TODO: Move to chat based logging instead of System loggers
        LOGGER.log(System.Logger.Level.INFO, String.format("XP: %d", levelService.getXp(testId)));
        LOGGER.log(System.Logger.Level.INFO, String.format("Level: %d", levelService.getLevel(testId)));
        // TODO: Move to server shutdown so JDBC resources are properly closed
        try {
            bootstrap.closeable().close();
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to close resources", e);
        }
    }
}
