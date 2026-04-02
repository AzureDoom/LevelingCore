package com.azuredoom.levelingcore.level.mobs;

/**
 * Represents the level data associated with a mob, encapsulating various attributes such as level, tier, multipliers,
 * and profile information. This class is designed to hold data related to mob-level scaling, providing features like
 * level locking, health and damage multipliers, experience point multipliers, and more.
 */
public final class MobLevelData {

    public int level;

    public boolean locked;

    public int lastAppliedLevel;

    public volatile long lastRecalcMs;

    public MobTier tier;

    public String profileId;

    public float healthMultiplier;

    public float damageMultiplier;

    public float damageThreshold;

    public float xpMultiplier;

    public boolean persistentProfile;

    public boolean rewardEligible;

    public MobLevelData(int level) {
        this.level = level;
        this.locked = false;
        this.lastAppliedLevel = level;
        this.lastRecalcMs = 0L;

        this.tier = MobTier.NORMAL;
        this.profileId = "normal";

        this.healthMultiplier = 1.0f;
        this.damageMultiplier = 1.0f;
        this.damageThreshold = 1.0f;
        this.xpMultiplier = 1.0f;

        this.persistentProfile = true;
        this.rewardEligible = true;
    }

    public static MobLevelData from(int level, MobEncounterProfile profile) {
        var data = new MobLevelData(level);
        data.tier = profile.tier();
        data.profileId = profile.id();
        data.healthMultiplier = profile.healthMultiplier();
        data.damageMultiplier = profile.damageMultiplier();
        data.damageThreshold = profile.damageThreshold();
        data.xpMultiplier = profile.xpMultiplier();
        return data;
    }
}
