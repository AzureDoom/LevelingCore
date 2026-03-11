package com.azuredoom.levelingcore.systems.nameplate;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;

@SuppressWarnings("removal")
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

        AtomicReference<String> locale = new AtomicReference<>();

        store.forEachChunk(PlayerRef.getComponentType(), (chunk, commandBuffer) -> {
            var size = chunk.size();
            for (var i = 0; i < size; i++) {
                var ref = chunk.getReferenceTo(i);

                var playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null)
                    continue;

                locale.set(playerRef.getLanguage());
                var lvl = levelingService.getLevel(playerRef.getUuid());
                insertNameplate(
                    commandBuffer,
                    ref,
                    formatNameplate(playerRef.getUsername(), config.get().isShowPlayerLvls() ? lvl : 0)
                );
            }
        });
        store.forEachChunk(NPCEntity.getComponentType(), (chunk, commandBuffer) -> {
            var size = chunk.size();
            for (var i = 0; i < size; i++) {
                var ref = chunk.getReferenceTo(i);

                var npc = commandBuffer.getComponent(ref, NPCEntity.getComponentType());
                if (npc == null)
                    continue;

                var blacklistedMobs = config.get().getBlacklistedMobs();
                var npcTypeId = npc.getNPCTypeId();
                if (Arrays.asList(blacklistedMobs).contains(npcTypeId)) {
                    continue;
                }
                final var entityId = npc.getUuid();
                var entityName = I18nModule.get()
                    .getMessage(
                        Boolean.parseBoolean(locale.get()) ? null : "en-US",
                        npc.getRole().getNameTranslationKey()
                    );
                var lvl = LevelingCore.mobLevelRegistry.getOrCreateWithPersistence(
                    entityId,
                    () -> MobLevelingUtil.computeSpawnLevel(npc),
                    0,
                    LevelingCore.mobLevelPersistence
                );
                if (lvl == null)
                    continue;

                String text = formatNameplate(entityName, config.get().isShowMobLvls() ? lvl.level : 0);
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
                var strip = buildSuffixStripPattern();
                var base = strip.matcher(current.getText()).replaceAll("");
                current.setText(base);
                commandBuffer.putComponent(ref, Nameplate.getComponentType(), current);
            }
            return;
        }
        var entityStatMap = commandBuffer.getComponent(ref, EntityStatMap.getComponentType());
        var healthStat = DefaultEntityStatTypes.getHealth();
        var healthValue = entityStatMap.get(healthStat);

        if (healthValue.get() <= 0)
            return;

        var current = commandBuffer.getComponent(ref, Nameplate.getComponentType());
        if (current != null) {
            var oldText = current.getText();
            var strip = buildSuffixStripPattern();

            if (strip.matcher(oldText).find()) {
                if (oldText.equals(desiredText))
                    return;
                current.setText(desiredText);
            } else {
                current.setText(oldText + desiredText);
            }

            commandBuffer.putComponent(ref, Nameplate.getComponentType(), current);
        } else {
            commandBuffer.putComponent(ref, Nameplate.getComponentType(), new Nameplate(desiredText));
        }
    }

    private String formatNameplate(@NullableDecl String entityName, int level) {
        if (level <= 0)
            return null;

        var rawTemplate = config.get().getMobNameplate();
        var template = unescape(rawTemplate);

        if (template == null || template.isBlank())
            return null;

        if ((entityName == null || entityName.isBlank()) && template.contains("{name}")) {
            return null;
        }

        return template
            .replace("{level}", Integer.toString(level))
            .replace("{name}", entityName == null ? "" : entityName);
    }

    private static String unescape(String s) {
        if (s == null)
            return null;
        return s.replace("\\n", "\n").replace("\\t", "\t");
    }

    private Pattern buildSuffixStripPattern() {
        var rawTemplate = config.get().getMobNameplate();
        if (rawTemplate == null || rawTemplate.isBlank()) {
            return Pattern.compile("(?!)");
        }

        var regex = Pattern.quote(rawTemplate)
            .replace("{level}", "\\E\\d+\\Q")
            .replace("{name}", "\\E.*?\\Q")
            .replace(" \\\\n", "\\E\\s*\\Q")
            .replace("\\\\n", "\\E\\s*\\Q");

        return Pattern.compile(regex + "$", Pattern.DOTALL);
    }
}
