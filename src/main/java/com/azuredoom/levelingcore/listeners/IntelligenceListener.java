package com.azuredoom.levelingcore.listeners;

import java.util.UUID;

public interface IntelligenceListener {

    void onIntelligenceChange(UUID playerId, int intelligence);
}
