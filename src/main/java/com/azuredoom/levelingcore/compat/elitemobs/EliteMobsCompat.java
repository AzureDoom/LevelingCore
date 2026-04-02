package com.azuredoom.levelingcore.compat.elitemobs;

import com.github.cedeli.api.component.EliteComponent;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

public final class EliteMobsCompat {

    private EliteMobsCompat() {}

    public static boolean isEliteMob(Store<EntityStore> store, NPCEntity entity) {
        var ref = entity.getReference();
        if (ref == null || !ref.isValid()) {
            return false;
        }
        var hasComponent = store.getComponent(ref, EliteComponent.TYPE);

        return hasComponent != null;
    }
}
