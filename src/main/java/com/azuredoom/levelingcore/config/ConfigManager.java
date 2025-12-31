package com.azuredoom.levelingcore.config;

import com.azuredoom.levelingcore.LevelingCoreException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * ConfigManager is a utility class responsible for managing the configuration of the LevelingCore system. It handles the
 * creation and loading of a YAML-based configuration file, ensuring that the configuration is properly initialized and
 * available for use.
 * <p>
 * This class is final and cannot be instantiated. It provides a static method to load or create the configuration in a
 * specified directory.
 * <p>
 * The configuration file contains settings for database connections and leveling formulas, with default values written
 * to the file if it does not already exist.
 */
public final class ConfigManager {

    private static final String DEFAULT_YAML = """
        # LevelingCore configuration
        #
        # =========================
        # Database Configuration
        # =========================
        #
        # Supported JDBC URLs:
        #   H2 (file):      jdbc:h2:file:./data/plugins/levelingcore/levelingcore;MODE=PostgreSQL
        #   MySQL:          jdbc:mysql://host:port/dbname
        #   MariaDB:        jdbc:mariadb://host:port/dbname
        #   PostgreSQL:     jdbc:postgresql://host:port/dbname
        #
        # Notes:
        # - H2 commonly uses empty username/password unless you configured otherwise.
        # - For MySQL/MariaDB/Postgres, set username/password.
        #
        database:
          jdbcUrl: "jdbc:h2:file:./data/plugins/levelingcore/levelingcore;MODE=PostgreSQL"
          username: ""
          password: ""
          maxPoolSize: 10

        # =========================
        # Leveling Formula
        # =========================
        #
        # Supported types:
        #   - EXPONENTIAL: XP floor at level L is baseXp * (L - 1) ^ exponent
        #   - LINEAR:      XP floor at level L is xpPerLevel * (L - 1)
        #   - TABLE:       XP floor at level L is defined in a CSV file
        #   - CUSTOM:      XP floor at level L is defined by a math expression
        #
        # Notes:
        # - XP migration is enabled by default. Set migrateXP to false to disable.
        # - Changing the formula will recompute XP to preserve player levels.
        #
        formula:
          type: "EXPONENTIAL"
          migrateXP: true
          exponential:
            baseXp: 100.0
            exponent: 1.7
          linear:
            xpPerLevel: 100
          table:
            # CSV file relative to the data directory
            file: "levels.csv"
          custom:
            # Expression returns the XP floor for a level.
            #
            # Available variables:
            #   - level      (current level, integer >= 1)
            #
            # You may also reference any constants defined below.
            #
            # Example:
            #   exp(a * (level - 1)) * b / c
            #
            xpForLevel: "exp(a * (level - 1)) * b / c"

            # Optional constants referenced in the expression
            constants:
              a: 0.12
              b: 100
              c: 1

            # Maximum level supported by this formula (used for binary search)
            maxLevel: 100000
        """;

    private ConfigManager() {}

    /**
     * Loads an existing LevelingCore configuration file from the specified directory, or creates a new one if it does not
     * exist. The configuration file is named "levelingcore.yml" and is stored in the provided directory. If creation is
     * required, a default configuration is written.
     *
     * @param dataDir The directory where the configuration file is located or will be created.
     * @return The loaded or newly created {@link LevelingCoreConfig} instance containing configuration data.
     * @throws LevelingCoreException If any error occurs during file creation, reading, or parsing the configuration.
     */
    public static LevelingCoreConfig loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);

            var configPath = dataDir.resolve("levelingcore.yml");
            if (Files.notExists(configPath)) {
                Files.writeString(
                    configPath,
                    DEFAULT_YAML,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
                );
            }

            var opts = new LoaderOptions();
            opts.setMaxAliasesForCollections(50);

            var yaml = new Yaml(new Constructor(LevelingCoreConfig.class, opts));
            try (var reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                LevelingCoreConfig cfg = yaml.load(reader);
                return (cfg != null) ? cfg : new LevelingCoreConfig();
            }
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load config", e);
        }
    }
}
