---
title: "Item Level Requirements"
order: 8
published: true
draft: false
---

This file controls **which items players are allowed to use or equip based on their level**.

This system is typically used to **gate stronger equipment behind progression** while still allowing items to be crafted, looted, or traded normally.

This is default off and changed in the main config at `/mods/com.azuredoom_levelingcore/levelingcore.json` by changing `EnableItemLevelRestriction` to true.

---

## File Location

The item level requirement file is automatically created on the first startup if it does not exist:

```
/mods/com.azuredoom_levelingcore/data/config/itemlevelrequirements.csv
```

If deleted, the file will be recreated with default values on the next launch.

---

## CSV Format

### Header (Required)

```csv
itemnameid,lvl
```

### Column Definitions

| Column     | Description                                                    |
|------------|----------------------------------------------------------------|
| itemnameid | The internal item ID used by the game (case-sensitive)         |
| lvl        | The **minimum player level required** to use or equip the item |

---

## Example Configuration

```csv
itemnameid,lvl
Weapon_Club_Iron,5
Weapon_Longsword_Iron,5
Weapon_Spear_Iron,5
Weapon_Staff_Iron,5
Weapon_Axe_Iron_Rusty,5
Weapon_Club_Iron_Rusty,5
```

### What this does:

- All listed weapons **cannot be used before level 5**
- Players may still:
    - Craft the items
    - Loot the items
    - Trade the items
- The restriction only applies when attempting to **use** the item

---

## Multiple Items Per Level

You can assign **any number of items** to the same required level by repeating the level value:

```csv
itemnameid,lvl
Weapon_Sword_Iron,10
Weapon_Axe_Iron,10
Weapon_Spear_Iron,10
```

All three items will require **level 10** to use.

---

## Item IDs (`itemnameid`)

- Must match the **exact in-game item ID**
- Case-sensitive
- Can be viewed in creative mode
- ⚠ There is currently no official online master list of item IDs