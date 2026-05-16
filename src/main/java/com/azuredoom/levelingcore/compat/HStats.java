package com.azuredoom.levelingcore.compat;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HStats {

    private final String URL_BASE = "https://api.hstats.dev/api/";

    private final String modUUID;

    private final String modVersion;

    private final String serverUUID;

    private final HytaleLogger logger;

    public HStats(String modUUID, String modVersion, HytaleLogger logger) {
        this.modUUID = modUUID;
        this.modVersion = modVersion;
        this.logger = logger;

        this.serverUUID = getServerUUID();
        if (this.serverUUID == null) {
            System.out.println("[HStats] Metrics are disabled on this server.");
            return;
        }

        logMetrics();
        addModToServer();
        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::logMetrics, 5, 5, TimeUnit.MINUTES);
    }

    private void logMetrics() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("server_uuid", this.serverUUID);
        arguments.put("players_online", String.valueOf(getOnlinePlayerCount()));
        arguments.put("os_name", System.getProperty("os.name"));
        arguments.put("os_version", System.getProperty("os.version"));
        arguments.put("java_version", System.getProperty("java.version"));
        arguments.put("cores", String.valueOf(Runtime.getRuntime().availableProcessors()));

        sendRequest(URL_BASE + "server/update-server", arguments);
    }

    private void addModToServer() {
        logger.atInfo().log("[HStats] Adding mod to server.");

        Map<String, String> arguments = new HashMap<>();
        arguments.put("server_uuid", this.serverUUID);
        arguments.put("plugin_uuid", this.modUUID);
        arguments.put("plugin_version", this.modVersion);

        sendRequest(URL_BASE + "server/add-plugin", arguments);
    }

    private String getServerUUID() {
        var serverUUIDFile = Paths.get("hstats-server-uuid.txt");
        try {
            if (Files.exists(serverUUIDFile)) {
                var content = Files.readString(serverUUIDFile);
                content = content.trim();
                var lines = content.split("\n");
                if (lines.length < 5)
                    return null;
                var enabled = lines[3].split("=")[1].trim();
                if (!enabled.equalsIgnoreCase("true"))
                    return null;
                return lines[4];
            } else {
                var uuid = UUID.randomUUID().toString();
                Files.writeString(
                    serverUUIDFile,
                    "HStats - Hytale Mod Metrics (hstats.dev)\nHStats is a simple metrics system for Hytale mods. This file is here because one of your mods/plugins uses it, please do not modify the UUID. HStats will apply little to no effect on your server and analytics are anonymous, however you can still disable it.\n\nenabled=true\n"
                        + uuid
                );
                return uuid;
            }
        } catch (IOException e) {
            return null;
        }
    }

    private void sendRequest(String urlString, Map<String, String> arguments) {
        try {
            var url = URI.create(urlString).toURL();
            var http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            var sj = new StringJoiner("&");
            for (var entry : arguments.entrySet()) {
                sj.add(
                    URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
                );
            }

            var out = sj.toString().getBytes(StandardCharsets.UTF_8);
            http.setFixedLengthStreamingMode(out.length);

            try (var os = http.getOutputStream()) {
                os.write(out);
            }

            http.disconnect();
        } catch (Exception e) {
            // pass
        }
    }

    private int getOnlinePlayerCount() {
        return Universe.get().getPlayerCount();
    }

}
