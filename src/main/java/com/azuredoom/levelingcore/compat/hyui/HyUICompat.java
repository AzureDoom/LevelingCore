package com.azuredoom.levelingcore.compat.hyui;

import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.utils.StatsUtils;

public class HyUICompat {

    private HyUICompat() {}

    public static void showStats(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref) {
        var levelService = LevelingCoreApi.getLevelServiceIfPresent().orElse(null);
        if (levelService == null)
            return;
        var uuid = playerRef.getUuid();
        var currentLevel = levelService.getLevel(uuid);
        var config = LevelingCore.getConfig();
        var currentXp = levelService.getXp(uuid);
        var xpForNextLevel = levelService.getXpForLevel(levelService.getLevel(uuid) + 1);
        var percentage = (float) currentXp / xpForNextLevel * 100;
        var playerStatMap = store.ensureAndGetComponent(playerRef.getReference(), EntityStatMap.getComponentType());
        var healthIndex = DefaultEntityStatTypes.getHealth();
        var staminaIndex = DefaultEntityStatTypes.getStamina();
        var oxygenIndex = DefaultEntityStatTypes.getOxygen();
        var manaIndex = DefaultEntityStatTypes.getMana();
        var healthModifierKey = "LevelingCore_health_stat";
        var staminaModifierKey = "LevelingCore_stamina_stat";
        var oxygenModifierKey = "LevelingCore_oxygen_stat";
        var manaModifierKey = "LevelingCore_mana_stat";

        var template = new TemplateProcessor()
            .setVariable(
                "playerHealth",
                String.format("%.0f", StatsUtils.formatXp(playerStatMap.get(healthIndex).get()))
            )
            .setVariable(
                "playerHealthMax",
                String.format("%.0f", StatsUtils.formatXp(playerStatMap.get(healthIndex).getMax()))
            )
            .setVariable(
                "playerStamina",
                String.format("%.0f", StatsUtils.formatXp(playerStatMap.get(staminaIndex).get()))
            )
            .setVariable(
                "playerStaminaMax",
                String.format("%.0f", StatsUtils.formatXp(playerStatMap.get(staminaIndex).getMax()))
            )
            .setVariable("playerMana", String.format("%.0f", StatsUtils.formatXp(playerStatMap.get(manaIndex).get())))
            .setVariable(
                "playerManaMax",
                String.format("%.0f", StatsUtils.formatXp(playerStatMap.get(manaIndex).getMax()))
            )
            .setVariable("playerName", playerRef.getUsername())
            .setVariable("playerLevel", CommandLang.SHOW_LEVEL.param("level", currentLevel).getAnsiMessage())
            .setVariable(
                "currentXp",
                CommandLang.XP_NEEDED.param("currentXp", StatsUtils.formatXp(currentXp))
                    .param("xpForNextLevel", StatsUtils.formatXp(xpForNextLevel))
                    .param("percentage", String.format("%.1f", percentage))
                    .getAnsiMessage()
            )
            .setVariable("ability_points", StatsUtils.formatXp(levelService.getAvailableAbilityPoints(uuid)))
            .setVariable(
                "strength",
                CommandLang.STR.param("points", StatsUtils.formatXp(levelService.getStr(playerRef.getUuid())))
                    .getAnsiMessage()
            )
            .setVariable("strength_desc", CommandLang.STR_DESC.getAnsiMessage())
            .setVariable(
                "agility",
                CommandLang.AGI.param("points", StatsUtils.formatXp(levelService.getAgi(playerRef.getUuid())))
                    .getAnsiMessage()
            )
            .setVariable("agility_desc", CommandLang.AGI_DESC.getAnsiMessage())
            .setVariable(
                "perception",
                CommandLang.PER.param("points", StatsUtils.formatXp(levelService.getPer(playerRef.getUuid())))
                    .getAnsiMessage()
            )
            .setVariable("perception_desc", CommandLang.PER_DESC.getAnsiMessage())
            .setVariable(
                "vitality",
                CommandLang.VIT.param("points", StatsUtils.formatXp(levelService.getVit(playerRef.getUuid())))
                    .getAnsiMessage()
            )
            .setVariable("vitality_desc", CommandLang.VIT_DESC.getAnsiMessage())
            .setVariable(
                "intelligence",
                CommandLang.INT.param("points", StatsUtils.formatXp(levelService.getInt(playerRef.getUuid())))
                    .getAnsiMessage()
            )
            .setVariable("intelligence_desc", CommandLang.INT_DESC.getAnsiMessage())
            .setVariable(
                "constitution",
                CommandLang.CON.param("points", StatsUtils.formatXp(levelService.getCon(playerRef.getUuid())))
                    .getAnsiMessage()
            )
            .setVariable("constitution_desc", CommandLang.CON_DESC.getAnsiMessage());
        PageBuilder.pageForPlayer(playerRef)
            .loadHtml("Pages/LevelingCore/statspage.html", template)
            .addEventListener("AddStr", CustomUIEventBindingType.Activating, (data, ctx) -> {
                if (levelService.getAvailableAbilityPoints(uuid) <= 0)
                    return;
                levelService.setStr(uuid, levelService.getStr(uuid) + 1);
                levelService.useAbilityPoints(uuid, 1);
                template.setVariable("ability_points", levelService.getAvailableAbilityPoints(uuid));
                template.setVariable(
                    "strength",
                    CommandLang.STR.param("points", levelService.getStr(uuid)).getAnsiMessage()
                );
                ctx.updatePage(false);
            })
            .addEventListener("AddAgi", CustomUIEventBindingType.Activating, (data, ctx) -> {
                if (levelService.getAvailableAbilityPoints(uuid) <= 0)
                    return;
                levelService.setAgi(uuid, levelService.getAgi(uuid) + 1);
                levelService.useAbilityPoints(uuid, 1);

                var staminaModifier = new StaticModifier(
                    Modifier.ModifierTarget.MAX,
                    StaticModifier.CalculationType.ADDITIVE,
                    levelService.getAgi(uuid) * config.get().getAgiStatMultiplier()
                );
                var oxygenModifier = new StaticModifier(
                    Modifier.ModifierTarget.MAX,
                    StaticModifier.CalculationType.ADDITIVE,
                    levelService.getAgi(uuid) * config.get().getAgiStatMultiplier()
                );
                playerStatMap.putModifier(staminaIndex, staminaModifierKey, staminaModifier);
                playerStatMap.putModifier(oxygenIndex, oxygenModifierKey, oxygenModifier);
                playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getStamina());
                template.setVariable("ability_points", levelService.getAvailableAbilityPoints(uuid));
                template.setVariable(
                    "agility",
                    CommandLang.AGI.param("points", levelService.getAgi(uuid)).getAnsiMessage()
                );
                ctx.updatePage(false);
            })
            .addEventListener("AddPer", CustomUIEventBindingType.Activating, (data, ctx) -> {
                if (levelService.getAvailableAbilityPoints(uuid) <= 0)
                    return;
                levelService.setPer(uuid, levelService.getPer(uuid) + 1);
                levelService.useAbilityPoints(uuid, 1);
                template.setVariable("ability_points", levelService.getAvailableAbilityPoints(uuid));
                template.setVariable(
                    "perception",
                    CommandLang.PER.param("points", levelService.getPer(uuid)).getAnsiMessage()
                );
                ctx.updatePage(false);
            })
            .addEventListener("AddVit", CustomUIEventBindingType.Activating, (data, ctx) -> {
                if (levelService.getAvailableAbilityPoints(uuid) <= 0)
                    return;
                levelService.setVit(uuid, levelService.getVit(uuid) + 1);
                levelService.useAbilityPoints(uuid, 1);
                var healthModifier = new StaticModifier(
                    Modifier.ModifierTarget.MAX,
                    StaticModifier.CalculationType.ADDITIVE,
                    levelService.getVit(uuid) * config.get().getVitStatMultiplier()
                );
                playerStatMap.putModifier(healthIndex, healthModifierKey, healthModifier);
                playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getHealth());
                template.setVariable("ability_points", levelService.getAvailableAbilityPoints(uuid));
                template.setVariable(
                    "vitality",
                    CommandLang.VIT.param("points", levelService.getVit(uuid)).getAnsiMessage()
                );
                ctx.updatePage(false);
            })
            .addEventListener("AddInt", CustomUIEventBindingType.Activating, (data, ctx) -> {
                if (levelService.getAvailableAbilityPoints(uuid) <= 0)
                    return;
                levelService.setInt(uuid, levelService.getInt(uuid) + 1);
                levelService.useAbilityPoints(uuid, 1);
                var manaModifier = new StaticModifier(
                    Modifier.ModifierTarget.MAX,
                    StaticModifier.CalculationType.ADDITIVE,
                    levelService.getInt(uuid) * config.get().getIntStatMultiplier()
                );
                playerStatMap.putModifier(manaIndex, manaModifierKey, manaModifier);
                var manaRegen = (int) Math.max(1, Math.floor(1 + (levelService.getInt(uuid) * 0.25)));
                playerStatMap.addStatValue(manaIndex, manaRegen);
                playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getMana());
                template.setVariable("ability_points", levelService.getAvailableAbilityPoints(uuid));
                template.setVariable(
                    "intelligence",
                    CommandLang.INT.param("points", levelService.getInt(uuid)).getAnsiMessage()
                );
                ctx.updatePage(false);
            })
            .addEventListener("AddCon", CustomUIEventBindingType.Activating, (data, ctx) -> {
                if (levelService.getAvailableAbilityPoints(uuid) <= 0)
                    return;
                levelService.setCon(uuid, levelService.getCon(uuid) + 1);
                levelService.useAbilityPoints(uuid, 1);
                template.setVariable("ability_points", levelService.getAvailableAbilityPoints(uuid));
                template.setVariable(
                    "constitution",
                    CommandLang.CON.param("points", levelService.getCon(uuid)).getAnsiMessage()
                );
                ctx.updatePage(false);
            })
            .enableAsyncImageLoading(true)
            .enableRuntimeTemplateUpdates(true)
            .open(store);
    }
}
