package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;

public final class LevelUpListenerRegistrar {

    private static final Set<UUID> REGISTERED =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void ensureRegistered(
        Store<EntityStore> store,
        Player player,
        PlayerRef playerRef,
        Config<GUIConfig> config
    ) {
        var playerUUID = playerRef.getUuid();
        if (!REGISTERED.add(playerUUID))
            return;

        var world = player.getWorld();
        if (world == null)
            return;
        var worldStore = world.getEntityStore();
        var levelUpSound = SoundEvent.getAssetMap().getIndex(config.get().getLevelUpSound());
        if (!config.get().isEnableStatLeveling())
            return;
        var levelService = LevelingCore.getLevelService();

        store.getExternalData()
            .getWorld()
            .execute(() -> levelService.registerLevelUpListener((playerId, oldLevel, newLevel) -> {
                if (!playerId.equals(playerUUID))
                    return;

                StatsUtils.applyAllStats(store, player, newLevel, config);

                world.execute(() -> {
                    if (player.getReference() == null)
                        return;
                    var transform = worldStore.getStore()
                        .getComponent(
                            Objects.requireNonNull(
                                store.getExternalData().getWorld().getEntityRef(playerUUID)
                            ),
                            EntityModule.get().getTransformComponentType()
                        );
                    if (transform == null)
                        return;
                    SoundUtil.playSoundEvent3dToPlayer(
                        player.getReference(),
                        levelUpSound,
                        SoundCategory.UI,
                        transform.getPosition(),
                        worldStore.getStore()
                    );
                });
                if (config.get().isEnableLevelUpRewardsConfig()) {
                    for (var lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
                        LevelUpRewardsUtil.giveRewards(lvl, player);
                    }
                }
                if (!config.get().isDisableStatPointGainOnLevelUp()) {
                    int pointsPerLevel;
                    if (config.get().isUseStatsPerLevelMapping()) {
                        pointsPerLevel = LevelingCore.statsPerLevel.getAddedStatsForLevel(
                            newLevel,
                            config.get().getStatsPerLevel()
                        );
                    } else {
                        pointsPerLevel = config.get().getStatsPerLevel();
                    }
                    var totalFromLeveling = Math.max(0, newLevel * pointsPerLevel);

                    levelService.setAbilityPoints(playerId, totalFromLeveling);

                    playerRef.sendMessage(
                        CommandLang.ABILITY_POINTS.param(
                            "ability_points",
                            levelService.getAvailableAbilityPoints(playerId)
                        )
                    );
                }
                LevelDownListenerRegistrar.clear(playerId);
                XPBarHud.updateHud(playerRef);
            }));
    }

    public static void clear(UUID playerId) {
        REGISTERED.remove(playerId);
    }
}
