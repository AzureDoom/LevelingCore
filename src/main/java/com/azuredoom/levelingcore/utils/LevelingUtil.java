package com.azuredoom.levelingcore.utils;

import java.util.Locale;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.level.formulas.loader.LevelTableLoader;

public class LevelingUtil {

    private LevelingUtil() {}

    /**
     * Computes the maximum level based on the leveling configuration and formula type. The formula type determines
     * which specific logic and configuration are used to calculate the maximum level.
     *
     * @return the computed maximum level as an integer. The value depends on the formula type specified in the
     *         configuration (e.g., LINEAR, TABLE, CUSTOM, or EXPONENTIAL).
     */
    public static int computeMaxLevel() {
        var internalConfig = LevelingCore.levelingCoreConfig;
        var type = internalConfig.formula.type.trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "LINEAR" -> internalConfig.formula.linear.maxLevel;
            case "TABLE" -> {
                var tableFormula = LevelTableLoader.loadOrCreateFromDataDir(internalConfig.formula.table.file);
                yield Math.max(1, tableFormula.getMaxLevel());
            }
            case "CUSTOM" -> internalConfig.formula.custom.maxLevel;
            default -> internalConfig.formula.exponential.maxLevel;
        };
    }
}
