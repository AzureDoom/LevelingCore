package com.azuredoom.levelingcore.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.util.Config;

import com.azuredoom.levelingcore.commands.level.AddLevelCommand;
import com.azuredoom.levelingcore.commands.level.RemoveLevelCommand;
import com.azuredoom.levelingcore.commands.level.SetLevelCommand;
import com.azuredoom.levelingcore.commands.stats.AddStatsCommand;
import com.azuredoom.levelingcore.commands.stats.RemoveStatsCommand;
import com.azuredoom.levelingcore.commands.stats.SetStatsCommand;
import com.azuredoom.levelingcore.commands.xp.AddXpCommand;
import com.azuredoom.levelingcore.commands.xp.RemoveXpCommand;
import com.azuredoom.levelingcore.config.GUIConfig;

public class LevelingCoreCommands extends AbstractCommandCollection {

    public LevelingCoreCommands(Config<GUIConfig> config) {
        super("levelingcore", "LevelingCore commands");
        this.addSubCommand(new AddLevelCommand(config));
        this.addSubCommand(new AddXpCommand(config));
        this.addSubCommand(new SetLevelCommand(config));
        this.addSubCommand(new RemoveLevelCommand(config));
        this.addSubCommand(new RemoveXpCommand(config));
        this.addSubCommand(new ShowStatsCommand(config));
        this.addSubCommand(new ShowLeaderboardCommand());
        this.addSubCommand(new AddStatsCommand());
        this.addSubCommand(new RemoveStatsCommand());
        this.addSubCommand(new SetStatsCommand());
    }
}
