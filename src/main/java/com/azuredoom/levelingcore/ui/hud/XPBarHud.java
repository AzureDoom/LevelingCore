package com.azuredoom.levelingcore.ui.hud;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.WeakHashMap;

import com.azuredoom.levelingcore.compat.party.PartyProCompat;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.utils.StatsUtils;

public class XPBarHud extends CustomUIHud {

    public static final WeakHashMap<PlayerRef, XPBarHud> hudMap = new WeakHashMap<>();

    private final LevelServiceImpl levelServiceImpl;

    private final Config<GUIConfig> config;

    public XPBarHud(
        @NonNullDecl PlayerRef playerRef,
        @NonNullDecl LevelServiceImpl levelServiceImpl,
        Config<GUIConfig> config
    ) {
        super(playerRef);
        this.levelServiceImpl = levelServiceImpl;
        this.config = config;
        hudMap.put(playerRef, this);
    }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Huds/LevelingCore/xpbar.ui");
        // update(uiCommandBuilder);
    }

    public void update(UICommandBuilder uiCommandBuilder) {
        if (!config.get().isEnableXPBarUI())
            return;
        var uuid = getPlayerRef().getUuid();
        var currentLevel = levelServiceImpl.getLevel(uuid);
        var currentXp = levelServiceImpl.getXp(uuid) - levelServiceImpl.getXpForLevel(currentLevel);
        var xpForNextLevel = levelServiceImpl.getXpForLevel(levelServiceImpl.getLevel(uuid) + 1) - levelServiceImpl
            .getXpForLevel(currentLevel);
        var progress = (double) currentXp / xpForNextLevel;
        var percentage = (float) levelServiceImpl.getXp(uuid) / levelServiceImpl.getXpForLevel(
            levelServiceImpl.getLevel(uuid) + 1
        ) * 100;

        uiCommandBuilder.set("#ProgressBar.Value", progress);
        if (config.get().isShowXPAmountInHUD()) {
            uiCommandBuilder.set(
                "#Level.TextSpans",
                Message.raw(
                    "LVL: " + currentLevel + "   " + "XP: " + StatsUtils.formatXp(currentXp) + " / " + StatsUtils
                        .formatXp(xpForNextLevel) + " (" + String.format(
                            "%.1f",
                            percentage
                        ) + "%)"
                )
            );
        } else {
            uiCommandBuilder.set(
                "#Level.TextSpans",
                Message.raw("LVL: " + currentLevel)
            );
        }
        if (PluginManager.get().getPlugin(new PluginIdentifier("tsumori", "partypro")) != null) {
            PartyProCompat.showLvlOnHUD(uuid, levelServiceImpl);
        }
        update(false, uiCommandBuilder); // false = don't clear existing UI
    }

    public static void updateHud(@NonNullDecl PlayerRef playerRef) {
        var hud = hudMap.get(playerRef);
        if (hud == null)
            return;
        var uiCommandBuilder = new UICommandBuilder();
        hud.update(uiCommandBuilder);
    }

    public static void removeHud(@NonNullDecl PlayerRef playerRef) {
        hudMap.remove(playerRef);
    }
}
