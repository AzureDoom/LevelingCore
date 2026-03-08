package com.azuredoom.levelingcore.playerdata;

import java.util.UUID;

/**
 * Represents the level-related data of a player within the leveling system. This includes the player's unique
 * identifier and their experience points (XP). The class provides methods to retrieve and modify the player's XP, with
 * constraints ensuring it remains non-negative.
 */
public class PlayerLevelData {

    private final UUID playerId;

    private long xp;

    private int str;

    private int agi;

    private int per;

    private int vit;

    private int intelligence;

    private int con;

    private int abilityPoints;

    private int usedAbilityPoints;

    public PlayerLevelData(UUID playerId) {
        this.playerId = playerId;
        this.xp = 0;
    }

    /**
     * Retrieves the unique identifier of the player.
     *
     * @return The player's unique identifier as a UUID.
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Retrieves the player's current experience points (XP).
     *
     * @return The current XP value of the player as a long.
     */
    public long getXp() {
        return xp;
    }

    /**
     * Sets the player's experience points (XP) to the specified value. Ensures that the XP cannot be set to a negative
     * number; any negative input value will be adjusted to zero.
     *
     * @param xp The experience points to assign to the player. Values less than zero will be automatically adjusted to
     *           zero.
     */
    public void setXp(long xp) {
        this.xp = Math.max(0, xp);
    }

    /**
     * Retrieves the player's current strength attribute value.
     *
     * @return The strength value of the player as an integer.
     */
    public int getStr() {
        return str;
    }

    /**
     * Sets the player's strength attribute to the specified value.
     *
     * @param str The strength value to assign to the player.
     */
    public void setStr(int str) {
        this.str = str;
    }

    /**
     * Retrieves the player's current agility attribute value.
     *
     * @return The agility value of the player as an integer.
     */
    public int getAgi() {
        return agi;
    }

    /**
     * Sets the player's agility attribute to the specified value.
     *
     * @param agi The agility value to assign to the player.
     */
    public void setAgi(int agi) {
        this.agi = agi;
    }

    /**
     * Retrieves the player's current perception attribute value.
     *
     * @return The perception value of the player as an integer.
     */
    public int getPer() {
        return per;
    }

    /**
     * Sets the player's perception attribute to the specified value.
     *
     * @param per The perception value to assign to the player.
     */
    public void setPer(int per) {
        this.per = per;
    }

    /**
     * Retrieves the player's current vitality attribute value.
     *
     * @return The vitality value of the player as an integer.
     */
    public int getVit() {
        return vit;
    }

    /**
     * Sets the player's vitality attribute to the specified value.
     *
     * @param vit The vitality value to assign to the player.
     */
    public void setVit(int vit) {
        this.vit = vit;
    }

    /**
     * Retrieves the player's current intelligence attribute value.
     *
     * @return The intelligence value of the player as an integer.
     */
    public int getIntelligence() {
        return intelligence;
    }

    /**
     * Sets the player's intelligence attribute to the specified value.
     *
     * @param intelligence The intelligence value to assign to the player.
     */
    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    /**
     * Retrieves the player's current constitution attribute value.
     *
     * @return The constitution value of the player as an integer.
     */
    public int getCon() {
        return con;
    }

    /**
     * Sets the player's constitution attribute to the specified value.
     *
     * @param con The constitution value to assign to the player.
     */
    public void setCon(int con) {
        this.con = con;
    }

    /**
     * Retrieves the player's current ability points. Ability points are used to enhance the player's attributes and
     * skills.
     *
     * @return The ability points of the player as an integer.
     */
    public int getAbilityPoints() {
        return abilityPoints;
    }

    /**
     * Sets the player's ability points to the specified value. Ability points represent unallocated points that can be
     * used to increase the player's attributes and skills.
     *
     * @param abilityPoints The ability points to assign to the player.
     */
    public void setAbilityPoints(int abilityPoints) {
        this.abilityPoints = abilityPoints;
    }

    /**
     * Retrieves the total number of ability points that the player has already used. Ability points are allocated to
     * enhance the player's attributes and skills.
     *
     * @return The number of used ability points as an integer.
     */
    public int getUsedAbilityPoints() {
        return usedAbilityPoints;
    }

    /**
     * Sets the total number of ability points that the player has used. Used ability points represent the points
     * already allocated to enhance the player's attributes or skills.
     *
     * @param usedAbilityPoints The number of ability points to set as used for the player.
     */
    public void setUsedAbilityPoints(int usedAbilityPoints) {
        this.usedAbilityPoints = usedAbilityPoints;
    }

}
