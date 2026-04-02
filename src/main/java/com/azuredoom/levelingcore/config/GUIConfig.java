package com.azuredoom.levelingcore.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.regex.Pattern;

/**
 * Represents the configuration for the Graphical User Interface (GUI) settings, particularly for managing experience
 * points (XP) and leveling mechanics. This class provides options to configure the behavior of experience loss, gain,
 * and level adjustments in different scenarios. The configuration is encoded and decoded using a predefined codec for
 * persistence and retrieval.
 */
public class GUIConfig {

    public static final BuilderCodec<GUIConfig> CODEC = BuilderCodec.builder(GUIConfig.class, GUIConfig::new)
        .append(
            new KeyedCodec<>("EnableXPLossOnDeath", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableXPLossOnDeath = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableXPLossOnDeath
        )
        .add()
        .append(
            new KeyedCodec<>("XPLossPercentage", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.xpLossPercentage = aDouble,
            (exConfig, extraInfo) -> exConfig.xpLossPercentage
        )
        .add()
        .append(
            new KeyedCodec<>("DefaultXPGainPercentage", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.defaultXPGainPercentage = aDouble,
            (exConfig, extraInfo) -> exConfig.defaultXPGainPercentage
        )
        .add()
        .append(
            new KeyedCodec<>("EnableDefaultXPGainSystem", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableDefaultXPGainSystem = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableDefaultXPGainSystem
        )
        .add()
        .append(
            new KeyedCodec<>("EnableLevelDownOnDeath", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLevelDownOnDeath = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLevelDownOnDeath
        )
        .add()
        .append(
            new KeyedCodec<>("EnableAllLevelsLostOnDeath", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableAllLevelsLostOnDeath = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableAllLevelsLostOnDeath
        )
        .add()
        .append(
            new KeyedCodec<>("MinLevelForLevelDown", Codec.INTEGER),
            (exConfig, aInteger, extraInfo) -> exConfig.minLevelForLevelDown = aInteger,
            (exConfig, extraInfo) -> exConfig.minLevelForLevelDown
        )
        .add()
        .append(
            new KeyedCodec<>("EnableLevelChatMsgs", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLevelChatMsgs = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLevelChatMsgs
        )
        .add()
        .append(
            new KeyedCodec<>("DisableXPGainNotification", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.disableXPGainNotification = aBoolean,
            (exConfig, extraInfo) -> exConfig.disableXPGainNotification
        )
        .add()
        .append(
            new KeyedCodec<>("EnableLevelAndXPTitles", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLevelAndXPTitles = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLevelAndXPTitles
        )
        .add()
        .append(
            new KeyedCodec<>("ShowXPAmountInHUD", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.showXPAmountInHUD = aBoolean,
            (exConfig, extraInfo) -> exConfig.showXPAmountInHUD
        )
        .add()
        .append(
            new KeyedCodec<>("EnableStatLeveling", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableStatLeveling = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableStatLeveling
        )
        .add()
        .append(
            new KeyedCodec<>("HealthLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.healthLevelUpMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.healthLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("StaminaLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.staminaLevelUpMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.staminaLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("ManaLevelUpMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.manaLevelUpMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.manaLevelUpMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("EnableStatHealing", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableStatHealing = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableStatHealing
        )
        .add()
        .append(
            new KeyedCodec<>("LevelUpSound", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.levelUpSound = aString,
            (exConfig, extraInfo) -> exConfig.levelUpSound
        )
        .add()
        .append(
            new KeyedCodec<>("LevelDownSound", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.levelDownSound = aString,
            (exConfig, extraInfo) -> exConfig.levelDownSound
        )
        .add()
        .append(
            new KeyedCodec<>("UseConfigXPMappingsInsteadOfHealthDefaults", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.useConfigXPMappingsInsteadOfHealthDefaults = aBoolean,
            (exConfig, extraInfo) -> exConfig.useConfigXPMappingsInsteadOfHealthDefaults
        )
        .add()
        .append(
            new KeyedCodec<>("EnableLevelUpRewardsConfig", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLevelUpRewardsConfig = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLevelUpRewardsConfig
        )
        .add()
        .append(
            new KeyedCodec<>("DisableStatPointGainOnLevelUp", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.disableStatPointGainOnLevelUp = aBoolean,
            (exConfig, extraInfo) -> exConfig.disableStatPointGainOnLevelUp
        )
        .add()
        .append(
            new KeyedCodec<>("StatsPerLevel", Codec.INTEGER),
            (exConfig, aInteger, extraInfo) -> exConfig.statsPerLevel = aInteger,
            (exConfig, extraInfo) -> exConfig.statsPerLevel
        )
        .add()
        .append(
            new KeyedCodec<>("UseStatsPerLevelMapping", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.useStatsPerLevelMapping = aBoolean,
            (exConfig, extraInfo) -> exConfig.useStatsPerLevelMapping
        )
        .add()
        .append(
            new KeyedCodec<>("StrStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.strStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.strStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("PerStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.perStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.perStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("VitStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.vitStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.vitStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("AgiStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.agiStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.agiStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("IntStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.intStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.intStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("ConStatMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.conStatMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.conStatMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("EnablePartyProXPShareCompat", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enablePartyProXPShareCompat = aBoolean,
            (exConfig, extraInfo) -> exConfig.enablePartyProXPShareCompat
        )
        .add()
        .append(
            new KeyedCodec<>("EnablePartyPluginXPShareCompat", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enablePartyPluginXPShareCompat = aBoolean,
            (exConfig, extraInfo) -> exConfig.enablePartyPluginXPShareCompat
        )
        .add()
        .append(
            new KeyedCodec<>("EnablePartyXPSplit", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enablePartyXPSplit = aBoolean,
            (exConfig, extraInfo) -> exConfig.enablePartyXPSplit
        )
        .add()
        .append(
            new KeyedCodec<>("PartyGroupXPMultiplier", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.partyGroupXPMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.partyGroupXPMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("KillerGetsFullXp", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.killerGetsFullXp = aBoolean,
            (exConfig, extraInfo) -> exConfig.killerGetsFullXp
        )
        .add()
        .append(
            new KeyedCodec<>("EnablePartyXPDistanceCheck", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enablePartyXPDistanceCheck = aBoolean,
            (exConfig, extraInfo) -> exConfig.enablePartyXPDistanceCheck
        )
        .add()
        .append(
            new KeyedCodec<>("PartyXPDistanceBlocks", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.partyXPDistanceBlocks = aDouble,
            (exConfig, extraInfo) -> exConfig.partyXPDistanceBlocks
        )
        .add()
        .append(
            new KeyedCodec<>("LevelModeArray", Codec.STRING_ARRAY),
            (exConfig, aStringArray, extraInfo) -> exConfig.levelMode = aStringArray,
            (exConfig, extraInfo) -> exConfig.levelMode
        )
        .add()
        .append(
            new KeyedCodec<>("LevelVariance", Codec.INTEGER),
            (exConfig, anInteger, extraInfo) -> exConfig.levelVariance = anInteger,
            (exConfig, extraInfo) -> exConfig.levelVariance
        )
        .add()
        .append(
            new KeyedCodec<>("MobHealthMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobHealthMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.mobHealthMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("MobDamageMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobDamageMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.mobDamageMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("MobBaseDamage", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobBaseDamage = aFloat,
            (exConfig, extraInfo) -> exConfig.mobBaseDamage
        )
        .add()
        .append(
            new KeyedCodec<>("MobRangeDamageMultiplier", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobRangeDamageMultiplier = aFloat,
            (exConfig, extraInfo) -> exConfig.mobRangeDamageMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("MobBaseRangeDamage", Codec.FLOAT),
            (exConfig, aFloat, extraInfo) -> exConfig.mobBaseRangeDamage = aFloat,
            (exConfig, extraInfo) -> exConfig.mobBaseRangeDamage
        )
        .add()
        .append(
            new KeyedCodec<>("EnableItemLevelRestriction", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableItemLevelRestriction = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableItemLevelRestriction
        )
        .add()
        .append(
            new KeyedCodec<>("EnableXPBarUI", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableXPBarUI = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableXPBarUI
        )
        .add()
        .append(
            new KeyedCodec<>("ShowPlayerLvls", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.showPlayerLvls = aBoolean,
            (exConfig, extraInfo) -> exConfig.showPlayerLvls
        )
        .add()
        .append(
            new KeyedCodec<>("ShowMobLvls", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.showMobLvls = aBoolean,
            (exConfig, extraInfo) -> exConfig.showMobLvls
        )
        .add()
        .append(
            new KeyedCodec<>("MobLevelMultiplier", Codec.DOUBLE),
            (exConfig, aDouble, extraInfo) -> exConfig.mobLevelMultiplier = aDouble,
            (exConfig, extraInfo) -> exConfig.mobLevelMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("MobNameplate", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.mobNameplate = aString,
            (exConfig, extraInfo) -> exConfig.mobNameplate
        )
        .add()
        .append(
            new KeyedCodec<>("BlacklistedMobs", Codec.STRING_ARRAY),
            (exConfig, aStringArray, extraInfo) -> exConfig.blacklistedMobs = aStringArray,
            (exConfig, extraInfo) -> exConfig.blacklistedMobs
        )
        .add()
        .append(
            new KeyedCodec<>("EnableItemStatRequirement", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableItemStatRequirement = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableItemStatRequirement
        )
        .add()
        .append(
            new KeyedCodec<>("BlacklistedNameplateMobs", Codec.STRING_ARRAY),
            (exConfig, aStringArray, extraInfo) -> exConfig.blacklistedNameplateMobs = aStringArray,
            (exConfig, extraInfo) -> exConfig.blacklistedNameplateMobs
        )
        .add()
        .append(
            new KeyedCodec<>("EnableLeaderboardScreen", Codec.BOOLEAN),
            (exConfig, aBoolean, extraInfo) -> exConfig.enableLeaderboardScreen = aBoolean,
            (exConfig, extraInfo) -> exConfig.enableLeaderboardScreen
        )
        .add()
        .append(
            new KeyedCodec<>("RankOneRankColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.rankOneRankColor = aString,
            (exConfig, extraInfo) -> exConfig.rankOneRankColor
        )
        .add()
        .append(
            new KeyedCodec<>("RankOneNameColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.rankOneNameColor = aString,
            (exConfig, extraInfo) -> exConfig.rankOneNameColor
        )
        .add()
        .append(
            new KeyedCodec<>("RankTwoRankColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.rankTwoRankColor = aString,
            (exConfig, extraInfo) -> exConfig.rankTwoRankColor
        )
        .add()
        .append(
            new KeyedCodec<>("RankTwoNameColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.rankTwoNameColor = aString,
            (exConfig, extraInfo) -> exConfig.rankTwoNameColor
        )
        .add()
        .append(
            new KeyedCodec<>("RankThreeRankColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.rankThreeRankColor = aString,
            (exConfig, extraInfo) -> exConfig.rankThreeRankColor
        )
        .add()
        .append(
            new KeyedCodec<>("RankThreeNameColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.rankThreeNameColor = aString,
            (exConfig, extraInfo) -> exConfig.rankThreeNameColor
        )
        .add()
        .append(
            new KeyedCodec<>("ViewersRankColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.viewersRankColor = aString,
            (exConfig, extraInfo) -> exConfig.viewersRankColor
        )
        .add()
        .append(
            new KeyedCodec<>("ViewersNameColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.viewersNameColor = aString,
            (exConfig, extraInfo) -> exConfig.viewersNameColor
        )
        .add()
        .append(
            new KeyedCodec<>("DefaultRankColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.defaultRankColor = aString,
            (exConfig, extraInfo) -> exConfig.defaultRankColor
        )
        .add()
        .append(
            new KeyedCodec<>("DefaultNameColor", Codec.STRING),
            (exConfig, aString, extraInfo) -> exConfig.defaultNameColor = aString,
            (exConfig, extraInfo) -> exConfig.defaultNameColor
        )
        .add()
        .append(
            new KeyedCodec<>("NormalMobHealthMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.normalMobHealthMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.normalMobHealthMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("NormalMobDamageMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.normalMobDamageMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.normalMobDamageMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("NormalMobDamageThreshold", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.normalMobDamageThreshold = aString,
            (exConfig, extraInfo) -> exConfig.normalMobDamageThreshold
        )
        .add()
        .append(
            new KeyedCodec<>("NormalMobXPMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.normalMobXPMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.normalMobXPMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("EliteMobHealthMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.eliteMobHealthMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.eliteMobHealthMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("EliteMobDamageMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.eliteMobDamageMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.eliteMobDamageMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("EliteMobDamageThreshold", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.eliteMobDamageThreshold = aString,
            (exConfig, extraInfo) -> exConfig.eliteMobDamageThreshold
        )
        .add()
        .append(
            new KeyedCodec<>("EliteMobXPMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.eliteMobXPMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.eliteMobXPMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("BossMobHealthMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.bossMobHealthMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.bossMobHealthMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("BossMobDamageMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.bossMobDamageMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.bossMobDamageMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("BossMobDamageThreshold", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.bossMobDamageThreshold = aString,
            (exConfig, extraInfo) -> exConfig.bossMobDamageThreshold
        )
        .add()
        .append(
            new KeyedCodec<>("BossMobXPMultiplier", Codec.FLOAT),
            (exConfig, aString, extraInfo) -> exConfig.bossMobXPMultiplier = aString,
            (exConfig, extraInfo) -> exConfig.bossMobXPMultiplier
        )
        .add()
        .append(
            new KeyedCodec<>("EliteMobs", Codec.STRING_ARRAY),
            (exConfig, aStringArray, extraInfo) -> exConfig.eliteMobs = aStringArray,
            (exConfig, extraInfo) -> exConfig.eliteMobs
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

    private String[] levelMode = { "NEARBY_PLAYERS_MEAN", "INSTANCE" };

    private int levelVariance = 5;

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

    private boolean enableItemStatRequirement = false;

    private String[] blacklistedMobs = {
        "Arrow_Crossbow_Signature",
        "Arrow_Crude",
        "Arrow_Fire",
        "Arrow_Frost",
        "Arrow_Iron",
        "Arrow_Ricochet",
        "Arrow_Ricochet_Signature",
        "Arrow_Shortbow_Signature",
        "Arrow_Vamp",
        "Arrow_Vamp_Signature",
        "Axe_Bone",
        "Axe_Stone_Trork",
        "Boat",
        "Bomb",
        "Bomb_Fire_Goblin",
        "Bomb_Fire_Goblin_Dud",
        "Bomb_Large_Fire_Goblin",
        "Bomb_Popberry",
        "Bomb_Potion_Poison",
        "Boy_Trail",
        "Bullet_Blunderbuss",
        "Cactee",
        "Cactee_Spike",
        "Crossbow_Turret",
        "Crossbow_Turret_Item_Projectile",
        "Dagger_Adamantite",
        "Dagger_Bone",
        "Dagger_Bronze",
        "Dagger_Bronze_Ancient",
        "Dagger_Cobalt",
        "Dagger_Copper",
        "Dagger_Crude",
        "Dagger_Doomed",
        "Dagger_Fang_Doomed",
        "Dagger_Iron",
        "Dagger_Mithril",
        "Dagger_Onyxium",
        "Dagger_Stone_Trork",
        "Dagger_Thorium",
        "Debug",
        "Deployable_Fire_Trap",
        "Deployable_Fire_Trap_Preview",
        "Egg",
        "Eye_Void_Blast",
        "Feran_Civilian",
        "Feran_Windwalker_Wind_Burst",
        "Feran_Windwalker_Wind_Vortex",
        "Fireball",
        "Healing_Totem",
        "Healing_Totem_Projectile",
        "Ice_Ball",
        "Ice_Bolt",
        "Ingredient_Poop",
        "Klops_Gentleman",
        "Klops_Merchant",
        "Klops_Miner",
        "Kunai",
        "Kweebec_Rootling",
        "Kweebec_Sapling",
        "Kweebec_Sapling_Brown",
        "Kweebec_Sapling_Christmas_Blue",
        "Kweebec_Sapling_Christmas_Green",
        "Kweebec_Sapling_Christmas_Pink",
        "Kweebec_Sapling_Green",
        "Kweebec_Sapling_HardHat",
        "Kweebec_Sapling_Orange",
        "Kweebec_Sapling_Pink",
        "Kweebec_Sapling_Razorleaf",
        "Kweebec_Sapling_Red",
        "Kweebec_Sapling_Treesinger",
        "Kweebec_Sapling_Yellow",
        "Kweebec_Seedling",
        "Kweebec_Sproutling",
        "Kweebec_Sproutling_Blue",
        "Kweebec_Sproutling_Lime",
        "Mannequin",
        "Minecart",
        "Model_Bee_Swarm",
        "Model_Deer_Stag",
        "NPC_Elf",
        "NPC_Path_Marker",
        "NPC_Santa",
        "NPC_Sound_Shoe",
        "NPC_Spawn_Marker",
        "Objective_Location_Marker",
        "Player",
        "PlayerTestModel_G",
        "PlayerTestModel_V",
        "Projectile",
        "Reindeer_Christmas",
        "Rubble_Aqua",
        "Rubble_Basalt",
        "Rubble_Calcite",
        "Rubble_Default",
        "Rubble_Ice",
        "Rubble_Marble",
        "Rubble_Quartzite",
        "Rubble_Sandstone",
        "Rubble_Sandstone_Red",
        "Rubble_Sandstone_White",
        "Rubble_Shale",
        "Rubble_Slate",
        "Rubble_Stone",
        "Rubble_Stone_Mossy",
        "Rubble_Volcanic",
        "Scarak_Seeker_Spitball",
        "Showcase_Cobalt_Gear",
        "Showcase_Copper_Gear",
        "Showcase_Iron_Gear",
        "Showcase_Iron_TargetDummy_1",
        "Showcase_Mannequin_Heal",
        "Showcase_Mannequin_Inv_Portal",
        "Showcase_Mannequin_Inv_Sphere",
        "Showcase_Mannequin_Lightning",
        "Showcase_Mannequin_Sitting",
        "Showcase_Onyxium_Gear",
        "Showcase_Prisma_Gear",
        "Showcase_Skeleton_Assasin",
        "Showcase_Skeleton_Dead",
        "Showcase_Skeleton_Guard",
        "Showcase_Skeleton_Tank",
        "Showcase_Wooden_Gear",
        "Skeleton_Mage_Corruption_Orb",
        "Slothian",
        "Slothian_Elder",
        "Slothian_Kid",
        "Slothian_Monk",
        "Slothian_Scout",
        "Slothian_Villager",
        "Slothian_Warrior",
        "Slowness_Totem",
        "Slowness_Totem_Projectile",
        "Spear_Adamantite",
        "Spear_Adamantite_Saurian",
        "Spear_Bone",
        "Spear_Bronze",
        "Spear_Cobalt",
        "Spear_Copper",
        "Spear_Crude",
        "Spear_Double_Incandescent",
        "Spear_Iron",
        "Spear_Leaf",
        "Spear_Mithril",
        "Spear_Onyxium",
        "Spear_Scrap",
        "Spear_Stone_Trork",
        "Spear_Thorium",
        "Spear_Tribal",
        "Spirit_Ember",
        "Spirit_Frost",
        "Spirit_Root",
        "Spirit_Thunder",
        "Sword_Charged_Test",
        "Tank",
        "Test_Platform",
        "Tornado",
        "Trash",
        "Tuluk",
        "Tuluk_Fisherman",
        "Warp",
        "Warrior_Quest",
        "Wraith_Lantern" };

    private String[] blacklistedNameplateMobs = {
        "HyCitizens_*"
    };

    private boolean enableLeaderboardScreen = true;

    private String rankOneRankColor = "#FFD700";

    private String rankOneNameColor = "#FFF4B0";

    private String rankTwoRankColor = "#C0C0C0";

    private String rankTwoNameColor = "#F2F2F2";

    private String rankThreeRankColor = "#CD7F32";

    private String rankThreeNameColor = "#FFE1C4";

    private String viewersRankColor = "#9fe3ff";

    private String viewersNameColor = "#ffffff";

    private String defaultRankColor = "#FFD700";

    private String defaultNameColor = "#ffffff";

    private float normalMobHealthMultiplier = 1.0F;

    private float normalMobDamageMultiplier = 1.0F;

    private float normalMobDamageThreshold = 1.0F;

    private float normalMobXPMultiplier = 1.0F;

    private float eliteMobHealthMultiplier = 1.75F;

    private float eliteMobDamageMultiplier = 1.15F;

    private float eliteMobDamageThreshold = 0.8F;

    private float eliteMobXPMultiplier = 1.4F;

    private float bossMobHealthMultiplier = 2.0F;

    private float bossMobDamageMultiplier = 1.5F;

    private float bossMobDamageThreshold = 0.7F;

    private float bossMobXPMultiplier = 1.8F;

    private String[] eliteMobs = {
        "Crawler_Void",
        "Dragon_Void",
        "Eye_Void",
        "Golem_Guardian_Void",
        "Larva_Void",
        "Necromancer_Void",
        "Spawn_Void",
        "Spectre_Void" };

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
     * Determines whether the notification for experience points (XP) gain is disabled in the configuration.
     *
     * @return {@code true} if the XP gain notification is disabled, otherwise {@code false}.
     */
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
     * Determines whether compatibility for party-based experience points (XP) sharing using the "Pro" party system is
     * enabled in the configuration.
     *
     * @return {@code true} if the party XP sharing compatibility for the "Pro" system is enabled, otherwise
     *         {@code false}.
     */
    public boolean isEnablePartyProXPShareCompat() {
        return enablePartyProXPShareCompat;
    }

    /**
     * Determines whether compatibility for party-based experience points (XP) sharing using a specific plugin-based
     * party system is enabled in the configuration.
     *
     * @return {@code true} if the party XP sharing compatibility for the plugin-based system is enabled, otherwise
     *         {@code false}.
     */
    public boolean isEnablePartyPluginXPShareCompat() {
        return enablePartyPluginXPShareCompat;
    }

    /**
     * Determines whether the party experience split feature is enabled.
     *
     * @return true if the party experience split feature is enabled; false otherwise.
     */
    public boolean isEnablePartyXPSplit() {
        return enablePartyXPSplit;
    }

    /**
     * Retrieves the experience points (XP) multiplier for the party group.
     *
     * @return The XP multiplier applied to the party group as a double.
     */
    public double getPartyGroupXPMultiplier() {
        return partyGroupXPMultiplier;
    }

    /**
     * Determines if the killer receives full experience points (XP).
     *
     * @return true if the killer gets full XP, false otherwise.
     */
    public boolean isKillerGetsFullXp() {
        return killerGetsFullXp;
    }

    /**
     * Determines if the party experience distance check is enabled.
     *
     * @return true if the party experience distance check is enabled, false otherwise.
     */
    public boolean isEnablePartyXPDistanceCheck() {
        return enablePartyXPDistanceCheck;
    }

    /**
     * Retrieves the distance in blocks within which party members can share experience points.
     *
     * @return The maximum distance in blocks within which party experience sharing is effective.
     */
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

    /**
     * Gets the sound identifier played when a player levels up.
     *
     * @return the sound asset ID used for level-up events
     */
    public String getLevelUpSound() {
        return levelUpSound;
    }

    /**
     * Gets the sound identifier played when a player loses a level.
     *
     * @return the sound asset ID used for level-down events
     */
    public String getLevelDownSound() {
        return levelDownSound;
    }

    /**
     * Determines whether XP-to-level mappings defined in the configuration should be used instead of the default
     * health-based XP calculations.
     *
     * @return true if configuration XP mappings should override default behavior
     */
    public boolean isUseConfigXPMappingsInsteadOfHealthDefaults() {
        return useConfigXPMappingsInsteadOfHealthDefaults;
    }

    /**
     * Determines whether level-up rewards should be loaded and applied from the configuration file.
     *
     * @return true if level-up rewards defined in config are enabled
     */
    public boolean isEnableLevelUpRewardsConfig() {
        return enableLevelUpRewardsConfig;
    }

    /**
     * Determines whether stat points are prevented from being granted automatically when a player levels up.
     *
     * @return true if stat point gain on level-up is disabled
     */
    public boolean isDisableStatPointGainOnLevelUp() {
        return disableStatPointGainOnLevelUp;
    }

    /**
     * Gets the number of stat points granted per level-up when using a fixed stat allocation model.
     *
     * @return the number of stat points awarded each level
     */
    public int getStatsPerLevel() {
        return statsPerLevel;
    }

    /**
     * Determines whether a configured mapping should determine stat points per level instead of a fixed value.
     *
     * @return true if stat-per-level mapping is enabled
     */
    public boolean isUseStatsPerLevelMapping() {
        return useStatsPerLevelMapping;
    }

    /**
     * Gets the multiplier applied to the Strength stat. This affects how much benefit each Strength point provides.
     *
     * @return the strength stat multiplier
     */
    public float getStrStatMultiplier() {
        return strStatMultiplier;
    }

    /**
     * Gets the multiplier applied to the Perception stat. This modifies the effectiveness of Perception points.
     *
     * @return the perception stat multiplier
     */
    public float getPerStatMultiplier() {
        return perStatMultiplier;
    }

    /**
     * Gets the multiplier applied to the Vitality stat.
     *
     * @return the vitality stat multiplier
     */
    public float getVitStatMultiplier() {
        return vitStatMultiplier;
    }

    /**
     * Gets the multiplier applied to the Agility stat.
     *
     * @return the agility stat multiplier
     */
    public float getAgiStatMultiplier() {
        return agiStatMultiplier;
    }

    /**
     * Gets the multiplier applied to the Intelligence stat.
     *
     * @return the intelligence stat multiplier
     */
    public float getIntStatMultiplier() {
        return intStatMultiplier;
    }

    /**
     * Gets the multiplier applied to the Constitution stat.
     *
     * @return the constitution stat multiplier
     */
    public float getConStatMultiplier() {
        return conStatMultiplier;
    }

    /**
     * Retrieves the current level mode configuration.
     *
     * @return An array of strings representing the level mode.
     */
    public String[] getLevelMode() {
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

    /**
     * Gets the multiplier applied to mob maximum health. This scales the base health of mobs based on the configured
     * value.
     *
     * @return the mob health multiplier
     */
    public float getMobHealthMultiplier() {
        return mobHealthMultiplier;
    }

    /**
     * Gets the multiplier applied to mob melee damage. This modifies how much damage mobs deal with close-range
     * attacks.
     *
     * @return the mob melee damage multiplier
     */
    public float getMobDamageMultiplier() {
        return mobDamageMultiplier;
    }

    /**
     * Gets the base melee damage value used for mobs. This value may be scaled further by damage multipliers.
     *
     * @return the base melee damage applied to mobs
     */
    public float getMobBaseDamage() {
        return mobBaseDamage;
    }

    /**
     * Gets the multiplier applied to mob ranged damage. This affects damage from projectiles or other ranged attacks.
     *
     * @return the mob ranged damage multiplier
     */
    public float getMobRangeDamageMultiplier() {
        return mobRangeDamageMultiplier;
    }

    /**
     * Gets the base-ranged damage value used for mobs. This value may be scaled further by the ranged damage
     * multiplier.
     *
     * @return the base ranged damage applied to mobs
     */
    public float getMobBaseRangeDamage() {
        return mobBaseRangeDamage;
    }

    /**
     * Determines whether item level restrictions are enabled. When enabled, players may be prevented from equipping or
     * using items that require a higher level than their current level.
     *
     * @return true if item level restrictions are enabled
     */
    public boolean isEnableItemLevelRestriction() {
        return enableItemLevelRestriction;
    }

    /**
     * Determines whether the experience bar UI is displayed to players.
     *
     * @return true if the XP bar interface should be visible
     */
    public boolean isEnableXPBarUI() {
        return enableXPBarUI;
    }

    /**
     * Determines whether player levels are displayed on nameplates.
     *
     * @return true if player levels should be shown
     */
    public boolean isShowPlayerLvls() {
        return showPlayerLvls;
    }

    /**
     * Determines whether mob levels are displayed in on mob nameplates.
     *
     * @return true if mob levels should be shown
     */
    public boolean isShowMobLvls() {
        return showMobLvls;
    }

    /**
     * Gets the multiplier used to scale mob levels.
     *
     * @return the mob level scaling multiplier
     */
    public double getMobLevelMultiplier() {
        return mobLevelMultiplier;
    }

    /**
     * Gets the format string used for mob nameplates. This may include placeholders for level, name, or other values.
     *
     * @return the configured mob nameplate format
     */
    public String getMobNameplate() {
        return mobNameplate;
    }

    /**
     * Gets the list of mob entity identifiers that are excluded from level scaling, nameplates, or other level-related
     * systems.
     *
     * @return an array of blacklisted mob IDs
     */
    public String[] getBlacklistedMobs() {
        return blacklistedMobs;
    }

    /**
     * Checks if the provided mob type ID is blacklisted.
     *
     * @param npcTypeId the unique identifier of the mob type to be checked
     * @return {@code true} if the mob type ID matches any pattern in the blacklist, {@code false} otherwise
     */
    public boolean isMobBlacklisted(String npcTypeId) {
        return matchesAnyPattern(blacklistedMobs, npcTypeId);
    }

    /**
     * Determines whether the item stat requirement feature is enabled.
     *
     * @return true if the item stat requirement is enabled, false otherwise.
     */
    public boolean isEnableItemStatRequirement() {
        return enableItemStatRequirement;
    }

    /**
     * Retrieves the list of mob names that are blacklisted from having nameplates.
     *
     * @return An array of strings containing the names of blacklisted mobs.
     */
    public String[] getBlacklistedNameplateMobs() {
        return blacklistedNameplateMobs;
    }

    /**
     * Determines if the leaderboard screen feature is enabled.
     *
     * @return true if the leaderboard screen is enabled; false otherwise.
     */
    public boolean isEnableLeaderboardScreen() {
        return enableLeaderboardScreen;
    }

    /**
     * Retrieves the color associated with the rank one rank.
     *
     * @return the color of the rank one rank as a String
     */
    public String getRankOneRankColor() {
        return rankOneRankColor;
    }

    /**
     * Retrieves the color associated with the rank one name.
     *
     * @return A string representing the color of the rank one name.
     */
    public String getRankOneNameColor() {
        return rankOneNameColor;
    }

    /**
     * Retrieves the color associated with rank two of a rank.
     *
     * @return a String representing the color associated with rank two.
     */
    public String getRankTwoRankColor() {
        return rankTwoRankColor;
    }

    /**
     * Retrieves the color associated with the rank two name.
     *
     * @return a string representing the color assigned to the rank two name.
     */
    public String getRankTwoNameColor() {
        return rankTwoNameColor;
    }

    /**
     * Retrieves the color associated with rank three.
     *
     * @return the color of rank three as a String.
     */
    public String getRankThreeRankColor() {
        return rankThreeRankColor;
    }

    /**
     * Retrieves the color associated with rank three names.
     *
     * @return A string representing the color for rank three names.
     */
    public String getRankThreeNameColor() {
        return rankThreeNameColor;
    }

    /**
     * Retrieves the color associated with the viewer's rank.
     *
     * @return a string representing the color of the viewer's rank.
     */
    public String getViewersRankColor() {
        return viewersRankColor;
    }

    /**
     * Retrieves the color associated with the viewer's name.
     *
     * @return A string representing the color of the viewer's name.
     */
    public String getViewersNameColor() {
        return viewersNameColor;
    }

    /**
     * Retrieves the default rank color.
     *
     * @return The default rank color as a String.
     */
    public String getDefaultRankColor() {
        return defaultRankColor;
    }

    /**
     * Retrieves the default name color.
     *
     * @return the default name color as a String
     */
    public String getDefaultNameColor() {
        return defaultNameColor;
    }

    public float getNormalMobHealthMultiplier() {
        return normalMobHealthMultiplier;
    }

    public float getNormalMobDamageMultiplier() {
        return normalMobDamageMultiplier;
    }

    public float getNormalMobDamageThreshold() {
        return normalMobDamageThreshold;
    }

    public float getNormalMobXPMultiplier() {
        return normalMobXPMultiplier;
    }

    public float getEliteMobHealthMultiplier() {
        return eliteMobHealthMultiplier;
    }

    public float getEliteMobDamageMultiplier() {
        return eliteMobDamageMultiplier;
    }

    public float getEliteMobDamageThreshold() {
        return eliteMobDamageThreshold;
    }

    public float getEliteMobXPMultiplier() {
        return eliteMobXPMultiplier;
    }

    public float getBossMobHealthMultiplier() {
        return bossMobHealthMultiplier;
    }

    public float getBossMobDamageMultiplier() {
        return bossMobDamageMultiplier;
    }

    public float getBossMobDamageThreshold() {
        return bossMobDamageThreshold;
    }

    public float getBossMobXPMultiplier() {
        return bossMobXPMultiplier;
    }

    /**
     * Checks whether the given mob id matches any configured nameplate blacklist entry. Supports exact matches and
     * wildcard patterns using '*'.
     *
     * @param npcTypeId the mob type id to check
     * @return true if the mob is blacklisted from showing a nameplate
     */
    public boolean isNameplateMobBlacklisted(String npcTypeId) {
        return matchesAnyPattern(blacklistedNameplateMobs, npcTypeId);
    }

    public boolean isEliteMob(String npcTypeId) {
        return matchesAnyPattern(eliteMobs, npcTypeId);
    }

    /**
     * Checks if the given value matches any of the patterns in the provided array.
     *
     * @param patterns an array of string patterns to match against, with possible use of wildcards
     * @param value    the string value to be checked for matches against the patterns
     * @return true if the value matches at least one pattern, false otherwise
     */
    private static boolean matchesAnyPattern(String[] patterns, String value) {
        if (value == null || patterns == null) {
            return false;
        }

        for (var pattern : patterns) {
            if (pattern == null) {
                continue;
            }

            var trimmed = pattern.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (wildcardMatch(trimmed, value)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the provided value matches the given pattern, where the pattern can include wildcard characters
     * represented by asterisks (*) to match any sequence of characters.
     *
     * @param pattern the input pattern containing literal characters and wildcard symbols (*)
     * @param value   the input string to be matched against the pattern
     * @return true if the value matches the pattern, otherwise false
     */
    private static boolean wildcardMatch(String pattern, String value) {
        if (pattern.indexOf('*') < 0) {
            return pattern.equals(value);
        }

        var parts = pattern.split("\\*", -1);
        var regex = new StringBuilder("^");

        for (var i = 0; i < parts.length; i++) {
            regex.append(Pattern.quote(parts[i]));
            if (i < parts.length - 1) {
                regex.append(".*");
            }
        }

        regex.append("$");
        return Pattern.compile(regex.toString()).matcher(value).matches();
    }
}
