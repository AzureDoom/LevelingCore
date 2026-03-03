package com.azuredoom.levelingcore.systems.equipment;

import com.azuredoom.levelingcore.utils.NotificationsUtil;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
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
     * Validates the armor equipped by the player to ensure they meet the required level criteria. Removes any armor
     * items that the player does not meet the level requirement for and either gives them back to the player or drops
     * them in the game world. Temporarily ignores related armor events to avoid cyclic processes during this
     * validation.
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
        if (armor == null)
            return;

        var playerLevel = LevelingCore.getLevelService().getLevel(player.getUuid());

        restoringArmor = true;
        try {
            var capacity = armor.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                var stack = armor.getItemStack(slot);
                if (stack == null || ItemStack.isEmpty(stack))
                    continue;

                var itemId = stack.getItemId();
                var req = LevelingCore.itemLevelMapping.get(itemId);
                if (req == null)
                    continue;
                if (playerLevel >= req)
                    continue;

                NotificationsUtil.sendLevelRequirementNotification(player.getPlayerRef(), req, stack, playerLevel);

                armor.setItemStackForSlot(slot, null, true);
                giveOrDrop(player, stack);
            }
        } finally {
            restoringArmor = false;
        }
    }

    /**
     * Handles inventory change events specifically related to armor slots for players. This method ensures that any
     * unauthorized changes to the armor slots, such as equipping items without meeting level restrictions, are rolled
     * back and appropriate actions are taken. The method temporarily ignores specific players or states to avoid cyclic
     * event handling.
     *
     * @param event the inventory change event that contains context about the entity, transaction, and the affected
     *              inventory container
     */
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

    /**
     * Rolls back the player's armor transaction in the event of a failed or invalid transaction, ensuring that
     * unauthorized or restricted items are removed and returned to the player or dropped into the world. This method is
     * recursively called for nested transactions and handles various transaction types, including move transactions,
     * list transactions, item stack transactions, and slot transactions.
     *
     * @param player         the player whose armor transaction is being rolled back; must not be null
     * @param armorContainer the container representing the player's current armor; must not be null
     * @param transaction    the transaction object representing the changes to the armor; may be null
     * @param refundedKeys   a set of keys used to track already refunded or processed slots and avoid duplicates; must
     *                       not be null
     */
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
                var after = slotTransaction.getSlotAfter();

                if (after == null || ItemStack.isEmpty(after))
                    return;

                if (sameStack(before, after))
                    return;

                var itemId = after.getItemId();
                var levelRestriction = LevelingCore.itemLevelMapping.get(itemId);
                if (levelRestriction == null)
                    return;

                var playerLevel = LevelingCore.getLevelService().getLevel(player.getUuid());
                if (playerLevel >= levelRestriction)
                    return;

                NotificationsUtil.sendLevelRequirementNotification(player.getPlayerRef(), levelRestriction, after, playerLevel);

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
            default -> {}
        }
    }

    /**
     * Compares two {@link ItemStack} objects and determines if they are considered equivalent. Two stacks are
     * considered equivalent if they have the same item ID, quantity, and metadata. Empty stacks are treated specially
     * and considered equivalent if both are empty.
     *
     * @param a the first {@link ItemStack} to compare, or {@code null}
     * @param b the second {@link ItemStack} to compare, or {@code null}
     * @return {@code true} if the two stacks are considered equivalent; {@code false} otherwise
     */
    private static boolean sameStack(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        if (ItemStack.isEmpty(a) && ItemStack.isEmpty(b))
            return true;
        if (ItemStack.isEmpty(a) || ItemStack.isEmpty(b))
            return false;

        if (!Objects.equals(a.getItemId(), b.getItemId()))
            return false;
        if (a.getQuantity() != b.getQuantity())
            return false;
        return Objects.equals(a.getMetadata(), b.getMetadata());
    }

    /**
     * Creates a new {@link ItemStack} with a quantity of 1, based on the given {@code stack}. This method retains the
     * item ID and metadata of the provided stack but modifies the quantity to ensure that only a single item is
     * represented.
     *
     * @param stack the {@link ItemStack} to be used as the base for creating the new stack; must not be null
     * @return a new {@link ItemStack} with the same item ID and metadata as the input stack, but with a quantity of 1
     */
    private static ItemStack oneOf(@Nonnull ItemStack stack) {
        return new ItemStack(stack.getItemId(), 1, stack.getMetadata());
    }

    /**
     * Attempts to add the specified item stack to the player's inventory. If the item stack cannot be fully added to
     * the inventory (e.g., due to lack of space), the remaining items are dropped in the game world at the player's
     * location.
     *
     * @param player the player to whom the item stack will be given or near whom the items will be dropped if there is
     *               not enough inventory space; must not be null
     * @param stack  the item stack to be given to the player or partially dropped; must not be null
     */
    private static void giveOrDrop(@Nonnull Player player, @Nonnull ItemStack stack) {
        if (ItemStack.isEmpty(stack))
            return;

        var inv = player.getInventory().getCombinedHotbarFirst();

        var tx = inv.addItemStack(stack);
        var remainder = tx.getRemainder();

        if (remainder != null && !ItemStack.isEmpty(remainder)) {
            var ref = player.getReference();
            if (ref != null)
                ItemUtils.dropItem(ref, stack, ref.getStore());
        }
    }
}
