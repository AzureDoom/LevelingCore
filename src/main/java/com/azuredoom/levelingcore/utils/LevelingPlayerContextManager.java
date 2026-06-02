package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LevelingPlayerContextManager {

    private static final Map<UUID, LevelingContext> PLAYER_CONTEXTS = new ConcurrentHashMap<>();

    public record LevelingContext(
        Player player,
        World world,
        Store<EntityStore> store,
        Ref<EntityStore> entityRef
    ) {}

    private LevelingPlayerContextManager() {}

    /**
     * Registers a player's context so other leveling systems can retrieve it via {@link #getContext(UUID)}.
     * <p>
     * The entity reference is validated immediately; if any argument is {@code null} or the reference is no longer
     * valid the player is silently ignored and not tracked.
     *
     * @param playerId  the UUID of the player to track
     * @param player    the {@link Player} instance
     * @param store     the {@link Store} holding the player's entity data
     * @param entityRef a {@link Ref} to the player's {@link EntityStore}; must be valid
     */
    public static void trackPlayer(
        UUID playerId,
        Player player,
        Store<EntityStore> store,
        Ref<EntityStore> entityRef
    ) {
        if (player == null || store == null || entityRef == null || !entityRef.isValid()) {
            return;
        }

        var world = store.getExternalData().getWorld();

        PLAYER_CONTEXTS.put(
            playerId,
            new LevelingContext(player, world, store, entityRef)
        );
    }

    /**
     * Returns the {@link LevelingContext} for the given player, or {@code null} if the player is not currently tracked.
     *
     * @param playerId the UUID of the player whose context to retrieve
     * @return the player's {@link LevelingContext}, or {@code null} if not present
     */
    public static LevelingContext getContext(UUID playerId) {
        return PLAYER_CONTEXTS.get(playerId);
    }

    /**
     * Removes all stored context for the given player.
     * <p>
     * Should be called when the player logs out to prevent memory leaks. Safe to call even if the player was never
     * tracked.
     *
     * @param playerId the UUID of the player to remove
     */
    public static void clear(UUID playerId) {
        PLAYER_CONTEXTS.remove(playerId);
    }
}
