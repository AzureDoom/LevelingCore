---
name: Normal Configuration
description: Standard configuration options.
author: AzureDoom
---

# Configuration File Location

LevelingCore’s configuration file is stored in the following location: `/mods/com.azuredoom_levelingcore/levelingcore.json`

## Configuration Keys

### `EnableXPLossOnDeath` (boolean)
**Default:** `false`

When enabled, players will lose a portion of their XP when they die.

**Notes**
- Works together with `XPLossPercentage` to determine how much XP is removed.
- If disabled, `XPLossPercentage` is effectively ignored.

---

### `XPLossPercentage` (double)
**Default:** `0.1`

The percentage of XP to remove on death **when** `EnableXPLossOnDeath` is enabled.

**Examples**
- `0.1` = lose **10%** XP on death
- `0.25` = lose **25%** XP on death

---

### `EnableDefaultXPGainSystem` (boolean)
**Default:** `true`

Toggles the “default XP gain” behavior. When enabled, the system applies the `DefaultXPGainPercentage` to whatever base XP gain logic you use.

**Notes**
- If disabled, `DefaultXPGainPercentage` is effectively ignored and XP gain should be handled elsewhere (custom logic, events, etc.).

---

### `DefaultXPGainPercentage` (double)
**Default:** `0.5`

Controls the default percentage multiplier/portion used by the XP gain system **when** `EnableDefaultXPGainSystem` is enabled.

**Examples**
- `0.5` = apply **50%** of the default XP gain amount
- `1.0` = apply **100%** of the default XP gain amount

---

### `EnableLevelDownOnDeath` (boolean)
**Default:** `false`

When enabled, death can trigger a **level decrease** (level down).

**Notes**
- You can restrict when level down applies using `MinLevelForLevelDown`.
- If disabled, `MinLevelForLevelDown` is effectively ignored.

---

### `EnableAllLevelsLostOnDeath` (boolean)
**Default:** `false`

When enabled, death causes the player to lose **all levels**.

**Important**
- This is an aggressive option. In most setups, you’ll want to ensure your death-handling logic clearly defines precedence between:
    - losing XP (`EnableXPLossOnDeath`)
    - losing a level (`EnableLevelDownOnDeath`)
    - losing all levels (`EnableAllLevelsLostOnDeath`)

A common rule is:
1. If `EnableAllLevelsLostOnDeath` is true → wipe levels
2. else if `EnableLevelDownOnDeath` is true → level down (respecting min level)
3. XP loss runs independently (if enabled)

(Adjust to match your actual implementation.)

---

### `MinLevelForLevelDown` (integer)
**Default:** `65`

Sets the minimum level required for level-down to occur **when** `EnableLevelDownOnDeath` is enabled.

**Example**
- If set to `65`, players below level 65 will not level down on death.

---

### `EnableLevelChatMsgs` (boolean)

**Default:** false

When enabled, players will receive chat messages when they gain or lose levels.

---

### `EnableLevelAndXPTitles` (boolean)

**Default:** true

When enabled, title popups are shown for level and XP changes.

---

### `ShowXPAmountInHUD` (boolean)

**Default:** true

When enabled, the current XP amount is displayed in the player HUD.

---

### `EnableStatLeveling` (boolean)

**Default:** true

When enabled, player stats scale with level.

> ⚠️ Disabling this prevents multiplier below from being used.

---

### `HealthLevelUpMultiplier` (double)

**Default:** 1.2

Multiplier applied to health increases on level-up.

Example

1.2 = increase health by 20% per level

---

### `StaminaLevelUpMultiplier` (double)

**Default:** 0.35

Multiplier applied to stamina increases on level-up.
integer
Example

0.35 = increase stamina by 35% of the base value per level

---

### `ManaLevelUpMultiplier` (double)

**Default:** 0.6

Multiplier applied to mana increases on level-up.

Example

0.6 = increase mana by 60% of the base value per level

---

### `EnableStatHealing` (boolean)

**Default:** true

When enabled, leveling up will heal affected stats (health, stamina, mana).

---

### `LevelUpSound` (string)

**Default:** "SFX_Divine_Respawn"

Sound event played when a player levels up.

> ⚠️ Must match a valid sound event ID.
> ⚠️ If invalid or empty, no sound will be played.

---

### `LevelDownSound` (string)

**Default:** "SFX_Divine_Respawn"

Sound event played when a player levels down.

> ⚠️ Must match a valid sound event ID.
> ⚠️ If invalid or empty, no sound will be played.

---

### `UseConfigXPMappingsInsteadOfHealthDefaults` (boolean)

**Default:** true

When enabled, XP gain is determined entirely by xpmapping.csv, rather than health-based XP defaults.

> ⚠️ As of January 18, 2026, all default NPCs are included in xpmapping.csv, so health-based XP will not be used unless this is disabled or explicitly configured elsewhere.

---

### `EnableLevelUpRewardsConfig` (boolean)

**Default:** false

When enabled, level up rewards will be granted. These are configured by following the [Level Reward Mapping](https://github.com/AzureDoom/LevelingCore/wiki/Level-Reward-Mapping)

---

### `DisableStatPointGainOnLevelUp` (boolean)

**Default:** false

When enabled, level up stats will not be granted.

---

### `StatsPerLevel` (integer)

**Default:** 5

The amount of stat points to grant on level up if not using the `UseStatsPerLevelMapping` config.

---

### `UseStatsPerLevelMapping` (boolean)

**Default:** false

Whether to use a custom mapping to determine stat points granted per level instead of a flat value.

---

### `StrStatMultiplier` (float)

**Default:** 0.1

Multiplier applied to Strength-based effects such as Melee weapon damage.

---

### `PerStatMultiplier` (float)

**Default:** 0.1

Multiplier applied to Perception-based effects such has Range weapon damage.

---

### `VitStatMultiplier` (float)

**Default:** 2.0

Multiplier applied to Vitality-based effects, such as health scaling.

---

### `AgiStatMultiplier` (float)

**Default:** 0.25

Multiplier applied to Agility-based effects, such as stamina.

---

### `IntStatMultiplier` (float)

**Default:** 2.0

Multiplier applied to Intelligence-based effects, such as incrased mana.

---

### `EnablePartyProXPShareCompat` (boolean)

**Default:** true

Enables compatibility with [Party Pro](https://www.curseforge.com/hytale/mods/partypro) plugin for party experience sharing.

---

### `EnablePartyPluginXPShareCompat` (boolean)

**Default:** true

Enables compatibility with the [Party Plugin](https://www.curseforge.com/hytale/mods/party-info) for party experience sharing.

---

### `LevelMode` (string)

**Default:** NEARBY_PLAYERS_MEAN

**Possible options:**
- `NEARBY_PLAYERS_MEAN`
- `BIOME`
- `INSTANCE`
- `ZONE`
- `SPAWN_ONLY`

Determines how player level is calculated, based on nearby players.

---

### `LevelVariance` (Integer)

**Default:** 0

A randomization factor applied to an NPC's base level. This allows for natural level diversity within the same zone or spawn group, ensuring that not every mob of the same type has identical stats.

The variance is calculated uniquely for each NPC using its UUID as a seed, meaning the level remains consistent for that specific entity once spawned.

**Formula**

The final level is determined by adding a random integer between 0 and the LevelVariance to the defined base level:
```
FinalLevel=BaseLevel+Random(0,LevelVariance)
```

**Examples**

| Base Level | Level Variance | Possible Final Levels |
| :--- | :--- | :--- |
| 5 | 0 | 5 (No change) |
| 5 | 2 | 5, 6, or 7 |
| 5 | 5 | 5, 6, 7, 8, 9, or 10 |

> ⚠️ LevelVariance applies to all LevelModes except SPAWN_ONLY. If the variance is set to 0, NPCs will always spawn exactly at their assigned base level.

---

### `MobHealthMultiplier` (float)

**Default:** 2.1

Multiplier applied to mob health values based on level.

---

### `MobDamageMultiplier` (float)

**Default:** 0.5

Multiplier applied to mob damage output based on level.

---

### `MobBaseDamage` (float)

**Default:** 0.0

Base melee damage scaling factor applied when a mob/NPC hits a player. This value is combined with the mob’s level and MobDamageMultiplier to produce a final melee damage multiplier.

#### Formula (melee hits)
When the hit is not detected as projectile/arrow:

```java
finalDamage = incomingDamage
            * conDamageMultiplier(playerCon)
            * (MobBaseDamage + (MobDamageMultiplier * mobLevel))

```

---

### `MobRangeDamageMultiplier` (float)

**Default:** 0.3

Multiplier applied specifically to ranged mob damage.

---

### `MobBaseRangeDamage` (float)

**Default:** 0.0

Base projectile/ranged damage scaling factor applied when a mob/NPC damages a player using a projectile (e.g., arrows or other projectile-based attacks). This value is combined with the mob’s level and MobRangeDamageMultiplier to produce the final ranged damage multiplier.

#### Formula (projectile hits):
```java
finalDamage = incomingDamage
            * conDamageMultiplier(playerCon)
            * (MobBaseRangeDamage + (MobRangeDamageMultiplier * mobLevel))

```

---

### `EnableItemLevelRestriction` (boolean)
**Default:** `false`

When enabled, the [Item Level Requirements](https://github.com/AzureDoom/LevelingCore/wiki/Item-Level-Requirements) system is used.

---

### `EnableXPBarUI` (boolean)
**Default:** `false`

When enabled, the XP bar on the HUD is shown.

---

### `ShowPlayerLvls` (boolean)
**Default:** `false`

When enabled, player levels are shown in the player nameplate.

---

### `ShowMobLvls` (boolean)
**Default:** `false`

When enabled, mob levels are shown in the player nameplate. When false, will remove any mob levels in nameplates.

---

### `MobLevelMultiplier` (double)
**Default:** `0.35`

Controls how strongly a mob’s level scales the amount of XP it rewards.

When calculating XP, the base XP value (derived either from the mob’s max health or from configured XP mappings) is multiplied by a level-based scaling factor:
```
levelScale = mobLevel ^ MobLevelMultiplier
xp = baseXP * levelScale
```

- Lower values (e.g. 0.1) result in gentle XP scaling per level

- Higher values (e.g. 1.0) cause XP to scale much more aggressively with mob level

- A value of 0 effectively disables level-based XP scaling

This allows fine-tuning how rewarding higher-level mobs are without changing base XP values.

---

### `MobNameplate` (string)
**Default:** ` [Lvl {level}]`

Controls the formatted suffix (or full template) appended to a mob’s name to display its level.

This is a string template that supports placeholders and is applied when a mob has a level greater than 0.

#### Placeholders:
You may use the following placeholders inside the string:

| Placeholder | Description |
|-------------|-------------|
| `{level}`   | The mob’s calculated level (integer) |
| `{name}`    | The mob’s original display name |

---

## Example Config Snippet

```json
{
  "EnableXPLossOnDeath": false,
  "XPLossPercentage": 0.1,
  "DefaultXPGainPercentage": 0.5,
  "EnableDefaultXPGainSystem": true,
  "EnableLevelDownOnDeath": false,
  "EnableAllLevelsLostOnDeath": false,
  "MinLevelForLevelDown": 65,
  "EnableLevelChatMsgs": false,
  "DisableXPGainNotification": false,
  "EnableLevelAndXPTitles": true,
  "ShowXPAmountInHUD": false,
  "EnableStatLeveling": true,
  "HealthLevelUpMultiplier": 2.200000047683716,
  "StaminaLevelUpMultiplier": 1.350000023841858,
  "ManaLevelUpMultiplier": 1.600000023841858,
  "EnableStatHealing": true,
  "LevelUpSound": "SFX_Divine_Respawn",
  "LevelDownSound": "SFX_Divine_Respawn",
  "UseConfigXPMappingsInsteadOfHealthDefaults": true,
  "EnableLevelUpRewardsConfig": false,
  "DisableStatPointGainOnLevelUp": false,
  "StatsPerLevel": 5,
  "UseStatsPerLevelMapping": false,
  "StrStatMultiplier": 0.10000000149011612,
  "PerStatMultiplier": 0.10000000149011612,
  "VitStatMultiplier": 2.0,
  "AgiStatMultiplier": 0.25,
  "IntStatMultiplier": 2.0,
  "ConStatMultiplier": 0.800000011920929,
  "EnablePartyProXPShareCompat": true,
  "EnablePartyPluginXPShareCompat": true,
  "EnablePartyXPSplit": false,
  "PartyGroupXPMultiplier": 0.5,
  "KillerGetsFullXp": true,
  "EnablePartyXPDistanceCheck": false,
  "PartyXPDistanceBlocks": -1.0,
  "LevelMode": "NEARBY_PLAYERS_MEAN",
  "LevelVariance": 0,
  "MobHealthMultiplier": 2.0999999046325684,
  "MobDamageMultiplier": 0.25,
  "MobBaseDamage": 0.0,
  "MobRangeDamageMultiplier": 0.30000001192092896,
  "MobBaseRangeDamage": 0.0,
  "EnableItemLevelRestriction": false,
  "EnableXPBarUI": true,
  "ShowPlayerLvls": true,
  "ShowMobLvls": true,
  "MobLevelMultiplier": 0.35,
  "MobNameplate": "{name} [Lvl. {level}]"
}
```