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

public class LevelDownListenerRegistrar {

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
        var world_store = world.getEntityStore();
        var leveldown_sound = SoundEvent.getAssetMap().getIndex(config.get().getLevelDownSound());

        LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService1 -> {
            LevelUpRewardsUtil.clear(player.getUuid());
            if (config.get().isEnableStatLeveling()) {
                store.getExternalData().getWorld().execute(() -> {
                    levelService1.registerLevelDownListener(((playerId, oldLevel, newLevel) -> {
                        StatsUtils.resetStats(store, player);
                        StatsUtils.applyAllStats(store, player, newLevel, config);
                        world.execute(() -> {
                            var transform = world_store.getStore()
                                .getComponent(playerRef.getReference(), EntityModule.get().getTransformComponentType());
                            SoundUtil.playSoundEvent3dToPlayer(
                                player.getReference(),
                                leveldown_sound,
                                SoundCategory.UI,
                                transform.getPosition(),
                                world_store.getStore()
                            );
                        });
                        if (!config.get().isDisableStatPointGainOnLevelUp()) {
                            int pointsPerLevel;
                            if (config.get().isUseStatsPerLevelMapping()) {
                                var mapping = StatsPerLevelMapping.loadOrCreate(LevelingCore.configPath);
                                pointsPerLevel = mapping.getOrDefault(newLevel, 5);
                            } else {
                                pointsPerLevel = config.get().getStatsPerLevel();
                            }
                            var totalFromLeveling = Math.max(0, newLevel * pointsPerLevel);

                            levelService1.setAbilityPoints(
                                playerId,
                                levelService1.getLevel(playerId) == 1 ? pointsPerLevel : totalFromLeveling
                            );
                            levelService1.setUsedAbilityPoints(playerId, 0);
                            levelService1.setStr(playerId, 0);
                            levelService1.setAgi(playerId, 0);
                            levelService1.setPer(playerId, 0);
                            levelService1.setVit(playerId, 0);
                            levelService1.setInt(playerId, 0);
                            playerRef.sendMessage(
                                CommandLang.ABILITY_POINTS.param("ability_points", totalFromLeveling)
                                    .param("player_name", playerRef.getUsername())
                            );
                        }

                        // Need to clear out mapping whenever a player levels down as well
                        LevelUpListenerRegistrar.clear(player.getUuid());
                    }));
                });
            }
        });
    }

    public static void clear(UUID playerId) {
        REGISTERED.remove(playerId);
    }
}
