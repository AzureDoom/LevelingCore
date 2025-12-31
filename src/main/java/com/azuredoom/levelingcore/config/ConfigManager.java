package com.azuredoom.levelingcore.config;

import com.azuredoom.levelingcore.LevelingCoreException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * ConfigManager is a utility class responsible for managing the configuration of the LevelingCore system. It handles
 * the creation and loading of a YAML-based configuration file, ensuring that the configuration is properly initialized
 * and available for use.
 * <p>
 * This class is final and cannot be instantiated. It provides a static method to load or create the configuration in a
 * specified directory.
 * <p>
 * The configuration file contains settings for database connections and leveling formulas, with default values written
 * to the file if it does not already exist.
 */
public final class ConfigManager {

    private ConfigManager() {}

    /**
     * Loads an existing LevelingCore configuration file from the specified directory or creates a new one if it does
     * not exist. The configuration file is named "levelingcore.yml" and is stored in the provided directory. If
     * creation is required, a default configuration is written.
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
                try (
                    var in = ConfigManager.class
                        .getClassLoader()
                        .getResourceAsStream("default.yml")
                ) {

                    if (in == null) {
                        throw new LevelingCoreException("default.yml not found in resources");
                    }

                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
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
