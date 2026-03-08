package com.azuredoom.levelingcore.compat.party;

import com.carsonk.partyplugin.party.PartyManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;
import com.azuredoom.levelingcore.utils.NotificationsUtil;
import com.azuredoom.levelingcore.utils.PartyCompatUtil;

public class PartyPluginCompat {

    private PartyPluginCompat() {}

    /**
     * Handles experience (XP) gain for a player or party, taking into account party sharing, distance checks, and
     * configuration settings for XP notifications and distribution.
     *
     * @param xp           The amount of XP to be awarded.
     * @param playerUuid   The unique identifier for the player gaining XP.
     * @param levelService The service responsible for handling player levels and XP.
     * @param config       The configuration object containing settings related to XP gain and party behavior.
     * @param playerRef    The reference to the player object gaining XP.
     */
    public static void onXPGain(
        long xp,
        UUID playerUuid,
        LevelServiceImpl levelService,
        Config<GUIConfig> config,
        PlayerRef playerRef
    ) {
        var cfg = config.get();
        var party = PartyManager.getInstance().getPartyDataById(playerUuid);
        if (party == null || !cfg.isEnablePartyPluginXPShareCompat()) {
            if (!cfg.isDisableXPGainNotification() && !levelService.isMaxLevel(playerUuid)) {
                NotificationsUtil.sendXPGainNotification(playerRef, xp);
            }
            levelService.addXp(playerUuid, xp);
            XPBarHud.updateHud(playerRef);
            return;
        }
        var members = Arrays.stream(party.getMemberUuids().toArray(new UUID[0]))
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

            if (!cfg.isDisableXPGainNotification() && !levelService.isMaxLevel(uuid)) {
                if (player != null) {
                    NotificationsUtil.sendXPGainNotification(
                        player,
                        award
                    );
                } else if (uuid.equals(playerUuid)) {
                    NotificationsUtil.sendXPGainNotification(
                        playerRef,
                        award
                    );
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
}
