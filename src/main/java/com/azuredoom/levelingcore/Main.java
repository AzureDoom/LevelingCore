package com.azuredoom.levelingcore;

import com.azuredoom.levelingcore.config.ConfigManager;
import com.azuredoom.levelingcore.config.LevelFormulaFactory;
import com.azuredoom.levelingcore.database.DataSourceFactory;
import com.azuredoom.levelingcore.database.JdbcLevelRepository;
import com.azuredoom.levelingcore.level.LevelServiceImpl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class Main {

    public static final System.Logger LOGGER = System.getLogger(Main.class.getName());

    public static Path configPath = Paths.get("./data/plugins/levelingcore/");

    static void main() {
        // TODO: Move to server startup so registration are auto handled
        var config = ConfigManager.loadOrCreate(configPath);
        var desc = LevelFormulaFactory.descriptorFromConfig(config);
        var formula = LevelFormulaFactory.fromConfig(config);
        var jdbcUrl = config.database.jdbcUrl;
        var user = config.database.username;
        var pass = config.database.password;
        var maxPoolSize = config.database.maxPoolSize;
        var dataSource = DataSourceFactory.create(jdbcUrl, user, pass, maxPoolSize);
        var repository = new JdbcLevelRepository(dataSource);
        if (config.formula.migrateXP) {
            repository.migrateFormulaIfNeeded(formula, desc);
        }
        var levelService = new LevelServiceImpl(formula, repository);

        // TODO: Remove once hooks into the player/mob kill events are found and integrable.
        var testId = UUID.fromString("d3804858-4bb8-4026-ae21-386255ed467d");
        levelService.addXp(testId, 500);
        // TODO: Move to chat based logging instead of System loggers
        LOGGER.log(System.Logger.Level.INFO, String.format("XP: %d", levelService.getXp(testId)));
        LOGGER.log(System.Logger.Level.INFO, String.format("Level: %d", levelService.getLevel(testId)));

        // TODO: Move to server shutdown so JDBC resources are properly closed
        Runtime.getRuntime().addShutdownHook(new Thread(repository::close));
    }
}
