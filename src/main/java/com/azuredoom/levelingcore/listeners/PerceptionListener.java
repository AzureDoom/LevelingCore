package com.azuredoom.levelingcore.listeners;

import java.util.UUID;

public interface PerceptionListener {

    void onPerceptionChange(UUID playerId, int perception);
}
