package com.azuredoom.levelingcore.level.stats;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class StatTypeArgumentType extends ArgumentType<StatType> {

    public static final StatTypeArgumentType INSTANCE = new StatTypeArgumentType();

    private static final String[] EXAMPLES = {
        "str",
        "agi",
        "per",
        "vit",
        "int",
        "con"
    };

    private StatTypeArgumentType() {
        super(
            "levelingcore.argument.stat",
            Message.raw("A valid stat type"),
            1,
            EXAMPLES
        );
    }

    /**
     * Parses the first token of {@code input} into a {@link StatType}.
     * <p>
     * Fails the parse result with a descriptive message if the input is empty or the token does not match any known
     * stat abbreviation ({@code str, agi, per, vit, int, con}).
     *
     * @param input       the raw command tokens to parse from
     * @param parseResult used to signal a parse failure with a player-facing error message
     * @return the matched {@link StatType}, or {@code null} if parsing failed
     */
    @Override
    @Nullable
    public StatType parse(@Nonnull String[] input, @Nonnull ParseResult parseResult) {
        if (input.length == 0) {
            parseResult.fail(Message.raw("Missing stat."));
            return null;
        }

        var entered = input[0];
        var statType = StatType.fromString(entered);

        if (statType == null) {
            parseResult.fail(
                Message.raw(
                    "Invalid stat '" + entered + "'. Valid stats: str, agi, per, vit, int, con"
                )
            );
            return null;
        }

        return statType;
    }

    /**
     * Populates tab-completion suggestions by filtering known stat abbreviations against the text the player has
     * already typed.
     * <p>
     * Matching is case-insensitive; only abbreviations that start with the entered prefix are added to {@code result}.
     *
     * @param sender             the command sender requesting suggestions
     * @param textAlreadyEntered the partial text the player has typed so far
     * @param numParametersTyped the number of parameters that have been fully typed
     * @param result             the suggestion result to populate
     */
    @Override
    public void suggest(
        @Nonnull CommandSender sender,
        @Nonnull String textAlreadyEntered,
        int numParametersTyped,
        @Nonnull SuggestionResult result
    ) {
        var entered = textAlreadyEntered.toLowerCase();

        Arrays.stream(EXAMPLES)
            .filter(id -> id.startsWith(entered))
            .forEach(result::suggest);
    }
}
