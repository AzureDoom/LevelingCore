package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.level.rewards.LevelRewards;

public final class LevelUpRewardsUtil {

    private static final ConcurrentHashMap<UUID, Integer> LAST_REWARDED_LEVEL =
        new ConcurrentHashMap<>();

    public static void giveRewards(int newLevel, Player player) {
        var last = LAST_REWARDED_LEVEL.getOrDefault(player.getUuid(), 0);
        if (newLevel <= last)
            return;

        var rewardsByLevel = LevelRewards.loadOrCreate(LevelingCore.configPath);

        var inv = player.getInventory().getCombinedStorageFirst();

        for (int lvl = last + 1; lvl <= newLevel; lvl++) {
            var rewards = rewardsByLevel.get(lvl);
            if (rewards == null || rewards.isEmpty())
                continue;

            for (var reward : rewards) {
                inv.addItemStack(new ItemStack(reward.itemNameId(), reward.quantity()));
            }
        }

        LAST_REWARDED_LEVEL.put(player.getUuid(), newLevel);
    }

    public static void clear(UUID playerId) {
        LAST_REWARDED_LEVEL.remove(playerId);
    }
}
