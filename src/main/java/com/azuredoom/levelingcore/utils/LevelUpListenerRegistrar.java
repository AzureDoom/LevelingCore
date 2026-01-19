package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.util.Config;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;

public final class LevelUpListenerRegistrar {

    private static final Set<UUID> REGISTERED =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void ensureRegistered(
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

            player.getWorld().execute(() -> levelService.registerLevelUpListener((playerId, newLevel) -> {
                if (!playerId.equals(id))
                    return;

                StatsUtils.applyAllStats(player, playerRef, newLevel, config);

                world.execute(() -> {
                    var transform = worldStore.getStore()
                        .getComponent(player.getReference(), EntityModule.get().getTransformComponentType());
                    SoundUtil.playSoundEvent3dToPlayer(
                        player.getReference(),
                        levelupSound,
                        SoundCategory.UI,
                        transform.getPosition(),
                        worldStore.getStore()
                    );
                });
                if (config.get().isEnableLevelUpRewardsConfig())
                    LevelUpRewardsUtil.giveRewards(newLevel, player);
            }));
        });
    }

    public static void clear(UUID playerId) {
        REGISTERED.remove(playerId);
    }
}
