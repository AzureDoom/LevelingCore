---
title: "Elite and Boss Entity Mapping"
order: 12
published: true
draft: false
---

# Elite and Boss Entity Mapping

This page explains how Elite and Boss entities are determined and mapped within the LevelingCore system, including how tiers are resolved, how boss mappings work, and how they influence leveling and XP systems.

---

## Overview

Mob classification is handled through three tiers:

- **NORMAL**
- **ELITE**
- **BOSS**

These tiers directly determine which `MobEncounterProfile` is applied, affecting:

- Health scaling
- Damage scaling
- XP rewards

---

## Mob Encounter Profiles

Each tier maps to a predefined profile:

| Tier   | Profile                      | Description          |
|--------|------------------------------|----------------------|
| NORMAL | `MobEncounterProfile.NORMAL` | Baseline mob stats   |
| ELITE  | `MobEncounterProfile.ELITE`  | Increased difficulty |
| BOSS   | `MobEncounterProfile.BOSS`   | Highest difficulty   |

Profiles define multipliers:
- Health
- Damage
- Damage threshold
- XP multiplier

These values are pulled from the configuration at runtime.

### Mermaid: Profile Mapping

```mermaid
flowchart TD
    A[MobTier.NORMAL] --> B[MobEncounterProfile.NORMAL]
    C[MobTier.ELITE] --> D[MobEncounterProfile.ELITE]
    E[MobTier.BOSS] --> F[MobEncounterProfile.BOSS]

    classDef normal fill:#11a2af,color:#000
    classDef elite fill:#f59e0b,color:#000
    classDef boss fill:#ef4444,color:#fff

    class A,B normal
    class C,D elite
    class E,F boss
```

---

## Boss Mapping System

Bosses are determined by a **CSV-driven mapping system**.

### File Location

```text
/mods/com.azuredoom_levelingcore/data/config/mobbossmapping.csv
```

### Format

```csv
instance,boss_name,lvl
```

### Example Boss Mapping Config

```csv
# Exact instance match
dungeon_fire,fire_dragon,50

# Wildcard instance match
raid_*,ancient_guardian,75
```

### How It Works

- Instance name + NPC type ID are matched
- Supports wildcard matching (`*`)
- Most specific match wins

```java
MobBossMapping.findLevel(rules, instanceName, bossName)
```

If:
- A match is found → the entity is a **BOSS**
- No match → not a boss

### Mermaid: Boss Mapping Lookup

```mermaid
flowchart TD
    A[Get world instance name] --> B[Get NPC type ID]
    B --> C[MobBossMapping.findLevel]
    C --> D{Mapped level >= 0?}
    D -- Yes --> E[Entity is BOSS]
    D -- No --> F[Entity is not BOSS]

    classDef process fill:#14b8a6,color:#000
    classDef decision fill:#3b82f6,color:#fff
    classDef success fill:#22c55e,color:#000
    classDef fail fill:#d1d5db,color:#000

    class A,B,C process
    class D decision
    class E success
    class F fail
```

---

## Boss Detection Logic

From resolver:

```java
int mappedLevel = MobBossMapping.findLevel(...);
return mappedLevel >= 0;
```

Key points:

- Boss status is **data-driven**
- Level is also derived from mapping
- Invalid or missing mappings return `-1` fileciteturn0file1turn0file3

---

## Elite Detection Logic

Elite detection supports multiple sources:

1. **EliteMobs plugin**
2. **RPGMobs plugin**
3. **Fallback config list**

```java
if (EliteMobs installed) → use compat
else if (RPGMobs installed) → use compat
else → config.isEliteMob(...)
```

### Mermaid: Elite Detection

```mermaid
flowchart TD
    A[Start isElite] --> B{EliteMobs plugin present?}
    B -- Yes --> C[EliteMobsCompat]
    B -- No --> D{RPGMobs plugin present?}
    D -- Yes --> E[RPGMobsCompat]
    D -- No --> F[Config fallback]

    classDef process fill:#14b8a6,color:#000
    classDef decision fill:#3b82f6,color:#fff
    classDef plugin fill:#a78bfa,color:#000
    classDef fallback fill:#d1d5db,color:#000

    class A process
    class B,D decision
    class C,E plugin
    class F fallback
```

---

## Integration with Mob Level System

The resolved profile and tier feed into:

**MobLevelSystem**

### Key Behavior

- Tier determines scaling multipliers
- Boss mappings can **override level and lock it**

```java
var bossLevel = computeBossOverrideLevel(...);
if (bossLevel > 0 && !data.locked) {
    data.level = bossLevel;
    data.locked = true;
}
```

Implications:

- Bosses ignore dynamic scaling after assignment
- They retain a fixed difficulty
- Locked boss levels are persisted for consistency

### Mermaid: Level Override Flow

```mermaid
flowchart TD
    A[Mob spawns] --> B[Resolve profile]
    B --> C[Compute boss override]
    C --> D{bossLevel > 0?}
    D -- Yes --> E[Set boss level]
    E --> F[Lock mob data]
    F --> G[Persist level]
    D -- No --> H[Dynamic scaling]

    classDef process fill:#14b8a6,color:#000
    classDef decision fill:#3b82f6,color:#fff
    classDef boss fill:#ef4444,color:#fff
    classDef dynamic fill:#9ca3af,color:#000

    class A,B,C process
    class D decision
    class E,F,G boss
    class H dynamic
```

---

## XP Scaling Impact

XP calculation is influenced by:

- Mob level
- Profile XP multiplier
- Config multipliers

### Simplified Formula

```text
XP = base * (level^multiplier) * (xpMultiplier^multiplier)
```

Where:
- `base` comes from either configured XP mappings or health-derived XP
- `level` comes from the resolved mob level
- `xpMultiplier` comes from the encounter profile (NORMAL / ELITE / BOSS) fileciteturn0file5

### Mermaid: XP Calculation Flow

```mermaid
flowchart TD
    A[Mob dies] --> B[Resolve level data]
    B --> C[Determine base XP]
    C --> D[Apply level scaling]
    D --> E[Apply profile multiplier]
    E --> F[Grant XP]

    classDef process fill:#24e8a6,color:#000
    classDef reward fill:#22c55e,color:#000

    class A,B,C,D,E process
    class F reward
```

---

## End-to-End Flow

```mermaid
flowchart TD
    A[NPC encountered] --> B[Resolve tier]
    B --> C{Boss?}
    C -- Yes --> D[Use BOSS profile]
    C -- No --> E{Elite?}
    E -- Yes --> F[Use ELITE profile]
    E -- No --> G[Use NORMAL profile]
    D --> H[Apply scaling]
    F --> H
    G --> H
    H --> I[Mob defeated]
    I --> J[Calculate XP]

    classDef boss fill:#ef4444,color:#fff
    classDef elite fill:#f59e0b,color:#000
    classDef normal fill:#9ca3af,color:#000
    classDef process fill:#14b8a6,color:#000
    classDef decision fill:#3b82f6,color:#fff
    classDef reward fill:#22c55e,color:#000

    class A,B,H,I process
    class C,E decision
    class D boss
    class F elite
    class G normal
    class J reward
```

---
