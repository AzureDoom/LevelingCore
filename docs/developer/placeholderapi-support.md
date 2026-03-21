---
title: "PlaceholderAPI Support"
order: 14
published: true
draft: false
---

LevelingCore includes native support for PlaceholderAPI from v0.9.5+, allowing you to display player leveling data dynamically in chat messages, UI text, scoreboards, and more.

This integration works with the Placeholder API mod: https://www.curseforge.com/hytale/mods/placeholder-api

## Placeholder Format

All placeholders provided by LevelingCore follow this format:
```java
%levelingcore_variable%
```
Replace `variable` with one of the supported identifiers listed below.

## Supported Placeholders

### Level & Experience

| Placeholder                  | Description                         |
|------------------------------|-------------------------------------|
| `%levelingcore_level%`       | Player’s current level              |
| `%levelingcore_xp%`          | Player’s current experience points  |
| `%levelingcore_xp_to_level%` | XP required to reach the next level |

### Ability Points

| Placeholder                               | Description              |
|-------------------------------------------|--------------------------|
| `%levelingcore_ability_points%`           | Total ability points     |
| `%levelingcore_available_ability_points%` | Available ability points |

### Player Stats

| Placeholder          | Description  |
|----------------------|--------------|
| `%levelingcore_str%` | Strength     |
| `%levelingcore_agi%` | Agility      |
| `%levelingcore_per%` | Perception   |
| `%levelingcore_vit%` | Vitality     |
| `%levelingcore_int%` | Intelligence |
| `%levelingcore_con%` | Constitution |

## Usage Examples

Basic Chat Message:
```java
player.sendMessage(
    PlaceholderAPI.setPlaceholders(
        player.getPlayerRef(),
        Message.raw("Welcome %player_name%! You are level %levelingcore_level%.")
    )
);
```

Stats Display Example:
```java
player.sendMessage(
    PlaceholderAPI.setPlaceholders(
        player.getPlayerRef(),
        Message.raw("STR: %levelingcore_str% | AGI: %levelingcore_agi% | VIT: %levelingcore_vit%")
    )
);
```

Ability Points Example:
```java
player.sendMessage(
    PlaceholderAPI.setPlaceholders(
        player.getPlayerRef(),
        Message.raw("You have %levelingcore_ability_points% ability points available.")
    )
);
```

## Notes

- Placeholders are resolved per-player

- Placeholder names are case-insensitive

- You must pass a valid `PlayerRef` to `PlaceholderAPI.setPlaceholders(...)`

- `%player_name%` is provided by PlaceholderAPI itself via https://ecloud.placeholderapi.com/expansions/player-hytale/, not LevelingCore