package com.azuredoom.levelingcore.level.mobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.azuredoom.levelingcore.LevelingCore;

public final class MobLevelPersistence {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static final Type FILE_TYPE =
        new TypeToken<ConcurrentHashMap<UUID, PersistedMobLevel>>() {}.getType();

    private final ConcurrentHashMap<UUID, PersistedMobLevel> persisted = new ConcurrentHashMap<>();

    private final AtomicBoolean dirty = new AtomicBoolean(false);

    private Path filePath;

    public MobLevelPersistence() {}

    public void load() {
        var worldDir = resolveConfigDataDir();

        var dir = worldDir.resolve("data");
        this.filePath = dir.resolve("mob-levels.json");

        try {
            Files.createDirectories(dir);

            if (!Files.exists(filePath)) {
                persisted.clear();
                dirty.set(false);
                return;
            }

            try (var reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                ConcurrentHashMap<UUID, PersistedMobLevel> loaded =
                    GSON.fromJson(reader, FILE_TYPE);

                persisted.clear();
                if (loaded != null) {
                    persisted.putAll(loaded);
                }
                dirty.set(false);
            }
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("Failed to load mob level persistence from " + filePath);
            persisted.clear();
            dirty.set(false);
        }
    }

    public void save() {
        if (filePath == null) {
            var worldDir = resolveConfigDataDir();
            var dir = worldDir.resolve("data");
            this.filePath = dir.resolve("mob-levels.json");
        }

        if (!dirty.get())
            return;

        try {
            Files.createDirectories(filePath.getParent());

            Map<UUID, PersistedMobLevel> snap = new ConcurrentHashMap<>(persisted);

            var tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            try (
                var writer = Files.newBufferedWriter(
                    tmp,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            ) {
                GSON.toJson(snap, FILE_TYPE, writer);
            }

            try {
                Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignore) {
                Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            dirty.set(false);
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("Failed to save mob level persistence to " + filePath);
        }
    }

    public Optional<PersistedMobLevel> get(UUID entityId) {
        return Optional.ofNullable(persisted.get(entityId));
    }

    public void put(UUID entityId, PersistedMobLevel persistedMobLevel) {
        persisted.put(entityId, persistedMobLevel);
        dirty.set(true);
    }

    public void dirty() {
        dirty.set(true);
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public int size() {
        return persisted.size();
    }

    private Path resolveConfigDataDir() {
        return LevelingCore.configDataPath;
    }

    public void remove(UUID uuid) {
        persisted.remove(uuid);
    }
}
