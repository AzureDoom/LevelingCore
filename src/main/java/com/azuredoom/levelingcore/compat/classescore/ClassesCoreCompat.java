package com.azuredoom.levelingcore.compat.classescore;

import com.azuredoom.classescore.api.ClassesCoreAPI;

import java.util.UUID;

public class ClassesCoreCompat {

    private ClassesCoreCompat() {}

    /**
     * Retrieves the display name of the player's selected class based on the provided UUID.
     * If the player has not selected a class, an empty string is returned.
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
}
