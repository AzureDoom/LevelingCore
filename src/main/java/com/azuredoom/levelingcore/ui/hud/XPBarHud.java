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

import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.LevelServiceImpl;

public class XPBarHud extends CustomUIHud {

    public static final WeakHashMap<PlayerRef, XPBarHud> hudMap = new WeakHashMap<>();

    private LevelServiceImpl levelServiceImpl;

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
        uiCommandBuilder.append("HUD/LevelingCore/xpbar.ui");
        update(uiCommandBuilder);
    }

    public void update(UICommandBuilder uiCommandBuilder) {
        var uuid = getPlayerRef().getUuid();
        var currentXp = levelServiceImpl.getXp(uuid);
        var currentLevel = levelServiceImpl.getLevel(uuid);
        var xpForCurrentLevel = levelServiceImpl.getXpForLevel(currentLevel);
        var xpForNextLevel = levelServiceImpl.getXpForLevel(currentLevel + 1);
        var xpIntoLevel = currentXp - xpForCurrentLevel;
        var xpNeededThisLevel = xpForNextLevel - xpForCurrentLevel;
        var progress = (double) xpIntoLevel / xpNeededThisLevel;

        uiCommandBuilder.set("#ProgressBar.Value", progress);
        if (config.get().isShowXPAmountInHUD()) {
            uiCommandBuilder.set(
                "#Level.TextSpans",
                Message.raw("LVL: " + currentLevel + "   " + "XP: " + currentXp + " / " + xpForNextLevel)
            );
        } else {
            uiCommandBuilder.set(
                "#Level.TextSpans",
                Message.raw("LVL: " + currentLevel)
            );
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
