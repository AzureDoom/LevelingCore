package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import com.azuredoom.levelingcore.level.mobs.MobLevelData;

/**
 * Represents a pending update for an NPC entity, encapsulating information related to its transformation and associated
 * level data. This record is used to manage updates to an NPC's state, including positional changes, entity-specific
 * attributes, and level-based data.
 *
 * @param npc       The NPC entity that is associated with this update.
 * @param transform The transformation component describing the positional and orientational data of the NPC.
 * @param data      The level data associated with the NPC, including attributes like health, damage, experience
 *                  multipliers, and more.
 */
public record PendingUpdate(
    NPCEntity npc,
    TransformComponent transform,
    MobLevelData data
) {}
