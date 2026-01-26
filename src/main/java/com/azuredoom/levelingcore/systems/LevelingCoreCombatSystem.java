package com.azuredoom.levelingcore.systems;

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
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Set;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;

public class LevelingCoreCombatSystem extends EntityEventSystem<EntityStore, Damage> {

    private Config<GUIConfig> config;

    public LevelingCoreCombatSystem(Config<GUIConfig> config) {
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

        var levelServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelServiceOpt.isEmpty())
            return;
        var levelService = levelServiceOpt.get();

        var cause = damage.getCause();
        if (cause == null)
            return;

        var causeId = cause.getId();
        var causeIdLower = causeId == null ? "" : causeId.toLowerCase();
        var isProjectile = causeIdLower.contains("projectile") || causeIdLower.contains("arrow");

        if (!(damage.getSource() instanceof Damage.EntitySource entitySource))
            return;

        var attackerRef = entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid())
            return;

        var playerAttacker = store.getComponent(attackerRef, Player.getComponentType());
        var npcAttacker = store.getComponent(attackerRef, NPCEntity.getComponentType());

        if (playerAttacker == null && npcAttacker == null)
            return;

        if (playerAttacker != null) {
            var attackerPlayerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
            if (attackerPlayerRef == null)
                return;

            var uuid = attackerPlayerRef.getUuid();
            var level = levelService.getLevel(uuid);

            var itemHand = playerAttacker.getInventory().getItemInHand();
            if (itemHand == null)
                return;
            var itemId = itemHand.getItemId();
            if (itemId != null && !itemId.isBlank()) {
                var requiredLevel = LevelingCore.itemLevelMapping.get(itemId);
                if (requiredLevel != null && level < requiredLevel) {
                    attackerPlayerRef.sendMessage(
                        Message.raw(
                            "You need level " + requiredLevel + " to use " + itemId + ". You are level " + level + "."
                        )
                    );
                    damage.setCancelled(true);
                    return;
                }
            }
            return;
        }

        var mobLevelData = LevelingCore.mobLevelRegistry.getOrCreate(
            npcAttacker.getUuid(),
            () -> MobLevelingUtil.computeSpawnLevel(npcAttacker)
        );
        var mobLevel = mobLevelData.level;

        if (isProjectile) {
            damage.setAmount(
                (float) (damage.getAmount() * (1.0 + mobLevel * config.get().getMobRangeDamageMultiplier()))
            );
        } else {
            damage.setAmount((float) (damage.getAmount() * (1.0 + mobLevel * config.get().getMobDamageMultiplier())));
        }
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(
            new SystemGroupDependency(Order.AFTER, DamageModule.get().getGatherDamageGroup()),
            new SystemDependency(Order.AFTER, DamageSystems.ApplyDamage.class)
        );
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
