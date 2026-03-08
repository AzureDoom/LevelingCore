package com.azuredoom.levelingcore.listeners;

import java.util.UUID;

public interface AgilityListener {

    void onAgilityChange(UUID playerId, int agility);
}
