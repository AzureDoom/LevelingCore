package com.azuredoom.levelingcore.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;

public class MobDamageFilter extends DamageEventSystem {

    private Config<GUIConfig> config;

    public MobDamageFilter(Config<GUIConfig> config) {
        this.config = config;
    }

    @Override
    public void handle(
        int index,
        @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
        @Nonnull Store<EntityStore> store,
        @Nonnull CommandBuffer<EntityStore> commandBuffer,
        @Nonnull Damage damage
    ) {
        var isPlayer = archetypeChunk.getArchetype().contains(EntityModule.get().getPlayerComponentType());
        if (isPlayer)
            return;
        var holder = EntityUtils.toHolder(index, archetypeChunk);
        var victimNPCRef = holder.getComponent(NPCEntity.getComponentType());
        if (victimNPCRef == null || !(victimNPCRef instanceof NPCEntity))
            return;
        if (!(damage.getSource() instanceof Damage.EntitySource entitySource))
            return;
        var attackerRef = entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid())
            return;

        var playerRefAttacker = store.getComponent(attackerRef, PlayerRef.getComponentType());
        if (playerRefAttacker == null)
            return;

        var levelServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelServiceOpt.isEmpty())
            return;

        var levelService = levelServiceOpt.get();

        var incoming = damage.getAmount();
        if (incoming <= 0f)
            return;
        if (config.get().isEnableItemLevelRestriction()) {
            var playerAttacker = store.getComponent(attackerRef, Player.getComponentType());
            if (playerAttacker == null)
                return;

            var level = levelService.getLevel(playerRefAttacker.getUuid());
            var itemHand = playerAttacker.getInventory().getItemInHand();
            if (itemHand == null)
                return;
            var itemId = itemHand.getItemId();
            if (itemId != null && !itemId.isBlank()) {
                var requiredLevel = LevelingCore.itemLevelMapping.get(itemId);
                if (requiredLevel != null && level < requiredLevel) {
                    playerRefAttacker.sendMessage(
                        CommandLang.LEVEL_REQUIRED.param("requiredlevel", requiredLevel)
                            .param("itemid", itemId)
                            .param("level", level)
                    );
                    damage.setCancelled(true);
                    return;
                }
            }
        }
        var cause = damage.getCause();
        if (cause == null)
            return;

        var causeId = cause.getId();
        var causeIdLower = causeId == null ? "" : causeId.toLowerCase();
        var isProjectile = causeIdLower.contains("projectile") || causeIdLower.contains("arrow");

        if (isProjectile) {
            var per = levelService.getPer(playerRefAttacker.getUuid());
            damage.setAmount(
                Math.round((float) (damage.getAmount() * (1.0 + per * config.get().getPerStatMultiplier())))
            );
        } else {
            var str = levelService.getStr(playerRefAttacker.getUuid());
            damage.setAmount(
                Math.round((float) (damage.getAmount() * (1.0 + str * config.get().getStrStatMultiplier())))
            );
        }
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return AllLegacyLivingEntityTypesQuery.INSTANCE;
    }
}
