package com.azuredoom.levelingcore.utils;

import java.util.Locale;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.level.formulas.loader.LevelTableLoader;

public class LevelingUtil {

    private LevelingUtil() {}

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
