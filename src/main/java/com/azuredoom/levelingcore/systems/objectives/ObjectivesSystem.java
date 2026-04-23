package com.azuredoom.levelingcore.systems.objectives;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;

public class ObjectivesSystem extends EntityTickingSystem<EntityStore> {

    private final Set<UUID> rewardedObjectives = ConcurrentHashMap.newKeySet();

    @Override
    public void tick(
        float dt,
        int index,
        @NotNull ArchetypeChunk<EntityStore> chunk,
        @NotNull Store<EntityStore> store,
        @NotNull CommandBuffer<EntityStore> cb
    ) {
        if (index != 0) {
            return;
        }

        var levelService = LevelingCoreApi.getLevelServiceIfPresent().orElse(null);
        if (levelService == null) {
            return;
        }

        var objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();
        if (objectiveDataStore == null) {
            return;
        }

        for (var objective : objectiveDataStore.getObjectiveCollection()) {
            if (objective == null) {
                continue;
            }

            var objectiveUuid = objective.getObjectiveUUID();
            var objectiveName = objective.getObjectiveId();

            if (!objective.isCompleted()) {
                continue;
            }

            if (!rewardedObjectives.add(objectiveUuid)) {
                continue;
            }

            for (var playerUuid : objective.getActivePlayerUUIDs()) {
                var objectsMapping = LevelingCore.objectiveXPMapping;
                if (!objectsMapping.containsKey(objectiveName)) {
                    continue;
                }
                levelService.addXp(playerUuid, objectsMapping.get(objectiveName));
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), PlayerRef.getComponentType());
    }
}
