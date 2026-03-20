package com.azuredoom.levelingcore.systems.items.stats;

/**
 * A record representing a snapshot of a player's current statistics.
 *
 * @param str          The player's strength, which typically influences physical damage or carrying capacity.
 * @param agi          The player's agility, which often affects speed, evasion, or dexterity.
 * @param per          The player's perception, which may relate to awareness, accuracy, or detection capabilities.
 * @param vit          The player's vitality, which usually impacts health or durability.
 * @param intelligence The player's intelligence, which can contribute to magical ability, knowledge, or reasoning.
 * @param con          The player's constitution, generally affecting stamina or resistance to negative effects.
 */
public record PlayerStatSnapshot(
    int str,
    int agi,
    int per,
    int vit,
    int intelligence,
    int con
) {}
