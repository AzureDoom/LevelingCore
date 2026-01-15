package com.azuredoom.levelingcore.commands;

import com.azuredoom.levelingcore.lang.CommandLang;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.api.LevelingCoreApi;

/**
 * Represents a command that removes a specific number of levels from a player. This command operates within the
 * Leveling Core system and adjusts the player's level based on the specified number of levels to be removed.
 */
public class RemoveLevelCommand extends CommandBase {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    public RemoveLevelCommand() {
        super("removelevel", "Remove level from player");
        this.playerArg = this.withRequiredArg(
            "player",
            "server.commands.levelingcore.addlevel.desc",
            ArgTypes.PLAYER_REF
        );
        this.levelArg = this.withRequiredArg("level", "server.commands.levelingcore.addlevel.desc", ArgTypes.INTEGER);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext commandContext) {
        if (LevelingCoreApi.getLevelServiceIfPresent().isEmpty()) {
            commandContext.sendMessage(CommandLang.NOT_INITIALIZED);
            return;
        }
        var playerRef = this.playerArg.get(commandContext);
        var levelRef = this.levelArg.get(commandContext);
        var playerUUID = playerRef.getUuid();
        LevelingCoreApi.getLevelServiceIfPresent().get().removeLevel(playerUUID, levelRef);
        var level = LevelingCoreApi.getLevelServiceIfPresent().get().getLevel(playerUUID);
        var removeLevelMsg = CommandLang.REMOVE_LEVEL_1.param("level", levelRef).param("player", playerRef.getUsername());
        var levelTotalMsg = CommandLang.REMOVE_LEVEL_2.param("player", playerRef.getUsername()).param("level", level);
        EventTitleUtil.showEventTitleToPlayer(playerRef, levelTotalMsg, removeLevelMsg, true);
        commandContext.sendMessage(removeLevelMsg);
        commandContext.sendMessage(levelTotalMsg);
    }
}
