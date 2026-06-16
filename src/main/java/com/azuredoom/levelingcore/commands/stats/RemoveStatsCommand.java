package com.azuredoom.levelingcore.commands.stats;

import com.hypixel.hytale.server.core.Message;
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
import com.azuredoom.levelingcore.utils.LevelingPlayerContextManager;
import com.azuredoom.levelingcore.utils.StatsUtils;

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

        var context = LevelingPlayerContextManager.getContext(playerUUID);

        if (context == null || context.entityRef() == null || !context.entityRef().isValid()) {
            return CompletableFuture.completedFuture(null);
        }

        var future = new CompletableFuture<Void>();

        if (value < 0) {
            commandContext.sendMessage(Message.raw("Value can't be below 0."));
            return future;
        }

        context.world().execute(() -> {
            try {
                boolean clamped = false;

                switch (statType) {
                    case STR -> {
                        var current = levelService.getStr(playerUUID);
                        if (current - value < 0)
                            clamped = true;
                        var newVal = Math.max(0, current - value);
                        levelService.setStr(playerUUID, newVal);
                    }
                    case AGI -> {
                        var current = levelService.getAgi(playerUUID);
                        if (current - value < 0)
                            clamped = true;
                        var newVal = Math.max(0, current - value);
                        levelService.setAgi(playerUUID, newVal);
                    }
                    case PER -> {
                        var current = levelService.getPer(playerUUID);
                        if (current - value < 0)
                            clamped = true;
                        var newVal = Math.max(0, current - value);
                        levelService.setPer(playerUUID, newVal);
                    }
                    case VIT -> {
                        var current = levelService.getVit(playerUUID);
                        if (current - value < 0)
                            clamped = true;
                        var newVal = Math.max(0, current - value);
                        levelService.setVit(playerUUID, newVal);
                    }
                    case INT -> {
                        var current = levelService.getInt(playerUUID);
                        if (current - value < 0)
                            clamped = true;
                        var newVal = Math.max(0, current - value);
                        levelService.setInt(playerUUID, newVal);
                    }
                    case CON -> {
                        var current = levelService.getCon(playerUUID);
                        if (current - value < 0)
                            clamped = true;
                        var newVal = Math.max(0, current - value);
                        levelService.setCon(playerUUID, newVal);
                    }
                }

                var pRef = playerRef.getReference();
                if (pRef != null) {
                    StatsUtils.applyStatModifiers(playerUUID, pRef.getStore(), pRef, levelService);
                }

                var statName = statType.name().toLowerCase();
                if (clamped) {
                    commandContext.sendMessage(
                        Message.raw(statName.toUpperCase() + " would go below 0, clamped to 0.")
                    );
                } else {
                    commandContext.sendMessage(
                        CommandLang.REMOVE_STATS.param("stat", statName)
                            .param("value", value)
                            .param("player", playerRef.getUsername())
                    );
                }
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning()
                    .withCause(e)
                    .log("Failed to remove stats for player {}", playerUUID);
                commandContext.sendMessage(Message.raw("Failed to update stats. Check the server log."));
            } finally {
                future.complete(null);
            }
        });

        return future;
    }
}
