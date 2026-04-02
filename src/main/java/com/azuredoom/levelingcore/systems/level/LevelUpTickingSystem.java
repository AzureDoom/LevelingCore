package com.azuredoom.levelingcore.systems.level;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.utils.LevelUpListenerRegistrar;

public class LevelUpTickingSystem extends EntityTickingSystem<EntityStore> {

    private final Config<GUIConfig> config;

    public LevelUpTickingSystem(Config<GUIConfig> config) {
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
        final var player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) {
            return;
        }
        var playerRef = player.getReference();
        if (playerRef == null) {
            return;
        }
        var playerRefComponent = playerRef.getStore()
            .getComponent(playerRef, PlayerRef.getComponentType());
        if (playerRefComponent == null) {
            return;
        }
        LevelUpListenerRegistrar.ensureRegistered(store, player, playerRefComponent, config);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
            Player.getComponentType(),
            PlayerRef.getComponentType()
        );
    }
}
