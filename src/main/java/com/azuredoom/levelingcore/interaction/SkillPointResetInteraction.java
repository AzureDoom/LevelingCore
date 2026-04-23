package com.azuredoom.levelingcore.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.logging.Level;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;

public class SkillPointResetInteraction extends SimpleInstantInteraction {

    private static final String HEALTH_MOD_KEY = "LevelingCore_health_stat";

    private static final String STAMINA_MOD_KEY = "LevelingCore_stamina_stat";

    private static final String OXYGEN_MOD_KEY = "LevelingCore_oxygen_stat";

    private static final String MANA_MOD_KEY = "LevelingCore_mana_stat";

    @Nonnull
    public static final BuilderCodec<SkillPointResetInteraction> CODEC = (BuilderCodec.builder(
        SkillPointResetInteraction.class,
        SkillPointResetInteraction::new,
        SimpleInstantInteraction.CODEC
    ).documentation("Resets the players skill points.")).build();

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

        var statMap = commandBuffer.ensureAndGetComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        statMap.removeModifier(DefaultEntityStatTypes.getHealth(), HEALTH_MOD_KEY);
        statMap.removeModifier(DefaultEntityStatTypes.getStamina(), STAMINA_MOD_KEY);
        statMap.removeModifier(DefaultEntityStatTypes.getOxygen(), OXYGEN_MOD_KEY);
        statMap.removeModifier(DefaultEntityStatTypes.getMana(), MANA_MOD_KEY);
        statMap.setStatValue(DefaultEntityStatTypes.getMana(), 0);

        var levelService = LevelingCore.getLevelService();
        if (levelService == null) {
            LevelingCore.LOGGER.at(Level.WARNING).log("SkillPointResetInteraction: Level service not present.");
            context.getState().state = InteractionState.Failed;
            return;
        }

        levelService.setUsedAbilityPoints(playerRef.getUuid(), 0);
        levelService.setStr(playerRef.getUuid(), 0);
        levelService.setAgi(playerRef.getUuid(), 0);
        levelService.setPer(playerRef.getUuid(), 0);
        levelService.setVit(playerRef.getUuid(), 0);
        levelService.setInt(playerRef.getUuid(), 0);
        levelService.setCon(playerRef.getUuid(), 0);

        context.getState().state = InteractionState.Finished;
    }

    @Nonnull
    public String toString() {
        return "SkillPointResetInteraction{} " + super.toString();
    }
}
