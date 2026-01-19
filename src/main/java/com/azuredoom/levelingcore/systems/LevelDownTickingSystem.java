package com.azuredoom.levelingcore.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.utils.LevelUpRewardsUtil;
import com.azuredoom.levelingcore.utils.StatsUtils;

public class LevelDownTickingSystem extends EntityTickingSystem<EntityStore> {

    private final Config<GUIConfig> config;

    public LevelDownTickingSystem(Config<GUIConfig> config) {
        this.config = config;
    }

    @Override
    public void tick(
        float var1,
        int index,
        @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
        @NonNullDecl Store<EntityStore> store,
        @NonNullDecl CommandBuffer<EntityStore> commandBuffer
    ) {
        final Holder<EntityStore> holder = EntityUtils.toHolder(index, archetypeChunk);
        final Player player = holder.getComponent(Player.getComponentType());
        final PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        if (player == null || playerRef == null) {
            return;
        }
        var world = player.getWorld();
        var world_store = world.getEntityStore();
        var leveldown_sound = SoundEvent.getAssetMap().getIndex(config.get().getLevelDownSound());
        LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService1 -> {
            if (config.get().isEnableStatLeveling()) {
                player.getWorld().execute(() -> {
                    levelService1.registerLevelDownListener(((playerId, newLevel) -> {
                        StatsUtils.resetStats(player, playerRef);
                        StatsUtils.applyAllStats(player, playerRef, newLevel, config);
                        world.execute(() -> {
                            var transform = world_store.getStore()
                                .getComponent(player.getReference(), EntityModule.get().getTransformComponentType());
                            SoundUtil.playSoundEvent3dToPlayer(
                                player.getReference(),
                                leveldown_sound,
                                SoundCategory.UI,
                                transform.getPosition(),
                                world_store.getStore()
                            );
                        });
                        // Need to clear out mapping whenever a player levels down as well
                        LevelUpRewardsUtil.clear(player.getUuid());
                    }));
                });
            }
        });
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}
