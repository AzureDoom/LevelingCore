---
title: "Item Stats Requirements"
order: 9
published: true
draft: false
---

This file controls **which items players are allowed to use or equip based on their level**.

This system is typically used to **gate stronger equipment behind progression** while still allowing items to be crafted, looted, or traded normally.

This is default off and changed in the main config at `/mods/com.azuredoom_levelingcore/levelingcore.json` by changing `EnableItemStatRequirement` to true.

---

## File Location

The item level requirement file is automatically created on the first startup if it does not exist:

```
/mods/com.azuredoom_levelingcore/data/config/itemstatrequirements.csv
```

If deleted, the file will be recreated with default values on the next launch.

---

## CSV Format

### Header (Required)

```csv
itemnameid,str,agi,per,vit,int,con
```

### Column Definitions

| Column     | Description                                            |
|------------|--------------------------------------------------------|
| itemnameid | The internal item ID used by the game (case-sensitive) |
| str        | Required Strength                                      |
| agi        | Required Agility                                       |
| per        | Required Perception                                    |
| vit        | Required Vitality                                      |
| int        | Required Intelligence                                  |
| con        | Required Constitution                                  |

Any stat set to 0 means no requirement for that stat.

---

## Example Configuration

```csv
itemnameid,str,agi,per,vit,int,con
Weapon_Sword_Adamantite,10,0,0,5,0,0
Weapon_Bow_Hunter,0,8,12,0,0,0
Armor_Chestplate_Titan,15,0,0,10,0,5
```

### What this does:

- Weapon_Sword_Adamantite
> Requires STR 10 and VIT 5

- Weapon_Bow_Hunter
> Requires AGI 8 and PER 12

- Armor_Chestplate_Titan
> Requires STR 15, VIT 10, and CON 5

---

## Item IDs (`itemnameid`)

- Must match the **exact in-game item ID**
- Case-sensitive
- Can be viewed in creative mode
- ⚠ There is currently no official online master list of item IDs