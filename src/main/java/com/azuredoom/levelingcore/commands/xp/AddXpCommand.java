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
import com.azuredoom.levelingcore.utils.LevelingUtil;

/**
 * The AddXpCommand class is responsible for handling the logic to add experience points (XP) to a player's progress
 * using the LevelingCore API. This command validates that the leveling system is initialized before proceeding with XP
 * modification. It updates the player's XP and calculates the resulting level, sending feedback messages to both the
 * player and the command executor.
 */
public class AddXpCommand extends AbstractAsyncCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> xpArg;

    private final Config<GUIConfig> config;

    public AddXpCommand(Config<GUIConfig> config) {
        super("addxp", "Add XP to player");
        this.config = config;

        this.playerArg = this.withRequiredArg(
            "player",
            "Player to add XP to.",
            ArgTypes.PLAYER_REF
        );

        this.xpArg = this.withRequiredArg(
            "xpvalue",
            "Amount of XP to add",
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
        var xpRef = this.xpArg.get(commandContext);
        var playerUUID = playerRef.getUuid();

        var maxLevel = LevelingUtil.computeMaxLevel();
        var currentLevel = levelService.getLevel(playerUUID);

        if (currentLevel >= maxLevel) {
            commandContext.sendMessage(
                CommandLang.ADD_LEVEL_MAX_LEVEL_REACHED
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
                levelService.addXp(playerUUID, xpRef);

                XPBarHud.updateHud(playerRef);

                var newLevel = levelService.getLevel(playerUUID);

                if (newLevel > maxLevel) {
                    levelService.removeLevel(playerUUID, newLevel - maxLevel);
                    newLevel = maxLevel;
                }

                var setXPMsg = CommandLang.ADD_XP_1
                    .param("xp", xpRef)
                    .param("player", playerRef.getUsername());

                var levelTotalMsg = CommandLang.ADD_XP_2
                    .param("player", playerRef.getUsername())
                    .param("level", newLevel);

                if (config.get().isEnableLevelAndXPTitles()) {
                    EventTitleUtil.showEventTitleToPlayer(
                        playerRef,
                        levelTotalMsg,
                        setXPMsg,
                        true
                    );
                }

                commandContext.sendMessage(setXPMsg);
                commandContext.sendMessage(levelTotalMsg);

                future.complete(null);
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning()
                    .withCause(e)
                    .log("Failed to add XP for player {}", playerUUID);

                commandContext.sendMessage(Message.raw("Failed to add XP. Check the server log."));
                future.complete(null);
            }
        });

        return future;
    }
}
