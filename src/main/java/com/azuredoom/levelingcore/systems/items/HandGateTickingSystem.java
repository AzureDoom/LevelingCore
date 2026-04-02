package com.azuredoom.levelingcore.systems.items;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import com.azuredoom.levelingcore.LevelingCore;

public class HandGateTickingSystem extends EntityTickingSystem<EntityStore> {

    private final Map<UUID, HandGateSnapshot> handGate;

    public HandGateTickingSystem(Map<UUID, HandGateSnapshot> handGate) {
        this.handGate = handGate;
    }

    /**
     * Processes an entity to check and update the state of an item held in the player's hand, applying item level
     * restrictions if the configuration allows it.
     *
     * @param dt    The delta time since the last tick, used for time-based calculations (if needed).
     * @param index The index of the entity within the chunk being processed.
     * @param chunk The chunk of entities being processed.
     * @param store The storage containing the entity data.
     * @param cb    A command buffer used for registering entity-related operations during the tick.
     */
    @Override
    public void tick(
        float dt,
        int index,
        @NotNull ArchetypeChunk<EntityStore> chunk,
        @NotNull Store<EntityStore> store,
        @NotNull CommandBuffer<EntityStore> cb
    ) {
        if (!LevelingCore.getConfig().get().isEnableItemLevelRestriction()) {
            return;
        }
        final var player = chunk.getComponent(index, Player.getComponentType());
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
        var playerUuid = playerRefComponent.getUuid();
        var hand = InventoryComponent.getItemInHand(cb, player.getReference());
        if (hand == null || ItemStack.isEmpty(hand)) {
            handGate.put(playerUuid, new HandGateSnapshot(false, 0, 0, hand));
            return;
        }

        var req = LevelingCore.itemLevelMapping.get(hand.getItemId());
        if (req == null) {
            handGate.put(playerUuid, new HandGateSnapshot(false, 0, 0, hand));
            return;
        }

        var lvl = LevelingCore.getLevelService().getLevel(playerUuid);
        var blocked = lvl < req;

        handGate.put(playerUuid, new HandGateSnapshot(blocked, req, lvl, hand));
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
            Player.getComponentType(),
            PlayerRef.getComponentType()
        );
    }
}
