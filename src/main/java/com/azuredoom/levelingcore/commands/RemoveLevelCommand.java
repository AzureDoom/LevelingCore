package com.azuredoom.levelingcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
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
 * Represents a command that removes a specific number of levels from a player. This command operates within the
 * Leveling Core system and adjusts the player's level based on the specified number of levels to be removed.
 */
public class RemoveLevelCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> levelArg;

    private final Config<GUIConfig> config;

    public RemoveLevelCommand(Config<GUIConfig> config) {
        super("removelevel", "Remove level from player");
        this.requirePermission(HytalePermissions.fromCommand("levelingcore.removelevel"));
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
        LevelingCoreApi.getLevelServiceIfPresent().get().removeLevel(playerUUID, levelRef);
        var level = LevelingCoreApi.getLevelServiceIfPresent().get().getLevel(playerUUID);
        var removeLevelMsg = CommandLang.REMOVE_LEVEL_1.param("level", levelRef)
            .param("player", playerRef.getUsername());
        var levelTotalMsg = CommandLang.REMOVE_LEVEL_2.param("player", playerRef.getUsername()).param("level", level);
        if (config.get().isEnableLevelAndXPTitles())
            EventTitleUtil.showEventTitleToPlayer(playerRef, levelTotalMsg, removeLevelMsg, true);
        commandContext.sendMessage(removeLevelMsg);
        commandContext.sendMessage(levelTotalMsg);
    }
}
