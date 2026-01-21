package com.azuredoom.levelingcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
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
 * The AddXpCommand class is responsible for handling the logic to add experience points (XP) to a player's progress
 * using the LevelingCore API. This command validates that the leveling system is initialized before proceeding with XP
 * modification. It updates the player's XP and calculates the resulting level, sending feedback messages to both the
 * player and the command executor.
 */
public class AddXpCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<Integer> xpArg;

    private final Config<GUIConfig> config;

    public AddXpCommand(Config<GUIConfig> config) {
        super("addxp", "Add XP to player");
        this.requirePermission("levelingcore.addxp");
        this.setPermissionGroup(GameMode.Creative);
        this.config = config;
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to add XP to.",
            ArgTypes.PLAYER_REF
        );
        this.xpArg = this.withRequiredArg("xpvalue", "Amount of XP to add", ArgTypes.INTEGER);
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
        var xpRef = this.xpArg.get(commandContext);
        var playerUUID = playerRef.getUuid();
        LevelingCoreApi.getLevelServiceIfPresent().get().addXp(playerUUID, xpRef);
        var level = LevelingCoreApi.getLevelServiceIfPresent().get().getLevel(playerUUID);
        var setXPMsg = CommandLang.ADD_XP_1.param("xp", xpRef).param("player", playerRef.getUsername());
        var levelTotalMsg = CommandLang.ADD_XP_2.param("player", playerRef.getUsername()).param("level", level);
        if (config.get().isEnableLevelAndXPTitles())
            EventTitleUtil.showEventTitleToPlayer(playerRef, levelTotalMsg, setXPMsg, true);
        commandContext.sendMessage(setXPMsg);
        commandContext.sendMessage(levelTotalMsg);
    }
}
