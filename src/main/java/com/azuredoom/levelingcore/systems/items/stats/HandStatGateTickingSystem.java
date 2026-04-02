package com.azuredoom.levelingcore.systems.items.stats;

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

public class HandStatGateTickingSystem extends EntityTickingSystem<EntityStore> {

    private final Map<UUID, HandStatGateSnapshot> handGate;

    public HandStatGateTickingSystem(Map<UUID, HandStatGateSnapshot> handGate) {
        this.handGate = handGate;
    }

    @Override
    public void tick(
        float dt,
        int index,
        @NotNull ArchetypeChunk<EntityStore> chunk,
        @NotNull Store<EntityStore> store,
        @NotNull CommandBuffer<EntityStore> cb
    ) {
        if (!LevelingCore.getConfig().get().isEnableItemStatRequirement()) {
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
            handGate.put(playerUuid, new HandStatGateSnapshot(false, null, null, hand));
            return;
        }

        var req = LevelingCore.itemStatRequirements.get(hand.getItemId());
        if (req == null) {
            handGate.put(playerUuid, new HandStatGateSnapshot(false, null, null, hand));
            return;
        }

        var levelService = LevelingCore.getLevelService();

        var stats = new PlayerStatSnapshot(
            levelService.getStr(playerUuid),
            levelService.getAgi(playerUuid),
            levelService.getPer(playerUuid),
            levelService.getVit(playerUuid),
            levelService.getInt(playerUuid),
            levelService.getCon(playerUuid)
        );

        var blocked = !req.matches(
            stats.str(),
            stats.agi(),
            stats.per(),
            stats.vit(),
            stats.intelligence(),
            stats.con()
        );

        handGate.put(playerUuid, new HandStatGateSnapshot(blocked, req, stats, hand));
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
            Player.getComponentType(),
            PlayerRef.getComponentType()
        );
    }
}
