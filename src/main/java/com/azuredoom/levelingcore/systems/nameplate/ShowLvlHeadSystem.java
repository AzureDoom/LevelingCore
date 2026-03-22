package com.azuredoom.levelingcore.systems.nameplate;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Arrays;
import java.util.Objects;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.compat.classescore.ClassesCoreCompat;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;

@SuppressWarnings("removal")
public class ShowLvlHeadSystem implements Runnable {

    private final Config<GUIConfig> config;

    /**
     * A marker string used to identify and differentiate nameplate components in the system.
     * This marker is composed of zero-width Unicode characters (\u200B, \u200C, \u200D)
     * that are invisible in rendered output but can be used for internal processing
     * or as separators in nameplate configuration and formatting.
     * <p>
     * This constantly enables the system to embed or parse hidden metadata within
     * strings associated with nameplates, ensuring that visual elements remain
     * unaffected while still allowing for structured data handling.
     */
    private static final String NAMEPLATE_MARKER = "\u200B\u200C\u200D";

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

    /**
     * Processes all entities within the world to update or insert nameplates for players and NPCs. The method iterates
     * through entity chunks, extracts relevant data, computes levels, and displays updated information based on the
     * game configuration and locale settings. Blacklisted entities are ignored. This method also accounts for optional
     * plugins and localization.
     *
     * @param world the world object containing entity data and stores; must not be null.
     */
    private void tickWorld(World world) {
        var store = world.getEntityStore().getStore();
        if (store == null) {
            return;
        }

        var guiConfig = config.get();
        var levelingServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
        if (levelingServiceOpt.isEmpty()) {
            return;
        }

        var levelingService = levelingServiceOpt.get();
        var hasClassesCore = PluginManager.get()
            .getPlugin(new PluginIdentifier("com.azuredoom", "classescore")) != null;

        var blacklisted = new java.util.HashSet<>(Arrays.asList(guiConfig.getBlacklistedMobs()));
        final String[] localeHolder = { "en-US" };

        store.forEachChunk(PlayerRef.getComponentType(), (chunk, commandBuffer) -> {
            var size = chunk.size();
            for (var i = 0; i < size; i++) {
                var ref = chunk.getReferenceTo(i);
                var playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null) {
                    continue;
                }

                localeHolder[0] = playerRef.getLanguage();
                var lvl = levelingService.getLevel(playerRef.getUuid());
                var className = hasClassesCore ? ClassesCoreCompat.getPlayerClass(playerRef.getUuid()) : null;

                insertNameplate(
                    commandBuffer,
                    ref,
                    playerRef.getUsername(),
                    formatNameplate(
                        guiConfig.getMobNameplate(),
                        playerRef.getUsername(),
                        guiConfig.isShowPlayerLvls() ? lvl : 0,
                        className,
                        false
                    )
                );
            }
        });

        store.forEachChunk(NPCEntity.getComponentType(), (chunk, commandBuffer) -> {
            var size = chunk.size();
            for (var i = 0; i < size; i++) {
                var ref = chunk.getReferenceTo(i);
                var npc = commandBuffer.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
                if (npc == null) {
                    continue;
                }

                if (blacklisted.contains(npc.getNPCTypeId())) {
                    continue;
                }

                var npcRole = npc.getRole();
                if (npcRole == null) {
                    continue;
                }

                var finalLocale = localeHolder[0] == null ? "en-US" : localeHolder[0];
                var entityName = I18nModule.get()
                    .getMessage(
                        finalLocale,
                        npcRole.getNameTranslationKey()
                    );

                var lvl = LevelingCore.mobLevelRegistry.getOrCreateWithPersistence(
                    npc.getUuid(),
                    () -> MobLevelingUtil.computeSpawnLevel(npc),
                    0,
                    LevelingCore.mobLevelPersistence
                );
                if (lvl == null) {
                    continue;
                }

                insertNameplate(
                    commandBuffer,
                    ref,
                    entityName,
                    formatNameplate(
                        guiConfig.getMobNameplate(),
                        entityName,
                        guiConfig.isShowMobLvls() ? lvl.level : 0,
                        null,
                        true
                    )
                );
            }
        });
    }

    /**
     * Updates or inserts a nameplate component for an entity based on the provided base name and suffix. If the suffix
     * is blank or null, only the base name is considered. If the entity lacks sufficient health or other required
     * conditions are not met, the nameplate update is skipped.
     *
     * @param commandBuffer the buffer used to manipulate entity components; must not be null.
     * @param ref           a reference to the target entity whose nameplate needs to be updated; must not be null.
     * @param baseName      the base name to display on the nameplate; may be null, in which case it defaults to an
     *                      empty string.
     * @param desiredSuffix the suffix to append to the base name; if null or blank, only the base name is used.
     */
    private void insertNameplate(
        CommandBuffer<EntityStore> commandBuffer,
        Ref<EntityStore> ref,
        String baseName,
        String desiredSuffix
    ) {
        var current = commandBuffer.getComponent(ref, Nameplate.getComponentType());

        var safeBaseName = baseName == null ? "" : baseName;

        if (desiredSuffix == null || desiredSuffix.isBlank()) {
            if (current != null) {
                if (!safeBaseName.equals(current.getText())) {
                    current.setText(safeBaseName);
                    commandBuffer.putComponent(ref, Nameplate.getComponentType(), current);
                }
            } else if (!safeBaseName.isBlank()) {
                commandBuffer.putComponent(ref, Nameplate.getComponentType(), new Nameplate(safeBaseName));
            }
            return;
        }

        var entityStatMap = commandBuffer.getComponent(ref, EntityStatMap.getComponentType());
        if (entityStatMap == null) {
            return;
        }

        var healthValue = entityStatMap.get(DefaultEntityStatTypes.getHealth());
        if (healthValue == null || healthValue.get() <= 0) {
            return;
        }

        var newText = safeBaseName + NAMEPLATE_MARKER + desiredSuffix;

        if (current != null) {
            if (newText.equals(current.getText())) {
                return;
            }

            current.setText(newText);
            commandBuffer.putComponent(ref, Nameplate.getComponentType(), current);
        } else {
            commandBuffer.putComponent(ref, Nameplate.getComponentType(), new Nameplate(newText));
        }
    }

    /**
     * Formats a nameplate string by replacing placeholders with appropriate values such as entity name, level, and
     * class name. The formatting includes actions such as uncapping the input template, resolving placeholders, and
     * cleaning up unnecessary whitespace or empty lines.
     *
     * @param rawTemplate           the raw template string containing placeholders such as {name}, {level}, and
     *                              {class}. May include escaped newline or tab characters that will be unescaped.
     * @param entityName            the name of the entity to be included in the formatted output. If {@code null}, the
     *                              name placeholder will be replaced accordingly based on
     *                              {@code includeNameInTemplate}.
     * @param level                 the level to be injected into the template. Must be greater than 0; otherwise, the
     *                              method will return {@code null}.
     * @param className             the class name to be included in the formatted output. If {@code null} or blank,
     *                              relevant placeholders in the template will be removed.
     * @param includeNameInTemplate a flag indicating whether the entity name should be included in the formatted
     *                              output. If {@code false}, the name placeholder will be omitted or replaced with an
     *                              empty string.
     * @return the formatted nameplate string with placeholders replaced by corresponding data, or {@code null} if the
     *         input template is invalid, empty, or the level is lower than or equal to 0.
     */
    private String formatNameplate(
        String rawTemplate,
        @NullableDecl String entityName,
        int level,
        String className,
        boolean includeNameInTemplate
    ) {
        if (level <= 0) {
            return null;
        }

        var template = unescape(rawTemplate);

        if (template == null || template.isBlank()) {
            return null;
        }

        var resolvedName = includeNameInTemplate
            ? (entityName == null ? "" : entityName)
            : "";

        if (className == null || className.isBlank()) {
            template = template
                .replace("[{class}] - ", "")
                .replace("[{class}]- ", "")
                .replace("[{class}] ", "")
                .replace("[{class}]", "");
        }

        var result = template
            .replace("{level}", Integer.toString(level))
            .replace("{name}", resolvedName)
            .replace("{class}", className == null ? "" : className);

        var cleaned = new StringBuilder();
        for (var line : result.split("\\R", -1)) {
            if (!line.isBlank()) {
                if (!cleaned.isEmpty()) {
                    cleaned.append('\n');
                }

                var normalizedLine = line.replaceAll(" {2,}", " ");
                cleaned.append(normalizedLine);
            }
        }

        return cleaned.isEmpty() ? null : cleaned.toString();
    }

    /**
     * Replaces escaped newline and tab characters in the input string with their actual values.
     *
     * @param s the string to unescape; may contain escaped newline (\n) or tab (\t) characters. If the input string is
     *          null, the method returns null.
     * @return a new string with escaped newline and tab characters replaced by their actual values, or null if the
     *         input string is null.
     */
    private static String unescape(String s) {
        if (s == null)
            return null;
        return s.replace("\\n", "\n").replace("\\t", "\t");
    }
}
