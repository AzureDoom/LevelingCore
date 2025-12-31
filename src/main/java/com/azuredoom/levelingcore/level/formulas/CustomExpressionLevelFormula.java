package com.azuredoom.levelingcore.level.formulas;

import com.azuredoom.levelingcore.LevelingCoreException;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Map;

/**
 * A customizable implementation of the {@link LevelFormula} interface that allows XP-to-level calculations using a
 * user-defined mathematical expression.
 * <p>
 * The XP progression is defined by an expression string that is evaluated dynamically for a given level. This enables
 * highly flexible leveling systems without requiring custom Java code.
 * <h2>Expression Support</h2>
 * <p>
 * The expression represents the <em>XP floor</em> required to reach a specific level and may reference:
 * <ul>
 * <li>The built-in variable {@code level} (an integer {@code >= 1})</li>
 * <li>Any number of user-defined constants supplied via the constants map</li>
 * </ul>
 * <h2>Examples</h2>
 * <p>
 * Valid expressions include:
 * <ul>
 * <li>{@code "100 * level"}</li>
 * <li>{@code "level^2 + 50"}</li>
 * <li>{@code "a * level + b"} (with constants {@code a} and {@code b})</li>
 * <li>{@code "exp(a * (level - 1)) * b"}</li>
 * </ul>
 * <h2>Behavior</h2>
 * <ul>
 * <li>{@link #getXpForLevel(int)} evaluates the expression to determine the minimum XP required for a level.</li>
 * <li>{@link #getLevelForXp(long)} determines the highest level whose XP floor is less than or equal to the provided XP
 * value.</li>
 * <li>Level lookup is performed using a binary search up to the configured maximum level.</li>
 * </ul>
 * <h2>Constraints</h2>
 * <p>
 * This class enforces the following invariants:
 * <ul>
 * <li>The expression string must not be {@code null} or blank.</li>
 * <li>The maximum level must be {@code >= 1}.</li>
 * <li>Level inputs must be {@code >= 1}.</li>
 * <li>The expression should be <em>monotonically increasing</em> with respect to {@code level} to ensure correct level
 * calculation.</li>
 * </ul>
 * <h2>Notes</h2>
 * <ul>
 * <li>Non-monotonic expressions may result in undefined or incorrect leveling behavior.</li>
 * </ul>
 */
public class CustomExpressionLevelFormula implements LevelFormula {

    private final String expressionText;

    private final Map<String, Double> constants;

    private final int maxLevel;

    /**
     * Constructs a new CustomExpressionLevelFormula instance, which evaluates experience point (XP) progression using
     * a custom mathematical expression. The formula is defined by a string expression and uses constants for calculation.
     * This constructor validates the provided parameters and initializes the instance with the given configuration.
     *
     * @param xpForLevelExpression The mathematical expression used to compute XP for a specific level.
     *                              Must not be null or blank. This expression may include variables such as "level"
     *                              and constants provided in the `constants` map.
     * @param constants            A map of constant values that will be used in the expression evaluation.
     *                              If null, an empty map will be used.
     * @param maxLevel             The maximum level supported by this formula. Must be greater than or equal to 1.
     * @throws LevelingCoreException If `xpForLevelExpression` is null, blank, or if `maxLevel` is less than 1.
     */
    public CustomExpressionLevelFormula(
        String xpForLevelExpression,
        Map<String, Double> constants,
        int maxLevel
    ) {
        if (xpForLevelExpression == null || xpForLevelExpression.isBlank()) {
            throw new LevelingCoreException("custom.xpForLevel must not be blank");
        }
        if (maxLevel < 1) {
            throw new LevelingCoreException("maxLevel must be >= 1");
        }

        this.expressionText = xpForLevelExpression.trim();
        this.constants = (constants == null) ? Map.of() : Map.copyOf(constants);
        this.maxLevel = maxLevel;
    }

    /**
     * Calculates the total experience points (XP) required to reach the specified level.
     * The calculation is performed using a custom mathematical expression defined in the instance.
     * If the evaluated value exceeds the maximum possible value for a long type, {@code Long.MAX_VALUE} is returned.
     * If the evaluated value is non-finite or invalid, the result is clamped accordingly.
     *
     * @param level The target level for which XP is to be calculated. Must be greater than or equal to 1.
     * @return The total XP required to reach the specified level. Returns {@code Long.MAX_VALUE} for overflow
     *         and {@code 0L} for invalid or out-of-bound values.
     * @throws LevelingCoreException If the provided level is less than 1.
     */
    @Override
    public long getXpForLevel(int level) {
        if (level < 1) {
            throw new LevelingCoreException("level must be >= 1");
        }

        var value = eval(level);
        if (!Double.isFinite(value) || value >= Long.MAX_VALUE)
            return Long.MAX_VALUE;
        if (value <= 0) {
            return 0L;
        }

        return (long) Math.ceil(value);
    }

    /**
     * Determines the level corresponding to the given number of experience points (XP).
     * This method uses a binary search to find the level within the range of possible levels
     * where the XP value lies. It ensures that the result is clamped between level 1 and the
     * maximum supported level.
     *
     * @param xp The total experience points for which the corresponding level is to be determined.
     *           Must be greater than or equal to 0.
     * @return The level corresponding to the specified XP. Returns 1 if the provided XP is less
     *         than the XP required for level 1, and the maximum level if the provided XP is
     *         greater than or equal to the maximum XP.
     * @throws IllegalArgumentException If the provided XP is negative.
     */
    @Override
    public int getLevelForXp(long xp) {
        if (xp < 0)
            throw new IllegalArgumentException("xp must be >= 0");

        if (getXpForLevel(1) > xp) {
            return 1;
        }
        if (getXpForLevel(maxLevel) <= xp) {
            return maxLevel;
        }

        var lo = 1;
        var hi = maxLevel;

        while (lo < hi) {
            var mid = lo + ((hi - lo + 1) / 2);
            var midXp = getXpForLevel(mid);

            if (midXp <= xp) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }

        return lo;
    }

    /**
     * Evaluates the expression defined in the instance using the provided level and constants. This method builds a
     * mathematical expression based on the configured expression text, substitutes the "level" variable with the given
     * level value, and substitutes additional constants before evaluating the result.
     *
     * @param level The level value to be substituted into the expression. Determines the context of the calculation.
     * @return The result of evaluating the expression after substituting the "level" variable and all defined
     *         constants.
     */
    private double eval(int level) {
        var builder = new ExpressionBuilder(expressionText)
            .variable("level");

        for (var k : constants.keySet()) {
            builder.variable(k);
        }

        var exp = builder.build();
        exp.setVariable("level", level);

        for (var e : constants.entrySet()) {
            exp.setVariable(e.getKey(), e.getValue());
        }

        return exp.evaluate();
    }
}
