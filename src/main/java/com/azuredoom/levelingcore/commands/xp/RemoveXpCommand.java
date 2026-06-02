package com.azuredoom.levelingcore.commands.xp;

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
import com.azuredoom.levelingcore.ui.hud.XPBarHud;
import com.azuredoom.levelingcore.utils.LevelingPlayerContextManager;

/**
 * The RemoveXpCommand class is responsible for handling the logic to remove experience points (XP) from a player's
 * progress using the LevelingCore API. This command ensures that the leveling system is initialized before modifying
 * the XP. It retrieves the player's XP and calculates their level after removal, providing feedback messages to both
 * the player and the command executor.
 */
public class RemoveXpCommand extends AbstractAsyncCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> xpArg;

    private final Config<GUIConfig> config;

    public RemoveXpCommand(Config<GUIConfig> config) {
        super("removexp", "Remove XP from player");
        // this.requirePermission("levelingcore.removexp");
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to remove XP from.",
            ArgTypes.PLAYER_REF
        );
        this.xpArg = this.withRequiredArg("xpvalue", "Amount of XP to remove", ArgTypes.INTEGER);
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
        var xpRef = this.xpArg.get(commandContext);
        var playerUUID = playerRef.getUuid();

        var currentXp = levelService.getXp(playerUUID);
        var totalXp = levelService.getXp(playerUUID);
        var minXpForLevelOne = levelService.getXpForLevel(1);

        if (totalXp - xpRef < minXpForLevelOne || currentXp - xpRef < 0) {
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
                levelService.removeXp(playerUUID, xpRef);

                XPBarHud.updateHud(playerRef);

                var level = levelService.getLevel(playerUUID);

                var removedXPMsg = CommandLang.REMOVE_XP_1
                    .param("xp", xpRef)
                    .param("player", playerRef.getUsername());

                var levelTotalMsg = CommandLang.REMOVE_XP_2
                    .param("player", playerRef.getUsername())
                    .param("level", level);

                if (config.get().isEnableLevelAndXPTitles()) {
                    EventTitleUtil.showEventTitleToPlayer(
                        playerRef,
                        levelTotalMsg,
                        removedXPMsg,
                        true
                    );
                }

                commandContext.sendMessage(removedXPMsg);
                commandContext.sendMessage(levelTotalMsg);

                future.complete(null);
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning()
                    .withCause(e)
                    .log("Failed to remove XP for player {}", playerUUID);

                commandContext.sendMessage(Message.raw("Failed to remove XP. Check the server log."));
                future.complete(null);
            }
        });

        return future;
    }
}
