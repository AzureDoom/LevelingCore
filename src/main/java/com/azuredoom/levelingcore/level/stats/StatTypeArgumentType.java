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
