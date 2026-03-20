package com.azuredoom.levelingcore.systems.items.stats;

import com.hypixel.hytale.server.core.inventory.ItemStack;

import com.azuredoom.levelingcore.level.itemlevellock.ItemStatRequirement;

/**
 * A snapshot representing the state of a player's hand and corresponding item stat requirements during gameplay.
 *
 * @param blocked A boolean value indicating whether the stat gate is currently blocking the player's interaction with
 *                the item.
 * @param req     The stat requirements that must be met for the player to use the item in hand.
 * @param stats   A snapshot of the player's current statistics including strength, agility, perception, vitality,
 *                intelligence, and constitution.
 * @param hand    The {@code ItemStack} representing the item currently held in the player's hand.
 */
public record HandStatGateSnapshot(
    boolean blocked,
    ItemStatRequirement req,
    PlayerStatSnapshot stats,
    ItemStack hand
) {}
