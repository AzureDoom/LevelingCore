package com.azuredoom.levelingcore.systems.level;

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

    private static final int MAX_UPDATES_PER_DRAIN = 1000;

    private final Config<GUIConfig> config;

    private boolean drainScheduled = false;

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

        pending.add(new PendingUpdate(npc, transform, data));

        if (!drainScheduled) {
            drainScheduled = true;
            drainPending(store);
        }
    }

    /**
     * Drains and processes pending mob level updates from the queue until the maximum allowed updates per drain are
     * processed or the queue is empty. If there are still updates remaining in the queue, the method schedules itself
     * recursively until all updates are processed.
     * <p>
     * This method adjusts the dynamic level of mobs based on the computed maximum mob level and other leveling rules
     * defined in the configuration. It applies the computed level to the mobs and ensures the changes are reflected
     * unless the mob data is locked. Any successfully applied changes are stored to avoid redundant updates.
     *
     * @param store The store containing entities and associated data, used for applying updates and retrieving
     *              additional context during level computation and scaling.
     */
    private void drainPending(@NonNullDecl Store<EntityStore> store) {
        try {
            var mobMaxLevel = computeMobMaxLevel();

            var processed = 0;
            PendingUpdate u;

            while (processed < MAX_UPDATES_PER_DRAIN && (u = pending.poll()) != null) {
                var npc = u.npc();
                var transform = u.transform();
                var data = u.data();

                if (data.locked)
                    continue;

                var newLevel = Math.max(
                    1,
                    Math.min(mobMaxLevel, MobLevelingUtil.computeDynamicLevel(config, npc, transform, store))
                );

                if (newLevel != data.level) {
                    data.level = newLevel;
                }

                if (!data.locked) {
                    data.level = Math.max(
                        1,
                        Math.min(
                            mobMaxLevel,
                            MobLevelingUtil.computeDynamicLevel(config, npc, transform, store)
                        )
                    );
                }

                if (data.level != data.lastAppliedLevel) {
                    if (MobLevelingUtil.applyMobScaling(config, npc, data.level, store)) {
                        data.lastAppliedLevel = data.level;
                    }
                }

                processed++;
            }
        } finally {
            if (!pending.isEmpty()) {
                drainScheduled = true;
                drainPending(store);
            } else {
                drainScheduled = false;
            }
        }
    }

    /**
     * Determines the maximum mob level based on the formula type defined in the LevelingCore configuration.
     * <p>
     * The value is resolved according to the configured formula type:
     * <ul>
     * <li><b>LINEAR</b> – Returns the maximum level specified in the linear formula configuration.</li>
     * <li><b>TABLE</b> – Loads the configured level table file and determines the maximum level from the table
     * data.</li>
     * <li><b>CUSTOM</b> – Returns the maximum level defined in the custom formula configuration.</li>
     * <li><b>Other / default</b> – Falls back to the maximum level defined in the exponential formula
     * configuration.</li>
     * </ul>
     *
     * @return the maximum mob level determined from the active formula configuration
     */
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
