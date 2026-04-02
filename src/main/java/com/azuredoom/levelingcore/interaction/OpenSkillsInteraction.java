package com.azuredoom.levelingcore.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;

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
            LevelingCore.LOGGER.at(Level.INFO)
                .log(
                    "SkillPointResetInteraction requires a Player but was used for entity: %s",
                    ref
                );
            context.getState().state = InteractionState.Failed;
            return;
        }

        var player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (player == null) {
            LevelingCore.LOGGER.at(Level.INFO)
                .log(
                    "SkillPointResetInteraction: Player component missing for entity: %s (uuid=%s)",
                    ref,
                    playerRef.getUuid()
                );
            context.getState().state = InteractionState.Failed;
            return;
        }
        CompletableFuture.runAsync(() -> CommandManager.get().handleCommand(player, "levelingcore showstats"));
    }
}
