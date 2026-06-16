package com.azuredoom.levelingcore.level.itemlevellock;

/**
 * A record representing the stat requirements for using an item. Each stat defines the minimum value a player must have
 * to meet the requirement.
 *
 * @param str          strength
 * @param agi          agility
 * @param per          perception
 * @param vit          vitality
 * @param intelligence
 * @param con          constitution
 */
@SuppressWarnings("unused")
public record ItemStatRequirement(
    int str,
    int agi,
    int per,
    int vit,
    int intelligence,
    int con
) {

    public static final ItemStatRequirement NONE =
        new ItemStatRequirement(0, 0, 0, 0, 0, 0);

    /**
     * Checks if the given player statistics meet or exceed the required statistics defined for an item.
     *
     * @param playerStr the strength of the player
     * @param playerAgi the agility of the player
     * @param playerPer the perception of the player
     * @param playerVit the vitality of the player
     * @param playerInt the intelligence of the player
     * @param playerCon the constitution of the player
     * @return {@code true} if all the player's statistics meet or exceed the required statistics; {@code false}
     *         otherwise
     */
    public boolean matches(
        int playerStr,
        int playerAgi,
        int playerPer,
        int playerVit,
        int playerInt,
        int playerCon
    ) {
        return playerStr >= str
            && playerAgi >= agi
            && playerPer >= per
            && playerVit >= vit
            && playerInt >= intelligence
            && playerCon >= con;
    }

    /**
     * Formats a string that describes the missing stat requirements a player needs to meet to satisfy the requirements
     * for an item. Each missing stat is included in the output with the required value and the player's current value.
     *
     * @param playerStr the strength of the player
     * @param playerAgi the agility of the player
     * @param playerPer the perception of the player
     * @param playerVit the vitality of the player
     * @param playerInt the intelligence of the player
     * @param playerCon the constitution of the player
     * @return a formatted string listing the missing stats and their values, or an empty string if no stats are missing
     */
    public String formatMissing(
        int playerStr,
        int playerAgi,
        int playerPer,
        int playerVit,
        int playerInt,
        int playerCon
    ) {
        var sb = new StringBuilder();

        appendIfMissing(sb, "STR", str, playerStr);
        appendIfMissing(sb, "AGI", agi, playerAgi);
        appendIfMissing(sb, "PER", per, playerPer);
        appendIfMissing(sb, "VIT", vit, playerVit);
        appendIfMissing(sb, "INT", intelligence, playerInt);
        appendIfMissing(sb, "CON", con, playerCon);

        return sb.toString();
    }

    /**
     * Appends a formatted string to the given {@code StringBuilder} to indicate the missing amount for a specific stat,
     * if the required amount is not met.
     *
     * @param sb   the {@code StringBuilder} to append the formatted string to
     * @param stat the name of the stat being checked (e.g., "STR", "AGI")
     * @param req  the required amount of the stat
     * @param have the current amount of the stat the player has
     */
    private static void appendIfMissing(StringBuilder sb, String stat, int req, int have) {
        if (have >= req)
            return;
        if (!sb.isEmpty())
            sb.append(", ");
        sb.append(stat).append(" ").append(req).append(" (have ").append(have).append(")");
    }
}
