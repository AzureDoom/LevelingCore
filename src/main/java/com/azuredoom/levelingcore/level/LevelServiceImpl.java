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

        if (oldLevel == LevelingUtil.computeMaxLevel()) {
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

    public void setStr(UUID id, int str) {
        var data = get(id);
        data.setStr(str);
        repository.save(data);

        strListeners.forEach(l -> l.onStrengthGain(id, str));
    }

    public int getStr(UUID id) {
        return get(id).getStr();
    }

    public void setAgi(UUID id, int agi) {
        var data = get(id);
        data.setAgi(agi);
        repository.save(data);

        agiListeners.forEach(l -> l.onAgilityGain(id, agi));
    }

    public int getAgi(UUID id) {
        return get(id).getAgi();
    }

    public void setPer(UUID id, int per) {
        var data = get(id);
        data.setPer(per);
        repository.save(data);

        perListeners.forEach(l -> l.onPerceptionGain(id, per));
    }

    public int getPer(UUID id) {
        return get(id).getPer();
    }

    public void setVit(UUID id, int vit) {
        var data = get(id);
        data.setVit(vit);
        repository.save(data);

        vitListeners.forEach(l -> l.onVitalityGain(id, vit));
    }

    public int getVit(UUID id) {
        return get(id).getVit();
    }

    public void setInt(UUID id, int intelligence) {
        var data = get(id);
        data.setIntelligence(intelligence);
        repository.save(data);

        intListeners.forEach(l -> l.onIntelligenceGain(id, intelligence));
    }

    public int getInt(UUID id) {
        return get(id).getIntelligence();
    }

    public void setCon(UUID id, int con) {
        var data = get(id);
        data.setCon(con);
        repository.save(data);

        conListeners.forEach(l -> l.onConstitutionGain(id, con));
    }

    public int getCon(UUID id) {
        return get(id).getCon();
    }

    public void setAbilityPoints(UUID id, int abilityPoints) {
        var data = get(id);
        data.setAbilityPoints(abilityPoints);
        repository.save(data);

        abilityPointsListeners.forEach(l -> l.onAbilityPointGain(id, abilityPoints));
    }

    public int getAbilityPoints(UUID id) {
        return get(id).getAbilityPoints();
    }

    public int getAvailableAbilityPoints(UUID id) {
        var data = get(id);
        return Math.max(0, data.getAbilityPoints() - data.getUsedAbilityPoints());
    }

    public int getUsedAbilityPoints(UUID id) {
        return get(id).getUsedAbilityPoints();
    }

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

    public void setUsedAbilityPoints(UUID id, int points) {
        var data = get(id);
        data.setUsedAbilityPoints(points);
        repository.save(data);

        abilityPointsListeners.forEach(
            l -> l.onAbilityPointLoss(id, points)
        );
    }

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
