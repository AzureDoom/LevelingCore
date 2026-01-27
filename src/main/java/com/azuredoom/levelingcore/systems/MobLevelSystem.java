package com.azuredoom.levelingcore.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.formulas.loader.LevelTableLoader;
import com.azuredoom.levelingcore.level.mobs.CoreLevelMode;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;

@SuppressWarnings("removal")
public class MobLevelSystem extends EntityTickingSystem<EntityStore> {

    private final AtomicLong nextSaveAtMs = new AtomicLong(0);

    private static final long SAVE_INTERVAL_MS = 30_000;

    private static final long RECALC_INTERVAL_MS = 2_000;

    private final Config<GUIConfig> config;

    public MobLevelSystem(Config<GUIConfig> config) {
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
        if (index == 0) {
            var nowMs = System.currentTimeMillis();
            var scheduled = nextSaveAtMs.get();
            if (nowMs >= scheduled && nextSaveAtMs.compareAndSet(scheduled, nowMs + 30_000)) {
                store.getExternalData().getWorld().execute(LevelingCore.mobLevelPersistence::save);
            }
        }

        final var holder = EntityUtils.toHolder(index, archetypeChunk);
        final var npc = holder.getComponent(NPCEntity.getComponentType());
        if (npc == null)
            return;

        final var transform = holder.getComponent(TransformComponent.getComponentType());
        if (transform == null)
            return;

        final var entityId = npc.getUuid();
        final var nowMs = System.currentTimeMillis();

        var data = LevelingCore.mobLevelRegistry.getOrCreateWithPersistence(
            entityId,
            () -> MobLevelingUtil.computeSpawnLevel(npc),
            nowMs,
            LevelingCore.mobLevelPersistence
        );
        if (data.locked)
            return;
        var last = data.lastRecalcMs;
        if (nowMs - last < RECALC_INTERVAL_MS)
            return;

        data.lastRecalcMs = nowMs;

        store.getExternalData().getWorld().execute(() -> {
            int mobMaxLevel;
            var internalConfig = LevelingCore.levelingCoreConfig;
            var type = internalConfig.formula.type.trim().toUpperCase(Locale.ROOT);

            switch (type) {
                case "LINEAR" -> mobMaxLevel = internalConfig.formula.linear.maxLevel;
                case "TABLE" -> {
                    var tableFormula = LevelTableLoader.loadOrCreateFromDataDir(
                        internalConfig.formula.table.file
                    );
                    mobMaxLevel = Math.max(1, tableFormula.getMaxLevel());
                }
                case "CUSTOM" -> mobMaxLevel = internalConfig.formula.custom.maxLevel;
                default -> mobMaxLevel = internalConfig.formula.exponential.maxLevel;
            }

            var newLevel = Math.max(
                1,
                Math.min(mobMaxLevel, MobLevelingUtil.computeDynamicLevel(config, npc, transform, store))
            );

            if (newLevel != data.level) {
                data.level = newLevel;
            }

            if (data.level != data.lastAppliedLevel) {
                MobLevelingUtil.applyMobScaling(config, npc, data.level, store);
                data.lastAppliedLevel = data.level;
            }

            if (
                config.get().getLevelMode().equalsIgnoreCase(CoreLevelMode.SPAWN_ONLY.name().toLowerCase(Locale.ROOT))
            ) {
                data.locked = true;
            }
        });
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}
