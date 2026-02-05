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
        this.update(uiCommandBuilder);
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddStr",
            new EventData().append("Type", "SpendStat").append("Stat", "str")
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddAgi",
            new EventData().append("Type", "SpendStat").append("Stat", "agi")
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddPer",
            new EventData().append("Type", "SpendStat").append("Stat", "per")
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddVit",
            new EventData().append("Type", "SpendStat").append("Stat", "vit")
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddInt",
            new EventData().append("Type", "SpendStat").append("Stat", "int")
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#AddCon",
            new EventData().append("Type", "SpendStat").append("Stat", "con")
        );
    }

    public void update(UICommandBuilder uiCommandBuilder) {
        var levelServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelServiceOpt.isEmpty()) {
            uiCommandBuilder.set("#PNTSLabel.TextSpans", Message.raw("Service unavailable"));
            return;
        }
        var levelServiceImpl = levelServiceOpt.get();
        var hasAbilityPoints = levelServiceImpl.getAvailableAbilityPoints(playerRef.getUuid()) > 0;
        var currentXp = levelServiceImpl.getXp(playerRef.getUuid());
        var currentLevel = levelServiceImpl.getLevel(playerRef.getUuid());
        var xpForNextLevel = levelServiceImpl.getXpForLevel(currentLevel + 1);
        var percentage = (float) currentXp / xpForNextLevel * 100;

        uiCommandBuilder.set(
            "#Level.TextSpans",
            CommandLang.SHOW_LEVEL.param("playername", playerRef.getUsername()).param("level", currentLevel)
        );
        uiCommandBuilder.set(
            "#XP.TextSpans",
            CommandLang.XP_NEEDED.param("currentXp", currentXp)
                .param("xpForNextLevel", xpForNextLevel)
                .param("percentage", String.format("%.1f", percentage))
        );
        uiCommandBuilder.set("#AddStr" + ".HitTestVisible", hasAbilityPoints);
        uiCommandBuilder.set("#AddAgi" + ".HitTestVisible", hasAbilityPoints);
        uiCommandBuilder.set("#AddPer" + ".HitTestVisible", hasAbilityPoints);
        uiCommandBuilder.set("#AddVit" + ".HitTestVisible", hasAbilityPoints);
        uiCommandBuilder.set("#AddInt" + ".HitTestVisible", hasAbilityPoints);
        uiCommandBuilder.set("#AddCon" + ".HitTestVisible", hasAbilityPoints);
        uiCommandBuilder.set(
            "#STR.TextSpans",
            CommandLang.STR.param("points", levelServiceImpl.getStr(playerRef.getUuid()))
        );
        uiCommandBuilder.set(
            "#STRDescription.TextSpans",
            CommandLang.STR_DESC
        );
        uiCommandBuilder.set(
            "#AGI.TextSpans",
            CommandLang.AGI.param("points", levelServiceImpl.getAgi(playerRef.getUuid()))
        );
        uiCommandBuilder.set(
            "#AGIDescription.TextSpans",
            CommandLang.AGI_DESC
        );
        uiCommandBuilder.set(
            "#PER.TextSpans",
            CommandLang.PER.param("points", levelServiceImpl.getPer(playerRef.getUuid()))
        );
        uiCommandBuilder.set(
            "#PERDescription.TextSpans",
            CommandLang.PER_DESC
        );
        uiCommandBuilder.set(
            "#VIT.TextSpans",
            CommandLang.VIT.param("points", levelServiceImpl.getVit(playerRef.getUuid()))
        );
        uiCommandBuilder.set(
            "#VITDescription.TextSpans",
            CommandLang.VIT_DESC
        );
        uiCommandBuilder.set(
            "#INT.TextSpans",
            CommandLang.INT.param("points", levelServiceImpl.getInt(playerRef.getUuid()))
        );
        uiCommandBuilder.set(
            "#INTDescription.TextSpans",
            CommandLang.INT_DESC
        );
        uiCommandBuilder.set(
            "#CON.TextSpans",
            CommandLang.CON.param("points", levelServiceImpl.getCon(playerRef.getUuid()))
        );
        uiCommandBuilder.set(
            "#CONDescription.TextSpans",
            CommandLang.CON_DESC
        );
        uiCommandBuilder.set(
            "#PNTSLabel.TextSpans",
            CommandLang.ABILITY_POINTS_AVAILABLE.param(
                "ability_points",
                levelServiceImpl.getAvailableAbilityPoints(playerRef.getUuid())
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
            this.refreshUI();
        }
    }

    private void handleStatsApplied(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull BindingData data
    ) {
        if (data == null || data.Type == null) {
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

        if (levelService.getAvailableAbilityPoints(uuid) <= 0) {
            this.refreshUI();
            return;
        }

        if (!levelService.useAbilityPoints(uuid, 1)) {
            this.refreshUI();
            return;
        }

        String stat = (data.Stat == null) ? "" : data.Stat.toLowerCase();
        switch (stat) {
            case "str" -> levelService.setStr(uuid, levelService.getStr(uuid) + 1);
            case "agi" -> levelService.setAgi(uuid, levelService.getAgi(uuid) + 1);
            case "per" -> levelService.setPer(uuid, levelService.getPer(uuid) + 1);
            case "vit" -> levelService.setVit(uuid, levelService.getVit(uuid) + 1);
            case "int" -> levelService.setInt(uuid, levelService.getInt(uuid) + 1);
            case "con" -> levelService.setCon(uuid, levelService.getCon(uuid) + 1);
            default -> {
                playerRef.sendMessage(CommandLang.UNKNOWN_STAT.param("stat", data.Stat));
                this.refreshUI();
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

        this.refreshUI();
    }

    private void refreshUI() {
        var builder = new UICommandBuilder();
        this.update(builder);
        this.sendUpdate(builder);
    }

    public static class BindingData {

        public String Type;

        public String Stat;

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
                .build();
    }

}
