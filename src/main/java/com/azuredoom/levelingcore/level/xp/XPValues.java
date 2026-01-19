package com.azuredoom.levelingcore.level.xp;

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

public class XPValues {

    public static final String FILE_NAME = "xpmapping.csv";

    public static final String RESOURCE_DEFAULT = "/defaultxpmapping.csv";

    private XPValues() {}

    public static Map<String, Integer> loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var configPath = dataDir.resolve(FILE_NAME);

            if (Files.notExists(configPath)) {
                try (InputStream in = ConfigManager.class.getResourceAsStream(RESOURCE_DEFAULT)) {
                    if (in == null) {
                        throw new LevelingCoreException(
                            "defaultxpmapping.csv not found in resources (expected at " + RESOURCE_DEFAULT + ")"
                        );
                    }
                    LevelingCore.LOGGER.at(Level.INFO).log("Creating default XP config at " + configPath);
                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            var mapping = readXpCsv(configPath);

            LevelingCore.LOGGER.at(Level.INFO)
                .log("Loaded XP mapping from " + configPath + " " + mapping.size() + " entries)");
            return mapping;

        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load XP config", e);
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
                    if (line.equalsIgnoreCase("npctypeid,xp")) {
                        continue;
                    }
                }

                var parts = line.split(",", 2);
                if (parts.length != 2) {
                    LevelingCore.LOGGER.at(Level.WARNING).log("Skipping invalid CSV line: " + line);
                    continue;
                }

                var npcTypeId = parts[0].trim();
                var xpStr = parts[1].trim();

                if (npcTypeId.isEmpty()) {
                    LevelingCore.LOGGER.at(Level.WARNING).log("Skipping CSV line with empty npcTypeId: {0}", line);
                    continue;
                }

                int xp;
                try {
                    xp = Integer.parseInt(xpStr);
                } catch (NumberFormatException nfe) {
                    LevelingCore.LOGGER.at(Level.WARNING)
                        .log(
                            "Invalid XP value for " + npcTypeId + ": " + xpStr + " (line: " + line + ")"
                        );
                    continue;
                }

                out.put(npcTypeId, xp);
            }
        }

        return out;
    }
}
