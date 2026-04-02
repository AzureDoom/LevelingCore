package com.azuredoom.levelingcore.level.mobs;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class MobLevelRegistry {

    private final ConcurrentHashMap<UUID, MobLevelData> levels = new ConcurrentHashMap<>();

    public MobLevelData getOrCreate(UUID entityId, IntSupplier initialLevelSupplier, long nowTick) {
        return levels.computeIfAbsent(entityId, id -> new MobLevelData(initialLevelSupplier.getAsInt()));
    }

    public MobLevelData getOrCreate(UUID entityId, IntSupplier initialLevelSupplier) {
        return levels.computeIfAbsent(
            entityId,
            id -> new MobLevelData(initialLevelSupplier.getAsInt())
        );
    }

    public MobLevelData getOrCreateWithPersistence(
        UUID entityId,
        IntSupplier spawnLevelSupplier,
        Supplier<MobEncounterProfile> profileSupplier,
        MobLevelPersistence persistence
    ) {
        return levels.computeIfAbsent(entityId, id -> {
            var persistedOpt = persistence.get(id);
            if (persistedOpt.isPresent()) {
                var persisted = persistedOpt.get();
                var data = new MobLevelData(persisted.spawnLevel());
                data.locked = persisted.locked();
                return data;
            }
            var spawnLevel = spawnLevelSupplier.getAsInt();

            var profile = profileSupplier.get();
            var data = MobLevelData.from(spawnLevel, profile);
            persistence.put(
                id,
                new PersistedMobLevel(
                    data.level,
                    data.locked,
                    data.profileId,
                    data.tier.name(),
                    data.healthMultiplier,
                    data.damageMultiplier,
                    data.xpMultiplier
                )
            );

            return data;
        });
    }

    public MobLevelData get(UUID entityId) {
        return levels.get(entityId);
    }

    public void set(UUID entityId, MobLevelData data) {
        levels.put(entityId, data);
    }

    public void remove(UUID entityId) {
        levels.remove(entityId);
    }

    public int size() {
        return levels.size();
    }

    public ConcurrentHashMap<UUID, MobLevelData> snapshot() {
        return new ConcurrentHashMap<>(levels);
    }

    public void loadSnapshot(ConcurrentHashMap<UUID, MobLevelData> snapshot) {
        levels.clear();
        levels.putAll(snapshot);
    }
}
