package com.azuredoom.levelingcore.compat;

import com.creditor.Creditor;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

public class CreditorCompat {

    public static void start(JavaPlugin plugin) {
        Creditor.start(plugin);
    }

    public static void setup(JavaPlugin plugin) {
        Creditor.setup(plugin);
    }

    public static void stop(JavaPlugin plugin) {}

}
