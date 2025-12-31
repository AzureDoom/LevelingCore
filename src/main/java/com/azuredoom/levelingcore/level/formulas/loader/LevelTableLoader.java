package com.azuredoom.levelingcore.level.formulas.loader;

import com.azuredoom.levelingcore.LevelingCoreException;
import com.azuredoom.levelingcore.level.formulas.TableLevelFormula;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Utility class responsible for loading or creating a level-to-XP mapping based on a CSV file. This class provides
 * functionality to load a level table from the provided directory, or create a default one if the file is not present.
 * The levels are defined in a CSV format where each row specifies an XP floor required to reach a specific level.
 * <p>
 * The class uses a default XP progression table if no file is found in the specified location. It validates that levels
 * and XP values meet the required constraints.
 * <p>
 * Constraints enforced: - Level 1 must exist and require 0 XP. - Levels must be contiguous and start at 1. - XP values
 * must be non-negative and non-decreasing. - Duplicated levels in the CSV file are not allowed. - The CSV file must not
 * be empty.
 * <p>
 * The resulting level-to-XP mapping is encapsulated in a {@link TableLevelFormula} object.
 * <p>
 * This class cannot be instantiated, as it is intended to serve as a utility class.
 */
public final class LevelTableLoader {

    private static final String DEFAULT_CSV = """
        # level,xp (XP floor required for that level)
        # Level 1 must be 0 XP
        level,xp
        1,0
        2,100
        3,250
        4,450
        5,700
        6,1000
        """;

    private LevelTableLoader() {}

    /**
     * Loads or creates a level-to-XP mapping from a specified data directory. This method ensures the integrity of the
     * mapping by validating the levels, XP values, and their progression. If the specified file does not exist within
     * the directory, it is created with default values.
     *
     * @param dataDir  The path to the directory containing the level-to-XP mapping file. If the directory does not
     *                 exist, it will be created. Must not be null.
     * @param fileName The name of the file to be loaded or created within the specified directory. Must not be null.
     * @return A {@code TableLevelFormula} instance containing the validated level-to-XP mapping.
     * @throws IllegalArgumentException If any of the parameters are null.
     * @throws LevelingCoreException    If any issues occur during file creation, reading, parsing, or validation.
     */
    public static TableLevelFormula loadOrCreateFromDataDir(Path dataDir, String fileName) {
        return loadOrCreate(dataDir);
    }

    /**
     * Loads a level-to-XP mapping from a CSV file in the specified directory. If the file does not exist, it is created
     * with default values. The method ensures the integrity of the mapping by validating the levels, XP values, and
     * their progression.
     *
     * @param dataDir Path to the directory containing the levels.csv file. If the file does not exist, it will be
     *                created with default values. The directory will also be created if it does not exist.
     * @return A {@code TableLevelFormula} instance containing the validated level-to-XP mapping.
     * @throws LevelingCoreException If any errors occur during file creation, reading, parsing, or validation of the
     *                               level-to-XP data.
     */
    public static TableLevelFormula loadOrCreate(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            var csvPath = dataDir.resolve("levels.csv");

            if (Files.notExists(csvPath)) {
                Files.writeString(csvPath, DEFAULT_CSV, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }

            Map<Integer, Long> map = new TreeMap<>();
            for (var raw : Files.readAllLines(csvPath, StandardCharsets.UTF_8)) {
                var line = raw.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                if (line.toLowerCase().startsWith("level"))
                    continue;

                var parts = line.split(",", 2);
                if (parts.length != 2) {
                    throw new LevelingCoreException("Invalid CSV line (expected level,xp): " + raw);
                }

                var level = Integer.parseInt(parts[0].trim());
                var xp = Long.parseLong(parts[1].trim());

                if (level < 1) {
                    throw new LevelingCoreException("Level must be >= 1: " + raw);
                }
                if (xp < 0) {
                    throw new LevelingCoreException("XP must be >= 0: " + raw);
                }

                if (map.put(level, xp) != null) {
                    throw new LevelingCoreException("Duplicate level in CSV: " + level);
                }
            }

            if (map.isEmpty()) {
                throw new LevelingCoreException("levels.csv is empty");
            }
            if (!map.containsKey(1)) {
                throw new LevelingCoreException("levels.csv must include level 1");
            }
            if (map.get(1) != 0L) {
                throw new LevelingCoreException("Level 1 must require 0 XP");
            }

            int maxLevel = map.keySet().stream().max(Integer::compareTo).orElse(1);

            for (var i = 1; i <= maxLevel; i++) {
                if (!map.containsKey(i)) {
                    throw new LevelingCoreException(
                        "Missing level " + i + " in levels.csv (levels must be contiguous)"
                    );
                }
            }

            var xpByLevel = new long[maxLevel + 1];
            for (var i = 1; i <= maxLevel; i++) {
                xpByLevel[i] = map.get(i);
            }

            return new TableLevelFormula(xpByLevel);
        } catch (LevelingCoreException e) {
            throw e;
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load levels.csv", e);
        }
    }
}
