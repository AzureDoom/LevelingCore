package com.azuredoom.levelingcore.level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.database.JdbcLevelRepository;
import com.azuredoom.levelingcore.level.formulas.LevelFormula;
import com.azuredoom.levelingcore.listeners.*;
import com.azuredoom.levelingcore.playerdata.PlayerLevelData;
import com.azuredoom.levelingcore.utils.LevelingUtil;

/**
 * Used for managing player levels and experience points (XP). This class provides methods to retrieve, modify, and
 * calculate levels and XP for individual players. It also supports notifying listeners for level-up, level-down, XP
 * gain, and XP loss events.
 */
public class LevelServiceImpl {

    private final LevelFormula formula;

    private final JdbcLevelRepository repository;

    private final Map<UUID, PlayerLevelData> cache = new ConcurrentHashMap<>();

    private final List<LevelDownListener> levelDownListeners = new ArrayList<>();

    private final List<LevelUpListener> levelUpListeners = new ArrayList<>();

    private final List<XpGainListener> xpGainListeners = new ArrayList<>();

    private final List<XpLossListener> xpLossListeners = new ArrayList<>();

    private final List<StrengthListener> strListeners = new ArrayList<>();

    private final List<AgilityListener> agiListeners = new ArrayList<>();

    private final List<PerceptionListener> perListeners = new ArrayList<>();

    private final List<VitalityListener> vitListeners = new ArrayList<>();

    private final List<IntelligenceListener> intListeners = new ArrayList<>();

    private final List<ConstitutionListener> conListeners = new ArrayList<>();

    private final List<AbilityPointsListener> abilityPointsListeners = new ArrayList<>();

    public LevelServiceImpl(LevelFormula formula, JdbcLevelRepository repository) {
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
    public long getXp(UUID id) {
        return get(id).getXp();
    }

    /**
     * Retrieves the level of a player based on their current experience points (XP).
     *
     * @param id The unique identifier (UUID) of the player whose level is being retrieved.
     * @return The player's level as an integer, calculated from their XP.
     */
    public int getLevel(UUID id) {
        return formula.getLevelForXp(get(id).getXp());
    }

    /**
     * Checks if a player has reached the maximum level based on their current experience points (XP).
     *
     * @param id The unique identifier (UUID) of the player whose level is being checked.
     * @return {@code true} if the player is at the maximum level, {@code false} otherwise.
     */
    public boolean isMaxLevel(UUID id) {
        return getLevel(id) >= LevelingUtil.computeMaxLevel();
    }

    /**
     * Adjusts the level of a specified entity by adding a given level change value. Positive values increase the level,
     * while negative values decrease it. Ensures the resulting level is within valid bounds and triggers level-up or
     * level-down listeners as appropriate.
     *
     * @param id    the unique identifier of the entity whose level is being modified
     * @param level the amount by which to adjust the entity's level; positive to increase, negative to decrease
     */
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
            levelUpListeners.forEach(l -> l.onLevelUp(id, oldLevel, newLevel));
        } else if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, oldLevel, newLevel));
        }
    }

    /**
     * Reduces the level of an entity identified by its unique ID. If the level reduction results in a level less than
     * 1, the level will be set to the minimum of 1. The method also notifies level-down listeners if the level changes.
     *
     * @param id    the unique identifier of the entity whose level is to be reduced
     * @param level the number of levels to remove; must be greater than 0
     * @throws IllegalArgumentException if the level is not greater than 0
     */
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
            levelDownListeners.forEach(l -> l.onLevelDown(id, oldLevel, newLevel));
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
    public int setLevel(UUID playerId, int level) {
        var targetLevel = Math.max(level, 1);

        var data = get(playerId);
        var oldLevel = getLevel(playerId);

        var targetXp = getXpForLevel(targetLevel);
        setDataXP(data, targetXp);

        var newLevel = getLevel(playerId);
        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(playerId, oldLevel, newLevel));
        } else if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(playerId, oldLevel, newLevel));
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
    public void addXp(UUID id, long amount) {
        var data = get(id);
        var oldLevel = getLevel(id);

        if (isMaxLevel(id)) {
            setDataXP(data, formula.getXpForLevel(oldLevel));
            return;
        }

        setDataXP(data, data.getXp() + amount);

        xpGainListeners.forEach(l -> l.onXpGain(id, amount));

        var newLevel = getLevel(id);
        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(id, oldLevel, newLevel));
        }
    }

    /**
     * Removes a specified number of experience points (XP) from the player identified by the given ID. If the reduction
     * in XP results in a decrease in the player's level, the appropriate level-down events are triggered.
     *
     * @param id     The unique identifier (UUID) of the player whose XP is being reduced.
     * @param amount The amount of XP to remove from the player's total.
     */
    public void removeXp(UUID id, long amount) {
        var data = get(id);
        var oldLevel = getLevel(id);

        setDataXP(data, data.getXp() - amount);

        xpLossListeners.forEach(l -> l.onXpLoss(id, amount));

        var newLevel = getLevel(id);
        if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, oldLevel, newLevel));
        }
    }

    /**
     * Sets the experience points (XP) of a player to a specified value. If the new XP value results in a level change,
     * the appropriate level-up or level-down listeners are triggered accordingly.
     *
     * @param id The unique identifier (UUID) of the player whose XP is being set.
     * @param xp The new experience points (XP) value to assign to the player.
     */
    public void setXp(UUID id, long xp) {
        var data = get(id);
        var oldLevel = getLevel(id);

        setDataXP(data, xp);

        var newLevel = getLevel(id);
        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(id, oldLevel, newLevel));
        } else if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, oldLevel, newLevel));
        }
    }

    /**
     * Updates the Strength value for the entity identified by the given UUID and notifies all registered listeners
     * about the change.
     *
     * @param id  the unique identifier of the entity whose Strength value is to be updated
     * @param str the new Strength value to set for the entity
     */
    public void setStr(UUID id, int str) {
        var data = get(id);
        var clamped = Math.max(0, str);
        data.setStr(clamped);
        repository.save(data);

        strListeners.forEach(l -> l.onStrengthChange(id, clamped));
    }

    /**
     * Retrieves the Strength value associated with the given UUID.
     *
     * @param id the unique identifier used to locate the target object
     * @return an integer representation of the Strength value.
     */
    public int getStr(UUID id) {
        return get(id).getStr();
    }

    /**
     * Updates the Agility value for the entity identified by the given UUID and notifies all registered listeners about
     * the change.
     *
     * @param id  the unique identifier of the entity whose Agility value is to be updated
     * @param agi the new Agility value to set for the entity
     */
    public void setAgi(UUID id, int agi) {
        var data = get(id);
        var clamped = Math.max(0, agi);
        data.setStr(clamped);
        repository.save(data);

        agiListeners.forEach(l -> l.onAgilityChange(id, clamped));
    }

    /**
     * Retrieves the Agility value associated with the given UUID.
     *
     * @param id the unique identifier used to locate the target object
     * @return an integer representation of the Agility value.
     */
    public int getAgi(UUID id) {
        return get(id).getAgi();
    }

    /**
     * Updates the Perception value for the entity identified by the given UUID and notifies all registered listeners
     * about the change.
     *
     * @param id  the unique identifier of the entity whose Perception value is to be updated
     * @param per the new Perception value to set for the entity
     */
    public void setPer(UUID id, int per) {
        var data = get(id);
        var clamped = Math.max(0, per);
        data.setStr(clamped);
        repository.save(data);

        perListeners.forEach(l -> l.onPerceptionChange(id, clamped));
    }

    /**
     * Retrieves the Perception value associated with the given UUID.
     *
     * @param id the unique identifier used to locate the target object
     * @return an integer representation of the Perception value.
     */
    public int getPer(UUID id) {
        return get(id).getPer();
    }

    /**
     * Updates the Vitality value for the entity identified by the given UUID and notifies all registered listeners
     * about the change.
     *
     * @param id  the unique identifier of the entity whose Vitality value is to be updated
     * @param vit the new Vitality value to set for the entity
     */
    public void setVit(UUID id, int vit) {
        var data = get(id);
        var clamped = Math.max(0, vit);
        data.setStr(clamped);
        repository.save(data);

        vitListeners.forEach(l -> l.onVitalityChange(id, clamped));
    }

    /**
     * Retrieves the Vitality value associated with the given UUID.
     *
     * @param id the unique identifier used to locate the target object
     * @return an integer representation of the Vitality value.
     */
    public int getVit(UUID id) {
        return get(id).getVit();
    }

    /**
     * Updates the Intelligence value for the entity identified by the given UUID and notifies all registered listeners
     * about the change.
     *
     * @param id           the unique identifier of the entity whose Intelligence value is to be updated
     * @param intelligence the new Intelligence value to set for the entity
     */
    public void setInt(UUID id, int intelligence) {
        var data = get(id);
        var clamped = Math.max(0, intelligence);
        data.setStr(clamped);
        repository.save(data);

        intListeners.forEach(l -> l.onIntelligenceChange(id, clamped));
    }

    /**
     * Retrieves the Intelligence value associated with the given UUID.
     *
     * @param id the unique identifier used to locate the target object
     * @return an integer representation of the Intelligence value.
     */
    public int getInt(UUID id) {
        return get(id).getIntelligence();
    }

    /**
     * Updates the Constitution value for the entity identified by the given UUID and notifies all registered listeners
     * about the change.
     *
     * @param id  the unique identifier of the entity whose Constitution value is to be updated
     * @param con the new Constitution value to set for the entity
     */
    public void setCon(UUID id, int con) {
        var data = get(id);
        var clamped = Math.max(0, con);
        data.setStr(clamped);
        repository.save(data);

        conListeners.forEach(l -> l.onConstitutionChange(id, clamped));
    }

    /**
     * Retrieves the Constitution value associated with the given UUID.
     *
     * @param id the unique identifier used to locate the target object
     * @return an integer representation of the Constitution value.
     */
    public int getCon(UUID id) {
        return get(id).getCon();
    }

    public void setAbilityPoints(UUID id, int abilityPoints) {
        var data = get(id);
        data.setAbilityPoints(abilityPoints);
        repository.save(data);

        abilityPointsListeners.forEach(l -> l.onAbilityPointGain(id, abilityPoints));
    }

    /**
     * Retrieves the Ability Points value associated with the given UUID.
     *
     * @param id the unique identifier used to locate the target object
     * @return an integer representation of the Ability Points value.
     */
    public int getAbilityPoints(UUID id) {
        return get(id).getAbilityPoints();
    }

    /**
     * Retrieves the Available Ability Points value associated with the given UUID.
     *
     * @param id the unique identifier used to locate the target object
     * @return an integer representation of the Available Ability Points value.
     */
    public int getAvailableAbilityPoints(UUID id) {
        var data = get(id);
        return Math.max(0, data.getAbilityPoints() - data.getUsedAbilityPoints());
    }

    /**
     * Retrieves the number of ability points used by the entity associated with the given identifier.
     *
     * @param id the unique identifier of the entity for which the used ability points are being retrieved
     * @return the number of ability points used by the entity
     */
    public int getUsedAbilityPoints(UUID id) {
        return get(id).getUsedAbilityPoints();
    }

    /**
     * Adds the specified number of ability points to the entity identified by the given UUID. If the number of points
     * to add is less than or equal to zero, the method will return without making any changes.
     *
     * @param id          the unique identifier of the entity to which ability points will be added
     * @param pointsToAdd the number of ability points to add; must be a positive integer
     */
    public void addAbilityPoints(UUID id, int pointsToAdd) {
        if (pointsToAdd <= 0)
            return;

        var data = get(id);
        data.setAbilityPoints(data.getAbilityPoints() + pointsToAdd);
        repository.save(data);

        abilityPointsListeners.forEach(
            l -> l.onAbilityPointGain(id, pointsToAdd)
        );
    }

    /**
     * Sets the number of ability points used for the entity identified by the given UUID and notifies all registered
     * listeners about the change.
     *
     * @param id     The unique identifier of the entity whose used ability points are being set.
     * @param points The number of ability points to mark as used for the specified entity.
     */
    public void setUsedAbilityPoints(UUID id, int points) {
        var data = get(id);
        data.setUsedAbilityPoints(points);
        repository.save(data);

        abilityPointsListeners.forEach(
            l -> l.onAbilityPointLoss(id, points)
        );
    }

    /**
     * Deducts the specified number of ability points from the available points of the entity identified by the given
     * UUID. If the requested amount exceeds the available points or if the amount is invalid, the operation will fail.
     *
     * @param id     the unique identifier of the entity whose ability points are being modified
     * @param amount the number of ability points to deduct
     * @return true if the ability points were successfully deducted, false otherwise
     */
    public boolean useAbilityPoints(UUID id, int amount) {
        if (amount <= 0)
            return false;

        var data = get(id);

        int total = data.getAbilityPoints();
        int used = data.getUsedAbilityPoints();
        int available = total - used;

        if (amount > available) {
            return false; // not enough points
        }

        data.setUsedAbilityPoints(used + amount);
        repository.save(data);

        abilityPointsListeners.forEach(
            l -> l.onAbilityPointUsed(id, amount)
        );

        return true;
    }

    /**
     * Registers a listener to be notified of events when a player levels down. The listener's {@code onLevelDown}
     * method will be triggered whenever a player's level is decreased due to a specific action or condition in the
     * system.
     *
     * @param listener The {@link LevelDownListener} to be registered for receiving level-down notifications.
     */
    public void registerLevelDownListener(LevelDownListener listener) {
        levelDownListeners.add(listener);
    }

    /**
     * Retrieves the list of registered {@link LevelDownListener} instances. These listeners are notified whenever a
     * player's level decreases due to specific actions or conditions in the system.
     *
     * @return A list of {@link LevelDownListener} objects currently registered to receive level-down notifications.
     */
    public List<LevelDownListener> getLevelDownListeners() {
        return levelDownListeners;
    }

    /**
     * Registers a listener to be notified of events when a player levels up. When a player's level increases, the
     * registered listener's {@code onLevelUp} method will be invoked.
     *
     * @param listener The {@link LevelUpListener} to be registered for receiving notifications about level-up events.
     */
    public void registerLevelUpListener(LevelUpListener listener) {
        levelUpListeners.add(listener);
    }

    /**
     * Retrieves the list of registered {@link LevelUpListener} instances. These listeners are notified whenever a
     * player's level increases due to specific actions or conditions in the system.
     *
     * @return A list of {@link LevelUpListener} objects currently registered to receive level-up notifications.
     */
    public List<LevelUpListener> getLevelUpListeners() {
        return levelUpListeners;
    }

    /**
     * Registers a listener to be notified of events when a player gains experience points (XP). The listener's
     * {@code onXpGain} method will be triggered whenever a player earns XP in the system.
     *
     * @param listener The {@link XpGainListener} to be registered for receiving XP gain notifications.
     */
    public void registerXpGainListener(XpGainListener listener) {
        xpGainListeners.add(listener);
    }

    /**
     * Retrieves the list of registered {@link XpGainListener} instances. These listeners are notified whenever a player
     * gains experience points (XP) due to specific actions or events in the system.
     *
     * @return A list of {@link XpGainListener} objects currently registered to handle XP gain notifications.
     */
    public List<XpGainListener> getXpGainListeners() {
        return xpGainListeners;
    }

    /**
     * Registers a listener to be notified of events when a player loses experience points (XP). The listener's
     * {@code onXpLoss} method will be triggered whenever a player loses XP in the system.
     *
     * @param listener The {@link XpLossListener} to be registered for receiving XP loss notifications.
     */
    public void registerXpLossListener(XpLossListener listener) {
        xpLossListeners.add(listener);
    }

    /**
     * Retrieves the list of registered {@link XpLossListener} instances. These listeners are notified whenever a player
     * loses experience points (XP) due to specific actions or conditions in the system.
     *
     * @return A list of {@link XpLossListener} objects currently registered to receive XP loss notifications.
     */
    public List<XpLossListener> getXpLossListeners() {
        return xpLossListeners;
    }

    public void registerStrengthListener(StrengthListener listener) {
        strListeners.add(listener);
    }

    public List<StrengthListener> getStrengthListeners() {
        return strListeners;
    }

    public void registerAgilityListener(AgilityListener listener) {
        agiListeners.add(listener);
    }

    public List<AgilityListener> getAgilityListeners() {
        return agiListeners;
    }

    public void registerPerceptionListener(PerceptionListener listener) {
        perListeners.add(listener);
    }

    public List<PerceptionListener> getPerceptionListeners() {
        return perListeners;
    }

    public void registerVitalityListener(VitalityListener listener) {
        vitListeners.add(listener);
    }

    public List<VitalityListener> getVitalityListeners() {
        return vitListeners;
    }

    public void registerIntelligenceListener(IntelligenceListener listener) {
        intListeners.add(listener);
    }

    public List<IntelligenceListener> getIntelligenceListeners() {
        return intListeners;
    }

    public void registerConstitutionListener(ConstitutionListener listener) {
        conListeners.add(listener);
    }

    public List<ConstitutionListener> getConstitutionListeners() {
        return conListeners;
    }

    public List<AbilityPointsListener> getAbilityPointsListeners() {
        return abilityPointsListeners;
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
