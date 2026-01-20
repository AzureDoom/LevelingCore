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