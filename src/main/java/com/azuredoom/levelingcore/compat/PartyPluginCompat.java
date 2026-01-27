package com.azuredoom.levelingcore.compat;

import com.carsonk.partyplugin.party.PartyManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;

import java.util.Arrays;
import java.util.UUID;

import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;
import com.azuredoom.levelingcore.utils.NotificationsUtil;

public class PartyPluginCompat {

    private PartyPluginCompat() {}

    public static void onXPGain(
        long xp,
        UUID playerUuid,
        LevelServiceImpl levelService,
        Config<GUIConfig> config,
        PlayerRef playerRef
    ) {
        var party = PartyManager.getInstance().getPartyDataById(playerUuid);
        if (party != null && config.get().isEnablePartyPluginXPShareCompat()) {
            Arrays.stream(party.getMemberUuids().toArray(new UUID[0]))
                .distinct()
                .forEach(uuid -> {
                    if (!config.get().isDisableXPGainNotification())
                        NotificationsUtil.sendNotification(
                            Universe.get().getPlayer(uuid),
                            CommandLang.GAINED.param("xp", xp)
                        );
                    levelService.addXp(uuid, xp);
                    XPBarHud.updateHud(playerRef);
                });
        } else {
            if (!config.get().isDisableXPGainNotification())
                NotificationsUtil.sendNotification(playerRef, CommandLang.GAINED.param("xp", xp));
            levelService.addXp(playerUuid, xp);
            XPBarHud.updateHud(playerRef);
        }
    }
}
