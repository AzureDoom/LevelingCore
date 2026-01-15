package com.azuredoom.levelingcore.events;

import com.azuredoom.levelingcore.lang.CommandLang;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;

/**
 * The {@code GainXPEventSystem} class handles the process of awarding experience points (XP) to players based on
 * specific actions or events in the game world, particularly when an entity dies. This system is invoked upon the
 * addition of a {@code DeathComponent} to an entity.
 * <ul>
 * <li>XP gain is calculated based on the maximum health of the defeated entity and a configurable percentage provided
 * by the {@code GUIConfig}.</li>
 * <li>The system ensures that XP is awarded only if the default XP gain system is enabled in the configuration.</li>
 * <li>Supports interaction with the leveling service to update player levels and send appropriate messages upon
 * leveling up.</li>
 * </ul>
 * The class extends {@code DeathSystems.OnDeathSystem} to seamlessly integrate with death-related events in the game.
 */
public class GainXPEventSystem extends DeathSystems.OnDeathSystem {

    private final Config<GUIConfig> config;

    public GainXPEventSystem(Config<GUIConfig> config) {
        this.config = config;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void onComponentAdded(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull DeathComponent component,
        @Nonnull Store<EntityStore> store,
        @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        var deathInfo = component.getDeathInfo();
        if (deathInfo == null)
            return;

        if (!this.config.get().isEnableDefaultXPGainSystem()) {
            return;
        }

        if (deathInfo.getSource() instanceof Damage.EntitySource entitySource) {
            var attackerRef = entitySource.getRef();
            if (attackerRef.isValid()) {
                var player = store.getComponent(attackerRef, Player.getComponentType());
                if (player == null)
                    return;
                var statMap = store.getComponent(ref, EntityStatMap.getComponentType());
                if (statMap == null)
                    return;
                var healthIndex = EntityStatType.getAssetMap().getIndex("Health");
                var healthStat = statMap.get(healthIndex);
                if (healthStat == null)
                    return;
                var maxHealth = healthStat.getMax();
                var xpAmount = Math.max(1, (long) (maxHealth * this.config.get().getDefaultXPGainPercentage()));
                LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
                    int levelBefore = levelService.getLevel(player.getUuid());
                    levelService.addXp(player.getUuid(), xpAmount);
                    player.sendMessage(CommandLang.GAINED.param("xp", xpAmount));
                    int levelAfter = levelService.getLevel(player.getUuid());
                    if (levelAfter > levelBefore) {
                        player.sendMessage(CommandLang.LEVEL_UP.param("level", levelAfter));
                    }
                });
            }
        }
    }
}
