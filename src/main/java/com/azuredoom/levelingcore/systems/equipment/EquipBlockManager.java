package com.azuredoom.levelingcore.systems.equipment;

import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveType;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.lang.CommandLang;

@SuppressWarnings("removal")
public class EquipBlockManager {

    @Nullable
    private volatile EventRegistration<?, LivingEntityInventoryChangeEvent> inventoryChangeRegistration;

    private final Set<UUID> ignoreArmorEvents = ConcurrentHashMap.newKeySet();

    private volatile boolean restoringArmor = false;

    public void start() {
        if (inventoryChangeRegistration == null || !inventoryChangeRegistration.isRegistered()) {
            inventoryChangeRegistration = LevelingCore.getInstance()
                .getEventRegistry()
                .registerGlobal(LivingEntityInventoryChangeEvent.class, this::onInventoryChange);
        }
    }

    public void shutdown() {
        EventRegistration<?, LivingEntityInventoryChangeEvent> inventoryRegistration = inventoryChangeRegistration;
        if (inventoryRegistration != null && inventoryRegistration.isRegistered()) {
            inventoryRegistration.unregister();
        }
        inventoryChangeRegistration = null;
    }

    /**
     * Validates the armor equipped by the player to ensure they meet the required level criteria.
     * Removes any armor items that the player does not meet the level requirement for and
     * either gives them back to the player or drops them in the game world.
     * Temporarily ignores related armor events to avoid cyclic processes during this validation.
     *
     * @param player the player whose equipped armor is to be validated
     */
    public void validateArmorOnReady(@Nonnull Player player) {
        ignoreArmorEvents.add(player.getUuid());
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> ignoreArmorEvents.remove(player.getUuid()),
                500L,
                TimeUnit.MILLISECONDS
        );
        var inventory = player.getInventory();
        var armor = inventory.getArmor();
        if (armor == null) return;

        var playerLevel = LevelingCore.getLevelService().getLevel(player.getUuid());

        restoringArmor = true;
        try {
            var capacity = armor.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                var stack = armor.getItemStack(slot);
                if (stack == null || ItemStack.isEmpty(stack)) continue;

                var itemId = stack.getItemId();
                var req = LevelingCore.itemLevelMapping.get(itemId);
                if (req == null) continue;
                if (playerLevel >= req) continue;

                player.sendMessage(
                        CommandLang.LEVEL_REQUIRED
                                .param("requiredlevel", req)
                                .param("itemid", itemId)
                                .param("level", playerLevel)
                );

                armor.setItemStackForSlot(slot, null, true);
                giveOrDrop(player, stack);
            }
        } finally {
            restoringArmor = false;
        }
    }

    private void onInventoryChange(@Nonnull LivingEntityInventoryChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (restoringArmor) {
            return;
        }

        if (ignoreArmorEvents.contains(player.getUuid())) {
            return;
        }

        var inventory = player.getInventory();
        var armorContainer = inventory.getArmor();
        if (armorContainer == null) {
            return;
        }

        var changedContainer = event.getItemContainer();
        if (changedContainer == null || changedContainer != armorContainer) {
            return;
        }

        var transaction = event.getTransaction();
        if (transaction == null) {
            return;
        }

        restoringArmor = true;
        try {
            rollbackArmorTransaction(player, armorContainer, transaction, new HashSet<>());
        } finally {
            restoringArmor = false;
        }
    }

    private void rollbackArmorTransaction(
        @Nonnull Player player,
        @Nonnull ItemContainer armorContainer,
        @Nullable Transaction transaction,
        @Nonnull Set<String> refundedKeys
    ) {
        if (transaction == null || !transaction.succeeded()) {
            return;
        }

        switch (transaction) {
            case MoveTransaction<?> moveTransaction -> {
                if (moveTransaction.getMoveType() == MoveType.MOVE_TO_SELF) {
                    rollbackArmorTransaction(player, armorContainer, moveTransaction.getAddTransaction(), refundedKeys);
                }
            }
            case ListTransaction<?> listTransaction -> {
                for (var nested : listTransaction.getList()) {
                    rollbackArmorTransaction(player, armorContainer, nested, refundedKeys);
                }
            }
            case ItemStackTransaction itemStackTransaction -> {
                for (var slotTransaction : itemStackTransaction.getSlotTransactions()) {
                    rollbackArmorTransaction(player, armorContainer, slotTransaction, refundedKeys);
                }
            }
            case SlotTransaction slotTransaction -> {
                var before = slotTransaction.getSlotBefore();
                var after  = slotTransaction.getSlotAfter();

                if (after == null || ItemStack.isEmpty(after)) return;

                if (sameStack(before, after)) return;

                var itemId = after.getItemId();
                var levelRestriction = LevelingCore.itemLevelMapping.get(itemId);
                if (levelRestriction == null) return;

                var playerLevel = LevelingCore.getLevelService().getLevel(player.getUuid());
                if (playerLevel >= levelRestriction) return;

                player.sendMessage(
                        CommandLang.LEVEL_REQUIRED
                                .param("requiredlevel", levelRestriction)
                                .param("itemid", itemId)
                                .param("level", playerLevel)
                );

                var swapping = (before != null && !ItemStack.isEmpty(before));

                armorContainer.setItemStackForSlot(slotTransaction.getSlot(), before, true);

                var key = "armorSlot:" + slotTransaction.getSlot();
                if (refundedKeys.add(key)) {
                    giveOrDrop(player, after);

                    if (swapping) {
                        var removeOne = oneOf(before);
                        player.getInventory().getCombinedHotbarFirst().removeItemStack(removeOne, false, true);
                    }
                }
            }
            default -> {
            }
        }
    }

    private static boolean sameStack(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (ItemStack.isEmpty(a) && ItemStack.isEmpty(b)) return true;
        if (ItemStack.isEmpty(a) || ItemStack.isEmpty(b)) return false;

        if (!Objects.equals(a.getItemId(), b.getItemId())) return false;
        if (a.getQuantity() != b.getQuantity()) return false;
        return Objects.equals(a.getMetadata(), b.getMetadata());
    }

    private static ItemStack oneOf(@Nonnull ItemStack stack) {
        return new ItemStack(stack.getItemId(), 1, stack.getMetadata());
    }

    private static void giveOrDrop(@Nonnull Player player, @Nonnull ItemStack stack) {
        if (ItemStack.isEmpty(stack)) return;

        var inv = player.getInventory().getCombinedHotbarFirst();

        var tx = inv.addItemStack(stack);
        var remainder = tx.getRemainder();

        if (remainder != null && !ItemStack.isEmpty(remainder)) {
            dropItem(player, remainder);
        }
    }

    private static void dropItem(@Nonnull Player player, @Nonnull ItemStack stack) {
        var ref = player.getReference();
        if (ref == null) return;
        ItemUtils.dropItem(player.getReference(), stack, player.getReference().getStore());
    }
}
