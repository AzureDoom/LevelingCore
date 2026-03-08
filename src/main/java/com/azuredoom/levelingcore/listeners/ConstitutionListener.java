package com.azuredoom.levelingcore.listeners;

import java.util.UUID;

public interface ConstitutionListener {

    void onConstitutionChange(UUID playerId, int intelligence);
}
