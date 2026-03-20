package com.azuredoom.levelingcore.level.itemlevellock;

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

public final class WeaponStatRequirementMapping {

    public static final String FILE_NAME = "weaponstatrequirements.csv";

    public static final String RESOURCE_DEFAULT = "/defaultweaponstatrequirements.csv";

    private WeaponStatRequirementMapping() {}

    public static Map<String, ItemStatRequirement> loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var configPath = dataDir.resolve(FILE_NAME);

            if (Files.notExists(configPath)) {
                try (var in = ConfigManager.class.getResourceAsStream(RESOURCE_DEFAULT)) {
                    if (in == null) {
                        throw new LevelingCoreException(
                            "defaultweaponstatrequirements.csv not found at " + RESOURCE_DEFAULT
                        );
                    }

                    LevelingCore.LOGGER.at(Level.INFO)
                        .log("Creating default Weapon Stat Requirement config at " + configPath);

                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            return readCsv(configPath);
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load weapon stat requirements", e);
        }
    }

    private static Map<String, ItemStatRequirement> readCsv(Path csvPath) throws Exception {
        Map<String, ItemStatRequirement> out = new LinkedHashMap<>();

        try (var reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstNonEmptyLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                if (firstNonEmptyLine) {
                    firstNonEmptyLine = false;
                    if (line.equalsIgnoreCase("itemnameid,str,agi,per,vit,int,con")) {
                        continue;
                    }
                }

                var parts = line.split(",", 7);
                if (parts.length != 7) {
                    LevelingCore.LOGGER.at(Level.WARNING)
                        .log("Skipping invalid weapon requirement line: " + line);
                    continue;
                }

                var itemId = parts[0].trim();
                if (itemId.isEmpty())
                    continue;

                try {
                    out.put(
                        itemId,
                        new ItemStatRequirement(
                            Integer.parseInt(parts[1].trim()),
                            Integer.parseInt(parts[2].trim()),
                            Integer.parseInt(parts[3].trim()),
                            Integer.parseInt(parts[4].trim()),
                            Integer.parseInt(parts[5].trim()),
                            Integer.parseInt(parts[6].trim())
                        )
                    );
                } catch (NumberFormatException ex) {
                    LevelingCore.LOGGER.at(Level.WARNING)
                        .log("Skipping invalid stat requirement line: " + line);
                }
            }
        }

        return out;
    }
}
