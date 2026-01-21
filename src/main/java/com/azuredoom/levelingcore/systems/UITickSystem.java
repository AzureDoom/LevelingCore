package com.azuredoom.levelingcore.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;

public class UITickSystem extends EntityTickingSystem<EntityStore> {

    private final Config<GUIConfig> config;

    public UITickSystem(Config<GUIConfig> config) {
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

        store.getExternalData().getWorld().execute(() -> {
            LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
                var hud = new XPBarHud(playerRef, levelService, config);
                var uiCommandBuilder = new UICommandBuilder();
                levelService.registerLevelUpListener((playerId, oldLevel, newLevel) -> {
                    if (newLevel == oldLevel)
                        return;
                    hud.update(uiCommandBuilder);
                });
                levelService.registerLevelDownListener((playerId, oldLevel, newLevel) -> {
                    if (newLevel == oldLevel)
                        return;
                    hud.update(uiCommandBuilder);
                });
            });
        });
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}
