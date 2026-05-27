package com.azuredoom.levelingcore.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.compat.hyui.HyUICompat;
import com.azuredoom.levelingcore.ui.page.StatsScreen;

public class OpenSkillsInteraction extends SimpleInstantInteraction {

    @Nonnull
    public static final BuilderCodec<OpenSkillsInteraction> CODEC = (BuilderCodec.builder(
        OpenSkillsInteraction.class,
        OpenSkillsInteraction::new,
        OpenSkillsInteraction.CODEC
    ).documentation("Opens the players skill points screen.")).build();

    @Override
    protected void firstRun(
        @NonNullDecl InteractionType interactionType,
        @NonNullDecl InteractionContext context,
        @NonNullDecl CooldownHandler cooldownHandler
    ) {
        var commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        var ref = context.getEntity();

        var playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            LevelingCore.LOGGER.atInfo()
                .log(
                    "SkillPointResetInteraction requires a Player but was used for entity: %s",
                    ref
                );
            context.getState().state = InteractionState.Failed;
            return;
        }

        var player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (player == null) {
            LevelingCore.LOGGER.atInfo()
                .log(
                    "OpenSkillsInteraction: Player component missing for entity: %s (uuid=%s)",
                    ref,
                    playerRef.getUuid()
                );
            context.getState().state = InteractionState.Failed;
            return;
        }
        var entityRef = playerRef.getReference();
        if (entityRef == null || !entityRef.isValid()) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        var store = entityRef.getStore();
        var world = store.getExternalData().getWorld();

        world.execute(() -> {
            if (!entityRef.isValid()) {
                return;
            }

            if (player.getPageManager().getCustomPage() != null) {
                return;
            }

            if (PluginManager.get().getPlugin(new PluginIdentifier("Ellie", "HyUI")) != null) {
                HyUICompat.showStats(playerRef, store, entityRef);
            } else {
                var page = new StatsScreen(
                    playerRef,
                    CustomPageLifetime.CanDismissOrCloseThroughInteraction,
                    LevelingCore.getConfig()
                );

                player.getPageManager().openCustomPage(entityRef, store, page);
            }
        });
    }
}
