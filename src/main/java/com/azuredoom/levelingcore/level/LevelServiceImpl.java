package com.azuredoom.levelingcore.level;

import com.azuredoom.levelingcore.database.LevelRepository;
import com.azuredoom.levelingcore.events.*;
import com.azuredoom.levelingcore.level.formulas.LevelFormula;
import com.azuredoom.levelingcore.playerdata.PlayerLevelData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the LevelService interface for managing player levels and experience points (XP). This class is
 * responsible for handling the retrieval, modification, and storage of player level data, as well as notifying
 * registered listeners for level-up, level-down, XP gain, and XP loss events.
 */
public class LevelServiceImpl implements LevelService {

    private final LevelFormula formula;

    private final LevelRepository repository;

    private final Map<UUID, PlayerLevelData> cache = new ConcurrentHashMap<>();

    private final List<LevelDownListener> levelDownListeners = new ArrayList<>();

    private final List<LevelUpListener> levelUpListeners = new ArrayList<>();

    private final List<XpGainListener> xpGainListeners = new ArrayList<>();

    private final List<XpLossListener> xpLossListeners = new ArrayList<>();

    public LevelServiceImpl(LevelFormula formula, LevelRepository repository) {
        this.formula = formula;
        this.repository = repository;
    }

    /**
     * Retrieves the {@link PlayerLevelData} associated with the given player ID. If the player data is not present in
     * the cache, it attempts to load it from the repository. If the repository does not contain data for the given ID,
     * a new instance of {@link PlayerLevelData} is created and cached.
     *
     * @param id The unique identifier (UUID) of the player whose level data is being retrieved.
     * @return The {@link PlayerLevelData} associated with the given player ID.
     */
    private PlayerLevelData get(UUID id) {
        return cache.computeIfAbsent(id, uuid -> {
            var stored = repository.load(uuid);
            return stored != null ? stored : new PlayerLevelData(uuid);
        });
    }

    /**
     * Retrieves the total experience points (XP) of the player associated with the given unique identifier (UUID).
     *
     * @param id The unique identifier (UUID) of the player whose experience points are being retrieved.
     * @return The total XP of the player as a long.
     */
    @Override
    public long getXp(UUID id) {
        return get(id).getXp();
    }

    /**
     * Retrieves the level of a player based on their current experience points (XP).
     *
     * @param id The unique identifier (UUID) of the player whose level is being retrieved.
     * @return The player's level as an integer, calculated from their XP.
     */
    @Override
    public int getLevel(UUID id) {
        return formula.getLevelForXp(get(id).getXp());
    }

    @Override
    public void addLevel(UUID id, int level) {
        if (level == 0) {
            return;
        }

        var data = get(id);
        var oldLevel = getLevel(id);

        int targetLevel = oldLevel + level;
        if (targetLevel < 1) {
            targetLevel = 1;
        }

        var targetXp = formula.getXpForLevel(targetLevel);
        setDataXP(data, targetXp);

        var newLevel = getLevel(id);

        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(id, newLevel));
        } else if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, newLevel));
        }
    }

    @Override
    public void removeLevel(UUID id, int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("level must be greater than 0");
        }

        var data = get(id);
        var oldLevel = getLevel(id);

        var targetLevel = oldLevel - level;
        if (targetLevel < 1) {
            targetLevel = 1;
        }

        var targetXp = formula.getXpForLevel(targetLevel);
        setDataXP(data, targetXp);

        var newLevel = getLevel(id);
        if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, newLevel));
        }
    }

    /**
     * Sets the level of the player associated with the given unique identifier (UUID). The method ensures that the
     * specified level is at least the minimum allowed (1). If the level changes, appropriate listeners for level-up or
     * level-down events are triggered.
     *
     * @param playerId The unique identifier (UUID) of the player whose level is being set.
     * @param level    The target level to set for the player. If the value provided is less than 1, it defaults to 1.
     * @return The new level of the player after the operation.
     */
    @Override
    public int setLevel(UUID playerId, int level) {
        var targetLevel = Math.max(level, 1);

        var data = get(playerId);
        var oldLevel = getLevel(playerId);

        var targetXp = getXpForLevel(targetLevel);
        setDataXP(data, targetXp);

        var newLevel = getLevel(playerId);
        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(playerId, newLevel));
        } else if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(playerId, newLevel));
        }

        return newLevel;
    }

    /**
     * Calculates the total experience points (XP) required to reach the specified level. If the level is less than or
     * equal to 1, the XP required is 0. For higher levels, the calculation is delegated to the associated
     * {@code LevelFormula}.
     *
     * @param level The target level for which the required XP is being calculated. Must be a positive integer.
     * @return The total XP required to reach the specified level. Returns 0 for level 1 or below.
     */
    @Override
    public long getXpForLevel(int level) {
        if (level <= 1) {
            return 0L;
        }

        return formula.getXpForLevel(level);
    }

    /**
     * Adds a specified number of experience points (XP) to the player associated with the given ID. If adding XP
     * results in the player leveling up, the appropriate level-up events are triggered. Notifications are sent to
     * registered listeners for both XP gain and level-up events, if applicable.
     *
     * @param id     The unique identifier (UUID) of the player whose XP is being modified.
     * @param amount The amount of XP to be added to the player's current XP balance.
     */
    @Override
    public void addXp(UUID id, long amount) {
        var data = get(id);
        var oldLevel = getLevel(id);

        setDataXP(data, data.getXp() + amount);

        xpGainListeners.forEach(l -> l.onXpGain(id, amount));

        var newLevel = getLevel(id);
        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(id, newLevel));
        }
    }

    /**
     * Removes a specified number of experience points (XP) from the player identified by the given ID. If the reduction
     * in XP results in a decrease in the player's level, the appropriate level-down events are triggered.
     *
     * @param id     The unique identifier (UUID) of the player whose XP is being reduced.
     * @param amount The amount of XP to remove from the player's total.
     */
    @Override
    public void removeXp(UUID id, long amount) {
        var data = get(id);
        var oldLevel = getLevel(id);

        setDataXP(data, data.getXp() - amount);

        xpLossListeners.forEach(l -> l.onXpLoss(id, amount));

        var newLevel = getLevel(id);
        if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, newLevel));
        }
    }

    /**
     * Sets the experience points (XP) of a player to a specified value. If the new XP value results in a level change,
     * the appropriate level-up or level-down listeners are triggered accordingly.
     *
     * @param id The unique identifier (UUID) of the player whose XP is being set.
     * @param xp The new experience points (XP) value to assign to the player.
     */
    @Override
    public void setXp(UUID id, long xp) {
        var data = get(id);
        var oldLevel = getLevel(id);

        setDataXP(data, xp);

        var newLevel = getLevel(id);
        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(id, newLevel));
        } else if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, newLevel));
        }
    }

    /**
     * Registers a listener to be notified of events when a player levels down. The listener's {@code onLevelDown}
     * method will be triggered whenever a player's level is decreased due to a specific action or condition in the
     * system.
     *
     * @param listener The {@link LevelDownListener} to be registered for receiving level-down notifications.
     */
    @Override
    public void registerLevelDownListener(LevelDownListener listener) {
        levelDownListeners.add(listener);
    }

    /**
     * Registers a listener to be notified of events when a player levels up. When a player's level increases, the
     * registered listener's {@code onLevelUp} method will be invoked.
     *
     * @param listener The {@link LevelUpListener} to be registered for receiving notifications about level-up events.
     */
    @Override
    public void registerLevelUpListener(LevelUpListener listener) {
        levelUpListeners.add(listener);
    }

    /**
     * Registers a listener to be notified of events when a player gains experience points (XP). The listener's
     * {@code onXpGain} method will be triggered whenever a player earns XP in the system.
     *
     * @param listener The {@link XpGainListener} to be registered for receiving XP gain notifications.
     */
    @Override
    public void registerXpGainListener(XpGainListener listener) {
        xpGainListeners.add(listener);
    }

    /**
     * Registers a listener to be notified of events when a player loses experience points (XP). The listener's
     * {@code onXpLoss} method will be triggered whenever a player loses XP in the system.
     *
     * @param listener The {@link XpLossListener} to be registered for receiving XP loss notifications.
     */
    @Override
    public void registerXpLossListener(XpLossListener listener) {
        xpLossListeners.add(listener);
    }

    /**
     * Updates the experience points (XP) of the specified player's level data and persists the changes to the
     * repository.
     *
     * @param data The {@link PlayerLevelData} object representing the player's level and experience data to be updated.
     * @param xp   The new experience points (XP) value to assign to the player. Values less than zero will be adjusted
     *             to zero by the underlying {@code setXp} method in {@link PlayerLevelData}.
     */
    private void setDataXP(PlayerLevelData data, long xp) {
        data.setXp(xp);
        repository.save(data);
    }
}
