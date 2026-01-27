package com.azuredoom.levelingcore.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.EntityUtils;
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
import com.azuredoom.levelingcore.utils.MobLevelingUtil;

@SuppressWarnings("removal")
public class PlayerDamageFilter extends DamageEventSystem {

    private Config<GUIConfig> config;

    public PlayerDamageFilter(Config<GUIConfig> config) {
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
        if (!isPlayer)
            return;
        var holder = EntityUtils.toHolder(index, archetypeChunk);
        var victimPlayerRef = holder.getComponent(PlayerRef.getComponentType());
        if (victimPlayerRef == null || !(victimPlayerRef instanceof PlayerRef))
            return;
        if (!(damage.getSource() instanceof Damage.EntitySource entitySource))
            return;
        var attackerRef = entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid())
            return;

        var npcAttacker = store.getComponent(attackerRef, NPCEntity.getComponentType());
        if (npcAttacker == null)
            return;

        var levelServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelServiceOpt.isEmpty())
            return;

        var levelService = levelServiceOpt.get();

        var incoming = damage.getAmount();
        if (incoming <= 0f)
            return;

        var cause = damage.getCause();
        if (cause == null)
            return;

        var causeId = cause.getId();
        var causeIdLower = causeId == null ? "" : causeId.toLowerCase();
        var isProjectile = causeIdLower.contains("projectile") || causeIdLower.contains("arrow");

        var mobLevelData = LevelingCore.mobLevelRegistry.getOrCreate(
            npcAttacker.getUuid(),
            () -> MobLevelingUtil.computeSpawnLevel(npcAttacker)
        );
        var mobLevel = mobLevelData.level;
        var meleeMulti = config.get().getMobDamageMultiplier();
        var projectileMulti = config.get().getMobRangeDamageMultiplier();

        var con = levelService.getCon(victimPlayerRef.getUuid());
        var mult = conDamageMultiplier(con);

        if (isProjectile) {
            damage.setAmount(incoming * mult * projectileMulti * mobLevel);
        } else {
            damage.setAmount(incoming * mult * meleeMulti * mobLevel);
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

    private float conDamageMultiplier(int con) {
        var reduction = (float) Math.min(config.get().getConStatMultiplier(), Math.max(0.0, con));
        return 1.0f - reduction;
    }
}
