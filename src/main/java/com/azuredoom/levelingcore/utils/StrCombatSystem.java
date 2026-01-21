package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import java.util.Set;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.itemlevellock.ItemToLevelMapping;

public class StrCombatSystem extends EntityEventSystem<EntityStore, Damage> {

    private Config<GUIConfig> config;

    public StrCombatSystem(Config<GUIConfig> config) {
        super(Damage.class);
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
        if (damage.isCancelled())
            return;

        var cause = damage.getCause();
        if (cause == null)
            return;

        var levelServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelServiceOpt.isEmpty())
            return;
        var levelService = levelServiceOpt.get();

        var map = ItemToLevelMapping.loadOrCreate(LevelingCore.configPath);

        if (!(damage.getSource() instanceof Damage.EntitySource entitySource))
            return;

        var attackerRef = entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid())
            return;

        var attacker = store.getComponent(attackerRef, Player.getComponentType());
        if (attacker == null)
            return;

        var attackerPlayerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
        if (attackerPlayerRef == null)
            return;

        var uuid = attackerPlayerRef.getUuid();
        var level = levelService.getLevel(uuid);

        var itemHand = attacker.getInventory().getItemInHand();
        var itemId = itemHand.getItemId();
        if (itemId == null || itemId.isBlank())
            return;

        var requiredLevel = map.get(itemId);

        var causeId = cause.getId();
        var causeIdLower = causeId == null ? "" : causeId.toLowerCase();
        var isProjectile = causeIdLower.contains("projectile") || causeIdLower.contains("arrow");

        if (requiredLevel != null && level < requiredLevel) {
            attackerPlayerRef.sendMessage(
                Message.raw("You need level " + requiredLevel + " to use " + itemId + ". You are level " + level + ".")
            );
            damage.setCancelled(true);
            return;
        }

        if (isProjectile) {
            var per = levelService.getPer(uuid);
            damage.setAmount((float) (damage.getAmount() * (1.0 + per * config.get().getPerStatMultiplier())));
        } else {
            var str = levelService.getStr(uuid);
            damage.setAmount((float) (damage.getAmount() * (1.0 + str * config.get().getStrStatMultiplier())));
        }
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(
            new SystemGroupDependency(Order.AFTER, DamageModule.get().getGatherDamageGroup()),
            new SystemDependency(Order.BEFORE, DamageSystems.ApplyDamage.class)
        );
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
