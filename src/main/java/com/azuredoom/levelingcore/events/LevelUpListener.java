package com.azuredoom.levelingcore.events;

import java.util.UUID;

/**
 * Interface for handling events triggered when a player's level increases in the system. Implementations of this
 * interface should define the specific behavior that occurs when a player levels up.
 */
public interface LevelUpListener {

    void onLevelUp(UUID playerId, int newLevel);
}
