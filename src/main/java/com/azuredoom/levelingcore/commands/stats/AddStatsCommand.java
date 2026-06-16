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

public class AddStatsCommand extends AbstractAsyncCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<StatType> statArg;

    @Nonnull
    private final RequiredArg<Integer> valueArg;

    public AddStatsCommand() {
        super("addstats", "Add stats to player");
        // this.requirePermission("levelingcore.addstats");
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to add stats to.",
            ArgTypes.PLAYER_REF
        );
        this.statArg = this.withRequiredArg(
            "stat",
            "Stat to add to (str, agi, per, vit, int, con).",
            StatTypeArgumentType.INSTANCE
        );
        this.valueArg = this.withRequiredArg(
            "value",
            "Amount of stat points to add.",
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

        context.world().execute(() -> {
            try {
                switch (statType) {
                    case STR -> levelService.setStr(playerUUID, levelService.getStr(playerUUID) + value);
                    case AGI -> levelService.setAgi(playerUUID, levelService.getAgi(playerUUID) + value);
                    case PER -> levelService.setPer(playerUUID, levelService.getPer(playerUUID) + value);
                    case VIT -> levelService.setVit(playerUUID, levelService.getVit(playerUUID) + value);
                    case INT -> levelService.setInt(playerUUID, levelService.getInt(playerUUID) + value);
                    case CON -> levelService.setCon(playerUUID, levelService.getCon(playerUUID) + value);
                }

                var pRef = playerRef.getReference();
                if (pRef != null) {
                    StatsUtils.applyStatModifiers(playerUUID, pRef.getStore(), pRef, levelService);
                }

                var statName = statType.name().toLowerCase();
                commandContext.sendMessage(
                    CommandLang.ADD_STATS.param("stat", statName)
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning()
                    .withCause(e)
                    .log("Failed to add stats for player {}", playerUUID);
                commandContext.sendMessage(Message.raw("Failed to update stats. Check the server log."));
            } finally {
                future.complete(null);
            }
        });

        return future;
    }
}
