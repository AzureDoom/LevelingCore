package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.mobs.CoreLevelMode;
import com.azuredoom.levelingcore.level.mobs.mapping.MobBossMapping;
import com.azuredoom.levelingcore.level.mobs.mapping.MobInstanceMapping;

/**
 * Utility class for computing levels for NPC entities in the game. This class provides methods to calculate dynamic
 * levels, base levels, spawn levels, and override levels for NPCs, bosses, zones, biomes, and environments based on
 * various game configurations, state data, and entity attributes.
 */
public final class MobLevelingUtil {

    public MobLevelingUtil() {}

    /**
     * Computes the dynamic level for an NPC entity based on various configuration modes, nearby entities, and potential
     * level overrides.
     *
     * @param config        the configuration instance for GUI settings, containing level mode preferences
     * @param npc           the NPC entity for which the dynamic level is being calculated
     * @param transform     the transform component representing the NPC's position and state
     * @param store         the entity store containing entities that may influence the NPC's level
     * @param commandBuffer the command buffer used for operations involving entity state changes or queries
     * @return the computed dynamic level for the given NPC entity
     */
    public static int computeDynamicLevel(
        Config<GUIConfig> config,
        NPCEntity npc,
        TransformComponent transform,
        Store<EntityStore> store,
        @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        var overrideLevel = computeNPCOverrideLevel(npc);
        if (overrideLevel != 0) {
            return overrideLevel;
        }

        var bossOverrideLevel = computeBossOverrideLevel(store, npc);
        if (bossOverrideLevel > 0) {
            return bossOverrideLevel;
        }

        var instanceBaseLevel = computeInstanceBaseLevel(store);

        if (instanceBaseLevel > 0) {
            return randomizeLevel(commandBuffer, instanceBaseLevel, npc);
        }

        var modeStrings = config.get().getLevelMode();

        if (modeStrings == null || modeStrings.length == 0) {
            var fallbackBase = computeNearbyPlayersMeanBaseLevel(transform, store);
            return randomizeLevel(commandBuffer, fallbackBase, npc);
        }

        List<WeightedLevel> weightedLevels = new ArrayList<>();

        for (var modeStr : modeStrings) {
            if (modeStr == null || modeStr.isBlank()) {
                continue;
            }

            modeStr = modeStr.trim();

            var modeOpt = CoreLevelMode.fromString(modeStr);
            if (modeOpt.isEmpty()) {
                LevelingCore.LOGGER.at(Level.INFO)
                    .log("Unknown level mode " + modeStr + ", skipping");
                continue;
            }

            var mode = modeOpt.get();

            var baseLevel = computeBaseLevelForMode(
                mode,
                npc,
                transform,
                store,
                commandBuffer
            );

            if (baseLevel > 0) {
                weightedLevels.add(new WeightedLevel(baseLevel, getModeWeight(mode)));
            }
        }

        if (weightedLevels.isEmpty()) {
            var fallbackBase = computeNearbyPlayersMeanBaseLevel(transform, store);
            return randomizeLevel(commandBuffer, fallbackBase, npc);
        }

        var combinedBaseLevel = combineWeightedLevels(weightedLevels);

        return randomizeLevel(commandBuffer, combinedBaseLevel, npc);
    }

    /**
     * Computes the base level for an NPC based on the specified CoreLevelMode.
     *
     * @param mode          The mode that determines the computation method for the NPC's base level.
     * @param npc           The NPC entity for which the base level is being computed.
     * @param transform     The transform component of the NPC, providing positional and orientation data.
     * @param store         The entity store used in certain level computations.
     * @param commandBuffer A buffer for issuing commands that may be needed during computation, such as
     *                      spawning-related actions.
     * @return The computed base level for the NPC based on the given mode.
     */
    private static int computeBaseLevelForMode(
        CoreLevelMode mode,
        NPCEntity npc,
        TransformComponent transform,
        Store<EntityStore> store,
        @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        return switch (mode) {
            case SPAWN_ONLY -> computeSpawnBaseLevel(commandBuffer, npc);
            case NEARBY_PLAYERS_MEAN -> computeNearbyPlayersMeanBaseLevel(transform, store);
            case BIOME -> computeBiomeBaseLevel(store);
            case ZONE -> computeZoneBaseLevel(store);
            case ENVIRONMENT -> computeEnvironmentBaseLevel(transform, store);
            case INSTANCE -> computeInstanceBaseLevel(store);
        };
    }

    /**
     * Computes the spawn base level for a given NPCEntity based on its unique identifier. If the NPCEntity has no
     * reference or UUID component, the method defaults the base level to 1.
     *
     * @param commandBuffer the command buffer used to retrieve components for the NPCEntity
     * @param npc           the NPCEntity for which the spawn base level is to be computed
     * @return the computed spawn base level, which is a random integer within the range [1, 10] based on the UUID of
     *         the NPCEntity, or 1 if the reference or UUID component is not available
     */
    public static int computeSpawnBaseLevel(@Nonnull CommandBuffer<EntityStore> commandBuffer, NPCEntity npc) {
        var npcRef = npc.getReference();
        if (npcRef == null) {
            return 1;
        }

        var uuidComponent = commandBuffer.getComponent(npcRef, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return 1;
        }

        var npcUUID = uuidComponent.getUuid();
        var seed = npcUUID.getMostSignificantBits() ^ npcUUID.getLeastSignificantBits();
        var rng = new Random(seed);

        final var spawnMin = 1;
        final var spawnMax = 10;

        return spawnMin + rng.nextInt((spawnMax - spawnMin) + 1);
    }

    /**
     * Computes the base level of an instance by using the provided store.
     *
     * @param store the store containing the external data and world instance details
     * @return the computed base level of the instance; returns -1 if the instance name is blank
     */
    public static int computeInstanceBaseLevel(Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var instanceName = world.getName();
        if (instanceName.isBlank()) {
            return -1;
        }

        return MobInstanceMapping.findLevel(LevelingCore.mobInstanceMapping, instanceName);
    }

    /**
     * Computes the base level for the current zone based on player data and a predefined zone mapping. This method
     * retrieves the current zone from the first available valid player and maps the zone name to a corresponding base
     * level. If no valid players or zones are found, a default level is returned.
     *
     * @param store The entity store that provides access to the world data, including player references, entity
     *              components, and zone information.
     * @return The computed base level for the current zone. Returns 0 if no valid players or zones are found, or a
     *         default value based on the zone mapping.
     */
    public static int computeZoneBaseLevel(@Nonnull Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var playerRefs = world.getPlayerRefs();
        if (playerRefs.isEmpty()) {
            return 0;
        }

        var firstPlayerRef = playerRefs.iterator().next();
        var playerEntityRef = firstPlayerRef.getReference();
        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            return 0;
        }

        var player = store.getComponent(playerEntityRef, Player.getComponentType());
        if (player == null) {
            return 0;
        }

        var worldMapTracker = player.getWorldMapTracker();
        var currentZone = worldMapTracker.getCurrentZone();
        if (currentZone == null) {
            return 0;
        }

        var zoneMapping = LevelingCore.mobZoneMapping;
        return zoneMapping.getOrDefault(currentZone.zoneName().toLowerCase(), 1);
    }

    /**
     * Computes the base level for a biome based on the entity data and the current biome's name. If no players are
     * found or the biome name is unavailable, a default level is returned.
     *
     * @param store The entity store that provides access to the game's world data and entity components. It is used to
     *              fetch player references, biome information, and mapping data.
     * @return The computed biome base level. Returns 0 if no valid players are found or the entity reference is
     *         invalid, 6 if the current biome name is null, or a default value based on the biome mapping.
     */
    public static int computeBiomeBaseLevel(Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var playerRefs = world.getPlayerRefs();
        if (playerRefs.isEmpty()) {
            return 0;
        }

        var firstPlayerRef = playerRefs.iterator().next();
        var playerEntityRef = firstPlayerRef.getReference();
        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            return 0;
        }

        var player = store.getComponent(playerEntityRef, Player.getComponentType());
        if (player == null) {
            return 0;
        }

        var worldMapTracker = player.getWorldMapTracker();
        var currentBiome = worldMapTracker.getCurrentBiomeName();
        if (currentBiome == null) {
            return 6;
        }

        var biomeMapping = LevelingCore.mobBiomeMapping;
        return biomeMapping.getOrDefault(currentBiome.toLowerCase(), 1);
    }

    /**
     * Computes the base level of the environment surrounding an entity based on the entity's position and the
     * environment data in memory. If the environment is not found or invalid, a default value of 1 is returned.
     *
     * @param transform The transform component of the entity, providing access to its position.
     * @param store     The entity store containing data about the game's world and loaded chunks.
     * @return The computed base level of the environment as an integer. Defaults to 1 if the environment or related
     *         data is unavailable or invalid.
     */
    public static int computeEnvironmentBaseLevel(
        TransformComponent transform,
        Store<EntityStore> store
    ) {
        var world = store.getExternalData().getWorld();
        var mobPos = transform.getPosition();
        var chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock((int) mobPos.x, (int) mobPos.z));

        if (chunk == null) {
            LevelingCore.LOGGER.at(Level.WARNING).log("Chunk not in memory; defaulting to 1");
            return 1;
        }

        var blockChunk = chunk.getBlockChunk();
        if (blockChunk == null) {
            LevelingCore.LOGGER.at(Level.WARNING).log("Block chunk not found; defaulting to 1");
            return 1;
        }

        var envID = blockChunk.getEnvironment(mobPos);
        var envAsset = Environment.getAssetMap().getAsset(envID);
        if (envAsset == null) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .log("Environment id " + envID + " does not exist in asset registry; defaulting to 1");
            return 1;
        }

        var envName = envAsset.getId();
        if (envName == null) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .log("Environment does not exist in asset registry; defaulting to 1");
            return 1;
        }

        var environmentMapping = LevelingCore.mobEnvironmentMapping;
        return environmentMapping.getOrDefault(envName.toLowerCase(), 1);
    }

    /**
     * Calculates the mean base level of players located within a specific radius of the given entity's position. If no
     * players are found, a default value of 5 is returned.
     *
     * @param transform The transform component of the entity whose nearby players are being considered. It provides
     *                  access to the entity's position.
     * @param store     The entity store that provides access to the game's world data, including player references.
     * @return The mean base level of nearby players as an integer. Returns 5 if no players are found or if the leveling
     *         service is unavailable.
     */
    public static int computeNearbyPlayersMeanBaseLevel(
        TransformComponent transform,
        Store<EntityStore> store
    ) {
        var world = store.getExternalData().getWorld();
        var mobPos = transform.getPosition();
        var playerRefs = world.getPlayerRefs();
        var sum = 0;
        var count = 0;

        final var nearbyRadius = 40f;
        final var nearbyRadiusSq = nearbyRadius * nearbyRadius;

        var lvlOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (lvlOpt.isEmpty()) {
            return 5;
        }

        var lvlService = lvlOpt.get();

        for (var playerRefComponent : playerRefs) {
            var playerRef = playerRefComponent.getReference();
            if (playerRef == null || !playerRef.isValid()) {
                continue;
            }

            var pPos = playerRefComponent.getTransform().getPosition();
            if (pPos.distanceSquaredTo(mobPos) <= nearbyRadiusSq) {
                var lvl = lvlService.getLevel(playerRefComponent.getUuid());
                sum += lvl;
                count++;
            }
        }

        if (count == 0) {
            return 5;
        }

        return (int) Math.round((double) sum / (double) count);
    }

    /**
     * Computes the spawn level for an NPC by first determining its base spawn level and then applying randomization to
     * introduce level variance.
     *
     * @param commandBuffer The command buffer used to query and modify entity components. Must not be null.
     * @param npc           The NPC entity for which the spawn level is being computed. Must have a valid reference.
     * @return The computed spawn level for the NPC. The level is randomized and no less than 1.
     */
    public static int computeSpawnLevel(@Nonnull CommandBuffer<EntityStore> commandBuffer, NPCEntity npc) {
        return randomizeLevel(commandBuffer, computeSpawnBaseLevel(commandBuffer, npc), npc);
    }

    /**
     * Computes the override level for an NPC based on its type identifier and a predefined override mapping. If the NPC
     * type identifier does not exist in the mapping, a default level of 0 is returned.
     *
     * @param npc The NPC entity for which the override level is being computed. It must provide a valid type
     *            identifier.
     * @return An integer representing the override level of the NPC. Returns 0 if the NPC type identifier is not found
     *         in the override mapping.
     */
    public static int computeNPCOverrideLevel(NPCEntity npc) {
        var npcTypeID = npc.getNPCTypeId();
        var overrideMapping = LevelingCore.mobOverrideMapping;

        return overrideMapping.getOrDefault(npcTypeID.toLowerCase(), 0);
    }

    /**
     * Computes the override level for a boss-type NPC based on the current world instance and the specific boss type.
     * This method uses the instance name of the world and the NPC's type identifier to determine the boss's level from
     * a predefined mapping. If the instance name or boss name is invalid, a default value of -1 is returned.
     *
     * @param store The entity store containing external data for retrieving the world instance. Must provide access to
     *              the current world context.
     * @param npc   The NPC entity representing the boss. Must have a valid type identifier to compute the override
     *              level.
     * @return An integer representing the boss override level. Returns -1 if the instance name or boss identifier is
     *         invalid or unavailable.
     */
    public static int computeBossOverrideLevel(Store<EntityStore> store, NPCEntity npc) {
        var world = store.getExternalData().getWorld();
        var instanceName = world.getName();
        if (instanceName.isBlank()) {
            return -1;
        }

        var bossName = npc.getNPCTypeId();
        if (bossName == null || bossName.isBlank()) {
            return -1;
        }

        return MobBossMapping.findLevel(
            LevelingCore.mobBossMapping,
            instanceName,
            bossName
        );
    }

    /**
     * Randomizes the level of an NPC based on its unique identifier and a specified level variance. If the NPC does not
     * have a valid reference or required components are missing, the base level is returned.
     *
     * @param commandBuffer The command buffer used to query and modify entity components. Must not be null.
     * @param baseLevel     The initial base level of the NPC that serves as the basis for randomization.
     * @param npc           The NPC entity whose level is to be randomized. Must have a valid reference to function
     *                      correctly.
     * @return The randomized level value, which is no less than 1 and adjusted by the configured level variance.
     *         Returns the base level if the NPC does not have a valid reference or required components are missing.
     */
    public static int randomizeLevel(@Nonnull CommandBuffer<EntityStore> commandBuffer, int baseLevel, NPCEntity npc) {
        var npcRef = npc.getReference();
        if (npcRef == null) {
            return baseLevel;
        }

        var uuidComponent = commandBuffer.getComponent(npcRef, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return baseLevel;
        }

        var entityUuid = uuidComponent.getUuid();

        var variance = LevelingCore.getConfig().get().getLevelVariance();
        if (variance <= 0) {
            return baseLevel;
        }

        var seed = entityUuid.getMostSignificantBits() ^ entityUuid.getLeastSignificantBits();
        var rng = new Random(seed);

        return Math.max(1, baseLevel - variance + rng.nextInt(variance * 2 + 1));
    }

    /**
     * Applies scaling modifiers to a given NPC's statistics based on its level and configuration settings.
     *
     * @param config Configuration object that provides access to GUI-specific settings, including scaling multipliers.
     * @param npc    The NPC entity to which scaling will be applied. This must have a valid reference.
     * @param level  The level of the NPC, used to determine the size of scaling.
     * @param store  The entity store that provides world context and access to components required for applying
     *               scaling.
     * @return Returns {@code true} if scaling was successfully applied, or {@code false} if the NPC's reference is
     *         invalid or modifications could not be performed.
     */
    public static boolean applyMobScaling(
        Config<GUIConfig> config,
        NPCEntity npc,
        int level,
        float healthTierMulti,
        Store<EntityStore> store
    ) {
        if (npc.getReference() == null || !npc.getReference().isValid()) {
            return false;
        }

        store.getExternalData().getWorld().execute(() -> {
            var healthMulti = Math.max(1f, (float) level * config.get().getMobHealthMultiplier() * healthTierMulti);
            var stats = store.getComponent(npc.getReference(), EntityStatMap.getComponentType());
            if (stats == null) {
                return;
            }

            var healthIndex = DefaultEntityStatTypes.getHealth();
            var modifier = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                healthMulti
            );
            stats.putModifier(healthIndex, "LevelingCore_mob_health", modifier);
            stats.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getHealth());
            stats.update();
        });

        return true;
    }

    /**
     * Determines the weight corresponding to a given CoreLevelMode.
     *
     * @param mode The CoreLevelMode for which the weight is to be determined.
     * @return The weight value associated with the provided CoreLevelMode.
     */
    private static double getModeWeight(CoreLevelMode mode) {
        return switch (mode) {
            case INSTANCE -> 0.70;
            case NEARBY_PLAYERS_MEAN -> 0.10;
            case ZONE -> 0.60;
            case BIOME -> 0.40;
            case ENVIRONMENT -> 0.50;
            case SPAWN_ONLY -> 0.20;
        };
    }

    /**
     * Combines a list of weighted levels and calculates the weighted average level.
     *
     * @param levels the list of WeightedLevel objects, where each object contains a level and its associated weight. A
     *               null or empty list will result in a return value of 0. Entries with non-positive weights are
     *               ignored.
     * @return the weighted average level as an integer. If no valid entries are available, or the total weight is
     *         non-positive, the method returns 0.
     */
    private static int combineWeightedLevels(List<WeightedLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return 0;
        }

        var weightedSum = 0.0;
        var totalWeight = 0.0;

        for (var entry : levels) {
            if (entry.weight() <= 0) {
                continue;
            }

            weightedSum += entry.level() * entry.weight();
            totalWeight += entry.weight();
        }

        if (totalWeight <= 0.0) {
            return 0;
        }

        return (int) Math.round(weightedSum / totalWeight);
    }

    /**
     * Immutable data class representing a level with an associated weight.
     */
    private record WeightedLevel(
        int level,
        double weight
    ) {}
}
