package com.azuredoom.levelingcore.compat;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;
import me.tsumori.partypro.api.PartyProAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;
import com.azuredoom.levelingcore.utils.NotificationsUtil;
import com.azuredoom.levelingcore.utils.PartyCompatUtil;

public class PartyProCompat {

    private PartyProCompat() {}

    public static void onXPGain(
        long xp,
        UUID playerUuid,
        LevelServiceImpl levelService,
        Config<GUIConfig> config,
        PlayerRef playerRef
    ) {
        var cfg = config.get();
        var party = PartyProAPI.getInstance().getPartyByPlayer(playerUuid);
        if (party == null || !cfg.isEnablePartyPluginXPShareCompat()) {
            if (!cfg.isDisableXPGainNotification()) {
                NotificationsUtil.sendNotification(playerRef, CommandLang.GAINED.param("xp", xp));
            }
            levelService.addXp(playerUuid, xp);
            XPBarHud.updateHud(playerRef);
            return;
        }
        var members = Arrays.stream(party.getAllMembers().toArray(new UUID[0]))
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (members.isEmpty())
            return;

        var killerPlayer = Universe.get().getPlayer(playerUuid);

        var eligible = new ArrayList<UUID>(members.size());
        for (var uuid : members) {
            if (uuid.equals(playerUuid)) {
                eligible.add(uuid);
                continue;
            }

            if (!cfg.isEnablePartyXPDistanceCheck()) {
                eligible.add(uuid);
                continue;
            }

            var otherPlayer = Universe.get().getPlayer(uuid);
            if (killerPlayer == null || otherPlayer == null) {
                continue;
            }

            var killerPos = killerPlayer.getTransform().getPosition();
            var otherPos = otherPlayer.getTransform().getPosition();

            var distance = killerPos.distanceTo(otherPos);
            if (distance <= cfg.getPartyXPDistanceBlocks()) {
                eligible.add(uuid);
            }
        }

        if (eligible.isEmpty())
            return;

        var mult = cfg.getPartyGroupXPMultiplier();
        if (mult < 0)
            mult = 0;

        var totalPartyXp = PartyCompatUtil.safeRoundToLong(xp * mult);

        var split = cfg.isEnablePartyXPSplit();
        var splitShare = split ? (totalPartyXp / eligible.size()) : totalPartyXp;

        for (var uuid : eligible) {
            var isKiller = uuid.equals(playerUuid);

            long award;
            if (isKiller && cfg.isKillerGetsFullXp()) {
                award = xp;
            } else {
                award = splitShare;
            }

            if (award <= 0)
                continue;

            var player = Universe.get().getPlayer(uuid);

            if (!cfg.isDisableXPGainNotification()) {
                if (player != null) {
                    NotificationsUtil.sendNotification(player, CommandLang.GAINED.param("xp", award));
                } else if (uuid.equals(playerUuid)) {
                    NotificationsUtil.sendNotification(playerRef, CommandLang.GAINED.param("xp", award));
                }
            }

            levelService.addXp(uuid, award);

            if (player != null) {
                XPBarHud.updateHud(player);
            } else if (uuid.equals(playerUuid)) {
                XPBarHud.updateHud(playerRef);
            }
        }
    }

    public static void showLvlOnHUD(UUID playerUuid, LevelServiceImpl levelService) {
        PartyProAPI.getInstance().setPlayerCustomText1(playerUuid, "Lvl " + levelService.getLevel(playerUuid));
    }
}
