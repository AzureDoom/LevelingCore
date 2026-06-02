package com.azuredoom.levelingcore.systems.stats;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.azuredoom.levelingcore.LevelingCore;

public final class AgilitySpeedManager {

    private static final Map<UUID, Float> CURRENT_MULTIPLIERS = new ConcurrentHashMap<>();

    private static final Map<UUID, ScheduledFuture<?>> RAMP_TASKS = new ConcurrentHashMap<>();

    private static final Map<UUID, SpeedContext> PLAYER_CONTEXTS = new ConcurrentHashMap<>();

    private static final long RAMP_INTERVAL_MS = 250L;

    private record SpeedContext(
        Player player,
        World world,
        Store<EntityStore> store,
        Ref<EntityStore> entityRef
    ) {}

    private AgilitySpeedManager() {}

    public static void trackPlayer(UUID playerId, Player player, Store<EntityStore> store, Ref<EntityStore> entityRef) {
        if (player == null || store == null || entityRef == null || !entityRef.isValid()) {
            return;
        }

        var world = store.getExternalData().getWorld();

        PLAYER_CONTEXTS.put(playerId, new SpeedContext(player, world, store, entityRef));
    }

    public static void applyForPlayer(UUID playerId) {
        var agility = LevelingCore.getLevelService().getAgi(playerId);

        var targetMultiplier = 1.0F + Math.min(
            LevelingCore.getConfig().get().getAgiMaxSpeedBonus(),
            agility * LevelingCore.getConfig().get().getAgiSpeedPerPoint()
        );

        rampTo(playerId, targetMultiplier);
    }

    public static void clear(UUID playerId) {
        var task = RAMP_TASKS.remove(playerId);
        if (task != null) {
            task.cancel(false);
        }

        CURRENT_MULTIPLIERS.remove(playerId);
        PLAYER_CONTEXTS.remove(playerId);
    }

    private static void rampTo(UUID playerId, float targetMultiplier) {
        var existing = RAMP_TASKS.remove(playerId);
        if (existing != null) {
            existing.cancel(false);
        }

        var current = CURRENT_MULTIPLIERS.getOrDefault(playerId, 1.0F);

        var task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                var step = LevelingCore.getConfig().get().getAgiSpeedRampStep();
                var value = CURRENT_MULTIPLIERS.getOrDefault(playerId, current);

                if (Math.abs(value - targetMultiplier) <= step) {
                    value = targetMultiplier;
                } else if (value < targetMultiplier) {
                    value += step;
                } else {
                    value -= step;
                }

                CURRENT_MULTIPLIERS.put(playerId, value);
                applyMovementMultiplier(playerId, value);

                if (value == targetMultiplier) {
                    var finished = RAMP_TASKS.remove(playerId);
                    if (finished != null) {
                        finished.cancel(false);
                    }
                }
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning().withCause(e).log("Failed to apply AGI speed");
                var failed = RAMP_TASKS.remove(playerId);
                if (failed != null) {
                    failed.cancel(false);
                }
            }
        }, 0L, RAMP_INTERVAL_MS, TimeUnit.MILLISECONDS);

        RAMP_TASKS.put(playerId, task);
    }

    private static void applyMovementMultiplier(UUID playerId, float multiplier) {
        var context = PLAYER_CONTEXTS.get(playerId);
        if (context == null) {
            return;
        }

        context.world().execute(() -> {
            try {
                var entityRef = context.player().getReference();
                if (entityRef == null || !entityRef.isValid()) {
                    return;
                }

                var store = entityRef.getStore();

                var movementManager = store.getComponent(
                    entityRef,
                    MovementManager.getComponentType()
                );

                if (movementManager == null) {
                    return;
                }

                var settings = movementManager.getSettings();
                if (settings == null) {
                    return;
                }

                settings.baseSpeed = 5.0F * multiplier;

                settings.maxSpeedMultiplier = multiplier;
                settings.forwardRunSpeedMultiplier = multiplier;
                settings.forwardSprintSpeedMultiplier = multiplier;

                var playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
                if (playerRef == null) {
                    return;
                }

                movementManager.update(playerRef.getPacketHandler());
            } catch (Exception e) {
                LevelingCore.LOGGER.atWarning().withCause(e).log("Failed to update AGI movement settings");
            }
        });
    }
}
