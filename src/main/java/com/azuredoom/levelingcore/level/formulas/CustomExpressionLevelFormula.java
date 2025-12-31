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
