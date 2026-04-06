package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;

import java.util.logging.Level;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.compat.MultipleHudCompat;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
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
        world.execute(() -> LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService1 -> {
            var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null)
                return;
            var xpHud = new XPBarHud(playerRef, levelService1, config);
            // TODO: Update 5 removes the need for MultipleHud/AutoMultiHud and setCustomHud -> addCustomHud with keyed
            // huds
            // instead
            // player.getHudManager().addCustomHud(playerRef, xpHud);
            if (PluginManager.get().getPlugin(new PluginIdentifier("Buuz135", "MultipleHUD")) != null) {
                MultipleHudCompat.showHud(player, playerRef, xpHud);
            } else {
                if (PluginManager.get().getPlugin(new PluginIdentifier("AutoMultiHud", "AutoMultiHud")) == null) {
                    playerRef.sendMessage(CommandLang.MISSING_MULTIPLE_HUD);
                    LevelingCore.LOGGER.at(Level.WARNING).log(CommandLang.MISSING_MULTIPLE_HUD.getRawText());
                }
                player.getHudManager().setCustomHud(playerRef, xpHud);
            }
            XPBarHud.updateHud(playerRef);
        }));
    }
}
