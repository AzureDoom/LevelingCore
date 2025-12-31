package com.azuredoom.levelingcore.config;

import com.azuredoom.levelingcore.Main;
import com.azuredoom.levelingcore.database.DataSourceFactory;
import com.azuredoom.levelingcore.database.JdbcLevelRepository;
import com.azuredoom.levelingcore.level.LevelService;
import com.azuredoom.levelingcore.level.LevelServiceImpl;

/**
 * ConfigBootstrap is a utility class that initializes and configures the core components of the LevelingCore system.
 * This includes loading configuration settings, setting up database connections, initializing the leveling formula,
 * and creating a level service for managing levels and experience points (XP).
 * <p>
 * This class cannot be instantiated and provides a static method to bootstrap the system.
 */
public final class ConfigBootstrap {

    private ConfigBootstrap() {}

    /**
     * Record Bootstrap is a utility data holder used during the initialization of the LevelingCore system.
     * It encapsulates core parts necessary for the functionality of the leveling system. The record is
     * primarily designed to simplify the return of multiple related objects from the bootstrap process.
     */
    public record Bootstrap(LevelService service, AutoCloseable closeable) {}

    /**
     * Initializes and configures the essential parts of the LevelingCore system. This method sets up the leveling
     * formula, loads configuration settings, establishes a database connection, and prepares the leveling service.
     * It also handles any necessary migration of XP formulas when specified in the configuration.
     *
     * @return A {@code Bootstrap} record containing the initialized {@code LevelService} and a {@code closeable}
     *         resource for managing the cleanup of associated resources.
     */
    public static Bootstrap bootstrap() {
        var config = ConfigManager.loadOrCreate(Main.configPath);
        var formulaDescriptor = LevelFormulaFactory.descriptorFromConfig(config);
        var formula = LevelFormulaFactory.fromConfig(config);
        var ds = DataSourceFactory.create(
                config.database.jdbcUrl,
                config.database.username,
                config.database.password,
                config.database.maxPoolSize
        );
        var repo = new JdbcLevelRepository(ds);

        if (config.formula.migrateXP) {
            repo.migrateFormulaIfNeeded(formula, formulaDescriptor);
        }
        var service = new LevelServiceImpl(formula, repo);

        return new Bootstrap(service, repo::close);
    }
}
