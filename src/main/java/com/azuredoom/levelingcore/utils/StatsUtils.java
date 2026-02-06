package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import com.azuredoom.levelingcore.config.GUIConfig;

/**
 * Utility class for handling player statistics operations such as modifying health, stamina, and mana. Provides methods
 * to interact with and manipulate a player's statistics using specific multipliers or reset them to default values.
 */
public class StatsUtils {

    private StatsUtils() {}

    private static @NullableDecl EntityStatMap getStatMap(@NonNullDecl Store<EntityStore> store, Player player) {
        if (store == null || player == null || player.getReference() == null) {
            return null;
        }
        return store.getComponent(player.getReference(), EntityStatMap.getComponentType());
    }

    public static void doHealthIncrease(Store<EntityStore> store, Player player, float healthMultiplier) {
        var playerStatMap = StatsUtils.getStatMap(store, player);
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

    public static void doStaminaIncrease(Store<EntityStore> store, Player player, float staminaMultiplier) {
        var playerStatMap = StatsUtils.getStatMap(store, player);
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

    public static void doManaIncrease(Store<EntityStore> store, Player player, float manaMultiplier) {
        var playerStatMap = StatsUtils.getStatMap(store, player);
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

    public static void resetStats(Store<EntityStore> store, Player player) {
        var playerStatMap = StatsUtils.getStatMap(store, player);
        if (playerStatMap == null)
            return;

        var healthIndex = DefaultEntityStatTypes.getHealth();
        var staminaIndex = DefaultEntityStatTypes.getStamina();
        var manaIndex = DefaultEntityStatTypes.getMana();

        playerStatMap.removeModifier(healthIndex, "LevelingCore_health_stat");
        playerStatMap.removeModifier(staminaIndex, "LevelingCore_stamina_stat");
        playerStatMap.removeModifier(manaIndex, "LevelingCore_mana_stat");
        playerStatMap.removeModifier(healthIndex, "LevelingCore_health");
        playerStatMap.removeModifier(staminaIndex, "LevelingCore_stamina");
        playerStatMap.removeModifier(manaIndex, "LevelingCore_mana");
        playerStatMap.resetStatValue(healthIndex);
        playerStatMap.resetStatValue(staminaIndex);
        playerStatMap.resetStatValue(manaIndex);
    }

    public static void applyAllStats(
        Store<EntityStore> store,
        Player player,
        int newLevel,
        Config<GUIConfig> config
    ) {
        StatsUtils.doHealthIncrease(
            store,
            player,
            newLevel * config.get().getHealthLevelUpMultiplier()
        );
        StatsUtils.doStaminaIncrease(
            store,
            player,
            newLevel * config.get().getStaminaLevelUpMultiplier()
        );
        StatsUtils.doManaIncrease(store, player, newLevel * config.get().getManaLevelUpMultiplier());
        if (config.get().isEnableStatHealing())
            healMaxStat(store, player);
    }

    private static void healMaxStat(Store<EntityStore> store, Player player) {
        var playerStatMap = StatsUtils.getStatMap(store, player);
        if (playerStatMap == null)
            return;
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getHealth());
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getStamina());
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getMana());
    }

    public static String formatXp(double value) {
        if (value >= 1_000_000_000)
            return String.format("%.2fB", value / 1_000_000_000);
        if (value >= 1_000_000)
            return String.format("%.2fM", value / 1_000_000);
        if (value >= 500_000)
            return String.format("%.2fk", value / 1_000);
        return String.format("%.0f", value);
    }
}
