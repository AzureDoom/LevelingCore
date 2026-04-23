package com.azuredoom.levelingcore.systems.xp;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;

/**
 * The LossXPEventSystem class is a subsystem that extends {@link DeathSystems.OnDeathSystem} to handle experience
 * points (XP) and level loss events when an entity with a {@link DeathComponent} dies. The system logic for XP or level
 * reduction is determined based on the {@link GUIConfig} provided during initialization.
 */
public class LossXPEventSystem extends DeathSystems.OnDeathSystem {

    private final Config<GUIConfig> config;

    public LossXPEventSystem(Config<GUIConfig> config) {
        this.config = config;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(DeathComponent.getComponentType());
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

        if (!this.config.get().isEnableXPLossOnDeath()) {
            return;
        }

        var player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;
        var levelService = LevelingCore.getLevelService();

        store.getExternalData()
            .getWorld()
            .execute(() -> {
                var playerRef = player.getReference();
                if (playerRef == null) {
                    return;
                }
                var playerRefComponent = playerRef.getStore()
                    .getComponent(playerRef, PlayerRef.getComponentType());
                if (playerRefComponent == null) {
                    return;
                }
                var playerUuid = playerRefComponent.getUuid();
                var currentXp = levelService.getXp(playerUuid);
                var currentLevel = levelService.getLevel(playerUuid);
                if (this.config.get().isEnableLevelDownOnDeath()) {
                    var xpLoss = (long) (currentXp * this.config.get().getXpLossPercentage());
                    if (xpLoss <= 0)
                        return;
                    levelService.removeXp(playerUuid, xpLoss);
                    playerRefComponent.sendMessage(CommandLang.XP_LOST.param("xp", xpLoss));
                    var levelAfter = levelService.getLevel(playerUuid);
                    if (levelAfter < currentLevel) {
                        playerRefComponent.sendMessage(CommandLang.LEVEL_DOWN.param("level", levelAfter));
                    }
                } else if (this.config.get().isEnableAllLevelsLostOnDeath()) {
                    levelService.setLevel(playerUuid, 1);
                    playerRefComponent.sendMessage(CommandLang.DEATH_ALL_LEVELS);
                } else if (this.config.get().getMinLevelForLevelDown() <= currentLevel) {
                    var levelFloorXp = levelService.getXpForLevel(currentLevel);
                    var xpLoss = (long) (currentXp * this.config.get().getXpLossPercentage());
                    var newXp = Math.max(levelFloorXp, currentXp - xpLoss);
                    var actualLoss = currentXp - newXp;

                    if (actualLoss <= 0) {
                        playerRefComponent.sendMessage(CommandLang.MIN_LEVEL_DEATH.param("level", currentLevel));
                        return;
                    }
                    levelService.setXp(playerUuid, newXp);
                    playerRefComponent.sendMessage(CommandLang.XP_LOST.param("xp", actualLoss));
                }
                XPBarHud.updateHud(playerRefComponent);
            });
    }
}
