package com.azuredoom.levelingcore.listeners;

import java.util.UUID;

public interface VitalityListener {

    void onVitalityChange(UUID playerId, int vitality);
}
