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

    public static int computeSpawnLevel(NPCEntity npc) {
        var seed = npc.getUuid().getMostSignificantBits() ^ npc.getUuid().getLeastSignificantBits();
        var rng = new Random(seed);
        final var spawnMin = 1;
        final var spawnMax = 10;

        return spawnMin + rng.nextInt((spawnMax - spawnMin) + 1);
    }

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

        int envID = chunk.getBlockChunk().getEnvironment(mobPos);
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
        if (lvlOpt == null || lvlOpt.isEmpty()) {
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

    public static int computeNPCOverrideLevel(NPCEntity npc) {
        var npcTypeID = npc.getNPCTypeId();
        var overrideMapping = LevelingCore.mobOverrideMapping;

        return overrideMapping.getOrDefault(npcTypeID.toLowerCase(), 0);
    }

    public static int randomizeLevel(int baseLevel, NPCEntity npc) {
        var variance = LevelingCore.getConfig().get().getLevelVariance();
        if (variance <= 0) {
            return baseLevel;
        }
        var seed = npc.getUuid().getMostSignificantBits() ^ npc.getUuid().getLeastSignificantBits();
        var rng = new Random(seed);

        var range = baseLevel + variance;
        return baseLevel + rng.nextInt(range - baseLevel);
    }
}
