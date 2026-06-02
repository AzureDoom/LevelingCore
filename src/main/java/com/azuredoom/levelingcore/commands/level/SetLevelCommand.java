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
 * This class represents a command that allows adjusting the level of a player within the context of the leveling
 * system.
 */
public class SetLevelCommand extends AbstractAsyncCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    private final Config<GUIConfig> config;

    public SetLevelCommand(Config<GUIConfig> config) {
        super("setlevel", "Set level of player");
        // this.requirePermission("levelingcore.setlevel");
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "Name of player to set level of.",
            ArgTypes.PLAYER_REF
        );
        this.levelArg = this.withRequiredArg("level", "Level to set player to.", ArgTypes.INTEGER);
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
                levelService.setLevel(playerUUID, levelRef);

                var level = levelService.getLevel(playerUUID);

                var setLevelMsg = CommandLang.SET_LEVEL_1
                    .param("player", playerRef.getUsername())
                    .param("level", levelRef);

                var levelTotalMsg = CommandLang.SET_LEVEL_2
                    .param("player", playerRef.getUsername())
                    .param("level", level);

                if (config.get().isEnableLevelAndXPTitles()) {
                    EventTitleUtil.showEventTitleToPlayer(
                        playerRef,
                        levelTotalMsg,
                        setLevelMsg,
                        true
                    );
                }

                commandContext.sendMessage(setLevelMsg);
                commandContext.sendMessage(levelTotalMsg);

                future.complete(null);
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning()
                    .withCause(e)
                    .log("Failed to set level for player {}", playerUUID);

                commandContext.sendMessage(Message.raw("Failed to set level. Check the server log."));

                future.complete(null);
            }
        });

        return future;
    }
}
