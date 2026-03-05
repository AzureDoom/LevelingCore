package com.azuredoom.levelingcore.systems.items;

import com.hypixel.hytale.server.core.inventory.ItemStack;

public record HandGateSnapshot(
    boolean blocked,
    int req,
    int lvl,
    ItemStack hand
) {}
