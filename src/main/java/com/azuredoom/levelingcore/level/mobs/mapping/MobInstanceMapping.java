package com.azuredoom.levelingcore.level.mobs.mapping;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.internal.ConfigManager;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;
import com.azuredoom.levelingcore.utils.MappingMatcher;

public class MobInstanceMapping {

    public static final String FILE_NAME = "mobinstancemapping.csv";

    public static final String RESOURCE_DEFAULT = "/defaultmobinstancemapping.csv";

    private MobInstanceMapping() {}

    public static List<InstanceRule> loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var configPath = dataDir.resolve(FILE_NAME);

            if (Files.notExists(configPath)) {
                try (InputStream in = ConfigManager.class.getResourceAsStream(RESOURCE_DEFAULT)) {
                    if (in == null) {
                        throw new LevelingCoreException(
                            "defaultmobinstancemapping.csv not found in resources (expected at "
                                + RESOURCE_DEFAULT + ")"
                        );
                    }
                    LevelingCore.LOGGER.at(Level.INFO)
                        .log("Creating default Mob Instance Levels Mapping config at " + configPath);
                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            var mapping = readXpCsv(configPath);

            LevelingCore.LOGGER.at(Level.INFO)
                .log("Loaded Mob Instance Levels Mapping from " + configPath + " (" + mapping.size() + " entries)");
            return mapping;

        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load Mob Instance Levels Mapping config", e);
        }
    }

    private static List<InstanceRule> readXpCsv(Path csvPath) throws Exception {
        List<InstanceRule> out = new ArrayList<>();

        try (var reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstNonEmptyLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (firstNonEmptyLine) {
                    firstNonEmptyLine = false;
                    if (line.equalsIgnoreCase("instance,lvl")) {
                        continue;
                    }
                }

                var parts = line.split(",", 2);
                if (parts.length != 2) {
                    LevelingCore.LOGGER.at(Level.WARNING).log("Skipping invalid CSV line: " + line);
                    continue;
                }

                var instancePattern = parts[0].trim().toLowerCase();
                var lvlStr = parts[1].trim();

                if (instancePattern.isEmpty()) {
                    LevelingCore.LOGGER.at(Level.WARNING).log("Skipping CSV line with empty instance: " + line);
                    continue;
                }

                int lvl;
                try {
                    lvl = Integer.parseInt(lvlStr);
                } catch (NumberFormatException nfe) {
                    LevelingCore.LOGGER.at(Level.WARNING)
                        .log("Invalid Instance value for " + instancePattern + ": " + lvlStr + " (line: " + line + ")");
                    continue;
                }

                out.add(new InstanceRule(instancePattern, lvl));
            }
        }

        return out;
    }

    /**
     * Determines the level associated with the specified instance name by evaluating a list of rules. The method looks
     * for the most specific rule that matches the instance name and returns its corresponding level.
     *
     * @param rules        the list of {@code InstanceRule} objects defining instance patterns and their associated
     *                     levels
     * @param instanceName the name of the instance to evaluate
     * @return the level corresponding to the most specific matching rule; -1 if the provided instance name is null,
     *         blank, or no rules match
     */
    public static int findLevel(List<InstanceRule> rules, String instanceName) {
        if (instanceName == null || instanceName.isBlank()) {
            return -1;
        }

        var normalizedInstance = instanceName.trim().toLowerCase();
        InstanceRule bestRule = null;

        for (var rule : rules) {
            if (!rule.matches(normalizedInstance)) {
                continue;
            }

            if (bestRule == null || rule.specificity() > bestRule.specificity()) {
                bestRule = rule;
            }
        }

        return bestRule != null ? bestRule.level() : -1;
    }

    /**
     * Represents a rule that defines an association between an instance pattern and a level.
     */
    public record InstanceRule(
        String instancePattern,
        int level
    ) {

        public InstanceRule {
            instancePattern = instancePattern.trim().toLowerCase();
        }

        public int specificity() {
            return MappingMatcher.specificity(instancePattern);
        }

        public boolean matches(String instanceName) {
            return MappingMatcher.wildcardMatch(instancePattern, instanceName);
        }
    }
}
