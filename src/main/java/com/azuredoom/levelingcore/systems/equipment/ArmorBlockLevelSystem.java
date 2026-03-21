package com.azuredoom.levelingcore.systems.equipment;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.utils.NotificationsUtil;

@SuppressWarnings("removal")
public class ArmorBlockLevelSystem extends EntityEventSystem<EntityStore, InventoryChangeEvent> {

    private final Set<UUID> ignoreArmorEvents = ConcurrentHashMap.newKeySet();

    private volatile boolean restoringArmor = false;

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
        final PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        if (player == null || playerRef == null) {
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

    protected void rollbackArmorTransaction(
        @NotNull Player player,
        @NotNull ItemContainer armorContainer,
        @Nullable Transaction transaction,
        @NotNull Set<String> refundedKeys
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
                var levelRestriction = LevelingCore.itemLevelMapping.get(itemId);
                if (levelRestriction == null) {
                    return;
                }

                var playerLevel = LevelingCore.getLevelService().getLevel(player.getUuid());
                if (playerLevel >= levelRestriction) {
                    return;
                }

                NotificationsUtil.sendLevelRequirementNotification(
                    player.getPlayerRef(),
                    levelRestriction,
                    after,
                    playerLevel
                );

                var swapping = before != null && !ItemStack.isEmpty(before);

                armorContainer.setItemStackForSlot(slotTransaction.getSlot(), before, true);

                var key = "armorSlot:" + slotTransaction.getSlot();
                if (refundedKeys.add(key)) {
                    giveOrDrop(player, after);

                    if (swapping) {
                        var removeOne = oneOf(before);
                        player.getInventory()
                            .getCombinedHotbarFirst()
                            .removeItemStack(removeOne, false, true);
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

        if (!Objects.equals(a.getItemId(), b.getItemId())) {
            return false;
        }
        if (a.getQuantity() != b.getQuantity()) {
            return false;
        }
        return Objects.equals(a.getMetadata(), b.getMetadata());
    }

    protected static ItemStack oneOf(@NotNull ItemStack stack) {
        return new ItemStack(stack.getItemId(), 1, stack.getMetadata());
    }

    protected static void giveOrDrop(@NotNull Player player, @NotNull ItemStack stack) {
        if (ItemStack.isEmpty(stack)) {
            return;
        }

        var inv = player.getInventory().getCombinedHotbarFirst();
        var tx = inv.addItemStack(stack);
        var remainder = tx.getRemainder();

        if (remainder != null && !ItemStack.isEmpty(remainder)) {
            var ref = player.getReference();
            if (ref != null) {
                ItemUtils.dropItem(ref, remainder, ref.getStore());
            }
        }
    }

    public void validateArmorOnReady(@NotNull Player player) {
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

        var playerLevel = LevelingCore.getLevelService().getLevel(player.getUuid());

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
                    player.getPlayerRef(),
                    req,
                    stack,
                    playerLevel
                );

                armor.setItemStackForSlot(slot, null, true);
                giveOrDrop(player, stack);
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
