package com.azuredoom.levelingcore.systems.equipment;

import com.hypixel.hytale.event.EventRegistration;
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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.lang.CommandLang;

public class EquipBlockManager {

    @Nullable
    private volatile EventRegistration<?, LivingEntityInventoryChangeEvent> inventoryChangeRegistration;

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

    private void onInventoryChange(@Nonnull LivingEntityInventoryChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (restoringArmor) {
            return;
        }

        Inventory inventory = player.getInventory();
        ItemContainer armorContainer = inventory.getArmor();
        if (armorContainer == null) {
            return;
        }

        ItemContainer changedContainer = event.getItemContainer();
        if (changedContainer == null || changedContainer != armorContainer) {
            return;
        }

        Transaction transaction = event.getTransaction();
        if (transaction == null) {
            return;
        }

        List<ItemStack> returnToInventory = new ArrayList<>();
        restoringArmor = true;
        try {
            rollbackArmorTransaction(player, armorContainer, transaction, returnToInventory);

            for (ItemStack stack : returnToInventory) {
                if (stack != null && !ItemStack.isEmpty(stack)) {
                    inventory.getCombinedHotbarFirst().addItemStack(stack);
                }
            }
        } finally {
            restoringArmor = false;
        }
    }

    private void rollbackArmorTransaction(
        @Nonnull Player player,
        @Nonnull ItemContainer armorContainer,
        @Nullable Transaction transaction,
        @Nonnull List<ItemStack> returnedItems
    ) {
        if (transaction == null || !transaction.succeeded()) {
            return;
        }

        if (transaction instanceof MoveTransaction<?> moveTransaction) {
            if (moveTransaction.getMoveType() == MoveType.MOVE_TO_SELF) {
                rollbackArmorTransaction(player, armorContainer, moveTransaction.getAddTransaction(), returnedItems);
            }
            return;
        }

        if (transaction instanceof ListTransaction<?> listTransaction) {
            for (Transaction nested : listTransaction.getList()) {
                rollbackArmorTransaction(player, armorContainer, nested, returnedItems);
            }
            return;
        }

        if (transaction instanceof ItemStackTransaction itemStackTransaction) {
            for (SlotTransaction slotTransaction : itemStackTransaction.getSlotTransactions()) {
                rollbackArmorTransaction(player, armorContainer, slotTransaction, returnedItems);
            }
            return;
        }

        if (transaction instanceof SlotTransaction slotTransaction) {
            ItemStack after = slotTransaction.getSlotAfter();
            if (after == null || ItemStack.isEmpty(after)) {
                return;
            }

            String itemId = after.getItemId();
            Integer levelRestriction = LevelingCore.itemLevelMapping.getOrDefault(itemId, null);
            Integer playerLevel = LevelingCore.getLevelService().getLevel(player.getUuid());
            if (levelRestriction == null)
                return;

            if (playerLevel >= levelRestriction)
                return;

            player.sendMessage(
                CommandLang.LEVEL_REQUIRED.param("requiredlevel", levelRestriction)
                    .param("itemid", itemId)
                    .param("level", playerLevel)
            );

            armorContainer.setItemStackForSlot(slotTransaction.getSlot(), slotTransaction.getSlotBefore(), false);
            returnedItems.add(after);
        }
    }
}
