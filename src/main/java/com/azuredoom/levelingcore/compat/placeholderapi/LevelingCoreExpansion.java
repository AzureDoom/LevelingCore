package com.azuredoom.levelingcore.compat.placeholderapi;

import at.helpch.placeholderapi.expansion.PlaceholderExpansion;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.azuredoom.levelingcore.LevelingCore;

public class LevelingCoreExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "levelingcore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AzureDoom";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.9.5";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(PlayerRef playerRef, @NotNull String params) {
        var levelService = LevelingCore.getLevelService();
        if (levelService == null) {
            return null;
        }
        if (params.equalsIgnoreCase("level")) {
            return String.valueOf(levelService.getLevel(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("xp")) {
            return String.valueOf(levelService.getXp(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("xp_to_level")) {
            return String.valueOf(levelService.getXpForLevel(levelService.getLevel(playerRef.getUuid()) + 1));
        }
        // Ability Points
        if (params.equalsIgnoreCase("ability_points")) {
            return String.valueOf(levelService.getAvailableAbilityPoints(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("available_ability_points")) {
            return String.valueOf(levelService.getAvailableAbilityPoints(playerRef.getUuid()));
        }
        // Stats
        if (params.equalsIgnoreCase("str")) {
            return String.valueOf(levelService.getStr(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("agi")) {
            return String.valueOf(levelService.getAgi(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("per")) {
            return String.valueOf(levelService.getPer(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("vit")) {
            return String.valueOf(levelService.getVit(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("int")) {
            return String.valueOf(levelService.getInt(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("con")) {
            return String.valueOf(levelService.getCon(playerRef.getUuid()));
        }
        return null;
    }
}
