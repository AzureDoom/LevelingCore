package com.azuredoom.levelingcore.commands;

import com.azuredoom.levelingcore.utils.LevelingUtil;
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
 * The AddLevelCommand class is responsible for handling the command logic to add levels to a player's progress using
 * the LevelingCore API. This command ensures that the leveling system is properly initialized before performing any
 * operations and updates the player's level accordingly. Feedback messages are sent to both the player and the command
 * executor.
 */
public class AddLevelCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    private final Config<GUIConfig> config;

    public AddLevelCommand(Config<GUIConfig> config) {
        super("addlevel", "Add level to player");
        this.requirePermission("levelingcore.addlevel");
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to add level to.",
            ArgTypes.PLAYER_REF
        );
        this.levelArg = this.withRequiredArg("level", "Amount of levels to add", ArgTypes.INTEGER);
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
        var levelRef = this.levelArg.get(commandContext);
        if (levelRef > LevelingUtil.computeMaxLevel()) {
            commandContext.sendMessage(CommandLang.ADD_LEVEL_MAX_LEVEL_REACHED);
            return;
        }
        var playerUUID = playerRef.getUuid();
        levelService.addLevel(playerUUID, levelRef);
        var level = levelService.getLevel(playerUUID);
        var addLevelMsg = levelRef == 1 ? CommandLang.ADD_LEVEL_1 : CommandLang.ADD_LEVEL_2;
        var finalAddLevelMsg = addLevelMsg.param("level", levelRef).param("player", playerRef.getUsername());
        var playerLevelNowMsg = CommandLang.ADD_LEVEL_3.param("player", playerRef.getUsername()).param("level", level);
        if (config.get().isEnableLevelAndXPTitles())
            EventTitleUtil.showEventTitleToPlayer(playerRef, playerLevelNowMsg, finalAddLevelMsg, true);
        commandContext.sendMessage(finalAddLevelMsg);
        commandContext.sendMessage(playerLevelNowMsg);
    }
}
