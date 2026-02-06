package com.azuredoom.levelingcore.commands;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.compat.hyui.HyUICompat;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.ui.page.StatsScreen;

public class ShowStatsCommand extends AbstractPlayerCommand {

    private final Config<GUIConfig> config;

    public ShowStatsCommand(Config<GUIConfig> config) {
        super("showstats", "Shows player stats");
        this.requirePermission("levelingcore.showstats");
        this.setPermissionGroup(GameMode.Adventure);
        this.config = config;
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
        var player = commandContext.senderAs(Player.class);

        CompletableFuture.runAsync(() -> {
            if (config.get().isDisableStatPointGainOnLevelUp()) {
                playerRef.sendMessage(CommandLang.STATS_DISABLED);
                return;
            }
            if (player.getPageManager().getCustomPage() == null) {
                if (PluginManager.get().getPlugin(new PluginIdentifier("Ellie", "HyUI")) != null) {
                    HyUICompat.showStats(playerRef, store, ref);
                } else {
                    var page = new StatsScreen(
                        playerRef,
                        CustomPageLifetime.CanDismissOrCloseThroughInteraction,
                        config
                    );
                    player.getPageManager().openCustomPage(ref, store, page);
                    player.sendMessage(
                        Message.raw(
                            "Missing HyUI plugin, stats will be shown in old screen. Please install HyUI to use the new screen."
                        )
                    );
                }
            }
        }, world);
    }
}
