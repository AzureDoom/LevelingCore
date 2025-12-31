package com.azuredoom.levelingcore.events;

import java.util.UUID;

/**
 * Interface for handling events when a player's level decreases in the system. Classes implementing this interface
 * should define specific behaviors that occur when a player's level is reduced due to various actions or conditions.
 */
public interface LevelDownListener {

    void onLevelDown(UUID playerId, int newLevel);
}
