v0.9.7 
- Added a mob leveling override mapping support for fixed mob levels. — Huge thanks to GlobalHive for this feature!
- Added a level variance config option to allow for mob leveling to be more random. — Huge thanks to GlobalHive for this feature!
- More edge case crash fixes improving stability. — Huge thanks to KabanFriends for this fix!

v0.9.6 
- Fixed stat points CSV mapping not working properly. – Huge thanks to KabanFriends for this fix!
- Fixed a bug where mob scaling would happen if the entity ref is invalid. — Huge thanks to KabanFriends for this fix!
- Added two new config options for mob base damage for melee/range. — Huge thanks to KabanFriends for this fix!
- Adds item translated names to the level required message.
- Adds mob environment mapping for mob leveling. — Huge thanks to GlobalHive for this feature!
- Fixed player names not showing in nameplates when enabled with level.

v0.9.5 
- Placeholder API support is finished and active now
- Updated manifest to use >= 2026.02.06-aa1b071c2 instead of *
- PlaceholderLib support is finished and active now
- Added health/stamina/mana info to the status screen when using HyUI
- Format large numbers more cleanly for million and billion values
- Fixed /setlevel command bypassing max level check
- Fixed /addlevel command bypassing max level check
- Fixed /removelevel command bypassing min level check
- Fixed /removexp command allowing xp removal below level one
- Fixed /addxp command allowing xp addition above max level
- Fixed xp in the status screen showing total xp instead of current xp needed for the next level.
- Fixed xp in the hud showing total xp instead of the current xp needed for the next level.
- Added a +5 button to the status screen to add 5 ability points to the player. (Only on the HyUI Status screen)

v0.9.4 
- Fixed a bug where the new HyUI stats menu would not properly apply stat increases for health, stamina, oxygen, and mana.
- Added optional support for AutoMultiHud to not show the MultipleHud missing message.
- Started basic support for PlaceholderLib and PlaceholderAPI but not release ready yet.

v0.9.3
- Fixed a bug where the StatsPerLevel config option could be lower than 5
- Fixed issues with MultipleHUD 1.0.4 compatibility
- Added percentage to XP needed in stats screen
- Added optional support for HyUI for the stats screen (This is a new screen that only shows if HyUI is installed)
- Added a new item, Tome of Skills. This item opens the stats screen for the player that holds it.
- Added a percentage amount if the show XP needed config option is enabled.

v0.9.2

***WARNING: THIS VERSION CHANGES THE CONFIG LOCATION PLEASE BACK UP YOUR OLD CONFIG BEFORE UPDATING***

- Fixed available ability points in the level-up message being the total number of points a player has not the number of points they can use.
- Added support for the Update Checker mod.
- Removed Simply Party XP Share compatibility, mod is no longer updated, it seems, and is not allowing mod distribution via CF.
- Fixed missing interaction JSON for SkillPointResetInteraction.
- Fixed an incorrect link in the manifest for the plugin along with a few other small changes there.
- Made MultipleHUD dependency optional, but with annoying messages if it is not installed.
- Fixed /showstats command not working for non-op players.
- Removed unused config options.
- Fixed nameplates not removing levels whenever the config is changed to disable levels on mobs or players.
- Fixed xp gain from running if xp is 0 or less.
- Added new config options for PartyPro and Party plugin compatibility.
  - Added a config option to split XP between party members with a configurable percentage.
  - Added a config option to if the player member who gained XP should be given the full instead of the split XP.
  - Added a config option to enable a configurable distance check to prevent XP gain from party members that are too far away.
- **Moved all data to com.azuredoom_levelingcore folder in the mods folder.**
  - **Old data will be need to be manually migrated. Sorry!**
- Fixed some more errors related to wrong threads.
- Added a config option for mob level multiplier.
  - Controls how strongly a mob’s level scales the amount of XP it rewards.

v0.9.1
- Removed the playername from the /showstats command completely. 
- Mob leveling is no longer applied to mobs with health <= 0, fully fixes death animation bug.
- Removes nameplate rendering for mobs with health <= 0
- Fixed level nameplate overwriting other nameplates, it now instead amends any existing nameplates.
- Fixed level nameplate appearing on dead mobs.
- Fixed a bug where offline party members would still be triggered for XP gain.

v0.9.0
- Fixes stats screen only showing Str values.
- Add an optional system to auto-remove dead mobs to avoid the vanilla death animation bug, uses a config option to enable.
- Add config for disabling XP bar UI
- Added levels to nameplates of mobs and players, with a config option to enable for players and/or mobs.

v0.8.3
- Rewrote the stats screen to better show what each stat does.
- Improved the mob leveling system to queue updates and process them in batches to reduce lag spikes.
- All messages are now translatable.
- Made /showstats command work without the player name argument.

v0.8.2
- Fixed log spamming on projectiles hitting mobs.
- Party Pro UI now shows the players level next to Player name.
- Adds a new potion to the Alchemy bench, Reset Skills Potion
  - Resets stat points allocated to 0 and adds them back to the pool.
  - Recipe: Potion Bottle, Water Bucket, 5x Life Essence, 5x Fire Essence, 5x Ice Essence, 5x Void Essence, 5x Water Essence, 2x Apple
- Health, Stamina, and Mana stats heal when stat points are used on respective stats.
- Mana regenerates based on players' intelligence stat now.
- Fixed damage increases in strength and perception stat increases not properly applying.
- Adds missing config value for EnableItemLevelRestriction and it's uses for item level requirement checks.

v0.8.1
- Fixed log spamming on server join.
- Fixed database not being created on first join due to a typo with intelligence mapping.
- Added constitution stat.
  - Reduces damage taken from mobs.
- Reduced default mobDamageMultiplier to 0.25.
- Fixed a bug where mobs with high levels would be undamageable after a hit or two. (hopefully)
- Crocodile default xp adjusted

v0.8.0
- Implemented mob leveling.
- Added a config option to set the mob LevelMode.
  - LevelModes:
    - NEARBY_PLAYERS_MEAN: Default leveling, mobs level up based on the average level of nearby players.
    - BIOME: Biome leveling, mobs level up based on the biome they spawn in.
      - Is controlled by the config: mods/levelingcore_LevelingCore/data/config/mobbiomemapping.csv
    - INSTANCE: Instance leveling, mobs level up based on the instance they spawn in.
      - Is controlled by the config: mods/levelingcore_LevelingCore/data/config/mobinstancemapping.csv
    - ZONE: Zone leveling, mobs level up based on the zone they spawn in.
      - Is controlled by the config: mods/levelingcore_LevelingCore/data/config/mobzonemapping.csv
    - SPAWN_ONLY: Mobs get their based on their spawn level.
- Added a config option to set the mob leveling health multiplier.
- Added a config option to set the mob leveling damage multiplier.
- Added a config option to set the mob leveling range damage multiplier.
- Increases XP gained on mobs with five levels or fewer of the attacker.

v0.7.2
- Removed UITickSystem, no longer needed and should resolve some lag on level up/downs.
- Adjusted default xp config values for bears.
- Fixed loading config logger not properly giving location.
- Added optional compatibility with the PartyPro plugin.
- Added optional compatibility with the Party Plugin.

***Note: Next step will be mob leveling, open to suggestions on how to implement this.***

v0.7.1
- Fixed log spamming on servers that update.
- Applies AP stats on server join if missing points.

v0.7.0
- Fixed a bug where the xp bar would not update on the HUD.
- Fixed a bug where killing mobs would cause more and more lag.
- Fully implemented the new item level requirement system for attacking weapons.
- STR now increases damage with melee attacks. (Configable multiplier, default: 0.1)
- PER now increases damage with ranged attacks. (Configable multiplier, default: 0.1)
- VIT now increases max health with a configuable multiplier.
- AGI now increases max stamina with a configuable multiplier.
- INT now increases max mana with a configuable multiplier.

v0.6.1
- Tweak to MHud usage with XP Bar Hud UI to hopefully fix some issues with other HUD mods.
- Fixed showstats command not working.
- Removed gamemode check from all commands.
- Added oxygen increases to AGI stat increases.
- Added a config option to configure the stat points per level per the new config `statsperlevelmapping.csv`. (Default: false)

***Note: STR and PER are currently not implemented due to issues with damage interactions.***

v0.6.0
- Fixes log for xp mapping skipping empty lines.
- Fix map not being cleaned when player disconnects
- Added stat points gained on level up. (Config option to disable this if you don't like it)
- Added a config option to disable stat point gain on level up. (Default: false)
- Add a tick check to refresh the xp bar on changes (xp gain, xp loss, level up, and level down)
- Fixed permissions not being correctly named
  - levelingcore.addlevel
  - levelingcore.removelevel
  - levelingcore.setlevel
  - levelingcore.addxp
  - levelingcore.removexp
  - levelingcore.showstats
- Removed checklevel command as no longer needed.
- Fixed commands descriptions.
- Add itemlevelmapping.csv to generate for mapping items to level requirements. (Currently not implemented of this version)

v0.5.1
- Added a config option to disable XP Gain Notifications. (Default: false)
- More Thread safety fixes.
- Added an icon for Better Modlist mod.
- Rewrite HUD rendering to long use EntityTickingSystem but instead PlayerReadyEvent
  - This fixes issues with other HUD mods not rendering correctly.

v0.5.0
- Hopefully fixed any Thread issues.
- Fixed Spawn_Void mob not giving XP.
- Beefed up base Level Up Multiplier values after some play testing. (Config reset recommended)
- Fixed xp mapping logging not logging correctly.
- Added level up rewards controlled by config.
  - Uses a cvs located at /mods/levelingcore_LevelingCore/data/config/levelrewardmapping.csv
  - Supports multiple rewards per level. Example:
    ```
    itemnameid,quantity,lvl
    Ore_Copper,3,10
    Ingredient_Fibre,16,10
    Ore_Iron,2,15
    Weapon_Arrow_Crude,8,15
    ```

v0.4.0
- Implemented stat healing on level up (Config option to disable this if you don't like it)
- Added a level up sound, with a config option to change it. (Default: SFX_Divine_Respawn)
- Added a level down sound, with a config option to change it. (Default: SFX_Divine_Respawn)
- Added a config option to use XP mappings from config instead of health defaults. (Default: true)

v0.3.0
- Implemented a new xp bar UI (Thanks to kiyo for texture and suggestions!)
- Implemented stats increase on level up and stat decreases on level down. (Config option to disable this if you don't like it)
- Updated commands to be proper player commands with permissions. (Was op only before but not has proper permissions)
- Moved XP gain chat messages to a notification system.
- Fixed a bug where PVP would result in XP gain.

v0.2.0
- Added new config options.
  - enableLevelChatMsgs ( default: false): Enables level up chat messages. 
  - enableXPChatMsgs ( default: true): Enables XP gain chat messages. 
  - enableLevelAndXPTitles ( default: true): Enables level up and XP gain title messages. 
  - enableSimplePartyXPShareCompat ( default: true): Enables compatibility with the Simple Party mod to share XP in parties.
- Added optional support for the Simple Party XP Share mod to share XP between party members.
- Added a fallback if the MultipleHUD mod is not installed, with a warning message and log to install it if want UI to work correctly with other mods.

v0.1.2
- Fixed a bug where the default.yml would have the wrong comment link for H2
- Fixed a bug where /removexp would ADD XP instead of removing it. Whoops.
- Moved all text messages to be translatable now.
  - Added pt-BR translation (machine translated, PRs encouraged to help improve it).
  - Added en-ES translation (machine translated, PRs encouraged to help improve it).
  - Added de-DE translation (machine translated, PRs encouraged to help improve it).
  - Added fr-FR translation (machine translated, PRs encouraged to help improve it).
  - Added ru-RU translation (machine translated, PRs encouraged to help improve it).
- Implemented XP bar in the UI using MultipleHUD mod, which is now required. https://www.curseforge.com/hytale/mods/multiplehud

v0.1.1
- Fixed a bug where all players would get XP and level messages.

v0.1.0
- Initial release with basic functionality that I could think of.