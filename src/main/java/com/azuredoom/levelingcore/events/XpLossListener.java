package com.azuredoom.levelingcore.events;

import java.util.UUID;

/**
 * Interface for handling events triggered when a player loses experience points (XP). Implementations of this interface
 * should define the specific behavior that occurs when XP is deducted from a player in the system.
 */
public interface XpLossListener {

    void onXpLoss(UUID playerId, long amount);
}
