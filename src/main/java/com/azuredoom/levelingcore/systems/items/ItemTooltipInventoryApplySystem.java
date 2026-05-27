package com.azuredoom.levelingcore.systems.items;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
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
import java.util.concurrent.ConcurrentHashMap;

public class ItemTooltipInventoryApplySystem extends EntityTickingSystem<EntityStore> {

    private final Map<UUID, Map<String, String>> appliedCache = new ConcurrentHashMap<>();

    @Override
    public void tick(
        float dt,
        int index,
        @NotNull ArchetypeChunk<EntityStore> chunk,
        @NotNull Store<EntityStore> store,
        @NotNull CommandBuffer<EntityStore> cb
    ) {
        if (!ItemTooltipMetadataManager.INSTANCE.isReady()) {
            return;
        }

        var player = chunk.getComponent(index, Player.getComponentType());
        if (player == null || player.getReference() == null) {
            return;
        }

        var playerRefComponent = player.getReference()
            .getStore()
            .getComponent(player.getReference(), PlayerRef.getComponentType());

        if (playerRefComponent == null) {
            return;
        }

        var playerUuid = playerRefComponent.getUuid();
        var playerCache = appliedCache.computeIfAbsent(playerUuid, _ -> new ConcurrentHashMap<>());

        applyToInventory(playerCache, chunk, index, InventoryComponent.Hotbar.getComponentType());
        applyToInventory(playerCache, chunk, index, InventoryComponent.Storage.getComponentType());
        applyToInventory(playerCache, chunk, index, InventoryComponent.Armor.getComponentType());
        applyToInventory(playerCache, chunk, index, InventoryComponent.Utility.getComponentType());
        applyToInventory(playerCache, chunk, index, InventoryComponent.Backpack.getComponentType());
        applyToInventory(playerCache, chunk, index, InventoryComponent.Tool.getComponentType());
    }

    private void applyToInventory(
        Map<String, String> playerCache,
        ArchetypeChunk<EntityStore> chunk,
        int index,
        ComponentType<EntityStore, ? extends InventoryComponent> componentType
    ) {
        var component = chunk.getComponent(index, componentType);
        if (component == null) {
            return;
        }

        var inventory = component.getInventory();
        if (inventory == null) {
            return;
        }

        for (short slot = 0; slot < inventory.getCapacity(); slot++) {
            var stack = inventory.getItemStack(slot);
            var cacheKey = slotKey(componentType, slot);

            if (stack == null || ItemStack.isEmpty(stack)) {
                playerCache.remove(cacheKey);
                continue;
            } else {
                stack.getItem();
            }

            var itemId = stack.getItem().getId();
            if (itemId == null) {
                playerCache.remove(cacheKey);
                continue;
            }

            var addition = ItemTooltipMetadataManager.INSTANCE.getAddition(itemId);
            if (addition == null || addition.isBlank()) {
                playerCache.remove(cacheKey);
                continue;
            }

            var signature = itemId + "|" +
                stack.getQuantity() + "|" +
                addition.hashCode() + "|" +
                ItemTooltipMetadataManager.INSTANCE.getRevision();

            if (signature.equals(playerCache.get(cacheKey))) {
                continue;
            }

            var displayStack = ItemTooltipMetadataManager.INSTANCE.applyDisplayMetadata(stack);

            if (displayStack != stack) {
                inventory.setItemStackForSlot(slot, displayStack, false);
                component.markDirty();
                playerCache.put(cacheKey, signature);
            }
        }
    }

    private String slotKey(ComponentType<EntityStore, ? extends InventoryComponent> componentType, short slot) {
        return componentType.toString() + ":" + slot;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
            Player.getComponentType(),
            PlayerRef.getComponentType()
        );
    }
}
