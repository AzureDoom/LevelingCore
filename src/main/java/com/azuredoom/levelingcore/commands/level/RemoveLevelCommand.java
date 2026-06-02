package com.azuredoom.levelingcore.commands.level;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.utils.LevelingPlayerContextManager;

/**
 * Represents a command that removes a specific number of levels from a player. This command operates within the
 * Leveling Core system and adjusts the player's level based on the specified number of levels to be removed.
 */
public class RemoveLevelCommand extends AbstractAsyncCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    private final Config<GUIConfig> config;

    public RemoveLevelCommand(Config<GUIConfig> config) {
        super("removelevel", "Remove level from player");
        // this.requirePermission("levelingcore.removelevel");
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to remove level from.",
            ArgTypes.PLAYER_REF
        );
        this.levelArg = this.withRequiredArg("level", "Amount of levels to remove", ArgTypes.INTEGER);
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
        var levelRef = this.levelArg.get(commandContext);
        var playerUUID = playerRef.getUuid();

        var currentLevel = levelService.getLevel(playerUUID);

        if (currentLevel - levelRef <= 0) {
            commandContext.sendMessage(
                CommandLang.CANNOT_REMOVE_LEVEL_BELOW_ONE
                    .param("player", playerRef.getUsername())
            );
            return CompletableFuture.completedFuture(null);
        }

        var context = LevelingPlayerContextManager.getContext(playerUUID);

        if (context == null || context.entityRef() == null || !context.entityRef().isValid()) {
            commandContext.sendMessage(Message.raw("Could not find active player context."));
            return CompletableFuture.completedFuture(null);
        }

        var future = new CompletableFuture<Void>();

        context.world().execute(() -> {
            try {
                levelService.removeLevel(playerUUID, levelRef);

                var level = levelService.getLevel(playerUUID);

                var removeLevelMsg = CommandLang.REMOVE_LEVEL_1
                    .param("level", levelRef)
                    .param("player", playerRef.getUsername());

                var levelTotalMsg = CommandLang.REMOVE_LEVEL_2
                    .param("player", playerRef.getUsername())
                    .param("level", level);

                if (config.get().isEnableLevelAndXPTitles()) {
                    EventTitleUtil.showEventTitleToPlayer(
                        playerRef,
                        levelTotalMsg,
                        removeLevelMsg,
                        true
                    );
                }

                commandContext.sendMessage(removeLevelMsg);
                commandContext.sendMessage(levelTotalMsg);

                future.complete(null);
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning()
                    .withCause(e)
                    .log("Failed to remove level for player {}", playerUUID);

                commandContext.sendMessage(Message.raw("Failed to remove level. Check the server log."));

                future.complete(null);
            }
        });

        return future;
    }
}
