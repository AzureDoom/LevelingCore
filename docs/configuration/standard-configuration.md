---
title: "Standard Configuration"
order: 5
published: true
draft: false
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

> [!WARNING]
> Disabling this prevents the multiplier below from being used.

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

> [!WARNING]
> Must match a valid sound event ID. If invalid or empty, no sound will be played.

---

### `LevelDownSound` (string)

**Default:** "SFX_Divine_Respawn"

Sound event played when a player levels down.

> [!WARNING]
> Must match a valid sound event ID. If invalid or empty, no sound will be played.

---

### `UseConfigXPMappingsInsteadOfHealthDefaults` (boolean)

**Default:** true

When enabled, XP gain is determined entirely by xpmapping.csv, rather than health-based XP defaults.

> [!NOTE]
> As of January 18, 2026, all default NPCs are included in xpmapping.csv, so health-based XP will not be used unless this is disabled or explicitly configured elsewhere.

---

### `EnableLevelUpRewardsConfig` (boolean)

**Default:** false

When enabled, level-up rewards will be granted. These are configured by following the [Level Reward Mapping](https://github.com/AzureDoom/LevelingCore/wiki/Level-Reward-Mapping)

---

### `DisableStatPointGainOnLevelUp` (boolean)

**Default:** false

When enabled, level-up stats will not be granted.

---

### `StatsPerLevel` (integer)

**Default:** 5

The number of stat points to grant on level up if not using the `UseStatsPerLevelMapping` config.

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

Multiplier applied to Perception-based effects such as Range weapon damage.

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

Multiplier applied to Intelligence-based effects, such as increased mana.

---

### `EnablePartyProXPShareCompat` (boolean)

**Default:** true

Enables compatibility with [the Party Pro](https://www.curseforge.com/hytale/mods/partypro) plugin for party experience sharing.

---

### `EnablePartyPluginXPShareCompat` (boolean)

**Default:** true

Enables compatibility with the [Party Plugin](https://www.curseforge.com/hytale/mods/party-info) for party experience sharing.

---

### `LevelModeArray` (List\<String\>)

**Default:** NEARBY_PLAYERS_MEAN, INSTANCE

**Possible options:**
- `NEARBY_PLAYERS_MEAN`
- `BIOME`
- `INSTANCE`
- `ZONE`
- `SPAWN_ONLY`

Determines how mob level is calculated.

---

### `LevelVariance` (Integer)

**Default:** 5

A randomization factor is applied to an NPC's base level. This allows for natural level diversity within the same zone or spawn group, ensuring that not every mob of the same type has identical stats.

The variance is calculated uniquely for each NPC using its UUID as a seed, meaning the level remains consistent for that specific entity once spawned.

**Formula**

The final level is determined by adding a random offset between **-LevelVariance and +LevelVariance** to the base level:
```
FinalLevel = BaseLevel + Random(-LevelVariance, +LevelVariance)
```
This means the NPC's level may spawn **below or above** the configured base level.

**Examples**

| Base Level | Level Variance | Possible Final Levels |
|:-----------|:---------------|:----------------------|
| 5          | 0              | 5 (No change)         |
| 5          | 2              | 3, 4, 5, 6, or 7      |
| 5          | 5              | 0–10                  |

> [!IMPORTANT]
> LevelVariance applies to all LevelModes except SPAWN_ONLY. If the variance is set to 0, NPCs will always spawn exactly at their assigned base level.

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

```
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
```
finalDamage = incomingDamage
            * conDamageMultiplier(playerCon)
            * (MobBaseRangeDamage + (MobRangeDamageMultiplier * mobLevel))

```

---

### `EnableItemLevelRestriction` (boolean)
**Default:** `false`

When enabled, the [Item Level Requirements](https://wiki.hytalemodding.dev/mod/levelingcore/item-level-requirements) system is used.

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

- Higher values (e.g., 1.0) cause XP to scale much more aggressively with mob level

- A value of 0 effectively disables level-based XP scaling

This allows fine-tuning how rewarding higher-level mobs are without changing base XP values.

---

### `MobNameplate` (string)
**Default:** ` [Lvl {level}]`

Controls the formatted suffix (or full template) appended to a mob’s name to display its level.

This is a string template that supports placeholders and is applied when a mob has a level greater than 0.

#### Placeholders:
You may use the following placeholders inside the string:

| Placeholder | Description                          |
|-------------|--------------------------------------|
| `{level}`   | The mob’s calculated level (integer) |
| `{name}`    | The mob’s original display name      |

---

### `BlacklistedMobs` (List\<String\>)
**Default:** See the list below

A list of entity IDs that should be ignored by the mob level system and nameplate system.

Entities included here will not receive level scaling and will not display custom nameplates.

This is typically used to exclude:

- Projectiles (arrows, bullets, etc.)

- NPCs and merchants

- Decorative entities

- Test or debug entities

- Environmental objects

> [!IMPORTANT]
> Only add entities you explicitly want to exclude. Removing entries may cause unintended behavior such as projectiles or NPCs receiving mob levels.

Wildcards are supported.

#### Default Value
```
[
  "Arrow_Crossbow_Signature",
  "Arrow_Crude",
  "Arrow_Fire",
  "Arrow_Frost",
  "Arrow_Iron",
  "Arrow_Ricochet",
  "Arrow_Ricochet_Signature",
  "Arrow_Shortbow_Signature",
  "Arrow_Vamp",
  "Arrow_Vamp_Signature",
  "Axe_Bone",
  "Axe_Stone_Trork",
  "Boat",
  "Bomb",
  "Bomb_Fire_Goblin",
  "Bomb_Fire_Goblin_Dud",
  "Bomb_Large_Fire_Goblin",
  "Bomb_Popberry",
  "Bomb_Potion_Poison",
  "Boy_Trail",
  "Bullet_Blunderbuss",
  "Cactee",
  "Cactee_Spike",
  "Crossbow_Turret",
  "Crossbow_Turret_Item_Projectile",
  "Dagger_Adamantite",
  "Dagger_Bone",
  "Dagger_Bronze",
  "Dagger_Bronze_Ancient",
  "Dagger_Cobalt",
  "Dagger_Copper",
  "Dagger_Crude",
  "Dagger_Doomed",
  "Dagger_Fang_Doomed",
  "Dagger_Iron",
  "Dagger_Mithril",
  "Dagger_Onyxium",
  "Dagger_Stone_Trork",
  "Dagger_Thorium",
  "Debug",
  "Deployable_Fire_Trap",
  "Deployable_Fire_Trap_Preview",
  "Egg",
  "Eye_Void_Blast",
  "Feran_Civilian",
  "Feran_Windwalker_Wind_Burst",
  "Feran_Windwalker_Wind_Vortex",
  "Fireball",
  "Healing_Totem",
  "Healing_Totem_Projectile",
  "Ice_Ball",
  "Ice_Bolt",
  "Ingredient_Poop",
  "Klops_Gentleman",
  "Klops_Merchant",
  "Klops_Miner",
  "Kunai",
  "Kweebec_Rootling",
  "Kweebec_Sapling",
  "Kweebec_Sapling_Brown",
  "Kweebec_Sapling_Christmas_Blue",
  "Kweebec_Sapling_Christmas_Green",
  "Kweebec_Sapling_Christmas_Pink",
  "Kweebec_Sapling_Green",
  "Kweebec_Sapling_HardHat",
  "Kweebec_Sapling_Orange",
  "Kweebec_Sapling_Pink",
  "Kweebec_Sapling_Razorleaf",
  "Kweebec_Sapling_Red",
  "Kweebec_Sapling_Treesinger",
  "Kweebec_Sapling_Yellow",
  "Kweebec_Seedling",
  "Kweebec_Sproutling",
  "Kweebec_Sproutling_Blue",
  "Kweebec_Sproutling_Lime",
  "Mannequin",
  "Minecart",
  "Model_Bee_Swarm",
  "Model_Deer_Stag",
  "NPC_Elf",
  "NPC_Path_Marker",
  "NPC_Santa",
  "NPC_Sound_Shoe",
  "NPC_Spawn_Marker",
  "Objective_Location_Marker",
  "Player",
  "PlayerTestModel_G",
  "PlayerTestModel_V",
  "Projectile",
  "Reindeer_Christmas",
  "Rubble_Aqua",
  "Rubble_Basalt",
  "Rubble_Calcite",
  "Rubble_Default",
  "Rubble_Ice",
  "Rubble_Marble",
  "Rubble_Quartzite",
  "Rubble_Sandstone",
  "Rubble_Sandstone_Red",
  "Rubble_Sandstone_White",
  "Rubble_Shale",
  "Rubble_Slate",
  "Rubble_Stone",
  "Rubble_Stone_Mossy",
  "Rubble_Volcanic",
  "Scarak_Seeker_Spitball",
  "Showcase_Cobalt_Gear",
  "Showcase_Copper_Gear",
  "Showcase_Iron_Gear",
  "Showcase_Iron_TargetDummy_1",
  "Showcase_Mannequin_Heal",
  "Showcase_Mannequin_Inv_Portal",
  "Showcase_Mannequin_Inv_Sphere",
  "Showcase_Mannequin_Lightning",
  "Showcase_Mannequin_Sitting",
  "Showcase_Onyxium_Gear",
  "Showcase_Prisma_Gear",
  "Showcase_Skeleton_Assasin",
  "Showcase_Skeleton_Dead",
  "Showcase_Skeleton_Guard",
  "Showcase_Skeleton_Tank",
  "Showcase_Wooden_Gear",
  "Skeleton_Mage_Corruption_Orb",
  "Slothian",
  "Slothian_Elder",
  "Slothian_Kid",
  "Slothian_Monk",
  "Slothian_Scout",
  "Slothian_Villager",
  "Slothian_Warrior",
  "Slowness_Totem",
  "Slowness_Totem_Projectile",
  "Spear_Adamantite",
  "Spear_Adamantite_Saurian",
  "Spear_Bone",
  "Spear_Bronze",
  "Spear_Cobalt",
  "Spear_Copper",
  "Spear_Crude",
  "Spear_Double_Incandescent",
  "Spear_Iron",
  "Spear_Leaf",
  "Spear_Mithril",
  "Spear_Onyxium",
  "Spear_Scrap",
  "Spear_Stone_Trork",
  "Spear_Thorium",
  "Spear_Tribal",
  "Spirit_Ember",
  "Spirit_Frost",
  "Spirit_Root",
  "Spirit_Thunder",
  "Sword_Charged_Test",
  "Tank",
  "Test_Platform",
  "Tornado",
  "Trash",
  "Tuluk",
  "Tuluk_Fisherman",
  "Warp",
  "Warrior_Quest",
  "Wraith_Lantern"
]
```

---

### `EnableItemStatRequirement` (boolean)
**Default:** `false`

When enabled, the [Item Stats Requirements](https://wiki.hytalemodding.dev/mod/levelingcore/item-stats-requirements) system is used.

---

### `BlacklistedNameplateMobs` (List\<String\>)
**Default:** `"[HyCitizens_*"]`

Blacklisted mob names that will not display custom nameplates. Wildcards are supported.

---

### `EnableLeaderboardScreen` (boolean)
**Default:** `false`

When enabled, the [Leaderboard Screen](https://wiki.hytalemodding.dev/mod/levelingcore/leaderboard-screen) is shown.

---

### `RankOneRankColor` (string)
**Default:** `#FFD700`

Controls the color of the first place rank on the leaderboard screen.

---

### `RankOneNameColor` (string)
**Default:** `#FFF4B0`

Controls the color of the first place name on the leaderboard screen.

---

### `RankTwoRankColor` (string)
**Default:** `#C0C0C0`

Controls the color of the second place rank on the leaderboard screen.

---

### `RankTwoNameColor` (string)
**Default:** `#F2F2F2`

Controls the color of the second place name on the leaderboard screen.

---

### `RankThreeRankColor` (string)
**Default:** `#CD7F32`

Controls the color of the third place rank on the leaderboard screen.

---

### `RankThreeNameColor` (string)
**Default:** `#FFE1C4`

Controls the color of the third place name on the leaderboard screen.

---

### `ViewersRankColor` (string)
**Default:** `#9fe3ff`

Controls the color belonging to the rank of the viewer on the leaderboard screen.

---

### `ViewersNameColor` (string)
**Default:** `#ffffff`

Controls the color belonging to the name of the viewer on the leaderboard screen.

---

### `DefaultRankColor` (string)
**Default:** `#FFD700`

Controls the color non-top three players are shown in the leaderboard screen, not including the viewer.

---

### `DefaultNameColor` (string)
**Default:** `#ffffff`

Controls the color non-top three players are shown in the leaderboard screen, not including the viewer.

---

### `NormalMobHealthMultiplier` (float)
**Default:** `1.0`

Controls the health multiplier applied to mobs that are not elite or boss mobs.

---

### `NormalMobDamageMultiplier` (float)
**Default:** `1.0`

Controls the damage multiplier applied to mobs that are not elite or boss mobs.

---

### `NormalMobDamageThreshold` (float)
**Default:** `1.0`

Controls the damage threshold applied to mobs that are not elite or boss mobs.

---

### `NormalMobXPMultiplier` (float)
**Default:** `1.0`

Controls the XP multiplier applied to mobs that are not elite or boss mobs.

---

### `EliteMobHealthMultiplier` (float)
**Default:** 1.75`

Controls the health multiplier applied to elite mobs.

---

### `EliteMobDamageMultiplier` (float)
**Default:** `1.149999976158142`

Controls the damage multiplier applied to elite mobs.

---

### `EliteMobDamageThreshold` (float)
**Default:** `0.800000011920929`

Controls the damage threshold applied to elite mobs.

---

### `EliteMobXPMultiplier` (float)
**Default:** `1.399999976158142`

Controls the XP multiplier applied to elite mobs.

---

### `BossMobHealthMultiplier` (float)
**Default:** `2.0`

Controls the health multiplier applied to boss mobs.

---

### `BossMobDamageMultiplier` (float)
**Default:** `1.5`

Controls the damage multiplier applied to boss mobs.

---

### `BossMobDamageThreshold` (float)
**Default:** `0.699999988079071`

Controls the damage threshold applied to boss mobs.

---

### `BossMobXPMultiplier` (float)
**Default:** `1.7999999523162842`

Controls the XP multiplier applied to boss mobs.

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
  "LevelModeArray": [
    "NEARBY_PLAYERS_MEAN",
    "INSTANCE"
  ],
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
  "MobNameplate": "{name} [Lvl. {level}]",
  "BlacklistedMobs": [
    "Arrow_Crossbow_Signature",
    "Arrow_Crude",
    "Arrow_Fire",
    "Arrow_Frost",
    "Arrow_Iron",
    "Arrow_Ricochet",
    "Arrow_Ricochet_Signature",
    "Arrow_Shortbow_Signature",
    "Arrow_Vamp",
    "Arrow_Vamp_Signature",
    "Axe_Bone",
    "Axe_Stone_Trork",
    "Boat",
    "Bomb",
    "Bomb_Fire_Goblin",
    "Bomb_Fire_Goblin_Dud",
    "Bomb_Large_Fire_Goblin",
    "Bomb_Popberry",
    "Bomb_Potion_Poison",
    "Boy_Trail",
    "Bullet_Blunderbuss",
    "Cactee",
    "Cactee_Spike",
    "Crossbow_Turret",
    "Crossbow_Turret_Item_Projectile",
    "Dagger_Adamantite",
    "Dagger_Bone",
    "Dagger_Bronze",
    "Dagger_Bronze_Ancient",
    "Dagger_Cobalt",
    "Dagger_Copper",
    "Dagger_Crude",
    "Dagger_Doomed",
    "Dagger_Fang_Doomed",
    "Dagger_Iron",
    "Dagger_Mithril",
    "Dagger_Onyxium",
    "Dagger_Stone_Trork",
    "Dagger_Thorium",
    "Debug",
    "Deployable_Fire_Trap",
    "Deployable_Fire_Trap_Preview",
    "Egg",
    "Eye_Void_Blast",
    "Feran_Civilian",
    "Feran_Windwalker_Wind_Burst",
    "Feran_Windwalker_Wind_Vortex",
    "Fireball",
    "Healing_Totem",
    "Healing_Totem_Projectile",
    "Ice_Ball",
    "Ice_Bolt",
    "Ingredient_Poop",
    "Klops_Gentleman",
    "Klops_Merchant",
    "Klops_Miner",
    "Kunai",
    "Kweebec_Rootling",
    "Kweebec_Sapling",
    "Kweebec_Sapling_Brown",
    "Kweebec_Sapling_Christmas_Blue",
    "Kweebec_Sapling_Christmas_Green",
    "Kweebec_Sapling_Christmas_Pink",
    "Kweebec_Sapling_Green",
    "Kweebec_Sapling_HardHat",
    "Kweebec_Sapling_Orange",
    "Kweebec_Sapling_Pink",
    "Kweebec_Sapling_Razorleaf",
    "Kweebec_Sapling_Red",
    "Kweebec_Sapling_Treesinger",
    "Kweebec_Sapling_Yellow",
    "Kweebec_Seedling",
    "Kweebec_Sproutling",
    "Kweebec_Sproutling_Blue",
    "Kweebec_Sproutling_Lime",
    "Mannequin",
    "Minecart",
    "Model_Bee_Swarm",
    "Model_Deer_Stag",
    "NPC_Elf",
    "NPC_Path_Marker",
    "NPC_Santa",
    "NPC_Sound_Shoe",
    "NPC_Spawn_Marker",
    "Objective_Location_Marker",
    "Player",
    "PlayerTestModel_G",
    "PlayerTestModel_V",
    "Projectile",
    "Reindeer_Christmas",
    "Rubble_Aqua",
    "Rubble_Basalt",
    "Rubble_Calcite",
    "Rubble_Default",
    "Rubble_Ice",
    "Rubble_Marble",
    "Rubble_Quartzite",
    "Rubble_Sandstone",
    "Rubble_Sandstone_Red",
    "Rubble_Sandstone_White",
    "Rubble_Shale",
    "Rubble_Slate",
    "Rubble_Stone",
    "Rubble_Stone_Mossy",
    "Rubble_Volcanic",
    "Scarak_Seeker_Spitball",
    "Showcase_Cobalt_Gear",
    "Showcase_Copper_Gear",
    "Showcase_Iron_Gear",
    "Showcase_Iron_TargetDummy_1",
    "Showcase_Mannequin_Heal",
    "Showcase_Mannequin_Inv_Portal",
    "Showcase_Mannequin_Inv_Sphere",
    "Showcase_Mannequin_Lightning",
    "Showcase_Mannequin_Sitting",
    "Showcase_Onyxium_Gear",
    "Showcase_Prisma_Gear",
    "Showcase_Skeleton_Assasin",
    "Showcase_Skeleton_Dead",
    "Showcase_Skeleton_Guard",
    "Showcase_Skeleton_Tank",
    "Showcase_Wooden_Gear",
    "Skeleton_Mage_Corruption_Orb",
    "Slothian",
    "Slothian_Elder",
    "Slothian_Kid",
    "Slothian_Monk",
    "Slothian_Scout",
    "Slothian_Villager",
    "Slothian_Warrior",
    "Slowness_Totem",
    "Slowness_Totem_Projectile",
    "Spear_Adamantite",
    "Spear_Adamantite_Saurian",
    "Spear_Bone",
    "Spear_Bronze",
    "Spear_Cobalt",
    "Spear_Copper",
    "Spear_Crude",
    "Spear_Double_Incandescent",
    "Spear_Iron",
    "Spear_Leaf",
    "Spear_Mithril",
    "Spear_Onyxium",
    "Spear_Scrap",
    "Spear_Stone_Trork",
    "Spear_Thorium",
    "Spear_Tribal",
    "Spirit_Ember",
    "Spirit_Frost",
    "Spirit_Root",
    "Spirit_Thunder",
    "Sword_Charged_Test",
    "Tank",
    "Test_Platform",
    "Tornado",
    "Trash",
    "Tuluk",
    "Tuluk_Fisherman",
    "Warp",
    "Warrior_Quest",
    "Wraith_Lantern"
  ],
  "EnableItemStatRequirement": false,
  "BlacklistedNameplateMobs": [],
  "EnableLeaderboardScreen": true,
  "RankOneRankColor": "#FFD700",
  "RankOneNameColor": "#FFF4B0",
  "RankTwoRankColor": "#C0C0C0",
  "RankTwoNameColor": "#F2F2F2",
  "RankThreeRankColor": "#CD7F32",
  "RankThreeNameColor": "#FFE1C4",
  "ViewersRankColor": "#9fe3ff",
  "ViewersNameColor": "#ffffff",
  "DefaultRankColor": "#FFD700",
  "DefaultNameColor": "#ffffff",
  "NormalMobHealthMultiplier": 1.0,
  "NormalMobDamageMultiplier": 1.0,
  "NormalMobDamageThreshold": 1.0,
  "NormalMobXPMultiplier": 1.0,
  "EliteMobHealthMultiplier": 1.75,
  "EliteMobDamageMultiplier": 1.149999976158142,
  "EliteMobDamageThreshold": 0.800000011920929,
  "EliteMobXPMultiplier": 1.399999976158142,
  "BossMobHealthMultiplier": 2.0,
  "BossMobDamageMultiplier": 1.5,
  "BossMobDamageThreshold": 0.699999988079071,
  "BossMobXPMultiplier": 1.7999999523162842
}
```