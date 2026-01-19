package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.azuredoom.levelingcore.config.GUIConfig;

/**
 * Utility class for handling player statistics operations such as modifying health, stamina, and mana. Provides methods
 * to interact with and manipulate a player's statistics using specific multipliers or reset them to default values.
 */
public class StatsUtils {

    private StatsUtils() {}

    private static @NullableDecl World getWorld(Player player) {
        if (player == null)
            return null;
        return player.getWorld();
    }

    private static @NullableDecl EntityStatMap getStatMap(Player player, PlayerRef playerRef) {
        return player.getReference()
            .getStore()
            .getComponent(player.getReference(), EntityStatMap.getComponentType());
    }

    public static void doHealthIncrease(Player player, PlayerRef playerRef, float healthMultiplier) {
        var playerStatMap = StatsUtils.getStatMap(player, playerRef);
        if (playerStatMap == null)
            return;
        var healthIndex = DefaultEntityStatTypes.getHealth();
        var modifier = new StaticModifier(
            Modifier.ModifierTarget.MAX,
            StaticModifier.CalculationType.ADDITIVE,
            healthMultiplier
        );
        var modifierKey = "LevelingCore_health";
        playerStatMap.putModifier(healthIndex, modifierKey, modifier);
    }

    public static void doStaminaIncrease(Player player, PlayerRef playerRef, float staminaMultiplier) {
        var playerStatMap = StatsUtils.getStatMap(player, playerRef);
        if (playerStatMap == null)
            return;
        var staminaIndex = DefaultEntityStatTypes.getStamina();
        var modifier = new StaticModifier(
            Modifier.ModifierTarget.MAX,
            StaticModifier.CalculationType.ADDITIVE,
            staminaMultiplier
        );
        var modifierKey = "LevelingCore_stamina";
        playerStatMap.putModifier(staminaIndex, modifierKey, modifier);
    }

    public static void doManaIncrease(Player player, PlayerRef playerRef, float manaMultiplier) {
        var playerStatMap = StatsUtils.getStatMap(player, playerRef);
        if (playerStatMap == null)
            return;
        var manaIndex = DefaultEntityStatTypes.getMana();
        var modifier = new StaticModifier(
            Modifier.ModifierTarget.MAX,
            StaticModifier.CalculationType.ADDITIVE,
            manaMultiplier
        );
        var modifierKey = "LevelingCore_mana";
        playerStatMap.putModifier(manaIndex, modifierKey, modifier);
    }

    public static void resetStats(Player player, PlayerRef playerRef) {
        var playerStatMap = StatsUtils.getStatMap(player, playerRef);
        if (playerStatMap == null)
            return;

        var healthIndex = DefaultEntityStatTypes.getHealth();
        var staminaIndex = DefaultEntityStatTypes.getStamina();
        var manaIndex = DefaultEntityStatTypes.getMana();

        playerStatMap.resetStatValue(healthIndex);
        playerStatMap.resetStatValue(staminaIndex);
        playerStatMap.resetStatValue(manaIndex);
    }

    public static void applyAllStats(
        Player player,
        PlayerRef playerRef,
        int newLevel,
        Config<GUIConfig> config
    ) {
        StatsUtils.doHealthIncrease(
            player,
            playerRef,
            newLevel * config.get().getHealthLevelUpMultiplier()
        );
        StatsUtils.doStaminaIncrease(
            player,
            playerRef,
            newLevel * config.get().getStaminaLevelUpMultiplier()
        );
        StatsUtils.doManaIncrease(player, playerRef, newLevel * config.get().getManaLevelUpMultiplier());
        if (config.get().isEnableStatHealing())
            healMaxStat(player, playerRef, config);
    }

    private static void healMaxStat(Player player, PlayerRef playerRef, Config<GUIConfig> config) {
        var playerStatMap = StatsUtils.getStatMap(player, playerRef);
        if (playerStatMap == null)
            return;
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getHealth());
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getStamina());
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getMana());
    }
}
