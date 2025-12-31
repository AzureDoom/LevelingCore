package com.azuredoom.levelingcore.playerdata;

import java.util.UUID;

/**
 * Represents the level-related data of a player within the leveling system. This includes the player's unique
 * identifier and their experience points (XP). The class provides methods to retrieve and modify the player's XP, with
 * constraints ensuring it remains non-negative.
 */
public class PlayerLevelData {

    private final UUID playerId;

    private long xp;

    public PlayerLevelData(UUID playerId) {
        this.playerId = playerId;
        this.xp = 0;
    }

    /**
     * Retrieves the unique identifier of the player.
     *
     * @return The player's unique identifier as a UUID.
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Retrieves the player's current experience points (XP).
     *
     * @return The current XP value of the player as a long.
     */
    public long getXp() {
        return xp;
    }

    /**
     * Sets the player's experience points (XP) to the specified value. Ensures that the XP cannot be set to a negative
     * number; any negative input value will be adjusted to zero.
     *
     * @param xp The experience points to assign to the player. Values less than zero will be automatically adjusted to
     *           zero.
     */
    public void setXp(long xp) {
        this.xp = Math.max(0, xp);
    }
}
