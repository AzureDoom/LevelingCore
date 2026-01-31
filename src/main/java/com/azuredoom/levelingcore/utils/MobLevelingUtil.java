package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.component.Store;
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

        if (modeStr == null) {
            return computeNearbyPlayersMeanLevel(transform, store);
        }

        return CoreLevelMode.fromString(modeStr)
            .map(mode -> switch (mode) {
                case SPAWN_ONLY ->
                    registry.get(npc.getUuid()).level;
                case NEARBY_PLAYERS_MEAN ->
                    computeNearbyPlayersMeanLevel(transform, store);
                case BIOME ->
                    computeBiomeLevel(store);
                case ZONE ->
                    computeZoneLevel(store);
                case INSTANCE ->
                    computeInstanceLevel(store);
            })
            .orElseGet(() -> {
                LevelingCore.LOGGER.at(Level.INFO)
                    .log("Unknown level mode " + modeStr + " defaulting to NEARBY_PLAYERS_MEAN");
                return computeNearbyPlayersMeanLevel(transform, store);
            });
    }

    public static void applyMobScaling(Config<GUIConfig> config, NPCEntity npc, int level, Store<EntityStore> store) {
        store.getExternalData().getWorld().execute(() -> {
            var healthMult = 1F + ((float) level - 1F) * config.get().getMobHealthMultiplier();

            if (npc.getReference() == null)
                return;
            var stats = store.getComponent(npc.getReference(), EntityStatMap.getComponentType());
            var healthIndex = DefaultEntityStatTypes.getHealth();
            var modifier = new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE,
                healthMult
            );
            var modifierKey = "LevelingCore_mob_health";
            stats.putModifier(healthIndex, modifierKey, modifier);
            stats.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getHealth());
            stats.update();
        });
    }

    public static int computeSpawnLevel(NPCEntity npc) {
        var seed = npc.getUuid().getMostSignificantBits() ^ npc.getUuid().getLeastSignificantBits();
        var rng = new Random(seed);
        final var spawnMin = 1;
        final var spawnMax = 10;

        return spawnMin + rng.nextInt((spawnMax - spawnMin) + 1);
    }

    public static int computeInstanceLevel(Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var instanceName = world.getName();
        var instanceMapping = LevelingCore.mobInstanceMapping;

        if (instanceName.isBlank()) {
            LevelingCore.LOGGER.at(Level.WARNING).log("World instance name was null/blank; defaulting to 0");
            return 0;
        }

        return instanceMapping.getOrDefault(instanceName.toLowerCase(), 1);
    }

    public static int computeZoneLevel(Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var worldMapTracker = world.getPlayers().getFirst().getWorldMapTracker();
        var currentZone = worldMapTracker.getCurrentZone();
        if (currentZone == null)
            return 0;
        var zoneMapping = LevelingCore.mobZoneMapping;

        return zoneMapping.getOrDefault(currentZone.zoneName().toLowerCase(), 1);
    }

    public static int computeBiomeLevel(Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var worldMapTracker = world.getPlayers().getFirst().getWorldMapTracker();
        var currentBiome = worldMapTracker.getCurrentBiomeName();

        if (currentBiome == null)
            return 6;

        var biomeMapping = LevelingCore.mobBiomeMapping;

        return biomeMapping.getOrDefault(currentBiome.toLowerCase(), 1);
    }

    public static int computeNearbyPlayersMeanLevel(TransformComponent transform, Store<EntityStore> store) {
        var world = store.getExternalData().getWorld();
        var mobPos = transform.getPosition();
        var players = world.getPlayers();
        var sum = 0;
        var count = 0;
        final var nearbyRadius = 40f;
        final float nearbyRadiusSq = nearbyRadius * nearbyRadius;
        final var fallbackNoPlayers = 5;
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
            return fallbackNoPlayers;

        var mean = (double) sum / (double) count;
        return (int) Math.round(mean);
    }
}
