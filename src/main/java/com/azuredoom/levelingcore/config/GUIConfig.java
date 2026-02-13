package com.azuredoom.levelingcore.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Represents the configuration for the Graphical User Interface (GUI) settings, particularly for managing experience
 * points (XP) and leveling mechanics. This class provides options to configure the behavior of experience loss, gain,
 * and level adjustments in different scenarios. The configuration is encoded and decoded using a predefined codec for
 * persistence and retrieval.
 */
public class GUIConfig {

    public static final BuilderCodec<GUIConfig> CODEC = BuilderCodec.builder(GUIConfig.class, GUIConfig::new)
        .append(
            new KeyedCodec<Boolean>("EnableXPLossOnDeath", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableXPLossOnDeath = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableXPLossOnDeath
        )
        .add()
        .append(
            new KeyedCodec<Double>("XPLossPercentage", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.xpLossPercentage = aDouble,
            (exConfig, extraInfo) -> exConfig.xpLossPercentage
        )
        .add()
        .append(
            new KeyedCodec<Double>("DefaultXPGainPercentage", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.defaultXPGainPercentage = aDouble,
            (exConfig, extraInfo) -> exConfig.defaultXPGainPercentage
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableDefaultXPGainSystem", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableDefaultXPGainSystem = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableDefaultXPGainSystem
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableLevelDownOnDeath", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLevelDownOnDeath = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLevelDownOnDeath
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableAllLevelsLostOnDeath", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableAllLevelsLostOnDeath = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableAllLevelsLostOnDeath
        )
        .add()
        .append(
            new KeyedCodec<Integer>("MinLevelForLevelDown", Codec.INTEGER),
            (exConfig, aInteger, extraInfo) -> exConfig.minLevelForLevelDown = aInteger,
            (exConfig, extraInfo) -> exConfig.minLevelForLevelDown
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableLevelChatMsgs", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLevelChatMsgs = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLevelChatMsgs
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("DisableXPGainNotification", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.disableXPGainNotification = aBoolean,
            (exConfig, extraInfo) -> exConfig.disableXPGainNotification
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableLevelAndXPTitles", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLevelAndXPTitles = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLevelAndXPTitles
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("ShowXPAmountInHUD", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.showXPAmountInHUD = aBoolean,
            (exConfig, extraInfo) -> exConfig.showXPAmountInHUD
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableStatLeveling", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableStatLeveling = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableStatLeveling
        )
        .add()
        .append(
            new KeyedCodec<Float>("HealthLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.healthLevelUpMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.healthLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("StaminaLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.staminaLevelUpMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.staminaLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("ManaLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.manaLevelUpMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.manaLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableStatHealing", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableStatHealing = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableStatHealing
        )
        .add()
        .append(
            new KeyedCodec<String>("LevelUpSound", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.levelUpSound = aString,
            (exConfig, extraInfo) -> exConfig.levelUpSound
        )
        .add()
        .append(
            new KeyedCodec<String>("LevelDownSound", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.levelDownSound = aString,
            (exConfig, extraInfo) -> exConfig.levelDownSound
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("UseConfigXPMappingsInsteadOfHealthDefaults", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.useConfigXPMappingsInsteadOfHealthDefaults = aBoolean,
            (exConfig, extraInfo) -> exConfig.useConfigXPMappingsInsteadOfHealthDefaults
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableLevelUpRewardsConfig", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLevelUpRewardsConfig = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLevelUpRewardsConfig
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("DisableStatPointGainOnLevelUp", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.disableStatPointGainOnLevelUp = aBoolean,
            (exConfig, extraInfo) -> exConfig.disableStatPointGainOnLevelUp
        )
        .add()
        .append(
            new KeyedCodec<Integer>("StatsPerLevel", Codec.INTEGER),
            (exConfig, aInteger, extraInfo) -> exConfig.statsPerLevel = aInteger,
            (exConfig, extraInfo) -> exConfig.statsPerLevel
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("UseStatsPerLevelMapping", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.useStatsPerLevelMapping = aBoolean,
            (exConfig, extraInfo) -> exConfig.useStatsPerLevelMapping
        )
        .add()
        .append(
            new KeyedCodec<Float>("StrStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.strStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.strStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("PerStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.perStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.perStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("VitStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.vitStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.vitStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("AgiStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.agiStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.agiStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("IntStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.intStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.intStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("ConStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.conStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.conStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnablePartyProXPShareCompat", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enablePartyProXPShareCompat = aBoolean,
            (exConfig, extraInfo) -> exConfig.enablePartyProXPShareCompat
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnablePartyPluginXPShareCompat", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enablePartyPluginXPShareCompat = aBoolean,
            (exConfig, extraInfo) -> exConfig.enablePartyPluginXPShareCompat
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnablePartyXPSplit", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enablePartyXPSplit = aBoolean,
            (exConfig, extraInfo) -> exConfig.enablePartyXPSplit
        )
        .add()
        .append(
            new KeyedCodec<Double>("PartyGroupXPMultiplier", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.partyGroupXPMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.partyGroupXPMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("KillerGetsFullXp", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.killerGetsFullXp = aBoolean,
            (exConfig, extraInfo) -> exConfig.killerGetsFullXp
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnablePartyXPDistanceCheck", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enablePartyXPDistanceCheck = aBoolean,
            (exConfig, extraInfo) -> exConfig.enablePartyXPDistanceCheck
        )
        .add()
        .append(
            new KeyedCodec<Double>("PartyXPDistanceBlocks", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.partyXPDistanceBlocks = aDouble,
            (exConfig, extraInfo) -> exConfig.partyXPDistanceBlocks
        )
        .add()
        .append(
            new KeyedCodec<String>("LevelMode", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.levelMode = aString,
            (exConfig, extraInfo) -> exConfig.levelMode
        )
        .add()
        .append(
            new KeyedCodec<Integer>("LevelVariance", Codec.INTEGER),
            (exConfig, anInteger, extraInfo) -> exConfig.levelVariance = anInteger,
            (exConfig, extraInfo) -> exConfig.levelVariance
        )
        .add()
        .append(
            new KeyedCodec<Float>("MobHealthMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobHealthMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.mobHealthMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("MobDamageMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobDamageMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.mobDamageMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("MobBaseDamage", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobBaseDamage = aFloat,
            (exConfig, extraInfo) -> exConfig.mobBaseDamage
        )
        .add()
        .append(
            new KeyedCodec<Float>("MobRangeDamageMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobRangeDamageMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.mobRangeDamageMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("MobBaseRangeDamage", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobBaseRangeDamage = aFloat,
            (exConfig, extraInfo) -> exConfig.mobBaseRangeDamage
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableItemLevelRestriction", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableItemLevelRestriction = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableItemLevelRestriction
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableXPBarUI", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableXPBarUI = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableXPBarUI
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("ShowPlayerLvls", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.showPlayerLvls = aBoolean,
            (exConfig, extraInfo) -> exConfig.showPlayerLvls
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("ShowMobLvls", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.showMobLvls = aBoolean,
            (exConfig, extraInfo) -> exConfig.showMobLvls
        )
        .add()
        .append(
            new KeyedCodec<Double>("MobLevelMultiplier", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.mobLevelMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.mobLevelMultiplier
        )
        .add()
        .append(
            new KeyedCodec<String>("MobNameplate", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.mobNameplate = aString,
            (exConfig, extraInfo) -> exConfig.mobNameplate
        )
        .add()
        .build();

    private boolean enableXPLossOnDeath = false;

    private double xpLossPercentage = 0.1;

    private double defaultXPGainPercentage = 0.5;

    private boolean enableDefaultXPGainSystem = true;

    private boolean enableLevelDownOnDeath = false;

    private boolean enableAllLevelsLostOnDeath = false;

    private int minLevelForLevelDown = 65;

    private boolean enableLevelChatMsgs = false;

    private boolean disableXPGainNotification = false;

    private boolean enableLevelAndXPTitles = true;

    private boolean enablePartyProXPShareCompat = true;

    private boolean enablePartyPluginXPShareCompat = true;

    private boolean enablePartyXPSplit = false;

    private double partyGroupXPMultiplier = 0.5;

    private boolean killerGetsFullXp = true;

    private boolean enablePartyXPDistanceCheck = false;

    private double partyXPDistanceBlocks = -1;

    private boolean showXPAmountInHUD = false;

    private boolean enableStatLeveling = true;

    private float healthLevelUpMultiplier = 2.2F;

    private float staminaLevelUpMultiplier = 1.35F;

    private float manaLevelUpMultiplier = 1.6F;

    private boolean enableStatHealing = true;

    private String levelUpSound = "SFX_Divine_Respawn";

    private String levelDownSound = "SFX_Divine_Respawn";

    private boolean useConfigXPMappingsInsteadOfHealthDefaults = true;

    private boolean enableLevelUpRewardsConfig = false;

    private boolean disableStatPointGainOnLevelUp = false;

    private int statsPerLevel = 5;

    private boolean useStatsPerLevelMapping = false;

    private float strStatMultiplier = 0.1F;

    private float perStatMultiplier = 0.1F;

    private float vitStatMultiplier = 2.0F;

    private float agiStatMultiplier = 0.25F;

    private float intStatMultiplier = 2.0F;

    private float conStatMultiplier = 0.80F;

    private String levelMode = "NEARBY_PLAYERS_MEAN";

    private int levelVariance = 0;

    private float mobHealthMultiplier = 2.10F;

    private float mobDamageMultiplier = 0.25F;

    private float mobBaseDamage = 0.0F;

    private float mobRangeDamageMultiplier = 0.3F;

    private float mobBaseRangeDamage = 0.0F;

    private boolean enableItemLevelRestriction = false;

    private boolean enableXPBarUI = true;

    private boolean showPlayerLvls = true;

    private boolean showMobLvls = true;

    private double mobLevelMultiplier = 0.35;

    private String mobNameplate = " [Lvl {level}]";

    public GUIConfig() {}

    /**
     * Retrieves the minimum level required to allow a level-down operation in the configuration.
     *
     * @return the minimum level as an integer required for level-down.
     */
    public int getMinLevelForLevelDown() {
        return minLevelForLevelDown;
    }

    /**
     * Retrieves the default percentage of experience points (XP) gained.
     *
     * @return the default XP gain percentage as a double.
     */
    public double getDefaultXPGainPercentage() {
        return defaultXPGainPercentage;
    }

    /**
     * Retrieves the percentage of experience points (XP) lost upon death.
     *
     * @return the XP loss percentage as a double.
     */
    public double getXpLossPercentage() {
        return xpLossPercentage;
    }

    /**
     * Indicates whether the loss of experience points (XP) upon death is enabled.
     *
     * @return {@code true} if XP loss on death is enabled, otherwise {@code false}.
     */
    public boolean isEnableXPLossOnDeath() {
        return enableXPLossOnDeath;
    }

    /**
     * Determines whether the default experience points (XP) gain system is enabled in the configuration.
     *
     * @return {@code true} if the default XP gain system is enabled, otherwise {@code false}.
     */
    public boolean isEnableDefaultXPGainSystem() {
        return enableDefaultXPGainSystem;
    }

    /**
     * Indicates whether the level-down system is enabled upon death.
     *
     * @return {@code true} if level-down on death is enabled, otherwise {@code false}.
     */
    public boolean isEnableLevelDownOnDeath() {
        return enableLevelDownOnDeath;
    }

    /**
     * Indicates whether the configuration is set to enable the loss of all levels upon death.
     *
     * @return {@code true} if all levels are lost upon death, otherwise {@code false}.
     */
    public boolean isEnableAllLevelsLostOnDeath() {
        return enableAllLevelsLostOnDeath;
    }

    /**
     * Determines whether the level-related chat messages are enabled in the configuration.
     *
     * @return {@code true} if level chat messages are enabled, otherwise {@code false}.
     */
    public boolean isEnableLevelChatMsgs() {
        return enableLevelChatMsgs;
    }

    public boolean isDisableXPGainNotification() {
        return disableXPGainNotification;
    }

    /**
     * Determines whether level and experience point (XP) titles are enabled in the configuration.
     *
     * @return {@code true} if level and XP titles are enabled, otherwise {@code false}.
     */
    public boolean isEnableLevelAndXPTitles() {
        return enableLevelAndXPTitles;
    }

    public boolean isEnablePartyProXPShareCompat() {
        return enablePartyProXPShareCompat;
    }

    public boolean isEnablePartyPluginXPShareCompat() {
        return enablePartyPluginXPShareCompat;
    }

    public boolean isEnablePartyXPSplit() {
        return enablePartyXPSplit;
    }

    public double getPartyGroupXPMultiplier() {
        return partyGroupXPMultiplier;
    }

    public boolean isKillerGetsFullXp() {
        return killerGetsFullXp;
    }

    public boolean isEnablePartyXPDistanceCheck() {
        return enablePartyXPDistanceCheck;
    }

    public double getPartyXPDistanceBlocks() {
        return partyXPDistanceBlocks;
    }

    /**
     * Determines whether the experience points (XP) amount is displayed in the Heads-Up Display (HUD).
     *
     * @return {@code true} if the XP amount should be shown in the HUD, otherwise {@code false}.
     */
    public boolean isShowXPAmountInHUD() {
        return showXPAmountInHUD;
    }

    /**
     * Determines whether the stat leveling system is enabled in the configuration.
     *
     * @return {@code true} if stat leveling is enabled, otherwise {@code false}.
     */
    public boolean isEnableStatLeveling() {
        return enableStatLeveling;
    }

    /**
     * Retrieves the multiplier value applied to health upon leveling up.
     *
     * @return the health level-up multiplier as a float.
     */
    public float getHealthLevelUpMultiplier() {
        return healthLevelUpMultiplier;
    }

    /**
     * Retrieves the multiplier value applied to stamina upon leveling up.
     *
     * @return the stamina level-up multiplier as a float.
     */
    public float getStaminaLevelUpMultiplier() {
        return staminaLevelUpMultiplier;
    }

    /**
     * Retrieves the multiplier value applied to mana upon leveling up.
     *
     * @return the mana level-up multiplier as a float.
     */
    public float getManaLevelUpMultiplier() {
        return manaLevelUpMultiplier;
    }

    /**
     * Determines whether the stat healing system is enabled in the configuration.
     *
     * @return {@code true} if stat healing is enabled, otherwise {@code false}.
     */
    public boolean isEnableStatHealing() {
        return enableStatHealing;
    }

    public String getLevelUpSound() {
        return levelUpSound;
    }

    public String getLevelDownSound() {
        return levelDownSound;
    }

    public boolean isUseConfigXPMappingsInsteadOfHealthDefaults() {
        return useConfigXPMappingsInsteadOfHealthDefaults;
    }

    public boolean isEnableLevelUpRewardsConfig() {
        return enableLevelUpRewardsConfig;
    }

    public boolean isDisableStatPointGainOnLevelUp() {
        return disableStatPointGainOnLevelUp;
    }

    public int getStatsPerLevel() {
        return statsPerLevel;
    }

    public boolean isUseStatsPerLevelMapping() {
        return useStatsPerLevelMapping;
    }

    public float getStrStatMultiplier() {
        return strStatMultiplier;
    }

    public float getPerStatMultiplier() {
        return perStatMultiplier;
    }

    public float getVitStatMultiplier() {
        return vitStatMultiplier;
    }

    public float getAgiStatMultiplier() {
        return agiStatMultiplier;
    }

    public float getIntStatMultiplier() {
        return intStatMultiplier;
    }

    public float getConStatMultiplier() {
        return conStatMultiplier;
    }

    public String getLevelMode() {
        return levelMode;
    }

    /**
     * Retrieves the configured level variance used by the leveling system.
     *
     * @return the level variance as an integer.
     */
    public int getLevelVariance() {
        return levelVariance;
    }

    public float getMobHealthMultiplier() {
        return mobHealthMultiplier;
    }

    public float getMobDamageMultiplier() {
        return mobDamageMultiplier;
    }

    public float getMobBaseDamage() {
        return mobBaseDamage;
    }

    public float getMobRangeDamageMultiplier() {
        return mobRangeDamageMultiplier;
    }

    public float getMobBaseRangeDamage() {
        return mobBaseRangeDamage;
    }

    public boolean isEnableItemLevelRestriction() {
        return enableItemLevelRestriction;
    }

    public boolean isEnableXPBarUI() {
        return enableXPBarUI;
    }

    public boolean isShowPlayerLvls() {
        return showPlayerLvls;
    }

    public boolean isShowMobLvls() {
        return showMobLvls;
    }

    public double getMobLevelMultiplier() {
        return mobLevelMultiplier;
    }

    public String getMobNameplate() {
        return mobNameplate;
    }
}
