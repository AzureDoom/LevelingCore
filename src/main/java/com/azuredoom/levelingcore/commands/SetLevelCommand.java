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
 * This class represents a command that allows adjusting the level of a player within the context of the leveling
 * system.
 */
public class SetLevelCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    private final Config<GUIConfig> config;

    public SetLevelCommand(Config<GUIConfig> config) {
        super("setlevel", "Set level of player");
        this.requirePermission("levelingcore.setlevel");
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "server.commands.levelingcore.addlevel.desc",
            ArgTypes.PLAYER_REF
        );
        this.levelArg = this.withRequiredArg("level", "server.commands.levelingcore.addlevel.desc", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(
        @NonNullDecl CommandContext commandContext,
        @NonNullDecl Store<EntityStore> store,
        @NonNullDecl Ref<EntityStore> ref,
        @NonNullDecl PlayerRef playerRef,
        @NonNullDecl World world
    ) {
        if (LevelingCoreApi.getLevelServiceIfPresent().isEmpty()) {
            commandContext.sendMessage(CommandLang.NOT_INITIALIZED);
            return;
        }
        playerRef = this.playerArg.get(commandContext);
        var levelRef = this.levelArg.get(commandContext);
        var playerUUID = playerRef.getUuid();
        LevelingCoreApi.getLevelServiceIfPresent().get().setLevel(playerUUID, levelRef);
        var level = LevelingCoreApi.getLevelServiceIfPresent().get().getLevel(playerUUID);
        var setLevelMsg = CommandLang.SET_LEVEL_1.param("player", playerRef.getUsername()).param("level", levelRef);
        var levelTotalMsg = CommandLang.SET_LEVEL_2.param("player", playerRef.getUsername()).param("level", level);
        if (config.get().isEnableLevelAndXPTitles())
            EventTitleUtil.showEventTitleToPlayer(playerRef, levelTotalMsg, setLevelMsg, true);
        commandContext.sendMessage(setLevelMsg);
        commandContext.sendMessage(levelTotalMsg);
    }
}
