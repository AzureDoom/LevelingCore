package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Random;
import java.util.logging.Level;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.mobs.CoreLevelMode;
import com.azuredoom.levelingcore.level.mobs.MobLevelRegistry;

@SuppressWarnings("removal")
public class MobLevelingUtil {

    private static final MobLevelRegistry registry = LevelingCore.mobLevelRegistry;

    public MobLevelingUtil() {}

    /**
     * Computes the dynamic level of an NPC based on various factors such as configuration, nearby players, biome, zone,
     * and other environmental aspects.
     *
     * @param config    The configuration instance that provides the current level mode settings.
     * @param npc       The NPC entity for which the level is being computed.
     * @param transform The transform component providing the NPC's positional data.
     * @param store     The store containing entity data, used for retrieving relevant contextual information.
     * @return The computed dynamic level of the NPC.
     */
    public static int computeDynamicLevel(
        Config<GUIConfig> config,
        NPCEntity npc,
        TransformComponent transform,
        Store<EntityStore> store
    ) {
        var modeStr = config.get().getLevelMode();
        var overrideLevel = computeNPCOverrideLevel(npc);

        if (overrideLevel != 0) {
            return overrideLevel;
        }

        if (modeStr == null) {
            return computeNearbyPlayersMeanLevel(transform, store, npc);
        }

        return CoreLevelMode.fromString(modeStr)
            .map(mode -> switch (mode) {
                case SPAWN_ONLY -> computeSpawnLevel(npc);
                case NEARBY_PLAYERS_MEAN -> computeNearbyPlayersMeanLevel(transform, store, npc);
                case BIOME -> computeBiomeLevel(store, npc);
                case ZONE -> computeZoneLevel(store, npc);
                case ENVIRONMENT -> computeEnvironmentLevel(transform, store, npc);
                case INSTANCE -> computeInstanceLevel(store, npc);
            })
            .orElseGet(() -> {
                LevelingCore.LOGGER.at(Level.INFO)
                    .log("Unknown level mode " + modeStr + " defaulting to NEARBY_PLAYERS_MEAN");
                return computeNearbyPlayersMeanLevel(transform, store, npc);
            });
    }

    /**
     * Applies level-based scaling to the health of a given NPC entity by modifying its internal stat map. The scaling
     * is determined by the provided configuration and level.
     *
     * @param config The configuration object containing GUI and gameplay settings. This is used to retrieve the mob
     *               health multiplier.
     * @param npc    The NPC entity to which level-based scaling will be applied. Must have a valid reference.
     * @param level  The level of the NPC, used to calculate the scaling factor for health.
     * @param store  The entity store providing access to external data and components needed for applying the scaling.
     * @return {@code true} if mob scaling was successfully applied; {@code false} if the NPC reference is invalid or no
     *         action could be performed.
     */
    public static boolean applyMobScaling(
        Config<GUIConfig> config,
        NPCEntity npc,
        int level,
        Store<EntityStore> store
    ) {
        if (npc.getReference() == null || !npc.getReference().isValid())
            return false;

        store.getExternalData().getWorld().execute(() -> {
            var healthMult = Math.max(1f, (float) level * config.get().getMobHealthMultiplier());
            var stats = store.getComponent(npc.getReference(), EntityStatMap.getComponentType());
            if (stats == null)
                return;
            var healthIndex = DefaultEntityStatTypes.getHealth();
            var modifier = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                healthMult
            );
            stats.putModifier(healthIndex, "LevelingCore_mob_health", modifier);
            stats.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getHealth());
            stats.update();
        });

        return true;
    }

    /**
     * Computes the spawn level for a given NPC entity. The level is determined using the UUID of the NPC as a seed for
     * randomization. If the NPC's UUID is null, the method returns a default level of 1. Otherwise, a random level
     * between 1 and 10 (inclusive) is generated.
     *
     * @param npc The NPC entity for which the spawn level is being computed. The UUID of the NPC is used to generate a
     *            consistent randomized level. If the UUID is null, a default value of 1 is returned.
     * @return The computed spawn level for the NPC. The result is a value between 1 and 10, inclusive. If the UUID is
     *         null, the method returns 1.
     */
    public static int computeSpawnLevel(NPCEntity npc) {
        var npcUUID = npc.getUuid();
        if (npcUUID == null) {
            return 1;
        }
        var seed = npcUUID.getMostSignificantBits() ^ npcUUID.getLeastSignificantBits();
        var rng = new Random(seed);
        final var spawnMin = 1;
        final var spawnMax = 10;

        return spawnMin + rng.nextInt((spawnMax - spawnMin) + 1);
    }

    /**
     * Computes the instance level for a given NPC entity based on the name of the instance retrieved from the world
     * data. The base level for the instance is determined using a predefined mapping. If the instance name is blank or
     * null, a default level of 0 is returned. The resulting level is then randomized using the NPC entity's
     * information.
     *
     * @param store The entity store providing access to the game's external data, including the world state and other
     *              related information.
     * @param npc   The NPC entity for which the instance level is being computed. The NPC's information is used during
     *              the randomization process.
     * @return The computed level for the NPC based on the instance data and adjusted by the randomization logic.
     *         Returns 0 if the instance name is blank or null.
     */
    public static int computeInstanceLevel(Store<EntityStore> store, NPCEntity npc) {
        var world = store.getExternalData().getWorld();
        var instanceName = world.getName();
        var instanceMapping = LevelingCore.mobInstanceMapping;

        if (instanceName.isBlank()) {
            LevelingCore.LOGGER.at(Level.WARNING).log("World instance name was null/blank; defaulting to 0");
            return 0;
        }

        var baseLevel = instanceMapping.getOrDefault(instanceName.toLowerCase(), 1);
        return randomizeLevel(baseLevel, npc);
    }

    /**
     * Computes the zone level for a given NPC entity by determining the current zone from the world data and mapping it
     * to a predefined set of zone-based level mappings. If the current zone is null, a default value of 0 is returned.
     * The computed zone-based level is then randomized based on the NPC's unique information to ensure variability.
     *
     * @param store The entity store providing access to the game's external data, including the world state and
     *              associated players.
     * @param npc   The NPC entity for which the zone level is being computed. The NPC's information is used in the
     *              randomization process to provide consistent variability.
     * @return The computed level for the NPC based on the current zone and the NPC-specific randomization. Returns 0 if
     *         the current zone information is unavailable or null.
     */
    public static int computeZoneLevel(Store<EntityStore> store, NPCEntity npc) {
        var world = store.getExternalData().getWorld();
        var worldMapTracker = world.getPlayers().getFirst().getWorldMapTracker();
        var currentZone = worldMapTracker.getCurrentZone();
        if (currentZone == null)
            return 0;
        var zoneMapping = LevelingCore.mobZoneMapping;

        var baseLevel = zoneMapping.getOrDefault(currentZone.zoneName().toLowerCase(), 1);
        return randomizeLevel(baseLevel, npc);
    }

    /**
     * Computes the biome-based level for a given NPC entity by mapping the current biome from the world data to a
     * predefined set of biome mappings. If no valid biome is found, a default value of 6 is returned. The resulting
     * level is further randomized based on the NPC entity's information.
     *
     * @param store The entity store providing access to the game's external data, including the world state and
     *              associated players.
     * @param npc   The NPC entity for which the biome level is being computed. The entity's information is used in the
     *              randomization process to ensure consistency.
     * @return The computed level based on the current biome and NPC randomization. Returns a value of 6 if the biome
     *         information is unavailable or null.
     */
    public static int computeBiomeLevel(Store<EntityStore> store, NPCEntity npc) {
        var world = store.getExternalData().getWorld();
        var worldMapTracker = world.getPlayers().getFirst().getWorldMapTracker();
        var currentBiome = worldMapTracker.getCurrentBiomeName();

        if (currentBiome == null)
            return 6;

        var biomeMapping = LevelingCore.mobBiomeMapping;
        var baseLevel = biomeMapping.getOrDefault(currentBiome.toLowerCase(), 1);
        return randomizeLevel(baseLevel, npc);
    }

    /**
     * Computes the environment level for an NPC entity based on its position and the environment data retrieved from
     * the game's world and asset system. The method evaluates the entity's current position to determine its associated
     * environment and maps it to a base level using predefined mappings. If any data necessary for the computation is
     * missing or invalid, a default level of 1 is returned.
     *
     * @param transform The transform component of the NPC entity, used to determine its position in the game world.
     * @param store     The entity store providing access to external data, including the world and its chunks.
     * @param npc       The NPC entity for which the environment level is being computed. The entity's information may
     *                  be utilized in the randomization process.
     * @return The computed environment-based level for the NPC. Returns the default level of 1 if any required data is
     *         unavailable or mismatched.
     */
    public static int computeEnvironmentLevel(TransformComponent transform, Store<EntityStore> store, NPCEntity npc) {
        var world = store.getExternalData().getWorld();
        var mobPos = transform.getPosition();
        var chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock((int) mobPos.x, (int) mobPos.z));

        if (chunk == null) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .log(
                    "Chunk not in memory; defaulting to 1"
                );
            return 1;
        }

        var blockChunk = chunk.getBlockChunk();
        if (blockChunk == null) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .log(
                    "Block chunk not found; defaulting to 1"
                );
            return 1;
        }
        var envID = blockChunk.getEnvironment(mobPos);
        var envAsset = Environment.getAssetMap().getAsset(envID);
        if (envAsset == null) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .log(
                    "Environment id " + envID + " does not exist in asset registry; defaulting to 1"
                );
            return 1;
        }
        var envName = envAsset.getId();

        if (envName == null) {
            LevelingCore.LOGGER.at(Level.WARNING)
                .log(
                    "Environment " + envName + " does not exist in asset registry; defaulting to 1"
                );
            return 1;
        }

        var environmentMapping = LevelingCore.mobEnvironmentMapping;
        var baseLevel = environmentMapping.getOrDefault(envName.toLowerCase(), 1);
        return randomizeLevel(baseLevel, npc);
    }

    /**
     * Calculates the mean level of players within a specific radius around a given NPC entity. The levels are
     * determined using the leveling service, and only players within the radius contribute to the mean calculation. If
     * no players are nearby, a default base level is returned. The computed mean level is then randomized to ensure
     * variability before returning.
     *
     * @param transform The transform component of the NPC entity, used to determine its position in the game world.
     * @param store     The entity store containing access to external data, including the world and its players.
     * @param npc       The NPC entity for which the mean level of nearby players is being computed.
     * @return The mean level of nearby players, randomized for variability. If no players are nearby, a default level
     *         of 5 is returned.
     */
    public static int computeNearbyPlayersMeanLevel(
        TransformComponent transform,
        Store<EntityStore> store,
        NPCEntity npc
    ) {
        var world = store.getExternalData().getWorld();
        var mobPos = transform.getPosition();
        var players = world.getPlayers();
        var sum = 0;
        var count = 0;
        final var nearbyRadius = 40f;
        final float nearbyRadiusSq = nearbyRadius * nearbyRadius;
        var lvlOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (lvlOpt.isEmpty()) {
            return 5;
        }
        var lvlService = lvlOpt.get();

        for (var p : players) {
            var pPos = p.getPlayerRef().getTransform().getPosition();
            if (pPos.distanceSquaredTo(mobPos) <= nearbyRadiusSq) {
                var lvl = lvlService.getLevel(p.getPlayerRef().getUuid());
                sum += lvl;
                count++;
            }
        }

        if (count == 0)
            return 5;

        var mean = (double) sum / (double) count;
        var baseLevel = (int) Math.round(mean);
        return randomizeLevel(baseLevel, npc);
    }

    /**
     * Computes the override level for a given NPC entity using the predefined mapping. The override level is determined
     * based on the NPC's type identifier and a mapping that associates type identifiers with specific override levels.
     *
     * @param npc The NPC entity for which the override level is being computed. The entity's type identifier is used to
     *            look up the associated override level.
     * @return The override level for the NPC entity. If no override mapping is found for the entity's type identifier,
     *         a default value of 0 is returned.
     */
    public static int computeNPCOverrideLevel(NPCEntity npc) {
        var npcTypeID = npc.getNPCTypeId();
        var overrideMapping = LevelingCore.mobOverrideMapping;

        return overrideMapping.getOrDefault(npcTypeID.toLowerCase(), 0);
    }

    /**
     * Randomizes the level of an NPC entity based on a base level and a predefined variance. The variance is determined
     * from the configuration settings and is used to calculate a randomized level within a specific range. This method
     * ensures the randomized level is always at least 1, even if the base level or variance is low.
     *
     * @param baseLevel The base level from which the randomization starts. This represents the NPC's default or
     *                  starting level.
     * @param npc       The NPC entity for which the level is being randomized. The UUID of the NPC is used as a seed to
     *                  generate consistent randomization for the same entity.
     * @return The randomized level for the NPC, adjusted based on the variance. The result is ensured to be no less
     *         than 1.
     */
    public static int randomizeLevel(int baseLevel, NPCEntity npc) {
        var variance = LevelingCore.getConfig().get().getLevelVariance();
        if (variance <= 0) {
            return baseLevel;
        }

        var npcUUID = npc.getUuid();
        if (npcUUID == null) {
            return baseLevel;
        }

        var seed = npcUUID.getMostSignificantBits() ^ npcUUID.getLeastSignificantBits();
        var rng = new Random(seed);

        return Math.max(1, baseLevel - variance + rng.nextInt(variance * 2 + 1));
    }
}
