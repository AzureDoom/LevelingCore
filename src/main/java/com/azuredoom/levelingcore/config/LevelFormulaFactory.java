package com.azuredoom.levelingcore.config;

import com.azuredoom.levelingcore.LevelingCoreException;
import com.azuredoom.levelingcore.Main;
import com.azuredoom.levelingcore.level.formulas.CustomExpressionLevelFormula;
import com.azuredoom.levelingcore.level.formulas.ExponentialLevelFormula;
import com.azuredoom.levelingcore.level.formulas.LevelFormula;
import com.azuredoom.levelingcore.level.formulas.LinearLevelFormula;
import com.azuredoom.levelingcore.level.formulas.loader.LevelTableLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A factory class for creating instances of {@link LevelFormula} and related objects based on configuration or
 * descriptors. This class supports dynamic initialization of different implementations of {@link LevelFormula}, such as
 * exponential or linear formulas, depending on the input.
 * <p>
 * This class is utility-based and cannot be instantiated.
 */
public final class LevelFormulaFactory {

    private LevelFormulaFactory() {}

    /**
     * Constructs a {@link LevelFormula} object based on the provided configuration. The method determines the type of
     * formula to use (e.g., "EXPONENTIAL", "LINEAR", "TABLE", and "CUSTOM") and initializes the appropriate
     * implementation with the parameters specified in the configuration. If the configuration is null or invalid, a
     * default {@link ExponentialLevelFormula} is returned. formula: custom: exp(a) * b/c
     *
     * @param config the configuration object containing the formula type and its associated parameters. Must not be
     *               null and must specify a valid type ("EXPONENTIAL", "LINEAR", "TABLE", or "CUSTOM").
     * @return an instance of {@link LevelFormula}, either {@link ExponentialLevelFormula} or
     *         {@link LinearLevelFormula}, depending on the type specified in the configuration.
     * @throws LevelingCoreException if the specified formula type is unknown or unsupported.
     */
    public static LevelFormula fromConfig(LevelingCoreConfig config) {
        if (config == null || config.formula == null || config.formula.type == null) {
            return new ExponentialLevelFormula(100, 1.7);
        }

        String type = config.formula.type.trim().toUpperCase(Locale.ROOT);

        return switch (type) {
            case "EXPONENTIAL" -> {
                double baseXp = config.formula.exponential.baseXp;
                double exponent = config.formula.exponential.exponent;
                yield new ExponentialLevelFormula(baseXp, exponent);
            }
            case "LINEAR" -> {
                long xpPerLevel = config.formula.linear.xpPerLevel;
                yield new LinearLevelFormula(xpPerLevel);
            }
            case "TABLE" -> {
                var dataDir = Main.configPath;
                yield LevelTableLoader.loadOrCreateFromDataDir(dataDir, config.formula.table.file);
            }
            case "CUSTOM" -> {
                var expr = config.formula.custom.xpForLevel;
                var constants = config.formula.custom.constants;
                var maxLevel = config.formula.custom.maxLevel;
                yield new CustomExpressionLevelFormula(expr, constants, maxLevel);
            }
            default -> throw new LevelingCoreException(
                "Unknown formula.type '" + config.formula.type + "'. Expected EXPONENTIAL or LINEAR."
            );
        };
    }

    /**
     * Constructs a {@link FormulaDescriptor} based on the provided {@link LevelingCoreConfig}. The type and parameters
     * for the descriptor are determined by the configuration's formula settings. Supports the following formula types:
     * "EXPONENTIAL", "LINEAR", "TABLE", and "CUSTOM". Throws an exception if an unsupported formula type is specified.
     *
     * @param cfg the configuration object containing the formula type and its relevant parameter values
     * @return a {@link FormulaDescriptor} instance encapsulating the formula type and its parameters
     * @throws LevelingCoreException if the formula type specified in the configuration is unknown
     */
    public static FormulaDescriptor descriptorFromConfig(LevelingCoreConfig cfg) {
        var type = cfg.formula.type.trim().toUpperCase(java.util.Locale.ROOT);
        return switch (type) {
            case "EXPONENTIAL" -> new FormulaDescriptor(
                "EXPONENTIAL",
                "baseXp=" + cfg.formula.exponential.baseXp + ";exponent=" + cfg.formula.exponential.exponent
            );
            case "LINEAR" -> new FormulaDescriptor(
                "LINEAR",
                "xpPerLevel=" + cfg.formula.linear.xpPerLevel
            );
            case "TABLE" -> new FormulaDescriptor(
                "TABLE",
                "file=" + cfg.formula.table.file
            );
            case "CUSTOM" -> {
                var expr = cfg.formula.custom.xpForLevel == null ? "" : cfg.formula.custom.xpForLevel;
                var exprB64 = b64(expr);
                var constantsB64 = encodeConstants(cfg.formula.custom.constants);
                var maxLevel = cfg.formula.custom.maxLevel;

                yield new FormulaDescriptor(
                    "CUSTOM",
                    "exprB64=" + exprB64 + ";constB64=" + constantsB64 + ";maxLevel=" + maxLevel
                );
            }
            default -> throw new LevelingCoreException("Unknown formula.type: " + cfg.formula.type);
        };
    }

    /**
     * Converts a {@link FormulaDescriptor} into a {@link LevelFormula} based on the descriptor's type and parameters.
     * The supported formula types are "EXPONENTIAL", "LINEAR", "TABLE", and "CUSTOM", each with specific parameter
     * requirements.
     *
     * @param d the formula descriptor containing the type and parameters for constructing the level formula
     * @return a {@link LevelFormula} instance constructed according to the descriptor
     * @throws LevelingCoreException if the descriptor contains an unknown formula type
     */
    public static LevelFormula formulaFromDescriptor(FormulaDescriptor d, Path dataDir) {
        var type = d.type().trim().toUpperCase(Locale.ROOT);

        Map<String, String> map = new HashMap<>();
        if (d.params() != null && !d.params().isBlank()) {
            for (var part : d.params().split(";")) {
                var kv = part.split("=", 2);
                if (kv.length == 2)
                    map.put(kv[0].trim(), kv[1].trim());
            }
        }

        return switch (type) {
            case "EXPONENTIAL" -> new ExponentialLevelFormula(
                Double.parseDouble(map.getOrDefault("baseXp", "100.0")),
                Double.parseDouble(map.getOrDefault("exponent", "1.7"))
            );
            case "LINEAR" -> new LinearLevelFormula(
                Long.parseLong(map.getOrDefault("xpPerLevel", "100"))
            );
            case "TABLE" -> {
                var file = map.getOrDefault("file", "levels.csv");
                yield LevelTableLoader.loadOrCreateFromDataDir(dataDir, file);
            }
            case "CUSTOM" -> {
                var expr = unb64(map.getOrDefault("exprB64", b64("")));
                var constants = decodeConstants(map.getOrDefault("constB64", ""));
                var maxLevel = Integer.parseInt(map.getOrDefault("maxLevel", "100000"));

                yield new CustomExpressionLevelFormula(expr, constants, maxLevel);
            }
            default -> throw new LevelingCoreException("Unknown stored formula.type: " + d.type());
        };
    }

    /**
     * Encodes the given string into a Base64 URL-safe format without padding.
     *
     * @param s the input string to be encoded; must not be null
     * @return a Base64 URL-safe representation of the input string
     */
    private static String b64(String s) {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a Base64 URL-safe encoded string into its original plain-text representation.
     *
     * @param s the Base64 URL-safe encoded string to decode; must not be null
     * @return the decoded plain-text string
     */
    private static String unb64(String s) {
        return new String(Base64.getUrlDecoder().decode(s), StandardCharsets.UTF_8);
    }

    /**
     * Encodes a map of constants into a Base64 URL-safe encoded string representation. The method sorts the map entries
     * by key, formats each entry as "key=value", joins them with commas, and then encodes the resulting string into a
     * Base64 URL-safe format.
     *
     * @param constants a map containing constant names (as keys) and their corresponding numeric values (as values);
     *                  must not be null or empty
     * @return a Base64 URL-safe encoded string representing the sorted and formatted map entries; returns an empty
     *         string if the input map is null or empty
     */
    private static String encodeConstants(Map<String, Double> constants) {
        if (constants == null || constants.isEmpty()) {
            return "";
        }
        var raw = constants.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(","));
        return b64(raw);
    }

    /**
     * Decodes a Base64 URL-safe encoded string into a map of constants. The input string is first Base64 decoded and
     * then split into key-value pairs separated by commas. Each key-value pair is further split by an equals sign
     * ("="). The keys are treated as strings and the values as doubles.
     *
     * @param b64 the Base64 URL-safe encoded string containing comma-separated key-value pairs. Each pair must be in
     *            the format "key=value". The key is a string and the value is parsed as a double. If the input string
     *            is null, empty, or invalid, an empty map is returned.
     * @return a map containing the decoded constants as key-value pairs, where keys are strings and values are doubles.
     *         If the input is null, empty, or invalid, the result will be an empty map.
     */
    private static Map<String, Double> decodeConstants(String b64) {
        Map<String, Double> out = new HashMap<>();
        if (b64 == null || b64.isBlank()) {
            return out;
        }

        var raw = unb64(b64);
        if (raw.isBlank())
            return out;

        for (var part : raw.split(",")) {
            var kv = part.split("=", 2);
            if (kv.length == 2) {
                out.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
            }
        }
        return out;
    }
}
