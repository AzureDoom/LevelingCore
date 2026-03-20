package com.azuredoom.levelingcore.systems.equipment;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.utils.NotificationsUtil;

@SuppressWarnings("removal")
public class EquipBlockStatManager extends EquipBlockManager {

    @Override
    public void validateArmorOnReady(@Nonnull Player player) {
        ignoreArmorEvents.add(player.getUuid());
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
            () -> ignoreArmorEvents.remove(player.getUuid()),
            500L,
            TimeUnit.MILLISECONDS
        );

        var inventory = player.getInventory();
        var armor = inventory.getArmor();
        if (armor == null) {
            return;
        }

        var levelService = LevelingCore.getLevelService();
        var uuid = player.getUuid();

        var playerStr = levelService.getStr(uuid);
        var playerAgi = levelService.getAgi(uuid);
        var playerPer = levelService.getPer(uuid);
        var playerVit = levelService.getVit(uuid);
        var playerInt = levelService.getInt(uuid);
        var playerCon = levelService.getCon(uuid);

        restoringArmor = true;
        try {
            var capacity = armor.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                var stack = armor.getItemStack(slot);
                if (stack == null || ItemStack.isEmpty(stack)) {
                    continue;
                }

                var itemId = stack.getItemId();
                var req = LevelingCore.itemStatRequirements.get(itemId);
                if (req == null) {
                    continue;
                }

                if (
                    req.matches(
                        playerStr,
                        playerAgi,
                        playerPer,
                        playerVit,
                        playerInt,
                        playerCon
                    )
                ) {
                    continue;
                }

                NotificationsUtil.sendStatRequirementNotification(
                    player.getPlayerRef(),
                    stack
                );

                armor.setItemStackForSlot(slot, null, true);
                giveOrDrop(player, stack);
            }
        } finally {
            restoringArmor = false;
        }
    }

    @Override
    protected void rollbackArmorTransaction(
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
                    rollbackArmorTransaction(
                        player,
                        armorContainer,
                        moveTransaction.getAddTransaction(),
                        refundedKeys
                    );
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

                if (after == null || ItemStack.isEmpty(after)) {
                    return;
                }

                if (sameStack(before, after)) {
                    return;
                }

                var itemId = after.getItemId();
                var req = LevelingCore.itemStatRequirements.get(itemId);
                if (req == null) {
                    return;
                }

                var levelService = LevelingCore.getLevelService();
                var uuid = player.getUuid();

                var playerStr = levelService.getStr(uuid);
                var playerAgi = levelService.getAgi(uuid);
                var playerPer = levelService.getPer(uuid);
                var playerVit = levelService.getVit(uuid);
                var playerInt = levelService.getInt(uuid);
                var playerCon = levelService.getCon(uuid);

                if (
                    req.matches(
                        playerStr,
                        playerAgi,
                        playerPer,
                        playerVit,
                        playerInt,
                        playerCon
                    )
                ) {
                    return;
                }

                NotificationsUtil.sendStatRequirementNotification(
                    player.getPlayerRef(),
                    after
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
            default -> {}
        }
    }
}
