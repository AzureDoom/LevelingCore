package com.azuredoom.levelingcore.ui.page;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.utils.StatsUtils;

public class StatsScreen extends InteractiveCustomUIPage<StatsScreen.BindingData> {

    private final Config<GUIConfig> config;

    public StatsScreen(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime, Config<GUIConfig> config) {
        super(playerRef, lifetime, BindingData.CODEC);
        this.config = config;
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder uiCommandBuilder,
        @Nonnull UIEventBuilder uiEventBuilder,
        @Nonnull Store<EntityStore> store
    ) {
        uiCommandBuilder.append("Pages/LevelingCore/statspage.ui");
        this.update(uiCommandBuilder, store);
        String[] stats = { "str", "agi", "per", "vit", "int", "con" };

        for (var stat : stats) {
            uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#Add" + capitalize(stat),
                new EventData()
                    .append("Type", "SpendStat")
                    .append("Stat", stat)
                    .append("Amount", "1")
            );
            uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#Add" + capitalize(stat) + "Five",
                new EventData()
                    .append("Type", "SpendStat")
                    .append("Stat", stat)
                    .append("Amount", "5")
            );
        }
    }

    public void update(UICommandBuilder uiCommandBuilder, Store<EntityStore> store) {
        var levelService = LevelingCoreApi.getLevelServiceIfPresent().orElse(null);
        if (levelService == null) {
            uiCommandBuilder.set("#PNTSLabel.TextSpans", Message.raw("Service unavailable"));
            return;
        }

        var playerUUID = playerRef.getUuid();
        var availablePoints = levelService.getAvailableAbilityPoints(playerUUID);

        var canAddOne = availablePoints >= 1;
        var canAddFive = availablePoints >= 5;

        var currentLevel = levelService.getLevel(playerUUID);
        var currentXp = levelService.getXp(playerUUID) - levelService.getXpForLevel(currentLevel);
        var xpForNextLevel = levelService.getXpForLevel(levelService.getLevel(playerUUID) + 1)
            - levelService.getXpForLevel(currentLevel);
        var percentage = (float) currentXp / xpForNextLevel * 100;
        var ref = playerRef.getReference();
        if (ref == null) {
            return;
        }
        var playerStatMap = store.ensureAndGetComponent(ref, EntityStatMap.getComponentType());

        uiCommandBuilder.set("#Level.TextSpans", CommandLang.SHOW_LEVEL.param("level", currentLevel));
        uiCommandBuilder.set("#Playername.TextSpans", Message.raw(playerRef.getUsername()));
        uiCommandBuilder.set(
            "#XP.TextSpans",
            CommandLang.XP_NEEDED.param("currentXp", StatsUtils.formatXp(currentXp))
                .param("xpForNextLevel", StatsUtils.formatXp(xpForNextLevel))
                .param("percentage", String.format("%.1f", percentage))
        );
        uiCommandBuilder.set(
            "#Health.TextSpans",
            Message.raw(
                StatsUtils.formatXp(playerStatMap.get(DefaultEntityStatTypes.getHealth()).get()) + "/" + StatsUtils
                    .formatXp(playerStatMap.get(DefaultEntityStatTypes.getHealth()).getMax())
            )
        );
        uiCommandBuilder.set(
            "#Stamina.TextSpans",
            Message.raw(
                StatsUtils.formatXp(playerStatMap.get(DefaultEntityStatTypes.getStamina()).get()) + "/" + StatsUtils
                    .formatXp(playerStatMap.get(DefaultEntityStatTypes.getStamina()).getMax())
            )
        );
        uiCommandBuilder.set(
            "#Mana.TextSpans",
            Message.raw(
                StatsUtils.formatXp(playerStatMap.get(DefaultEntityStatTypes.getMana()).get()) + "/" + StatsUtils
                    .formatXp(playerStatMap.get(DefaultEntityStatTypes.getMana()).getMax())
            )
        );

        String[] stats = { "Str", "Agi", "Per", "Vit", "Int", "Con" };
        for (String stat : stats) {
            uiCommandBuilder.set("#Add" + stat + ".Visible", canAddOne);
            uiCommandBuilder.set("#Add" + stat + ".HitTestVisible", canAddOne);

            uiCommandBuilder.set("#Add" + stat + "Five.Visible", canAddFive);
            uiCommandBuilder.set("#Add" + stat + "Five.HitTestVisible", canAddFive);
        }

        uiCommandBuilder.set(
            "#STR.TextSpans",
            CommandLang.STR.param("points", StatsUtils.formatXp(levelService.getStr(playerUUID)))
        );
        uiCommandBuilder.set("#STRDescription.TextSpans", CommandLang.STR_DESC);

        uiCommandBuilder.set(
            "#AGI.TextSpans",
            CommandLang.AGI.param("points", StatsUtils.formatXp(levelService.getAgi(playerUUID)))
        );
        uiCommandBuilder.set("#AGIDescription.TextSpans", CommandLang.AGI_DESC);

        uiCommandBuilder.set(
            "#PER.TextSpans",
            CommandLang.PER.param("points", StatsUtils.formatXp(levelService.getPer(playerUUID)))
        );
        uiCommandBuilder.set("#PERDescription.TextSpans", CommandLang.PER_DESC);

        uiCommandBuilder.set(
            "#VIT.TextSpans",
            CommandLang.VIT.param("points", StatsUtils.formatXp(levelService.getVit(playerUUID)))
        );
        uiCommandBuilder.set("#VITDescription.TextSpans", CommandLang.VIT_DESC);

        uiCommandBuilder.set(
            "#INT.TextSpans",
            CommandLang.INT.param("points", StatsUtils.formatXp(levelService.getInt(playerUUID)))
        );
        uiCommandBuilder.set("#INTDescription.TextSpans", CommandLang.INT_DESC);

        uiCommandBuilder.set(
            "#CON.TextSpans",
            CommandLang.CON.param("points", StatsUtils.formatXp(levelService.getCon(playerUUID)))
        );
        uiCommandBuilder.set("#CONDescription.TextSpans", CommandLang.CON_DESC);

        uiCommandBuilder.set(
            "#PNTSLabel.TextSpans",
            CommandLang.ABILITY_POINTS_AVAILABLE.param(
                "ability_points",
                StatsUtils.formatXp(availablePoints)
            )
        );
    }

    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull BindingData data
    ) {
        try {
            super.handleDataEvent(ref, store, data);
            handleStatsApplied(ref, store, data);
        } catch (Throwable t) {
            playerRef.sendMessage(
                CommandLang.ERROR_UI.param("class_name", t.getClass().getSimpleName()).param("msg", t.getMessage())
            );
        } finally {
            this.refreshUI(store);
        }
    }

    private void handleStatsApplied(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull BindingData data
    ) {
        if (data.Type == null || data.Amount == null) {
            return;
        }

        if (!"SpendStat".equalsIgnoreCase(data.Type)) {
            return;
        }

        var levelService = LevelingCoreApi.getLevelServiceIfPresent().orElse(null);
        if (levelService == null) {
            playerRef.sendMessage(CommandLang.NOT_INITIALIZED);
            return;
        }

        var uuid = playerRef.getUuid();

        int amount = 0;
        try {
            amount = Integer.parseInt(data.Amount);
        } catch (Exception ignored) {}

        if (levelService.getAvailableAbilityPoints(uuid) < amount) {
            this.refreshUI(store);
            return;
        }

        if (!levelService.useAbilityPoints(uuid, amount)) {
            this.refreshUI(store);
            return;
        }

        var stat = (data.Stat == null) ? "" : data.Stat.toLowerCase();

        switch (stat) {
            case "str" -> levelService.setStr(uuid, levelService.getStr(uuid) + amount);
            case "agi" -> levelService.setAgi(uuid, levelService.getAgi(uuid) + amount);
            case "per" -> levelService.setPer(uuid, levelService.getPer(uuid) + amount);
            case "vit" -> levelService.setVit(uuid, levelService.getVit(uuid) + amount);
            case "int" -> levelService.setInt(uuid, levelService.getInt(uuid) + amount);
            case "con" -> levelService.setCon(uuid, levelService.getCon(uuid) + amount);
            default -> {
                playerRef.sendMessage(CommandLang.UNKNOWN_STAT.param("stat", data.Stat));
                this.refreshUI(store);
                return;
            }
        }

        var playerStatMap = store.ensureAndGetComponent(ref, EntityStatMap.getComponentType());

        var healthIndex = DefaultEntityStatTypes.getHealth();
        var staminaIndex = DefaultEntityStatTypes.getStamina();
        var oxygenIndex = DefaultEntityStatTypes.getOxygen();
        var manaIndex = DefaultEntityStatTypes.getMana();

        var healthModifier = new StaticModifier(
            Modifier.ModifierTarget.MAX,
            StaticModifier.CalculationType.ADDITIVE,
            levelService.getVit(uuid) * config.get().getVitStatMultiplier()
        );
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
        var manaModifier = new StaticModifier(
            Modifier.ModifierTarget.MAX,
            StaticModifier.CalculationType.ADDITIVE,
            levelService.getInt(uuid) * config.get().getIntStatMultiplier()
        );

        var healthModifierKey = "LevelingCore_health_stat";
        var staminaModifierKey = "LevelingCore_stamina_stat";
        var oxygenModifierKey = "LevelingCore_oxygen_stat";
        var manaModifierKey = "LevelingCore_mana_stat";

        playerStatMap.putModifier(healthIndex, healthModifierKey, healthModifier);
        playerStatMap.putModifier(staminaIndex, staminaModifierKey, staminaModifier);
        playerStatMap.putModifier(oxygenIndex, oxygenModifierKey, oxygenModifier);
        playerStatMap.putModifier(manaIndex, manaModifierKey, manaModifier);
        var manaRegen = (int) Math.max(1, Math.floor(1 + (levelService.getInt(uuid) * 0.25)));
        playerStatMap.addStatValue(manaIndex, manaRegen);
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getHealth());
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getStamina());
        playerStatMap.maximizeStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getMana());

        this.refreshUI(store);
    }

    private void refreshUI(Store<EntityStore> store) {
        var builder = new UICommandBuilder();
        this.update(builder, store);
        this.sendUpdate(builder);
    }

    private static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static class BindingData {

        public String Type;

        public String Stat;

        public String Amount;

        public static final BuilderCodec<BindingData> CODEC =
            BuilderCodec.builder(BindingData.class, BindingData::new)
                .append(
                    new KeyedCodec<>("Type", Codec.STRING),
                    (d, v) -> d.Type = v,
                    d -> d.Type
                )
                .add()
                .append(
                    new KeyedCodec<>("Stat", Codec.STRING),
                    (d, v) -> d.Stat = v,
                    d -> d.Stat
                )
                .add()
                .append(
                    new KeyedCodec<>("Amount", Codec.STRING),
                    (d, v) -> d.Amount = v,
                    d -> d.Amount
                )
                .add()
                .build();
    }

}
