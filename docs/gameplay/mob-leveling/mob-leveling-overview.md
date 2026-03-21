---
title: "Mob Leveling Overview"
order: 11
published: true
draft: false
---

The Mob Leveling system dynamically assigns and updates levels for NPC mobs based on configurable rules and in-world context. This allows mobs to scale naturally with player progression, location, or instance difficulty while remaining performant and predictable.

## How Mob Levels Are Assigned

When a mob is first encountered, it is given an initial **spawn level**. This level is persisted and periodically recalculated depending on the configured leveling mode.

Key characteristics:

- Mob levels are stored persistently per entity
- Levels are recalculated at a fixed interval (every ~30 seconds)
- Levels are clamped between **1** and the configured **maximum mob level**
- Scaling is only re-applied when the mob's level actually changes

## Spawn Level

On the first creation, a mob is assigned a deterministic random level:

- Range: **1–10**
- Seeded by the mob's UUID
- Ensures consistent spawn levels across restarts

This spawn level is used immediately and may later be overridden by dynamic recalculation.

## Dynamic Recalculation

Mob levels are recalculated periodically based on the configured **Level Mode**. If a mob is locked, recalculation is skipped.

Recalculation includes:

1. Computing the new level based on the active mode
2. Clamping the level to the configured maximum
3. Applying stat scaling if the level changed

## Mob Scaling

When a mob's level changes, its stats are scaled accordingly.

Currently applied scaling:

- **Health Scaling**
    - Health increases linearly per level
    - Formula:
        - `1 + (level - 1) * MobHealthMultiplier`
    - Applied as a max-health modifier

- **Damage Scaling**
    - Damage is modified when an NPC mob damages a player.
    - Two multipliers exist depending on the damage type:
        - Melee: MobDamageMultiplier
        - Projectile/Ranged: MobRangeDamageMultiplier
    - Player mitigation via CON:
        - CON reduces incoming damage by a flat multiplier:
        - ConMultiplier = 1 - clamp(CON, 0, ∞) capped at ConStatMultiplier
        - (In code: 1.0f - min(ConStatMultiplier, max(0, CON)))
    - Mob level factor:
        - The attacker mob’s level is fetched (or computed on spawn) and applied as a direct multiplier.
        - Current behavior: damage scales linearly with level by multiplying by mobLevel directly (so level 1 = ×1, level 2 = ×2, etc.).
    - Final formulas (current implementation):
        - Projectile/Ranged:
            - FinalDamage = BaseDamage * ConMultiplier * MobRangeDamageMultiplier * MobLevel
        - Melee:
            - FinalDamage = BaseDamage * ConMultiplier * MobDamageMultiplier * MobLevel

## Persistence & Performance

- Mob levels are saved automatically at intervals
- Recalculation is throttled to prevent excessive computation
- Scaling is idempotent and only applied when necessary

This design ensures the system remains efficient even in high-entity environments.