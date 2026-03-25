package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.LevelingCore;

@SuppressWarnings("removal")
public final class LevelUpRewardsUtil {

    private static final ConcurrentHashMap<UUID, Integer> LAST_REWARDED_LEVEL =
        new ConcurrentHashMap<>();

    public static void giveRewards(int newLevel, Player player) {
        var last = LAST_REWARDED_LEVEL.getOrDefault(player.getUuid(), 0);
        if (newLevel <= last)
            return;

        var rewardsByLevel = LevelingCore.levelRewardMapping;

        var inv = player.getInventory().getCombinedStorageFirst();

        for (int lvl = last + 1; lvl <= newLevel; lvl++) {
            var rewards = rewardsByLevel.get(lvl);
            if (rewards == null || rewards.isEmpty())
                continue;

            for (var reward : rewards) {
                inv.addItemStack(new ItemStack(reward.itemNameId(), reward.quantity()));
            }
        }
        var playerRef = player.getReference();
        if (playerRef == null) {
            return;
        }
        var playerRefComponent = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
        if (playerRefComponent == null) {
            return;
        }

        LAST_REWARDED_LEVEL.put(playerRefComponent.getUuid(), newLevel);
    }

    public static void clear(UUID playerId) {
        LAST_REWARDED_LEVEL.remove(playerId);
    }
}
