package com.azuredoom.levelingcore.events;

import java.util.UUID;

/**
 * Interface for handling events when a player gains experience points (XP). Implementations of this interface should
 * define the specific behavior that occurs when a player earns XP in the system.
 */
public interface XpGainListener {

    void onXpGain(UUID playerId, long amount);
}
