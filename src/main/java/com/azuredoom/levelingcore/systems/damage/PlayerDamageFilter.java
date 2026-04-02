package com.azuredoom.levelingcore.systems.damage;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
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
        final var victim = archetypeChunk.getComponent(index, Player.getComponentType());
        if (victim == null) {
            return;
        }
        var victimPlayerRef = victim.getReference();
        if (victimPlayerRef == null) {
            return;
        }
        var playerRefComponent = victimPlayerRef.getStore()
            .getComponent(victimPlayerRef, PlayerRef.getComponentType());
        if (playerRefComponent == null) {
            return;
        }
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
        var mobDamageMultiplier = mobLevelData.damageMultiplier;
        var baseDamage = isProjectile ? config.get().getMobBaseRangeDamage() : config.get().getMobBaseDamage();
        var multiplier = isProjectile
            ? config.get().getMobRangeDamageMultiplier()
            : config.get().getMobDamageMultiplier();

        var con = levelService.getCon(playerRefComponent.getUuid());
        var mitigationFactor = conDamageMultiplier(con);

        damage.setAmount(incoming * mitigationFactor * (baseDamage + multiplier * mobLevel) * mobDamageMultiplier);
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

    /**
     * Calculates the damage multiplier based on the Constitution (Con) stat. The formula applies a scale factor to the
     * Constitution stat and reduces the effective damage by a calculated percentage, which is indirectly proportional
     * to the Constitution stat value.
     *
     * @param con the Constitution stat value of the player or entity
     * @return the damage multiplier derived from the Constitution stat
     */
    private float conDamageMultiplier(int con) {
        var scale = config.get().getConStatMultiplier();
        var reduction = (con * scale) / (con + 100.0f);
        return 1.0f - reduction;
    }
}
