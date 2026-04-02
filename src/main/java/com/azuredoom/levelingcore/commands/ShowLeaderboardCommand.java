package com.azuredoom.levelingcore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.ui.page.LeaderboardScreen;

public class ShowLeaderboardCommand extends AbstractPlayerCommand {

    public ShowLeaderboardCommand() {
        super("leaderboard", "Shows the player leaderboard");
        this.requirePermission("levelingcore.leaderboard");
        this.setPermissionGroup(GameMode.Adventure);
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
            return;
        }
        var config = LevelingCore.getConfig().get();
        if (config == null) {
            return;
        }
        if (!config.isEnableLeaderboardScreen()) {
            playerRef.sendMessage(Message.raw("Leaderboard is disabled in the config."));
            return;
        }

        var player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }

        if (player.getPageManager().getCustomPage() == null) {
            var page = new LeaderboardScreen(
                playerRef,
                CustomPageLifetime.CanDismissOrCloseThroughInteraction,
                levelService
            );

            player.getPageManager().openCustomPage(ref, store, page);
        }
    }
}
