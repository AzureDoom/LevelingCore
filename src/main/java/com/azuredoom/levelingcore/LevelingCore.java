package com.azuredoom.levelingcore;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.commands.*;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.config.internal.ConfigBootstrap;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.systems.*;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;
import com.azuredoom.levelingcore.utils.HudPlayerReady;
import com.azuredoom.levelingcore.utils.LevelDownListenerRegistrar;
import com.azuredoom.levelingcore.utils.LevelUpListenerRegistrar;
import com.azuredoom.levelingcore.utils.StrCombatSystem;

public class LevelingCore extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final Path configPath = Paths.get("./mods/levelingcore_LevelingCore/data/config/");

    public static final ConfigBootstrap.Bootstrap bootstrap = ConfigBootstrap.bootstrap(configPath);

    public static LevelServiceImpl levelingService;

    private static LevelingCore INSTANCE;

    private final Config<GUIConfig> config;

    /**
     * Constructs a new {@code LevelingCore} instance and initializes the core components of the leveling system. This
     * constructor takes a non-null {@link JavaPluginInit} object to set up the necessary dependencies and
     * configurations required for the leveling system to function.
     *
     * @param init a {@link JavaPluginInit} instance used to initialize the plugin environment and dependencies. Must
     *             not be {@code null}.
     */
    public LevelingCore(@Nonnull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
        config = this.withConfig("levelingcore", GUIConfig.CODEC);
    }

    /**
     * Initializes the core components of the leveling system. This method sets up necessary configurations, registers
     * commands, and configures systems to handle player leveling and experience management. It also initializes the
     * singleton instance of the {@code LevelingCore} class.
     */
    @Override
    protected void setup() {
        INSTANCE = this;
        this.config.save();
        LOGGER.at(Level.INFO).log("Leveling Core initializing");
        levelingService = bootstrap.service();
        this.registerAllCommands();
        this.registerAllSystems();
        this.getEventRegistry()
            .registerGlobal(
                PlayerReadyEvent.class,
                (playerReadyEvent -> {
                    var player = playerReadyEvent.getPlayer();
                    if (player != null) {
                        LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService -> {
                            if (
                                levelService.getLevel(player.getUuid()) == 1 && levelService.getAvailableAbilityPoints(
                                    player.getUuid()
                                ) == 0
                            ) {
                                levelService.setAbilityPoints(player.getUuid(), 5);
                            }
                        });
                    }
                    HudPlayerReady.ready(playerReadyEvent, config);
                })
            );
        // Cleans up hudMap map
        this.getEventRegistry()
            .registerGlobal(PlayerDisconnectEvent.class, (event) -> {
                XPBarHud.removeHud(event.getPlayerRef());
                LevelUpListenerRegistrar.clear(event.getPlayerRef().getUuid());
                LevelDownListenerRegistrar.clear(event.getPlayerRef().getUuid());
            });
    }

    /**
     * Shuts down the {@code LevelingCore} instance and releases allocated resources. This method performs cleanup
     * operations required to properly terminate the leveling system. It includes closing any resources associated with
     * the {@code bootstrap} object and logging the shutdown process.
     *
     * @throws LevelingCoreException if resource cleanup fails.
     */
    @Override
    protected void shutdown() {
        super.shutdown();
        LOGGER.at(Level.INFO).log("Leveling Core shutting down");
        try {
            LevelingCore.bootstrap.closeable().close();
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to close resources", e);
        }
    }

    /**
     * Retrieves the {@link LevelServiceImpl} instance managed by the {@code LevelingCore} class. The
     * {@code LevelService} provides methods for managing player levels and experience points (XP).
     *
     * @return the {@link LevelServiceImpl} instance used by the leveling system.
     */
    public static LevelServiceImpl getLevelService() {
        return levelingService;
    }

    /**
     * Provides access to the singleton instance of the {@code LevelingCore} class. This instance serves as the primary
     * entry point for managing the core functionality of the leveling system, including initialization, configuration,
     * and lifecycle management.
     *
     * @return the singleton instance of {@code LevelingCore}.
     */
    public static LevelingCore getInstance() {
        return INSTANCE;
    }

    public void registerAllCommands() {
        getCommandRegistry().registerCommand(new AddLevelCommand(config));
        getCommandRegistry().registerCommand(new AddXpCommand(config));
        getCommandRegistry().registerCommand(new SetLevelCommand(config));
        getCommandRegistry().registerCommand(new RemoveLevelCommand(config));
        getCommandRegistry().registerCommand(new RemoveXpCommand(config));
        getCommandRegistry().registerCommand(new ShowStatsCommand(config));
    }

    public void registerAllSystems() {
        getEntityStoreRegistry().registerSystem(new UITickSystem(config));
        getEntityStoreRegistry().registerSystem(new LevelUpTickingSystem(config));
        getEntityStoreRegistry().registerSystem(new LevelDownTickingSystem(config));
        getEntityStoreRegistry().registerSystem(new GainXPEventSystem(config));
        getEntityStoreRegistry().registerSystem(new LossXPEventSystem(config));
        getEntityStoreRegistry().registerSystem(new StrCombatSystem(config));
    }
}
