package com.azuredoom.levelingcore.level.stats;

import java.util.Locale;

public enum StatType {

    STR,
    AGI,
    PER,
    VIT,
    INT,
    CON;

    /**
     * Converts a string representation of a stat type into its corresponding {@code StatType} enum value. The
     * conversion is case-insensitive and supports multiple name formats for each stat type.
     *
     * @param input the string representation of the stat type (e.g., "strength", "agi", "INTELLIGENCE").
     * @return the {@code StatType} enum corresponding to the input string, or {@code null} if no match is found.
     */
    public static StatType fromString(String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "str", "strength" -> STR;
            case "agi", "agility" -> AGI;
            case "per", "perception" -> PER;
            case "vit", "vitality" -> VIT;
            case "int", "intelligence" -> INT;
            case "con", "constitution" -> CON;
            default -> null;
        };
    }
}
