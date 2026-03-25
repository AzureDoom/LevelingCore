package com.azuredoom.levelingcore.systems.damage;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
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

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;

public class PlayerDamageFilter extends DamageEventSystem {

    private final Config<GUIConfig> config;

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
        if (victimPlayerRef == null)
            return;
        if (!(damage.getSource() instanceof Damage.EntitySource entitySource))
            return;
        var attackerRef = entitySource.getRef();
        if (!attackerRef.isValid())
            return;

        var npcAttacker = store.getComponent(attackerRef, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npcAttacker == null)
            return;
        var npcAttackerRef = npcAttacker.getReference();
        if (npcAttackerRef == null)
            return;

        var levelServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelServiceOpt.isEmpty())
            return;

        var levelService = levelServiceOpt.get();

        var incoming = damage.getAmount();
        if (incoming <= 0f)
            return;

        var cause = DamageCause.getAssetMap().getAsset(damage.getDamageCauseIndex());
        if (cause == null)
            return;

        var causeId = cause.getId();
        var causeIdLower = causeId == null ? "" : causeId.toLowerCase();
        var isProjectile = causeIdLower.contains("projectile") || causeIdLower.contains("arrow");

        var uuidComponent = commandBuffer.getComponent(npcAttackerRef, UUIDComponent.getComponentType());
        if (uuidComponent == null)
            return;
        var entityUuid = uuidComponent.getUuid();
        var mobLevelData = LevelingCore.mobLevelRegistry.getOrCreate(
            entityUuid,
            () -> MobLevelingUtil.computeSpawnLevel(commandBuffer, npcAttacker)
        );
        var mobLevel = mobLevelData.level;
        var baseMelee = config.get().getMobBaseDamage();
        var meleeMulti = config.get().getMobDamageMultiplier();
        var baseProjectile = config.get().getMobBaseRangeDamage();
        var projectileMulti = config.get().getMobRangeDamageMultiplier();

        var con = levelService.getCon(victimPlayerRef.getUuid());
        var mult = conDamageMultiplier(con);

        if (isProjectile) {
            damage.setAmount(incoming * mult * (baseMelee + projectileMulti * mobLevel));
        } else {
            damage.setAmount(incoming * mult * (baseProjectile + meleeMulti * mobLevel));
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
        return EntityModule.get().getPlayerComponentType();
    }

    private float conDamageMultiplier(int con) {
        var reduction = (float) Math.min(config.get().getConStatMultiplier(), Math.max(0.0, con));
        return 1.0f - reduction;
    }
}
