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
import com.azuredoom.levelingcore.utils.LevelingUtil;

/**
 * The AddLevelCommand class is responsible for handling the command logic to add levels to a player's progress using
 * the LevelingCore API. This command ensures that the leveling system is properly initialized before performing any
 * operations and updates the player's level accordingly. Feedback messages are sent to both the player and the command
 * executor.
 */
public class AddLevelCommand extends AbstractAsyncCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    private final Config<GUIConfig> config;

    public AddLevelCommand(Config<GUIConfig> config) {
        super("addlevel", "Add level to player");
        // this.requirePermission("levelingcore.addlevel");
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to add level to.",
            ArgTypes.PLAYER_REF
        );
        this.levelArg = this.withRequiredArg("level", "Amount of levels to add", ArgTypes.INTEGER);
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

        if (levelRef > LevelingUtil.computeMaxLevel()) {
            commandContext.sendMessage(CommandLang.ADD_LEVEL_MAX_LEVEL_REACHED);
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
                levelService.addLevel(playerUUID, levelRef);

                var level = levelService.getLevel(playerUUID);

                var addLevelMsg = levelRef == 1
                    ? CommandLang.ADD_LEVEL_1
                    : CommandLang.ADD_LEVEL_2;

                var finalAddLevelMsg = addLevelMsg
                    .param("level", levelRef)
                    .param("player", playerRef.getUsername());

                var playerLevelNowMsg = CommandLang.ADD_LEVEL_3
                    .param("player", playerRef.getUsername())
                    .param("level", level);

                if (config.get().isEnableLevelAndXPTitles()) {
                    EventTitleUtil.showEventTitleToPlayer(
                        playerRef,
                        playerLevelNowMsg,
                        finalAddLevelMsg,
                        true
                    );
                }

                commandContext.sendMessage(finalAddLevelMsg);
                commandContext.sendMessage(playerLevelNowMsg);

                future.complete(null);
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning()
                    .withCause(e)
                    .log("Failed to add level for player {}", playerUUID);

                commandContext.sendMessage(Message.raw("Failed to add level. Check the server log."));
                future.complete(null);
            }
        });

        return future;
    }
}
