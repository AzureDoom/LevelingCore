package com.azuredoom.levelingcore.level.rewards;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.internal.ConfigManager;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;

public class LevelRewards {

    public static final String FILE_NAME = "levelrewardmapping.csv";

    public static final String RESOURCE_DEFAULT = "/defaultlevelrewardmapping.csv";

    private LevelRewards() {}

    public static Map<Integer, List<RewardEntry>> loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var configPath = dataDir.resolve(FILE_NAME);

            if (Files.notExists(configPath)) {
                try (InputStream in = ConfigManager.class.getResourceAsStream(RESOURCE_DEFAULT)) {
                    if (in == null) {
                        throw new LevelingCoreException(
                            "defaultlevelrewardmapping.csv not found in resources (expected at " + RESOURCE_DEFAULT
                                + ")"
                        );
                    }
                    LevelingCore.LOGGER.at(Level.INFO).log("Creating default Level Rewards config at " + configPath);
                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            var mapping = readXpCsv(configPath);

            int rewardCount = mapping.values().stream().mapToInt(List::size).sum();
            LevelingCore.LOGGER.at(Level.INFO)
                .log(
                    "Loaded Level Rewards mapping from " + configPath + " (" + mapping.size() + " levels, "
                        + rewardCount + " rewards)"
                );
            return mapping;

        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load Level Rewards config", e);
        }
    }

    private static Map<Integer, List<RewardEntry>> readXpCsv(Path csvPath) throws Exception {
        Map<Integer, List<RewardEntry>> out = new LinkedHashMap<>();

        try (var reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            var firstNonEmptyLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                if (firstNonEmptyLine) {
                    firstNonEmptyLine = false;
                    if (line.equalsIgnoreCase("itemnameid,quantity,lvl"))
                        continue;
                }

                var parts = line.split(",", 3);
                if (parts.length != 3) {
                    LevelingCore.LOGGER.at(Level.WARNING).log("Skipping invalid CSV line: " + line);
                    continue;
                }

                var itemNameId = parts[0].trim();
                var quantityString = parts[1].trim();
                var lvlString = parts[2].trim();

                if (itemNameId.isEmpty()) {
                    LevelingCore.LOGGER.at(Level.WARNING).log("Skipping CSV line with empty itemNameId: " + line);
                    continue;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(quantityString);
                } catch (NumberFormatException nfe) {
                    LevelingCore.LOGGER.at(Level.WARNING)
                        .log("Invalid quantity for " + itemNameId + ": " + quantityString + " (line: " + line + ")");
                    continue;
                }

                int lvl;
                try {
                    lvl = Integer.parseInt(lvlString);
                } catch (NumberFormatException nfe) {
                    LevelingCore.LOGGER.at(Level.WARNING)
                        .log("Invalid level for " + itemNameId + ": " + lvlString + " (line: " + line + ")");
                    continue;
                }

                out.computeIfAbsent(lvl, k -> new ArrayList<>())
                    .add(new RewardEntry(itemNameId, quantity));
            }
        }

        return out;
    }
}
