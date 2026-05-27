package com.azuredoom.levelingcore.level.stats;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.internal.ConfigManager;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;

public class StatsPerLevelMapping {

    public static final String FILE_NAME = "statsperlevelmapping.csv";

    public static final String RESOURCE_DEFAULT = "/defaultstatsperlevelmapping.csv";

    private Map<Integer, Integer> addedStatsPerLevel = new TreeMap<>();

    private Map<Integer, Integer> cumulativeStatsPerLevel = new HashMap<>();

    public StatsPerLevelMapping(Path dataDir) {
        loadOrCreate(dataDir);
    }

    public void loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var configPath = dataDir.resolve(FILE_NAME);

            if (Files.notExists(configPath)) {
                try (InputStream in = ConfigManager.class.getResourceAsStream(RESOURCE_DEFAULT)) {
                    if (in == null) {
                        throw new LevelingCoreException(
                            "defaultstatsperlevelmapping.csv not found in resources (expected at " + RESOURCE_DEFAULT
                                + ")"
                        );
                    }
                    LevelingCore.LOGGER.atInfo()
                        .log("Creating default Stats Per Level Mapping config at " + configPath);
                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            addedStatsPerLevel = readXpCsv(configPath);
            cumulativeStatsPerLevel = new HashMap<>();
            int total = 0;
            for (var entry : addedStatsPerLevel.entrySet()) {
                cumulativeStatsPerLevel.put(entry.getKey(), total += entry.getValue());
            }

            LevelingCore.LOGGER.atInfo()
                .log(
                    "Loaded Stats Per Level Mapping mapping from " + configPath + " " + addedStatsPerLevel.size()
                        + " entries)"
                );
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load Stats Per Level Mapping config", e);
        }
    }

    public int getAddedStatsForLevel(int level, int defaultValue) {
        return addedStatsPerLevel.getOrDefault(level, defaultValue);
    }

    public int getCumulativeStatsForLevel(int level, int fallbackValue) {
        if (!cumulativeStatsPerLevel.containsKey(level)) {
            int maxDefinedLevel = cumulativeStatsPerLevel.keySet().stream().max(Integer::compareTo).orElse(0);
            if (level > maxDefinedLevel) {
                return cumulativeStatsPerLevel.get(maxDefinedLevel);
            } else {
                return fallbackValue;
            }
        }
        return cumulativeStatsPerLevel.get(level);
    }

    private static TreeMap<Integer, Integer> readXpCsv(Path csvPath) throws Exception {
        TreeMap<Integer, Integer> out = new TreeMap<>();

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
                    if (line.equalsIgnoreCase("statpoints,lvl")) {
                        continue;
                    }
                }

                var parts = line.split(",", 2);
                if (parts.length != 2) {
                    LevelingCore.LOGGER.atWarning().log("Skipping invalid CSV line: " + line);
                    continue;
                }

                var statsStr = parts[0].trim();
                var lvlStr = parts[1].trim();

                int stats;
                try {
                    stats = Integer.parseInt(statsStr);
                } catch (NumberFormatException nfe) {
                    LevelingCore.LOGGER.atWarning()
                        .log("Invalid Stats value for " + statsStr + ": " + lvlStr + " (line: " + line + ")");
                    continue;
                }

                int lvl;
                try {
                    lvl = Integer.parseInt(lvlStr);
                } catch (NumberFormatException nfe) {
                    LevelingCore.LOGGER.atWarning()
                        .log(
                            "Invalid Stats value for " + statsStr + ": " + lvlStr + " (line: " + line + ")"
                        );
                    continue;
                }

                out.put(lvl, stats);
            }
        }

        return out;
    }
}
