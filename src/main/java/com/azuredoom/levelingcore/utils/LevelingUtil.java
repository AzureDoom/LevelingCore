package com.azuredoom.levelingcore.utils;

import java.util.Locale;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.level.formulas.loader.LevelTableLoader;

public class LevelingUtil {

    private LevelingUtil() {}

    /**
     * Determines the maximum mob level based on the formula type defined in the LevelingCore configuration.
     * <p>
     * The value is resolved according to the configured formula type:
     * <ul>
     * <li><b>LINEAR</b> – Returns the maximum level specified in the linear formula configuration.</li>
     * <li><b>TABLE</b> – Loads the configured level table file and determines the maximum level from the table
     * data.</li>
     * <li><b>CUSTOM</b> – Returns the maximum level defined in the custom formula configuration.</li>
     * <li><b>Other / default</b> – Falls back to the maximum level defined in the exponential formula
     * configuration.</li>
     * </ul>
     *
     * @return the maximum mob level determined from the active formula configuration
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
