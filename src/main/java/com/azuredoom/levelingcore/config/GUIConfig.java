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
            (exConfig, aDouble, extraInfo) -> exConfig.enableXPLossOnDeath = aDouble,
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
            (exConfig, aDouble, extraInfo) -> exConfig.enableDefaultXPGainSystem = aDouble,
            (exConfig, extraInfo) -> exConfig.enableDefaultXPGainSystem
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableLevelDownOnDeath", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableLevelDownOnDeath = aDouble,
            (exConfig, extraInfo) -> exConfig.enableLevelDownOnDeath
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableAllLevelsLostOnDeath", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableAllLevelsLostOnDeath = aDouble,
            (exConfig, extraInfo) -> exConfig.enableAllLevelsLostOnDeath
        )
        .add()
        .append(
            new KeyedCodec<Integer>("MinLevelForLevelDown", Codec.INTEGER),
            (exConfig, aDouble, extraInfo) -> exConfig.minLevelForLevelDown = aDouble,
            (exConfig, extraInfo) -> exConfig.minLevelForLevelDown
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableLevelChatMsgs", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableLevelChatMsgs = aDouble,
            (exConfig, extraInfo) -> exConfig.enableLevelChatMsgs
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableXPChatMsgs", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableXPChatMsgs = aDouble,
            (exConfig, extraInfo) -> exConfig.enableXPChatMsgs
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("DisableXPGainNotification", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.disableXPGainNotification = aDouble,
            (exConfig, extraInfo) -> exConfig.disableXPGainNotification
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableLevelAndXPTitles", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableLevelAndXPTitles = aDouble,
            (exConfig, extraInfo) -> exConfig.enableLevelAndXPTitles
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableSimplePartyXPShareCompat", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableSimplePartyXPShareCompat = aDouble,
            (exConfig, extraInfo) -> exConfig.enableSimplePartyXPShareCompat
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("ShowXPAmountInHUD", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.showXPAmountInHUD = aDouble,
            (exConfig, extraInfo) -> exConfig.showXPAmountInHUD
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableStatLeveling", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableStatLeveling = aDouble,
            (exConfig, extraInfo) -> exConfig.enableStatLeveling
        )
        .add()
        .append(
            new KeyedCodec<Float>("HealthLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aDouble, extraInfo) -> exConfig.healthLevelUpMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.healthLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("StaminaLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aDouble, extraInfo) -> exConfig.staminaLevelUpMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.staminaLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("ManaLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aDouble, extraInfo) -> exConfig.manaLevelUpMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.manaLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableStatHealing", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableStatHealing = aDouble,
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
            (exConfig, aDouble, extraInfo) -> exConfig.useConfigXPMappingsInsteadOfHealthDefaults = aDouble,
            (exConfig, extraInfo) -> exConfig.useConfigXPMappingsInsteadOfHealthDefaults
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("EnableLevelUpRewardsConfig", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.enableLevelUpRewardsConfig = aDouble,
            (exConfig, extraInfo) -> exConfig.enableLevelUpRewardsConfig
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("DisableStatPointGainOnLevelUp", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.disableStatPointGainOnLevelUp = aDouble,
            (exConfig, extraInfo) -> exConfig.disableStatPointGainOnLevelUp
        )
        .add()
        .append(
            new KeyedCodec<Integer>("StatsPerLevel", Codec.INTEGER),
            (exConfig, aDouble, extraInfo) -> exConfig.statsPerLevel = aDouble,
            (exConfig, extraInfo) -> exConfig.statsPerLevel
        )
        .add()
        .append(
            new KeyedCodec<Boolean>("UseStatsPerLevelMapping", Codec.BOOLEAN),
            (exConfig, aDouble, extraInfo) -> exConfig.useStatsPerLevelMapping = aDouble,
            (exConfig, extraInfo) -> exConfig.useStatsPerLevelMapping
        )
        .add()
        .append(
            new KeyedCodec<Float>("StrStatMultiplier", Codec.FLOAT),
            (exConfig, aDouble, extraInfo) -> exConfig.strStatMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.strStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("PerStatMultiplier", Codec.FLOAT),
            (exConfig, aDouble, extraInfo) -> exConfig.perStatMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.perStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("VitStatMultiplier", Codec.FLOAT),
            (exConfig, aDouble, extraInfo) -> exConfig.vitStatMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.vitStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("AgiStatMultiplier", Codec.FLOAT),
            (exConfig, aDouble, extraInfo) -> exConfig.agiStatMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.agiStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<Float>("IntStatMultiplier", Codec.FLOAT),
            (exConfig, aDouble, extraInfo) -> exConfig.intStatMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.intStatMultiplier
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

    private boolean enableXPChatMsgs = true;

    private boolean disableXPGainNotification = false;

    private boolean enableLevelAndXPTitles = true;

    private boolean enableSimplePartyXPShareCompat = true;

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

    /**
     * Determines whether the experience points (XP) related chat messages are enabled in the configuration.
     *
     * @return {@code true} if XP chat messages are enabled, otherwise {@code false}.
     */
    public boolean isEnableXPChatMsgs() {
        return enableXPChatMsgs;
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

    /**
     * Determines whether the simple party experience points (XP) share compatibility is enabled in the configuration.
     *
     * @return {@code true} if simple party XP share compatibility is enabled, otherwise {@code false}.
     */
    public boolean isEnableSimplePartyXPShareCompat() {
        return enableSimplePartyXPShareCompat;
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
}
