package com.azuredoom.levelingcore.compat.rpgmobs;

import com.frotty27.rpgmobs.api.RPGMobsAPI;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

public final class RPGMobsCompat {

    private RPGMobsCompat() {}

    public static boolean isEliteMob(Store<EntityStore> store, NPCEntity entity) {
        var tier = RPGMobsAPI.query().getTier(entity.getReference()).orElse(0);
        return tier != 0;
    }
}
