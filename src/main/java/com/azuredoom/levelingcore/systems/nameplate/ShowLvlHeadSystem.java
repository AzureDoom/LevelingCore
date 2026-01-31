package com.azuredoom.levelingcore.systems.nameplate;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.regex.Pattern;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;

public class ShowLvlHeadSystem implements Runnable {

    private final Config<GUIConfig> config;

    public ShowLvlHeadSystem(Config<GUIConfig> config) {
        this.config = config;
    }

    @Override
    public void run() {
        var universe = Universe.get();
        if (universe != null) {
            for (var world : universe.getWorlds().values()) {
                if (world != null && world.isAlive()) {
                    world.execute(() -> tickWorld(world));
                }
            }
        }
    }

    private void tickWorld(World world) {
        var store = world.getEntityStore().getStore();

        if (store == null)
            return;

        var levelingServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelingServiceOpt.isEmpty())
            return;

        var levelingService = levelingServiceOpt.get();

        store.forEachChunk(PlayerRef.getComponentType(), (chunk, commandBuffer) -> {
            var size = chunk.size();
            for (var i = 0; i < size; i++) {
                var ref = chunk.getReferenceTo(i);

                var playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null)
                    continue;

                int lvl = levelingService.getLevel(playerRef.getUuid());
                insertNameplate(commandBuffer, ref, formatNameplate(config.get().isShowPlayerLvls() ? lvl : 0));
            }
        });
        store.forEachChunk(NPCEntity.getComponentType(), (chunk, commandBuffer) -> {
            var size = chunk.size();
            for (var i = 0; i < size; i++) {
                var ref = chunk.getReferenceTo(i);

                var npc = commandBuffer.getComponent(ref, NPCEntity.getComponentType());
                if (npc == null)
                    continue;

                final var entityId = npc.getUuid();
                var lvl = LevelingCore.mobLevelRegistry.getOrCreateWithPersistence(
                    entityId,
                    () -> MobLevelingUtil.computeSpawnLevel(npc),
                    0,
                    LevelingCore.mobLevelPersistence
                );
                if (lvl == null)
                    continue;

                String text = formatNameplate(config.get().isShowMobLvls() ? lvl.level : 0);
                insertNameplate(commandBuffer, ref, text);
            }
        });
    }

    private void insertNameplate(
        CommandBuffer<EntityStore> commandBuffer,
        Ref<EntityStore> ref,
        String desiredText
    ) {
        if (desiredText == null || desiredText.isBlank()) {
            var current = commandBuffer.getComponent(ref, Nameplate.getComponentType());
            if (current != null) {
                var base = Pattern.compile("\\s*\\[Lvl \\d+]\\s*$")
                        .matcher(current.getText())
                        .replaceAll("");
                current.setText(base);
                commandBuffer.putComponent(ref, Nameplate.getComponentType(), current);
            }
            return;
        }
        var entityStatMap = commandBuffer.getComponent(ref, EntityStatMap.getComponentType());
        var healthStat = DefaultEntityStatTypes.getHealth();
        var healthValue = entityStatMap.get(healthStat);

        var current = commandBuffer.getComponent(ref, Nameplate.getComponentType());
        if (current != null) {
            var old = current.getText();
            var base = Pattern.compile("\\s*\\[Lvl \\d+]\\s*$").matcher(old).replaceAll("");
            var newText = base + " " + desiredText;
            if (newText.equals(old)) {
                return;
            }

            current.setText(newText);
            if (healthValue.get() > 0)
                commandBuffer.putComponent(ref, Nameplate.getComponentType(), current);
        } else if (healthValue.get() > 0) {
            commandBuffer.putComponent(ref, Nameplate.getComponentType(), new Nameplate(desiredText));
        }
    }

    private String formatNameplate(int level) {
        if (level == 0)
            return null;
        return " [Lvl " + level + "]";
    }
}
