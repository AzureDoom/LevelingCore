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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.level.stats.StatsPerLevelMapping;
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
        UUID id = playerRef.getUuid();
        if (!REGISTERED.add(id))
            return;

        var world = player.getWorld();
        var worldStore = world.getEntityStore();
        var levelupSound = SoundEvent.getAssetMap().getIndex(config.get().getLevelUpSound());

        LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
            if (!config.get().isEnableStatLeveling())
                return;

            store.getExternalData()
                .getWorld()
                .execute(() -> levelService.registerLevelUpListener((playerId, oldLevel, newLevel) -> {
                    if (!playerId.equals(id))
                        return;

                    StatsUtils.applyAllStats(store, player, newLevel, config);

                    world.execute(() -> {
                        if (player.getReference() == null)
                            return;
                        var transform = worldStore.getStore()
                            .getComponent(playerRef.getReference(), EntityModule.get().getTransformComponentType());
                        SoundUtil.playSoundEvent3dToPlayer(
                            player.getReference(),
                            levelupSound,
                            SoundCategory.UI,
                            transform.getPosition(),
                            worldStore.getStore()
                        );
                    });
                    if (config.get().isEnableLevelUpRewardsConfig()) {
                        for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
                            LevelUpRewardsUtil.giveRewards(lvl, player);
                        }
                    }
                    if (!config.get().isDisableStatPointGainOnLevelUp()) {
                        int pointsPerLevel;
                        if (config.get().isUseStatsPerLevelMapping()) {
                            var mapping = StatsPerLevelMapping.loadOrCreate(LevelingCore.configPath);
                            pointsPerLevel = mapping.getOrDefault(newLevel, 5);
                        } else {
                            pointsPerLevel = config.get().getStatsPerLevel();
                        }
                        var totalFromLeveling = Math.max(5, newLevel * pointsPerLevel);

                        levelService.setAbilityPoints(playerId, totalFromLeveling);

                        playerRef.sendMessage(
                            CommandLang.ABILITY_POINTS.param("ability_points", totalFromLeveling)
                                .param("player_name", playerRef.getUsername())
                        );
                    }
                    LevelDownListenerRegistrar.clear(playerId);
                    XPBarHud.updateHud(playerRef);
                }));
        });
    }

    public static void clear(UUID playerId) {
        REGISTERED.remove(playerId);
    }
}
