package com.azuredoom.levelingcore.level.mobs;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.GUIConfig;

/**
 * Represents a profile for a mob encounter with specific multipliers and tier settings. This class encapsulates the
 * unique characteristics of a mob encounter, such as health, damage, and experience point scaling, based on a defined
 * {@link MobTier}. Instances of this record are immutable and thread-safe.
 *
 * @param id               The unique identifier of the mob encounter profile.
 * @param tier             The {@link MobTier} associated with this mob encounter profile.
 * @param healthMultiplier A multiplier applied to the mob's base health.
 * @param damageMultiplier A multiplier applied to the mob's base damage.
 * @param xpMultiplier     A multiplier applied to the experience points earned from the mob.
 */
public record MobEncounterProfile(
    String id,
    MobTier tier,
    float healthMultiplier,
    float damageMultiplier,
    float damageThreshold,
    float xpMultiplier
) {

    private static GUIConfig config = LevelingCore.getConfig().get();

    /**
     * A predefined mob encounter profile representing the default or baseline configuration for mob encounters. This
     * profile is associated with the {@link MobTier#NORMAL} tier and uses multipliers with a value of 1.0, signifying
     * no scaling adjustments to the base attributes of the mob.
     */
    public static final MobEncounterProfile NORMAL =
        new MobEncounterProfile(
            "normal",
            MobTier.NORMAL,
            config.getNormalMobHealthMultiplier(),
            config.getNormalMobDamageMultiplier(),
            config.getNormalMobDamageThreshold(),
            config.getNormalMobXPMultiplier()
        );

    /**
     * A predefined mob encounter profile representing the "Elite" tier of mobs. This profile introduces enhanced
     * difficulty compared to the {@code NORMAL} tier, featuring increased health, damage, and experience point
     * multipliers. Mobs assigned to this profile are more challenging and serve as a step up from common encounters.
     */
    public static final MobEncounterProfile ELITE =
        new MobEncounterProfile(
            "elite",
            MobTier.ELITE,
            config.getEliteMobHealthMultiplier(),
            config.getEliteMobDamageMultiplier(),
            config.getEliteMobDamageThreshold(),
            config.getEliteMobXPMultiplier()
        );

    /**
     * A predefined mob encounter profile representing the "Boss" tier of mobs. This profile is associated with the
     * {@link MobTier#BOSS} tier and significantly enhances the difficulty of the encounter, serving as a major or
     * climactic challenge within the game. Mobs assigned to this profile are designed to pose a considerable threat,
     * featuring significant increases to health, damage, and experience point rewards.
     */
    public static final MobEncounterProfile BOSS =
        new MobEncounterProfile(
            "boss",
            MobTier.BOSS,
            config.getBossMobHealthMultiplier(),
            config.getBossMobDamageMultiplier(),
            config.getBossMobDamageThreshold(),
            config.getBossMobXPMultiplier()
        );

    public static GUIConfig getConfig() {
        return config;
    }

    public static void setConfig(GUIConfig config) {
        MobEncounterProfile.config = config;
    }
}
