package com.azuredoom.levelingcore.compat.classescore;

import com.azuredoom.classescore.ClassesCore;
import com.azuredoom.classescore.api.ClassesCoreAPI;
import com.azuredoom.classescore.data.ClassRegistry;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.systems.items.ItemTooltipMetadataManager;

public class ClassesCoreCompat {

    private static final Set<String> processedItems = ConcurrentHashMap.newKeySet();

    private ClassesCoreCompat() {}

    /**
     * Retrieves the display name of the player's selected class based on the provided UUID. If the player has not
     * selected a class, an empty string is returned.
     *
     * @param playerUUID the unique identifier (UUID) of the player whose class is being retrieved.
     * @return the display name of the player's selected class, or an empty string if no class is selected.
     */
    public static String getPlayerClass(UUID playerUUID) {
        var selectedClass = ClassesCoreAPI.getSelectedClass(playerUUID);
        if (selectedClass.isEmpty()) {
            return "";
        }
        return selectedClass.get().displayName();
    }

    public static void tryScanForItems() {
        try {
            scanForItems();
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            LevelingCore.LOGGER.atWarning().withCause(e).log("ClassesCore Not Loaded");
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("ClassesCore compat item tooltip scan failed");
        }
    }

    private static void scanForItems() {
        try {
            for (var item : Item.getAssetMap().getAssetMap().values()) {
                var itemId = item.getId();

                if (itemId == null || processedItems.contains(itemId)) {
                    continue;
                }

                if (processItem(item)) {
                    processedItems.add(itemId);
                }
            }
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("ClassesCore class tooltip scan failed");
        }
    }

    private static boolean processItem(Item item) {
        try {
            var itemId = item.getId();
            if (itemId == null || itemId.isBlank()) {
                return false;
            }

            var allowedClasses = findAllowedClassesForItem(itemId);

            if (allowedClasses.isEmpty()) {
                return false;
            }

            ItemTooltipMetadataManager.INSTANCE.addTooltipLine(
                itemId,
                "Classes allowed: " + String.join(", ", allowedClasses)
            );

            return true;
        } catch (Exception e) {
            LevelingCore.LOGGER.atWarning()
                .withCause(e)
                .log("Failed to process ClassesCore tooltip for item " + item.getId());
            return false;
        }
    }

    private static List<String> findAllowedClassesForItem(String itemId) {
        var result = new ArrayList<String>();

        for (var classDef : ClassesCore.getClassRegistryIfPresent().map(ClassRegistry::all).orElse(List.of())) {
            if (classDef == null || classDef.equipmentRules() == null) {
                continue;
            }

            var allowed = classDef.equipmentRules().isWeaponAllowed(itemId) ||
                classDef.equipmentRules().isArmorAllowed(itemId);

            if (allowed) {
                result.add(classDef.displayName());
            }
        }

        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}
