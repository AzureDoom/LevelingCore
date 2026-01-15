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

    private boolean enableLevelAndXPTitles = true;

    private boolean enableSimplePartyXPShareCompat = true;

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
}
