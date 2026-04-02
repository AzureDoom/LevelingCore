package com.azuredoom.levelingcore.level.mobs;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.List;
import java.util.logging.Level;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.compat.elitemobs.EliteMobsCompat;
import com.azuredoom.levelingcore.compat.rpgmobs.RPGMobsCompat;
import com.azuredoom.levelingcore.level.mobs.mapping.MobBossMapping;
import com.azuredoom.levelingcore.level.mobs.mapping.MobBossMapping.BossRule;

/**
 * Resolves mob encounter profiles and tiers for specified NPC entities based on predefined rules and game data. This
 * class is responsible for determining appropriate mob classifications, such as Normal, Elite, or Boss tiers, and
 * mapping them to corresponding {@link MobEncounterProfile} instances.
 */
public final class MobEncounterProfileResolver {

    private final List<BossRule> bossRules;

    public MobEncounterProfileResolver(List<BossRule> bossRules) {
        this.bossRules = bossRules;
    }

    /**
     * Resolves the appropriate {@link MobEncounterProfile} for the given {@code NPCEntity} within the context of the
     * specified game data {@code Store}. The method determines the tier of the NPC entity using
     * {@link #resolveTier(Store, NPCEntity)} and returns the corresponding mob encounter profile by invoking
     * {@link #resolveProfile(MobTier)}.
     *
     * @param store  the {@code Store} containing game data and external information used to evaluate the NPC entity
     * @param entity the {@code NPCEntity} for which the mob encounter profile should be resolved
     * @return the resolved {@link MobEncounterProfile} associated with the tier of the given {@code NPCEntity}
     */
    public MobEncounterProfile resolve(Store<EntityStore> store, NPCEntity entity) {
        return resolveProfile(resolveTier(store, entity));
    }

    /**
     * Resolves the appropriate {@link MobEncounterProfile} for a given {@link MobLevelData}. The method determines the
     * profile to use based on the tier specified in the {@code levelData}.
     *
     * @param levelData the {@link MobLevelData} containing information about the mob, including the tier used to
     *                  determine the appropriate encounter profile
     * @return the {@link MobEncounterProfile} corresponding to the tier specified in the {@code levelData}
     */
    public MobEncounterProfile resolve(MobLevelData levelData) {
        return resolveProfile(levelData.tier);
    }

    /**
     * Resolves the appropriate {@link MobTier} for a given NPC entity within the provided game data store. The method
     * determines the tier classification of the NPC entity based on specific criteria such as boss status. If the
     * entity is identified as a boss, it returns {@link MobTier#BOSS}. Otherwise, the default tier
     * {@link MobTier#NORMAL} is returned.
     *
     * @param store  the {@code Store} containing game data and external information used to evaluate the entity
     * @param entity the {@code NPCEntity} for which the mob tier needs to be resolved; if this parameter is
     *               {@code null}, the method defaults to {@link MobTier#NORMAL}
     * @return the resolved {@link MobTier} for the given NPC entity, either {@link MobTier#BOSS} or
     *         {@link MobTier#NORMAL}
     */
    public MobTier resolveTier(Store<EntityStore> store, NPCEntity entity) {
        if (entity == null) {
            return MobTier.NORMAL;
        }

        if (isBoss(store, entity)) {
            return MobTier.BOSS;
        }

        if (isElite(store, entity)) {
            return MobTier.ELITE;
        }

        return MobTier.NORMAL;
    }

    /**
     * Resolves the appropriate {@link MobEncounterProfile} based on the specified {@link MobTier}. The method maps each
     * tier to a predefined {@link MobEncounterProfile} instance, which encapsulates the characteristics associated with
     * the corresponding mob tier.
     *
     * @param tier the {@link MobTier} representing the tier classification of the mob
     * @return the {@link MobEncounterProfile} associated with the given {@link MobTier}
     */
    public MobEncounterProfile resolveProfile(MobTier tier) {
        return switch (tier) {
            case BOSS -> MobEncounterProfile.BOSS;
            case ELITE -> MobEncounterProfile.ELITE;
            case NORMAL -> MobEncounterProfile.NORMAL;
        };
    }

    /**
     * Determines whether a given NPC entity qualifies as a boss in the context of the provided game store.
     * <p>
     * The method evaluates the entity's type and the world instance it belongs to, in conjunction with boss rules
     * defined in the resolver. If the entity satisfies the criteria for a boss, the method returns {@code true}. If an
     * error occurs during the evaluation, the method defaults to returning {@code false}.
     *
     * @param store  the {@code Store} containing game data and external information used for resolving the world
     *               instance
     * @param entity the {@code NPCEntity} whose status as a boss is being determined
     * @return {@code true} if the entity is identified as a boss; {@code false} otherwise
     */
    public boolean isBoss(Store<EntityStore> store, NPCEntity entity) {
        try {
            var world = store.getExternalData().getWorld();
            var instanceName = world.getName();
            if (instanceName.isBlank()) {
                return false;
            }

            int mappedLevel = MobBossMapping.findLevel(bossRules, instanceName, entity.getNPCTypeId());
            return mappedLevel >= 0;
        } catch (Exception ex) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .withCause(ex)
                .log("Failed to resolve boss mapping for npc, defaulting to NORMAL tier.");
            return false;
        }
    }

    /**
     * Determines whether the given {@code NPCEntity} qualifies as an elite mob. The method evaluates compatibility with
     * external plugins like "EliteMobs" and "RPGMobs," falling back to the game's configuration for elite mob
     * classification.
     *
     * @param store  the {@code Store} containing game data and external information used to evaluate the NPC entity
     * @param entity the {@code NPCEntity} whose elite status is being determined
     * @return {@code true} if the entity is identified as an elite mob; {@code false} otherwise
     */
    public boolean isElite(Store<EntityStore> store, NPCEntity entity) {
        if (PluginManager.get().getPlugin(new PluginIdentifier("Cedeli", "EliteMobs")) != null) {
            return EliteMobsCompat.isEliteMob(store, entity);
        }

        if (PluginManager.get().getPlugin(new PluginIdentifier("Frotty27", "RPGMobs")) != null) {
            return RPGMobsCompat.isEliteMob(store, entity);
        }

        var config = LevelingCore.getConfig().get();

        return config.isEliteMob(entity.getNPCTypeId());
    }
}
