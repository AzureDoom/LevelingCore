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
 * This class represents a command that allows adjusting the level of a player within the context of the leveling
 * system.
 */
public class SetLevelCommand extends CommandBase {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    public SetLevelCommand() {
        super("setlevel", "Set level of player");
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
        LevelingCoreApi.getLevelServiceIfPresent().get().setLevel(playerUUID, levelRef);
        var level = LevelingCoreApi.getLevelServiceIfPresent().get().getLevel(playerUUID);
        var setLevelMsg = CommandLang.SET_LEVEL_1.param("player", playerRef.getUsername()).param("level", levelRef);
        var levelTotalMsg = CommandLang.SET_LEVEL_2.param("player", playerRef.getUsername()).param("level", level);
        EventTitleUtil.showEventTitleToPlayer(playerRef, levelTotalMsg, setLevelMsg, true);
        commandContext.sendMessage(setLevelMsg);
        commandContext.sendMessage(levelTotalMsg);
    }

}
