package com.azuredoom.levelingcore.systems.equipment;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.utils.NotificationsUtil;

public class ArmorBlockStatSystem extends ArmorBlockLevelSystem {

    public ArmorBlockStatSystem() {
        super();
    }

    @Override
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
                var req = LevelingCore.itemStatRequirements.get(itemId);
                if (req == null) {
                    return;
                }

                var levelService = LevelingCore.getLevelService();
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

                var playerStr = levelService.getStr(playerUuid);
                var playerAgi = levelService.getAgi(playerUuid);
                var playerPer = levelService.getPer(playerUuid);
                var playerVit = levelService.getVit(playerUuid);
                var playerInt = levelService.getInt(playerUuid);
                var playerCon = levelService.getCon(playerUuid);

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
                    playerRefComponent,
                    after
                );

                var swapping = (before != null && !ItemStack.isEmpty(before));

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
}
