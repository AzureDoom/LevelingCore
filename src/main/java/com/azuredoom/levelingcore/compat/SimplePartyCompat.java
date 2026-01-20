package com.azuredoom.levelingcore.compat;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;
import net.justmadlime.SimpleParty.party.PartyManager;

import java.util.Arrays;
import java.util.UUID;

import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.utils.NotificationsUtil;

/**
 * Utility class to provide compatibility for handling XP gain in a party.
 */
public class SimplePartyCompat {

    /**
     * Handles XP gain for a player, distributing XP either to the player individually or to all members of their party
     * if party XP sharing is enabled.
     *
     * @param xp           The number of experience points gained.
     * @param playerUuid   The unique identifier of the player gaining XP.
     * @param levelService The service handling XP and level management.
     * @param config       The configuration object containing settings related to XP sharing and notifications.
     * @param player       The player instance receiving the XP or notifications.
     */
    public static void onXPGain(
        long xp,
        UUID playerUuid,
        LevelServiceImpl levelService,
        Config<GUIConfig> config,
        PlayerRef playerRef
    ) {
        var party = PartyManager.getInstance().getPartyFromPlayer(playerUuid);
        if (party != null && config.get().isEnableSimplePartyXPShareCompat()) {
            Arrays.stream(party.getAllPartyMembers())
                .distinct()
                .forEach(uuid -> {
                    if (!config.get().isDisableXPGainNotification())
                        NotificationsUtil.sendNotification(Universe.get().getPlayer(uuid), "Gained " + xp + " XP");
                    levelService.addXp(uuid, xp);
                });
        } else {
            if (!config.get().isDisableXPGainNotification())
                NotificationsUtil.sendNotification(playerRef, "Gained " + xp + " XP");
            levelService.addXp(playerUuid, xp);
        }
    }
}
