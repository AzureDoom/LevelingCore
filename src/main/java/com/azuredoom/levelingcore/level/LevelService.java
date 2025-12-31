package com.azuredoom.levelingcore.level;

import com.azuredoom.levelingcore.events.*;

import java.util.UUID;

/**
 * Service interface for managing player levels and experience points (XP) within a leveling system. This interface
 * provides methods for retrieving and modifying XP, as well as managing listeners for level-related events.
 */
public interface LevelService {

    void addLevel(UUID playerId, int level);

    void removeLevel(UUID playerId, int level);

    int setLevel(UUID playerId, int level);

    int getLevel(UUID playerId);

    long getXpForLevel(int level);

    long getXp(UUID playerId);

    void addXp(UUID playerId, long amount);

    void removeXp(UUID playerId, long amount);

    void setXp(UUID playerId, long xp);

    void registerXpGainListener(XpGainListener listener);

    void registerXpLossListener(XpLossListener listener);

    void registerLevelDownListener(LevelDownListener listener);

    void registerLevelUpListener(LevelUpListener listener);
}
