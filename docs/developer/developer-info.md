---
title: "Developer Info"
order: 13
published: true
draft: false
---

This page explains how to include LevelingCore as a dependency in your Hytale plugin using Gradle and the AzureDoom Maven repository.

# Adding the Maven Repository

LevelingCore is hosted on the AzureDoom and HytaleModding Maven repositories.
Add the following repositories block to your build.gradle file:
```gradle
repositories {
    maven {
        name = "azuredoomMods"
        url = uri("https://maven.azuredoom.com/mods")
    }
    maven {
        name = "hytalemoddingReleases"
        url = uri("https://maven.hytalemodding.dev/releases")
    }
}
```

# Adding LevelingCore as a Dependency

Once the repository is added, include LevelingCore as a dependency:

```gradle
dependencies {
    implementation("com.azuredoom.levelingcore:LevelingCore:0.+")
}
```

Now in your mods/plugins `manifest.json` add `"com.azuredoom:LevelingCore": "*"` to either `Dependencies` to set as hard or `OptionalDependencies` if you are adding integration like so:

```json
{
    // rest of manifest.json
    "Dependencies": {
        "com.azuredoom:LevelingCore": "*"
    },
    "OptionalDependencies": {
        "com.azuredoom:LevelingCore": "*"
    },
    // rest of manifest.json
}
```

# Using the API

LevelingCore exposes its API through `LevelingCoreApi`.

Because LevelingCore may or may not be started correctly on the server, the API is accessed safely using Optional.

## Accessing the Level Service

To access the level service, call:
```java
LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
    // Use the LevelService here
});
```

### Common Usage Examples

Below are some common operations you can perform using the LevelService.

#### Getting Player Level and XP
```java
LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
    int level = levelService.getLevel(playerUUID);
    long xp = levelService.getXp(playerUUID);
});
```

#### Adding or Removing XP
```java
LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
    levelService.addXp(playerUUID, 250);
    levelService.removeXp(playerUUID, 100);
});
```

XP changes trigger automatically:
- XP gain/loss events
- Level up/down events (if applicable)

#### Setting XP or Level Directly
```java
LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
    levelService.setXp(playerUUID, 5000);
    levelService.setLevel(playerUUID, 10);
});
```
> [!IMPORTANT]
> Levels are clamped to a minimum of 1.

#### Adding or Removing Levels
```java
LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
    levelService.addLevel(playerUUID, 2);
    levelService.removeLevel(playerUUID, 1);
});
```

#### Setting and Getting Player Stats
```java
LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
     levelService.getStr(playerUUID); // Gets the players str
     levelService.getAgi(playerUUID); // Gets the players agi
     levelService.getPer(playerUUID); // Gets the players per
     levelService.getVit(playerUUID); // Gets the players vit
     levelService.getInt(playerUUID); // Gets the players int
     levelService.getCon(playerUUID); // Gets the players con
     levelService.getAbilityPoints(playerUUID); // Gets the players ability points
     levelService.getUsedAbilityPoints(playerUUID); // Gets the players used ability points

     levelService.setStr(playerUUID, 1); // Sets the players str to 1
     levelService.setAgi(playerUUID, 1); // Sets the players agi to 1
     levelService.setPer(playerUUID, 1); // Sets the players per to 1
     levelService.setVit(playerUUID, 1); // Sets the players vit to 1
     levelService.setInt(playerUUID, 1); // Sets the players int to 1
     levelService.setCon(playerUUID, 1); // Sets the players con to 1
     levelService.setAbilityPoints(playerUUID, 1); // Sets the players ability points to 1
     levelService.setUsedAbilityPoints(playerUUID, 1); // Sets the players used ability points to 1
);
```

### Listening to Level & XP Events
You can register listeners to react to player progression events.

#### Level Up /Down Listeners
```java
levelService.registerLevelUpListener((playerUUID, newLevel) -> {
    // Handle level up
});

levelService.registerLevelDownListener((playerUUID, newLevel) -> {
    // Handle level down
});
```

#### XP Gain / Loss Listeners
```java
levelService.registerXpGainListener((playerUUID, amount) -> {
    // Handle XP gain
});

levelService.registerXpLossListener((playerUUID, amount) -> {
    // Handle XP loss
});
```

> [!CAUTION]
> Listener getter methods are provided for advanced integrations and debugging. Most plugins should only register listeners and should not modify listener lists directly.

# Support

If you encounter issues or have feature requests, please open an issue [here](https://github.com/AzureDoom/LevelingCore/issues).