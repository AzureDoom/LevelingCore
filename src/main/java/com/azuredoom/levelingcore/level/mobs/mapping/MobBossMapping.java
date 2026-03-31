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

public final class MobBossMapping {

    public static final String FILE_NAME = "mobbossmapping.csv";

    public static final String RESOURCE_DEFAULT = "/defaultmobbossmapping.csv";

    private MobBossMapping() {}

    public static List<BossRule> loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var configPath = dataDir.resolve(FILE_NAME);

            if (Files.notExists(configPath)) {
                try (InputStream in = ConfigManager.class.getResourceAsStream(RESOURCE_DEFAULT)) {
                    if (in == null) {
                        throw new LevelingCoreException("defaultmobbossmapping.csv not found");
                    }
                    LevelingCore.LOGGER.at(Level.INFO)
                        .log("Creating default boss mapping config at " + configPath);
                    Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            return readCsv(configPath);
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load boss mapping config", e);
        }
    }

    private static List<BossRule> readCsv(Path csvPath) throws Exception {
        List<BossRule> out = new ArrayList<>();

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
                    if (line.equalsIgnoreCase("instance,boss_name,lvl")) {
                        continue;
                    }
                }

                var parts = line.split(",", 3);
                if (parts.length != 3) {
                    continue;
                }

                var instancePattern = parts[0].trim().toLowerCase();
                var bossName = parts[1].trim().toLowerCase();
                var lvl = Integer.parseInt(parts[2].trim());

                out.add(new BossRule(instancePattern, bossName, lvl));
            }
        }

        return out;
    }

    /**
     * Determines the level of a boss in a particular instance by evaluating a list of rules. The method finds the most
     * specific rule that matches the given instance and boss names and returns its associated level.
     *
     * @param rules        the list of {@code BossRule} objects defining instance-boss associations and levels
     * @param instanceName the name of the instance to locate
     * @param bossName     the name of the boss to locate within the instance
     * @return the level of the boss if a matching {@code BossRule} is found; -1 if the provided instance or boss names
     *         are {@code null}, or no rules match
     */
    public static int findLevel(List<BossRule> rules, String instanceName, String bossName) {
        if (instanceName == null || bossName == null) {
            return -1;
        }

        var normalizedInstance = instanceName.trim().toLowerCase();
        var normalizedBoss = bossName.trim().toLowerCase();

        BossRule bestMatch = null;

        for (var rule : rules) {
            if (!rule.matches(normalizedInstance, normalizedBoss)) {
                continue;
            }

            if (bestMatch == null || rule.specificity() > bestMatch.specificity()) {
                bestMatch = rule;
            }
        }

        return bestMatch != null ? bestMatch.level() : -1;
    }

    /**
     * Represents a rule that defines associations between instance patterns, boss names, and their respective levels.
     */
    public record BossRule(
        String instancePattern,
        String bossName,
        int level
    ) {

        public BossRule {
            instancePattern = instancePattern.trim().toLowerCase();
            bossName = bossName.trim().toLowerCase();
        }

        public int specificity() {
            return MappingMatcher.specificity(instancePattern);
        }

        public boolean matches(String instanceName, String bossName) {
            return this.bossName.equals(bossName)
                && MappingMatcher.wildcardMatch(this.instancePattern, instanceName);
        }
    }
}
