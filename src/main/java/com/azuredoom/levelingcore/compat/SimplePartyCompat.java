package com.azuredoom.levelingcore.compat;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.util.Config;
import net.justmadlime.SimpleParty.party.PartyManager;

import java.util.Arrays;
import java.util.UUID;

import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.level.LevelServiceImpl;

public class SimplePartyCompat {

    public static void onXPGain(
        long xp,
        UUID playerUuid,
        LevelServiceImpl levelService,
        Config<GUIConfig> config,
        Player player
    ) {
        var party = PartyManager.getInstance().getPartyFromPlayer(playerUuid);
        if (party != null && config.get().isEnableSimplePartyXPShareCompat()) {
            Arrays.stream(party.getAllPartyMembers())
                .distinct()
                .forEach(uuid -> levelService.addXp(uuid, xp));
        } else {
            if (config.get().isEnableXPChatMsgs())
                player.sendMessage(CommandLang.GAINED.param("xp", xp));
            levelService.addXp(playerUuid, xp);
        }
    }
}
