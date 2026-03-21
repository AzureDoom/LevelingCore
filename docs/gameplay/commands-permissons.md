---
title: "Commands & Permissions"
order: 12
published: true
draft: false
---

## `/showstats`
Opens the player stats GUI, allowing players to view their current leveling stats. This command is only available if the leveling system is initialized and stat points are enabled in the configuration.

### Usage
`/showstats`

### Permission
`levelingcore.showstats`

## `/setlevel`
Sets a player’s level to an exact value. This directly overwrites the player’s current level.

### Usage
`/setlevel player level`

### Required Arguments
`player` — Target player

`level` — Level to set the player to

### Permission
`levelingcore.setlevel`

## `/addlevel`
Adds a specified number of levels to a player.

### Usage
`/addlevel <player> <level>`

### Required Arguments
`player` — Target player

`level` — Number of levels to add

### Permission
`levelingcore.addlevel`

## `/removelevel`
Removes a specified number of levels from a player.

### Usage
`/removelevel player level`

### Required Arguments
`player` — Target player

`level` — Number of levels to remove

### Permission
`levelingcore.removelevel`

## `/addxp`
Adds experience points (XP) to a player and recalculates their level accordingly.

### Usage
`/addxp player xpvalue`

### Required Arguments
`player` — Target player

`xpvalue` — Amount of XP to add

### Permission
`levelingcore.addxp`

## `/removexp`
Removes experience points (XP) from a player and updates their level if necessary.

### Usage
`/removexp player xpvalue`

### Required Arguments
`player` — Target player
`xpvalue` — Amount of XP to remove

### Permission
`levelingcore.removexp`

## `/addstats`
Adds a specified amount of stat value to a player.

### Usage
`/addstats player stat value`

### Required Arguments
`player` — The target player who will receive the stat points.

`stat` — The stat to increase. Valid values:
- `str` — Strength
- `agi` — Agility
- `per` — Perception
- `vit` — Vitality
- `int` — Intelligence
- `con` — Constitution

`value` — The number of stat points to add.

### Permission
`levelingcore.addstats`

## `/removestats`
Removes a specified amount of stat value from a player.

### Usage
`/removestats player stat value`

### Required Arguments
`player` — The target player who will lose the stat points.

`stat` — The stat to decrease. Valid values:
- `str` — Strength
- `agi` — Agility
- `per` — Perception
- `vit` — Vitality
- `int` — Intelligence
- `con` — Constitution

`value` — The number of stat points to remove.

### Permission
`levelingcore.removestats`

## `/setstats`
Sets a specified amount of stat value to a player.

### Usage
`/setstats player stat value`

### Required Arguments
`player` — The target player who will receive the stat points.

`stat` — The stat to set. Valid values:
- `str` — Strength
- `agi` — Agility
- `per` — Perception
- `vit` — Vitality
- `int` — Intelligence
- `con` — Constitution

`value` — The number of stat points to set.

### Permission
`levelingcore.setstats`