---
title: "Mob Leveling Modes"
order: 16
published: true
draft: false
---

Mob Leveling Modes define *how* a mob's level is dynamically calculated. These modes allow server owners to tailor difficulty based on players, geography, or instances.

The active mode is controlled via the `LevelModeArray` configuration value.

### Behavior

- Levels are weighted
  - INSTANCE -> 0.70
  - ZONE -> 0.60
  - NEARBY_PLAYERS_MEAN -> 0.10
  - BIOME -> 0.40
  - ENVIRONMENT -> 0.50
  - SPAWN_ONLY -> 0.20
- Levels are randomized with the variance defined in the `LevelVariance` configuration value

## SPAWN_ONLY

**Description:**  
Mobs keep their original spawn level permanently.

**Behavior:**
- Uses the level assigned at spawn
- No dynamic recalculation
- Ideal for static or RPG-style worlds

**Use Cases:**
- Fixed difficulty zones
- Hand-balanced encounters

---

## NEARBY_PLAYERS_MEAN

**Description:**  
Mob level is based on the average level of nearby players.

**Behavior:**
- Detects players within a 40-block radius
- Computes the mean player level
- Rounds to the nearest whole number
- Falls back to level **5** if no players are nearby

**Use Cases:**
- Dynamic overworld scaling
- Co-op friendly encounters
- Prevents over- or under-leveled mobs

---

## BIOME

**Description:**  
Mob level is determined by the biome they are currently in.

**Behavior:**
- Reads the active biome name
- Uses biome-to-level mappings
- Defaults to level **1**
- If biome cannot be resolved, defaults to **6**

**Use Cases:**
- Thematic biome difficulty
- Progressive exploration

**Config Location:**
```
mods/com.azuredoom_levelingcore/data/config/mobbiomemapping.csv
```

**Config Example:**
```csv
biome,lvl
River_Plains_Smooth,5
```

---

## ZONE

**Description:**  
Mob level is based on the current world zone.

**Behavior:**
- Uses the active zone name
- Reads from zone-to-level mappings
- Defaults to level **1**

**Use Cases:**
- MMO-style regions
- Structured progression maps

**Config Location:**
```
mods/com.azuredoom_levelingcore/data/config/mobinstancemapping.csv
```

**Config Example:**
```csv
zone,lvl
Emerald_Wilds,5
```

---

## INSTANCE

**Description:**  
Mob level is based on the current world instance.

**Behavior:**
- Uses the instance/world name
- Reads from instance-to-level mappings
- Defaults to level **1**
- Logs a warning if the instance name is invalid

**Use Cases:**
- Dungeons
- Raids
- Scaled private instances

**Config Location:**
```
mods/com.azuredoom_levelingcore/data/config/mobzonemapping.csv
```

**Config Example:**
```csv
instance,lvl
default,5
```

---

## ENVIRONMENT

**Description:**  
Mob level is based on the mob’s current environment ID at its world position. The system reads the environment from the chunk the mob is in and maps it to a configured level value.

**Behavior:**
- Retrieves the mob’s world position
- Determines the chunk at that position
- Reads the environment ID from the chunk
- Resolves the environment asset name from the registry
- Defaults to level 1 if:
    - The chunk does not exist
    - The environment asset cannot be resolved
    - No mapping exists for the environment name
- Logs a warning when:
    - The chunk is missing
    - The environment asset is invalid or not found

**Use Cases:**
- Dungeons with fixed difficulty
- Raid environments
- Private or instanced content
- Custom world regions with defined difficulty tiers

**Config Location:**
```
mods/com.azuredoom_levelingcore/data/config/defaultmobenvironmentmapping.csv
```

**Config Example:**
```csv
env,lvl
env_zone1_plains,5
```

---

## Invalid or Unknown Modes

If an invalid `LevelMode` value is provided:

- A warning is logged
- The system automatically falls back to **NEARBY_PLAYERS_MEAN**

This ensures mobs always receive a valid level.