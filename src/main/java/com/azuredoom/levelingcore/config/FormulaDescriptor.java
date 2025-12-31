package com.azuredoom.levelingcore.config;

/**
 * A descriptor class for defining the type and parameters of a leveling formula.
 * <p>
 * This record is used to represent the configuration of a formula within the LevelingCore system. It provides a
 * structured way to specify the formula type and its associated parameters.
 * <p>
 * Fields:
 * <p>
 * - type: The type of the formula (e.g., "EXPONENTIAL", "LINEAR"). Determines how the experience or leveling system is
 * calculated.
 * <p>
 * - params: A string representation of specific parameters required for the formula type. This may include details such
 * as base XP, exponent, or XP per level depending on the formula.
 */
public record FormulaDescriptor(
    String type,
    String params
) {}
