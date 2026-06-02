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

public class RemoveStatsCommand extends AbstractAsyncCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<StatType> statArg;

    @Nonnull
    private final RequiredArg<Integer> valueArg;

    public RemoveStatsCommand() {
        super("removestats", "Remove stats from player");
        // this.requirePermission("levelingcore.addstats");
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to remove stats to.",
            ArgTypes.PLAYER_REF
        );
        this.statArg = this.withRequiredArg(
            "stat",
            "Stat to add to (str, agi, per, vit, int, con).",
            StatTypeArgumentType.INSTANCE
        );
        this.valueArg = this.withRequiredArg(
            "value",
            "Amount of stat points to remove.",
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
                var current = levelService.getStr(playerUUID);
                levelService.setStr(playerUUID, current - value);
                commandContext.sendMessage(
                    CommandLang.REMOVE_STATS.param("stat", "strength")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case AGI -> {
                var current = levelService.getAgi(playerUUID);
                levelService.setAgi(playerUUID, current - value);
                commandContext.sendMessage(
                    CommandLang.REMOVE_STATS.param("stat", "agility")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case PER -> {
                var current = levelService.getPer(playerUUID);
                levelService.setPer(playerUUID, current - value);
                commandContext.sendMessage(
                    CommandLang.REMOVE_STATS.param("stat", "perception")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case VIT -> {
                var current = levelService.getVit(playerUUID);
                levelService.setVit(playerUUID, current - value);
                commandContext.sendMessage(
                    CommandLang.REMOVE_STATS.param("stat", "vitality")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case INT -> {
                var current = levelService.getInt(playerUUID);
                levelService.setInt(playerUUID, current - value);
                commandContext.sendMessage(
                    CommandLang.REMOVE_STATS.param("stat", "intelligence")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case CON -> {
                var current = levelService.getCon(playerUUID);
                levelService.setCon(playerUUID, current - value);
                commandContext.sendMessage(
                    CommandLang.REMOVE_STATS.param("stat", "constitution")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
