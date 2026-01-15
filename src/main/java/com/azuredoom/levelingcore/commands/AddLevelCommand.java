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
 * The AddLevelCommand class is responsible for handling the command logic to add levels to a player's progress using
 * the LevelingCore API. This command ensures that the leveling system is properly initialized before performing any
 * operations and updates the player's level accordingly. Feedback messages are sent to both the player and the command
 * executor.
 */
public class AddLevelCommand extends CommandBase {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    public AddLevelCommand() {
        super("addlevel", "Add level to player");
        this.playerArg = this.withRequiredArg(
            "player",
            "player to add levels to",
            ArgTypes.PLAYER_REF
        );
        this.levelArg = this.withRequiredArg("level", "amount of levels to add", ArgTypes.INTEGER);
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
        LevelingCoreApi.getLevelServiceIfPresent().get().addLevel(playerUUID, levelRef);
        var level = LevelingCoreApi.getLevelServiceIfPresent().get().getLevel(playerUUID);
        var addLevelMsg = levelRef == 1 ? CommandLang.ADD_LEVEL_1 : CommandLang.ADD_LEVEL_2;
        var finalAddLevelMsg = addLevelMsg.param("level", levelRef).param("player", playerRef.getUsername());
        var playerLevelNowMsg = CommandLang.ADD_LEVEL_3.param("player", playerRef.getUsername()).param("level", level);
        EventTitleUtil.showEventTitleToPlayer(playerRef, playerLevelNowMsg, finalAddLevelMsg, true);
        commandContext.sendMessage(finalAddLevelMsg);
        commandContext.sendMessage(playerLevelNowMsg);
    }
}
