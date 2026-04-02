package com.azuredoom.levelingcore.systems.damage;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;

public class MobDamageFilter extends DamageEventSystem {

    private final Config<GUIConfig> config;

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
        var victimNPCRef = holder.getComponent(Objects.requireNonNull(NPCEntity.getComponentType()));
        if (victimNPCRef == null)
            return;

        if (!(damage.getSource() instanceof Damage.EntitySource entitySource))
            return;

        var attackerRef = entitySource.getRef();
        if (!attackerRef.isValid())
            return;

        var playerRefAttacker = store.getComponent(attackerRef, PlayerRef.getComponentType());
        if (playerRefAttacker == null)
            return;

        var playerComponent = store.getComponent(attackerRef, Player.getComponentType());
        if (playerComponent == null || playerComponent.getInventory() == null)
            return;

        var levelServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelServiceOpt.isEmpty())
            return;
        var levelService = levelServiceOpt.get();

        var uuid = playerRefAttacker.getUuid();
        var str = levelService.getStr(uuid);
        var per = levelService.getPer(uuid);

        var incoming = damage.getAmount();
        if (incoming <= 0f)
            return;

        var cause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
        if (cause == null)
            return;

        var causeId = cause.getId();
        var causeIdLower = causeId == null ? "" : causeId.toLowerCase();
        var isProjectile = causeIdLower.contains("projectile") || causeIdLower.contains("arrow");

        var mobData = LevelingCore.mobLevelRegistry.getOrCreate(
            uuid,
            () -> MobLevelingUtil.computeSpawnLevel(commandBuffer, victim)
        );
        var mobDamageThreshold = mobData.damageThreshold;
        var statMultiplier = isProjectile
            ? config.get().getPerStatMultiplier()
            : config.get().getStrStatMultiplier();

        var statValue = isProjectile ? per : str;
        var statBonus = 1.0 + ((statValue * statMultiplier / 100.0) / (1.0 + statValue / 100.0));

        damage.setAmount((float) (incoming * mobDamageThreshold * statBonus));
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
            NPCEntity.getComponentType(),
            Query.not(EntityModule.get().getPlayerComponentType())
        );
    }
}
