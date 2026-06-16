package com.azuredoom.levelingcore.commands.stats;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.level.stats.StatType;
import com.azuredoom.levelingcore.level.stats.StatTypeArgumentType;

public class SetStatsCommand extends AbstractAsyncCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<StatType> statArg;

    @Nonnull
    private final RequiredArg<Integer> valueArg;

    public SetStatsCommand() {
        super("setstats", "Set stats value to player");
        // this.requirePermission("levelingcore.addstats");
        this.playerArg = this.withRequiredArg(
            "player",
            "Player whom's stats to modify.",
            ArgTypes.PLAYER_REF
        );
        this.statArg = this.withRequiredArg(
            "stat",
            "Stat to modify (str, agi, per, vit, int, con).",
            StatTypeArgumentType.INSTANCE
        );
        this.valueArg = this.withRequiredArg(
            "value",
            "Amount of stat points to set.",
            ArgTypes.INTEGER
        );
    }

    @NotNull
    @Override
    protected CompletableFuture<Void> executeAsync(@NotNull CommandContext commandContext) {
        var levelService = LevelingCore.getLevelService();

        if (levelService == null) {
            commandContext.sendMessage(CommandLang.NOT_INITIALIZED);
            return CompletableFuture.completedFuture(null);
        }

        var playerRef = this.playerArg.get(commandContext);
        var statType = this.statArg.get(commandContext);
        var value = this.valueArg.get(commandContext);
        var playerUUID = playerRef.getUuid();

        switch (statType) {
            case STR -> {
                levelService.setStr(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "strength")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case AGI -> {
                levelService.setAgi(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "agility")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case PER -> {
                levelService.setPer(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "perception")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case VIT -> {
                levelService.setVit(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "vitality")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case INT -> {
                levelService.setInt(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "intelligence")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case CON -> {
                levelService.setCon(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "constitution")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
