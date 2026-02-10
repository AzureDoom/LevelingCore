package com.azuredoom.levelingcore.level.mobs.mapping;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.internal.ConfigManager;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;

public class MobEnvironmentMapping {

    public static final String FILE_NAME = "mobenvironmentmapping.csv";

    public static final String RESOURCE_DEFAULT = "/defaultmobenvironmentmapping.csv";

    private MobEnvironmentMapping() {}

    public static Map<String, Integer> loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var configPath = dataDir.resolve(FILE_NAME);

            if (Files.notExists(configPath)) {
                try (InputStream in = ConfigManager.class.getResourceAsStream(RESOURCE_DEFAULT)) {
                    if (in == null) {
                        throw new LevelingCoreException(
                            "defaultmobenvironmentmapping.csv not found in resources (expected at " + RESOURCE_DEFAULT
                                + ")"
                        );
                    }
                    LevelingCore.LOGGER.at(Level.INFO)
                        .log("Creating default Mob Environment Levels Mapping config at " + configPath);
                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            var mapping = readXpCsv(configPath);

            LevelingCore.LOGGER.at(Level.INFO)
                .log(
                    "Loaded Mob Environment Levels Mapping mapping from " + configPath + " " + mapping.size()
                        + " entries)"
                );
            return mapping;

        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load Mob Environment Levels Mapping config", e);
        }
    }

    private static Map<String, Integer> readXpCsv(Path csvPath) throws Exception {
        Map<String, Integer> out = new LinkedHashMap<>();

        try (var reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            var firstNonEmptyLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                if (line.startsWith("#"))
                    continue;

                if (firstNonEmptyLine) {
                    firstNonEmptyLine = false;
                    if (line.equalsIgnoreCase("env,lvl")) {
                        continue;
                    }
                }

                var parts = line.split(",", 2);
                if (parts.length != 2) {
                    LevelingCore.LOGGER.at(Level.WARNING).log("Skipping invalid CSV line: " + line);
                    continue;
                }

                var envStr = parts[0].trim();
                var lvlStr = parts[1].trim();

                if (envStr.isEmpty()) {
                    LevelingCore.LOGGER.at(Level.WARNING).log("Skipping CSV line with empty environment: " + line);
                    continue;
                }

                int lvl;
                try {
                    lvl = Integer.parseInt(lvlStr);
                } catch (NumberFormatException nfe) {
                    LevelingCore.LOGGER.at(Level.WARNING)
                        .log(
                            "Invalid Environment value for " + envStr + ": " + lvlStr + " (line: " + line + ")"
                        );
                    continue;
                }

                out.put(envStr, lvl);
            }
        }

        return out;
    }
}
