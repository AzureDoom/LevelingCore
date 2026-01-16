package com.azuredoom.levelingcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
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
 * The CheckLevelCommand class is a concrete implementation of the CommandBase class. This command allows users to check
 * the current level of a specified player in the game. It interacts with the Leveling Core API to retrieve level data
 * for a given player.
 */
public class CheckLevelCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    private final Config<GUIConfig> config;

    public CheckLevelCommand(Config<GUIConfig> config) {
        super("checklevel", "Check level of player");
        this.requirePermission(HytalePermissions.fromCommand("levelingcore.checklevel"));
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "server.commands.levelingcore.checklevel.desc",
            ArgTypes.PLAYER_REF
        );
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
        var playerUUID = playerRef.getUuid();
        var levelRef = LevelingCoreApi.getLevelServiceIfPresent().get().getLevel(playerUUID);
        var currentLevelMsg = CommandLang.CHECK_LEVEL.param("player", playerRef.getUsername()).param("level", levelRef);
        if (config.get().isEnableLevelAndXPTitles())
            EventTitleUtil.showEventTitleToPlayer(playerRef, currentLevelMsg, Message.raw(""), true);
        commandContext.sendMessage(currentLevelMsg);
    }
}
