package com.azuredoom.levelingcore.systems.items.stats;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
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

        var holder = EntityUtils.toHolder(index, chunk);
        var player = holder.getComponent(Player.getComponentType());
        var playerRef = holder.getComponent(PlayerRef.getComponentType());
        if (player == null || playerRef == null)
            return;

        var hand = player.getInventory().getItemInHand();
        if (hand == null || ItemStack.isEmpty(hand)) {
            handGate.put(playerRef.getUuid(), new HandStatGateSnapshot(false, null, null, hand));
            return;
        }

        var req = LevelingCore.itemStatRequirements.get(hand.getItemId());
        if (req == null) {
            handGate.put(playerRef.getUuid(), new HandStatGateSnapshot(false, null, null, hand));
            return;
        }

        var levelService = LevelingCore.getLevelService();
        var uuid = playerRef.getUuid();

        var stats = new PlayerStatSnapshot(
            levelService.getStr(uuid),
            levelService.getAgi(uuid),
            levelService.getPer(uuid),
            levelService.getVit(uuid),
            levelService.getInt(uuid),
            levelService.getCon(uuid)
        );

        var blocked = !req.matches(
            stats.str(),
            stats.agi(),
            stats.per(),
            stats.vit(),
            stats.intelligence(),
            stats.con()
        );

        handGate.put(playerRef.getUuid(), new HandStatGateSnapshot(blocked, req, stats, hand));
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
            Player.getComponentType(),
            PlayerRef.getComponentType()
        );
    }
}
