package com.azuredoom.levelingcore.config.internal;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.azuredoom.levelingcore.database.DataSourceFactory;
import com.azuredoom.levelingcore.database.JdbcLevelRepository;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.level.rewards.LevelRewards;
import com.azuredoom.levelingcore.level.rewards.RewardEntry;
import com.azuredoom.levelingcore.level.xp.XPValues;

/**
 * ConfigBootstrap is a utility class that initializes and configures the core components of the LevelingCore system.
 * This includes loading configuration settings, setting up database connections, initializing the leveling formula, and
 * creating a level service for managing levels and experience points (XP).
 * <p>
 * This class cannot be instantiated and provides a static method to bootstrap the system.
 */
public final class ConfigBootstrap {

    private ConfigBootstrap() {}

    /**
     * Record Bootstrap is a utility data holder used during the initialization of the LevelingCore system. It
     * encapsulates core parts necessary for the functionality of the leveling system. The record is primarily designed
     * to simplify the return of multiple related objects from the bootstrap process.
     */
    public record Bootstrap(
        LevelServiceImpl service,
        Map<String, Integer> xpMapping,
        Map<Integer, List<RewardEntry>> levelRewardMapping,
        AutoCloseable closeable
    ) {}

    /**
     * Initializes and configures the essential parts of the LevelingCore system. This method sets up the leveling
     * formula, loads configuration settings, establishes a database connection, and prepares the leveling service. It
     * also handles any necessary migration of XP formulas when specified in the configuration.
     *
     * @return A {@code Bootstrap} record containing the initialized {@code LevelService} and a {@code closeable}
     *         resource for managing the cleanup of associated resources.
     */
    public static Bootstrap bootstrap(Path dataDir) {
        if (dataDir == null) {
            throw new LevelingCoreException("dataDir cannot be null");
        }
        var config = ConfigManager.loadOrCreate(dataDir);
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
        var xpMapping = XPValues.loadOrCreate(dataDir);
        var levelRewardMapping = LevelRewards.loadOrCreate(dataDir);

        return new Bootstrap(service, xpMapping, levelRewardMapping, repo::close);
    }
}
