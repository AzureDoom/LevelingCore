package com.azuredoom.levelingcore.compat.dynamictooltips;

import org.herolias.tooltips.api.DynamicTooltipsApiProvider;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.lang.CommandLang;

public class DynamicTooltipsLibCompat {

    private static boolean registered = false;

    private DynamicTooltipsLibCompat() {}

    /**
     * Registers dynamic tooltip translations for items based on their required levels. This method ensures the
     * registration happens only once per runtime by checking the `registered` flag.
     */
    public static void register() {
        if (registered)
            return;
        registered = true;

        var api = DynamicTooltipsApiProvider.get();
        if (api == null)
            return;

        for (var entry : LevelingCore.itemLevelMapping.entrySet()) {
            var itemId = entry.getKey();
            var requiredLevel = entry.getValue();

            api.addGlobalLine(itemId, "Level Requirement: " + requiredLevel);
        }
    }
}
