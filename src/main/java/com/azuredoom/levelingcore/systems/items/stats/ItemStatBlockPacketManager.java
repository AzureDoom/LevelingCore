package com.azuredoom.levelingcore.systems.items.stats;

import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.utils.NotificationsUtil;

public class ItemStatBlockPacketManager {

    private volatile boolean registered = false;

    private final Map<UUID, Long> notifyCooldownMs = new ConcurrentHashMap<>();

    private final Map<UUID, HandStatGateSnapshot> handGate = new ConcurrentHashMap<>();

    public Map<UUID, HandStatGateSnapshot> getHandGate() {
        return handGate;
    }

    public void start() {
        if (registered)
            return;
        registered = true;

        PacketAdapters.registerInbound((PlayerPacketFilter) (playerRef, packet) -> {
            if (!LevelingCore.getConfig().get().isEnableItemStatRequirement()) {
                return false;
            }

            if (packet instanceof SyncInteractionChains chainsPacket) {
                var cancel = false;
                for (var chain : chainsPacket.updates) {
                    if (shouldBlock(playerRef, chain)) {
                        cancel = true;
                        break;
                    }
                }
                return cancel;
            }

            if (packet instanceof SyncInteractionChain chain) {
                return shouldBlock(playerRef, chain);
            }

            return false;
        });
    }

    public void shutdown() {
        registered = false;
        notifyCooldownMs.clear();
        handGate.clear();
    }

    private boolean shouldBlock(PlayerRef playerRef, SyncInteractionChain chain) {
        if (playerRef == null || chain == null)
            return false;

        switch (chain.interactionType) {
            case Primary, Secondary, Use, Ability1, Ability2, Ability3, ProjectileSpawn -> {}
            default -> {
                return false;
            }
        }

        if (chain.state != InteractionState.NotFinished)
            return false;

        var snap = handGate.get(playerRef.getUuid());
        if (snap == null || !snap.blocked())
            return false;

        maybeNotify(playerRef, snap.req(), snap.hand());
        return true;
    }

    private void maybeNotify(
        PlayerRef playerRef,
        com.azuredoom.levelingcore.level.itemlevellock.ItemStatRequirement req,
        ItemStack hand
    ) {
        var now = System.currentTimeMillis();
        var nextOk = notifyCooldownMs.getOrDefault(playerRef.getUuid(), 0L);
        if (now < nextOk)
            return;
        notifyCooldownMs.put(playerRef.getUuid(), now + 750L);

        NotificationsUtil.sendStatRequirementNotification(
            playerRef,
            hand
        );
    }
}
