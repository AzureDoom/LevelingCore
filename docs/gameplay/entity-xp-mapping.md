---
title: "Entity XP Mapping"
order: 11
published: true
draft: false
---

The xpmapping.csv file allows you to define custom XP rewards per NPC type.
This lets you override the default XP calculation for specific NPCs while leaving all others unchanged.

## File Location
```
/mods/com.azuredoom_levelingcore/data/config/xpmapping.csv
```

# File Format

The file uses standard CSV (Comma-Separated Values) formatting with a required header row.

## Header
```csv
npctypeid,xp
```

| Column Name | Description                                            |
|-------------|--------------------------------------------------------|
| npctypeid   | The NPC type identifier (case-sensitive)               |
| xp          | The fixed amount of XP awarded when this NPC is killed |

## Example Configuration

```csv
npctypeid,xp
Antelope,3
Archaeopteryx,2
```

In this example:
- Killing an Antelope grants 3 XP
- Killing an Archaeopteryx grants 2 XP

# Default Behavior (Missing Entries)
If an NPC does not appear in xpmapping.csv, its XP value will be calculated automatically using the health-based XP formula.

This means:
- You only need to list NPCs you want to override

### ⚠️ Default NPC Coverage (Effective Jan 18, 2026)

> [!IMPORTANT]
> As of **January 18, 2026**, all **default NPCs** are explicitly defined in `xpmapping.csv`.

Because of this:
- Default NPCs will **no longer fall back** to health-based XP calculation
- Health-based XP is only used if:
    - An NPC is truly custom or mod-added and not listed, **or**
    - The configuration is explicitly set to use health-based XP only

If you want default NPCs to use health-based XP again, you must remove or override their entries or adjust the relevant configuration option.

# Notes
- NPC type IDs must match their internal identifiers exactly
- The file is read on startup (restart required after changes)
- Duplicate npctypeid entries may cause unexpected behavior
- XP values should be positive integers