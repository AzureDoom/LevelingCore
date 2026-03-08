package com.azuredoom.levelingcore.listeners;

import java.util.UUID;

public interface StrengthListener {

    void onStrengthChange(UUID playerId, int strength);
}
