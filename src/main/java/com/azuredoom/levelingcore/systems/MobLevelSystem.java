package com.azuredoom.levelingcore.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.formulas.loader.LevelTableLoader;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;
import com.azuredoom.levelingcore.utils.PendingUpdate;

@SuppressWarnings("removal")
public class MobLevelSystem extends EntityTickingSystem<EntityStore> {

    private final AtomicLong nextSaveAtMs = new AtomicLong(0);

    private static final long SAVE_INTERVAL_MS = 30_000;

    private static final long RECALC_INTERVAL_MS = 2_000;

    private final ConcurrentLinkedQueue<PendingUpdate> pending = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean drainScheduled = new AtomicBoolean(false);

    private static final int MAX_UPDATES_PER_DRAIN = 1000;

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
            if (nowMs >= scheduled && nextSaveAtMs.compareAndSet(scheduled, nowMs + SAVE_INTERVAL_MS)) {
                store.getExternalData().getWorld().execute(LevelingCore.mobLevelPersistence::save);
            }
        }

        final var holder = EntityUtils.toHolder(index, archetypeChunk);
        final var npc = holder.getComponent(NPCEntity.getComponentType());
        if (npc == null)
            return;

        var entityStatMap = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
        var healthStat = DefaultEntityStatTypes.getHealth();
        var healthValue = entityStatMap.get(healthStat);
        if (healthValue.get() <= 0)
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

        pending.add(new PendingUpdate(npc, transform, store, data));

        if (drainScheduled.compareAndSet(false, true)) {
            store.getExternalData().getWorld().execute(() -> drainPending(store));
        }
    }

    private void drainPending(@NonNullDecl Store<EntityStore> store) {
        try {
            var mobMaxLevel = computeMobMaxLevel();

            var processed = 0;
            PendingUpdate u;

            while (processed < MAX_UPDATES_PER_DRAIN && (u = pending.poll()) != null) {
                var npc = u.npc();
                var transform = u.transform();
                var store1 = u.store();
                var data = u.data();

                if (data.locked)
                    continue;

                var newLevel = Math.max(
                    1,
                    Math.min(mobMaxLevel, MobLevelingUtil.computeDynamicLevel(config, npc, transform, store1))
                );

                if (newLevel != data.level) {
                    data.level = newLevel;
                }

                if (data.level != data.lastAppliedLevel) {
                    MobLevelingUtil.applyMobScaling(config, npc, data.level, store1);
                    data.lastAppliedLevel = data.level;
                }

                processed++;
            }
        } finally {
            drainScheduled.set(false);

            if (!pending.isEmpty() && drainScheduled.compareAndSet(false, true)) {
                store.getExternalData().getWorld().execute(() -> drainPending(store));
            }
        }
    }

    private int computeMobMaxLevel() {
        var internalConfig = LevelingCore.levelingCoreConfig;
        var type = internalConfig.formula.type.trim().toUpperCase(Locale.ROOT);

        return switch (type) {
            case "LINEAR" -> internalConfig.formula.linear.maxLevel;
            case "TABLE" -> {
                var tableFormula = LevelTableLoader.loadOrCreateFromDataDir(
                    internalConfig.formula.table.file
                );
                yield Math.max(1, tableFormula.getMaxLevel());
            }
            case "CUSTOM" -> internalConfig.formula.custom.maxLevel;
            default -> internalConfig.formula.exponential.maxLevel;
        };
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                NPCEntity.getComponentType(),
                TransformComponent.getComponentType(),
                EntityStatMap.getComponentType()
        );
    }
}
