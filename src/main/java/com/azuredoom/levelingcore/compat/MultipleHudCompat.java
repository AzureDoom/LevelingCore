package com.azuredoom.levelingcore.compat;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class MultipleHudCompat {

    private MultipleHudCompat() {}

    /**
     * Configures and displays a custom HUD (Heads-Up Display) for a player using the MultipleHUD mod.
     *
     * @param player    The player for whom the HUD is to be displayed.
     * @param playerRef A reference to the player that provides additional contextual or component data.
     * @param xpHud     The custom HUD instance that should be displayed for the player.
     */
    public static void showHud(Player player, PlayerRef playerRef, CustomUIHud xpHud) {
        MultipleHUD.getInstance().setCustomHud(player, playerRef, "levelingcore_xpbar", xpHud);
    }
}
