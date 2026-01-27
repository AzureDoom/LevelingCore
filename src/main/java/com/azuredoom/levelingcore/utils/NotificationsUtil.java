package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;

/**
 * Utility class for sending notifications to players. Provides a method to send success notifications with a given
 * message to a specified player reference.
 */
public class NotificationsUtil {

    /**
     * Sends a success notification to a specific player.
     *
     * @param playerRef The reference object representing the target player.
     * @param message   The message to be displayed in the notification.
     */
    public static void sendNotification(PlayerRef playerRef, Message message) {
        NotificationUtil.sendNotification(
            playerRef.getPacketHandler(),
            message,
            NotificationStyle.Success
        );
    }
}
