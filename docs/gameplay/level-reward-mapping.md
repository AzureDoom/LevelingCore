---
title: "Level Reward Mapping"
order: 11
published: true
draft: false
---

This file controls **which items players receive when they reach specific levels**. This is default off and changed in the main config at `/mods/com.azuredoom_levelingcore/levelingcore.json` by changing `EnableLevelUpRewardsConfig` to true.

Each row in the CSV defines **one reward**. Multiple rewards can be assigned to the same level by adding multiple rows with the same `lvl` value.

---

## File Location

The file is automatically created on first startup if it does not exist:
`/mods/com.azuredoom_levelingcore/data/config/levelrewardmapping.csv`

If deleted, the file will be recreated with default values on the next launch.

---

# CSV Format

## Header (Required)

```csv
itemnameid,quantity,lvl
```

## Column Definitions</h2>

| Column     | Description                                     |
|------------|-------------------------------------------------|
| itemnameid | The internal item ID used by the game           |
| quantity   | How many of the item the player receives        |
| lvl        | The player level required to receive the reward |

## Example (Default Configuration)
```csv
itemnameid,quantity,lvl
Ore_Copper,3,10
Ingredient_Fibre,16,10
Ore_Iron,2,15
Weapon_Arrow_Crude,8,15
```

### What this does:

At level 10, the player receives:
- 3 × Copper Ore
- 16 × Fibre

At level 15, the player receives:
- 2 × Iron Ore
- 8 × Crude Arrows

## Multiple Rewards Per Level

You can assign any number of rewards to the same level by repeating the level value:
```cvs
itemnameid,quantity,lvl
Ore_Silver,2,20
Ingredient_Leather,5,20
Weapon_Sword_Iron,1,20
```
All three rewards will be granted when the player reaches level 20.

## Item IDs (itemnameid)
- The itemnameid must match the in-game item ID exactly
- Item IDs can be viewed in creative mode
- ⚠ There is currently no official or online master list of item IDs
- IDs are case-sensitive