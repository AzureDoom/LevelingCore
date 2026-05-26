package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;

public class HudPlayerReady {

    private HudPlayerReady() {}

    /**
     * Handles the initialization of the XP bar HUD when a player is ready. Checks whether the XP bar UI is enabled in
     * the configuration and sets up the appropriate HUD display based on the availability of external compatibility
     * plugins.
     *
     * @param event  the event triggered when a player is ready; contains information about the player
     * @param config the configuration object providing GUI-related settings, including HUD enablement
     */
    public static void ready(PlayerReadyEvent event, Config<GUIConfig> config) {
        var player = event.getPlayer();
        var ref = event.getPlayerRef();
        var store = ref.getStore();
        var world = store.getExternalData().getWorld();

        if (!config.get().isEnableXPBarUI())
            return;
        world.execute(() -> {
            var levelService = LevelingCore.getLevelService();
            var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null)
                return;
            var xpHud = new XPBarHud(playerRef, levelService, config);
            player.getHudManager().addCustomHud(playerRef, xpHud);
            XPBarHud.updateHud(playerRef);
        });
    }
}
