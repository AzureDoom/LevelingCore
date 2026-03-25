package com.azuredoom.levelingcore.systems.equipment;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.utils.NotificationsUtil;

public class ArmorBlockLevelSystem extends EntityEventSystem<EntityStore, InventoryChangeEvent> {

    protected final Set<UUID> ignoreArmorEvents = ConcurrentHashMap.newKeySet();

    protected volatile boolean restoringArmor = false;

    public ArmorBlockLevelSystem() {
        super(InventoryChangeEvent.class);
    }

    @Override
    public void handle(
        int index,
        @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
        @NotNull Store<EntityStore> store,
        @NotNull CommandBuffer<EntityStore> commandBuffer,
        @NotNull InventoryChangeEvent event
    ) {
        final Holder<EntityStore> holder = EntityUtils.toHolder(index, archetypeChunk);
        final Player player = holder.getComponent(Player.getComponentType());
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

        if (restoringArmor) {
            return;
        }

        if (ignoreArmorEvents.contains(playerUuid)) {
            return;
        }

        var armorContainer = archetypeChunk.getComponent(index, InventoryComponent.Armor.getComponentType());
        if (armorContainer == null) {
            return;
        }
        var armor = armorContainer.getInventory();
        if (armor == null) {
            return;
        }

        var changedContainer = event.getItemContainer();
        if (changedContainer == null || changedContainer != armor) {
            return;
        }

        var transaction = event.getTransaction();
        if (transaction == null) {
            return;
        }

        restoringArmor = true;
        try {
            rollbackArmorTransaction(player, armor, transaction, new HashSet<>(), commandBuffer);
        } finally {
            restoringArmor = false;
        }
    }

    protected void rollbackArmorTransaction(
        @NotNull Player player,
        @NotNull ItemContainer armorContainer,
        @Nullable Transaction transaction,
        @NotNull Set<String> refundedKeys,
        @NotNull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (transaction == null || !transaction.succeeded()) {
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

        switch (transaction) {
            case MoveTransaction<?> moveTransaction -> {
                if (moveTransaction.getMoveType() == MoveType.MOVE_TO_SELF) {
                    rollbackArmorTransaction(
                        player,
                        armorContainer,
                        moveTransaction.getAddTransaction(),
                        refundedKeys,
                        commandBuffer
                    );
                }
            }

            case ListTransaction<?> listTransaction -> {
                for (var nested : listTransaction.getList()) {
                    rollbackArmorTransaction(player, armorContainer, nested, refundedKeys, commandBuffer);
                }
            }

            case ItemStackTransaction itemStackTransaction -> {
                for (var slotTransaction : itemStackTransaction.getSlotTransactions()) {
                    rollbackArmorTransaction(player, armorContainer, slotTransaction, refundedKeys, commandBuffer);
                }
            }

            case SlotTransaction slotTransaction -> {
                var before = slotTransaction.getSlotBefore();
                var after = slotTransaction.getSlotAfter();

                if (after == null || ItemStack.isEmpty(after)) {
                    return;
                }

                if (sameStack(before, after)) {
                    return;
                }

                var itemId = after.getItemId();
                var levelRestriction = LevelingCore.itemLevelMapping.get(itemId);
                if (levelRestriction == null) {
                    return;
                }

                var playerLevel = LevelingCore.getLevelService().getLevel(playerUuid);
                if (playerLevel >= levelRestriction) {
                    return;
                }

                NotificationsUtil.sendLevelRequirementNotification(
                    playerRefComponent,
                    levelRestriction,
                    after,
                    playerLevel
                );

                var swapping = before != null && !ItemStack.isEmpty(before);

                armorContainer.setItemStackForSlot(slotTransaction.getSlot(), before, true);

                var key = "armorSlot:" + slotTransaction.getSlot();
                if (refundedKeys.add(key)) {
                    var everythingInventoryComponent = InventoryComponent.getCombined(
                        commandBuffer,
                        playerRef,
                        InventoryComponent.EVERYTHING
                    );
                    giveOrDrop(player, after, everythingInventoryComponent);

                    if (swapping) {
                        var removeOne = oneOf(before);
                        everythingInventoryComponent.removeItemStack(removeOne, false, true);
                    }
                }
            }

            default -> {}
        }
    }

    protected static boolean sameStack(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (ItemStack.isEmpty(a) && ItemStack.isEmpty(b)) {
            return true;
        }
        if (ItemStack.isEmpty(a) || ItemStack.isEmpty(b)) {
            return false;
        }

        return a.getQuantity() == b.getQuantity() && a.isEquivalentType(b);
    }

    protected static ItemStack oneOf(@NotNull ItemStack stack) {
        return stack.withQuantity(1);
    }

    protected static void giveOrDrop(
        @NotNull Player player,
        @NotNull ItemStack stack,
        @NotNull CombinedItemContainer inventoryComponent
    ) {
        if (ItemStack.isEmpty(stack)) {
            return;
        }

        var tx = inventoryComponent.addItemStack(stack);
        var remainder = tx.getRemainder();

        if (remainder != null && !ItemStack.isEmpty(remainder)) {
            var ref = player.getReference();
            if (ref != null) {
                ItemUtils.dropItem(ref, remainder, ref.getStore());
            }
        }
    }

    public void validateArmorOnReady(@NotNull Player player) {
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
        ignoreArmorEvents.add(playerUuid);

        HytaleServer.SCHEDULED_EXECUTOR.schedule(
            () -> ignoreArmorEvents.remove(playerUuid),
            500L,
            TimeUnit.MILLISECONDS
        );

        var armorComponent = playerRef.getStore().getComponent(playerRef, InventoryComponent.Armor.getComponentType());
        if (armorComponent == null) {
            return;
        }
        var armor = armorComponent.getInventory();
        if (armor == null) {
            return;
        }

        var playerLevel = LevelingCore.getLevelService().getLevel(playerUuid);

        restoringArmor = true;
        try {
            var capacity = armor.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                var stack = armor.getItemStack(slot);
                if (stack == null || ItemStack.isEmpty(stack)) {
                    continue;
                }

                var itemId = stack.getItemId();
                var req = LevelingCore.itemLevelMapping.get(itemId);
                if (req == null) {
                    continue;
                }

                if (playerLevel >= req) {
                    continue;
                }

                NotificationsUtil.sendLevelRequirementNotification(
                    playerRefComponent,
                    req,
                    stack,
                    playerLevel
                );

                armor.setItemStackForSlot(slot, null, true);
                var everythingInventoryComponent = InventoryComponent.getCombined(
                    playerRef.getStore(),
                    playerRef,
                    InventoryComponent.EVERYTHING
                );
                giveOrDrop(player, stack, everythingInventoryComponent);
            }
        } finally {
            restoringArmor = false;
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
