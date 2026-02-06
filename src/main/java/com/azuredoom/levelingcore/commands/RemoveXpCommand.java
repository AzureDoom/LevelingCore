package com.azuredoom.levelingcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;

/**
 * The RemoveXpCommand class is responsible for handling the logic to remove experience points (XP) from a player's
 * progress using the LevelingCore API. This command ensures that the leveling system is initialized before modifying
 * the XP. It retrieves the player's XP and calculates their level after removal, providing feedback messages to both
 * the player and the command executor.
 */
public class RemoveXpCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> xpArg;

    private final Config<GUIConfig> config;

    public RemoveXpCommand(Config<GUIConfig> config) {
        super("removexp", "Remove XP from player");
        this.requirePermission("levelingcore.removexp");
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to remove XP from.",
            ArgTypes.PLAYER_REF
        );
        this.xpArg = this.withRequiredArg("xpvalue", "Amount of XP to remove", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(
        @NonNullDecl CommandContext commandContext,
        @NonNullDecl Store<EntityStore> store,
        @NonNullDecl Ref<EntityStore> ref,
        @NonNullDecl PlayerRef playerRef,
        @NonNullDecl World world
    ) {
        var levelService = LevelingCoreApi.getLevelServiceIfPresent().orElse(null);
        if (levelService == null) {
            commandContext.sendMessage(CommandLang.NOT_INITIALIZED);
            return;
        }
        playerRef = this.playerArg.get(commandContext);
        var xpRef = this.xpArg.get(commandContext);
        var playerUUID = playerRef.getUuid();
        var currentXp = levelService.getXp(playerUUID);
        var totalXp = levelService.getXp(playerUUID);
        var minXpForLevelOne = levelService.getXpForLevel(1);
        if (totalXp - xpRef < minXpForLevelOne) {
            commandContext.sendMessage(
                CommandLang.CANNOT_REMOVE_LEVEL_BELOW_ONE
                    .param("player", playerRef.getUsername())
            );
            return;
        }
        if (currentXp - xpRef < 0) {
            commandContext.sendMessage(
                CommandLang.CANNOT_REMOVE_LEVEL_BELOW_ONE
                    .param("player", playerRef.getUsername())
            );
            return;
        }
        levelService.removeXp(playerUUID, xpRef);
        var level = levelService.getLevel(playerUUID);
        var removedXPMsg = CommandLang.REMOVE_XP_1.param("xp", xpRef).param("player", playerRef.getUsername());
        var levelTotalMsg = CommandLang.REMOVE_XP_2.param("player", playerRef.getUsername()).param("level", level);
        if (config.get().isEnableLevelAndXPTitles())
            EventTitleUtil.showEventTitleToPlayer(playerRef, levelTotalMsg, removedXPMsg, true);
        commandContext.sendMessage(removedXPMsg);
        commandContext.sendMessage(levelTotalMsg);
    }
}
