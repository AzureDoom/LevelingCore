package com.azuredoom.levelingcore.level.objectives;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.internal.ConfigManager;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;

public class ObjectivesXPMapping {

    public static final String FILE_NAME = "objectivesxpmapping.csv";

    public static final String RESOURCE_DEFAULT = "/defaultobjectivesxpmapping.csv";

    private ObjectivesXPMapping() {}

    public static Map<String, Integer> loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var configPath = dataDir.resolve(FILE_NAME);

            if (Files.notExists(configPath)) {
                try (InputStream in = ConfigManager.class.getResourceAsStream(RESOURCE_DEFAULT)) {
                    if (in == null) {
                        throw new LevelingCoreException(
                            "defaultobjectivesxpmapping.csv not found in resources (expected at " + RESOURCE_DEFAULT
                                + ")"
                        );
                    }
                    LevelingCore.LOGGER.atInfo()
                        .log("Creating default Objectives XP Mapping config at " + configPath);
                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            var mapping = readXpCsv(configPath);

            LevelingCore.LOGGER.atInfo()
                .log("Loaded Objectives XP Mapping mapping from " + configPath + " " + mapping.size() + " entries)");
            return mapping;

        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load Objectives XP Mapping config", e);
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
                    if (line.equalsIgnoreCase("objective,xp")) {
                        continue;
                    }
                }

                var parts = line.split(",", 2);
                if (parts.length != 2) {
                    LevelingCore.LOGGER.atWarning().log("Skipping invalid CSV line: " + line);
                    continue;
                }

                var objectiveId = parts[0].trim();
                var xpStr = parts[1].trim();

                if (objectiveId.isEmpty()) {
                    LevelingCore.LOGGER.atWarning().log("Skipping CSV line with empty objectiveId: " + line);
                    continue;
                }

                int xp;
                try {
                    xp = Integer.parseInt(xpStr);
                } catch (NumberFormatException nfe) {
                    LevelingCore.LOGGER.atWarning()
                        .log(
                            "Invalid XP value for " + objectiveId + ": " + xpStr + " (line: " + line + ")"
                        );
                    continue;
                }

                out.put(objectiveId, xp);
            }
        }

        return out;
    }
}
