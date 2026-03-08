package com.azuredoom.levelingcore.commands.stats;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.level.stats.StatType;

public class SetStatsCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    @Nonnull
    private final RequiredArg<String> statArg;

    @Nonnull
    private final RequiredArg<Integer> valueArg;

    public SetStatsCommand() {
        super("setstats", "Set stats value to player");
        this.requirePermission("levelingcore.addstats");
        this.playerArg = this.withRequiredArg(
            "player",
            "Player to add stats to.",
            ArgTypes.PLAYER_REF
        );
        this.statArg = this.withRequiredArg(
            "stat",
            "Stat to set to (str, agi, per, vit, int, con).",
            ArgTypes.STRING
        );
        this.valueArg = this.withRequiredArg(
            "value",
            "Amount of stat points to set.",
            ArgTypes.INTEGER
        );
    }

    @Override
    protected void execute(
        @NonNullDecl CommandContext commandContext,
        @NonNullDecl Store<EntityStore> store,
        @NonNullDecl Ref<EntityStore> ref,
        @NonNullDecl PlayerRef playerRef,
        @NonNullDecl World world
    ) {
        var levelService = LevelingCoreApi.getLevelServiceIfPresent().orElse(null);
        if (levelService == null) {
            commandContext.sendMessage(CommandLang.NOT_INITIALIZED);
            return;
        }
        playerRef = this.playerArg.get(commandContext);
        var statInput = this.statArg.get(commandContext);
        var value = this.valueArg.get(commandContext);
        var playerUUID = playerRef.getUuid();
        var statType = StatType.fromString(statInput);
        if (statType == null) {
            commandContext.sendMessage(
                Message.raw(
                    "Invalid stat '" + statInput + "'. Valid stats: str, agi, per, vit, int, con"
                )
            );
            return;
        }
        switch (statType) {
            case STR -> {
                levelService.setStr(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "strength")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case AGI -> {
                levelService.setAgi(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "agility")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case PER -> {
                levelService.setPer(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "perception")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case VIT -> {
                levelService.setVit(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "vitality")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case INT -> {
                levelService.setInt(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "intelligence")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
            case CON -> {
                levelService.setCon(playerUUID, value);
                commandContext.sendMessage(
                    CommandLang.SET_STATS.param("stat", "constitution")
                        .param("value", value)
                        .param("player", playerRef.getUsername())
                );
            }
        }
    }
}
