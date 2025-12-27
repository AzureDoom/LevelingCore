package com.azuredoom.hyleveling.config;

import com.azuredoom.hyleveling.HyLevelingException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * ConfigManager is a utility class responsible for managing the configuration of the HyLeveling system. It handles the
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
        # HyLeveling configuration
        #
        # Supported JDBC URLs:
        #   H2 (file):      jdbc:h2:file:./data/hyleveling;MODE=PostgreSQL
        #   MySQL:          jdbc:mysql://host:3306/dbname
        #   MariaDB:        jdbc:mariadb://host:3307/dbname
        #   PostgreSQL:     jdbc:postgresql://host:5432/dbname
        #
        # Notes:
        # - H2 commonly uses empty username/password unless you configured otherwise.
        # - For MySQL/MariaDB/Postgres, set username/password.
        #
        database:
          jdbcUrl: "jdbc:h2:file:./data/hyleveling;MODE=PostgreSQL"
          username: ""
          password: ""
          maxPoolSize: 10
        # Leveling formula configuration
        #
        # Supported types:
        #   - EXPONENTIAL: XP floor at level L is baseXp * (L - 1) ^ exponent
        #   - LINEAR:      XP floor at level L is xpPerLevel * (L - 1)
        #
        # Notes:
        # - XP migration is enabled by default. Set migrateXP to false to disable.
        #
        formula:
          type: "EXPONENTIAL"
          migrateXP: true
          exponential:
            baseXp: 100.0
            exponent: 1.7
          linear:
            xpPerLevel: 100
        """;

    private ConfigManager() {}

    /**
     * Loads an existing HyLeveling configuration file from the specified directory, or creates a new one if it does not
     * exist. The configuration file is named "hyleveling.yml" and is stored in the provided directory. If creation is
     * required, a default configuration is written.
     *
     * @param dataDir The directory where the configuration file is located or will be created.
     * @return The loaded or newly created {@link HyLevelingConfig} instance containing configuration data.
     * @throws HyLevelingException If any error occurs during file creation, reading, or parsing the configuration.
     */
    public static HyLevelingConfig loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);

            Path configPath = dataDir.resolve("hyleveling.yml");
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

            var yaml = new Yaml(new Constructor(HyLevelingConfig.class, opts));
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                HyLevelingConfig cfg = yaml.load(reader);
                return (cfg != null) ? cfg : new HyLevelingConfig();
            }
        } catch (Exception e) {
            throw new HyLevelingException("Failed to load config", e);
        }
    }
}
