package com.azuredoom.levelingcore;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.Config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.commands.*;
import com.azuredoom.levelingcore.compat.HStats;
import com.azuredoom.levelingcore.compat.placeholderapi.PlaceholderAPICompat;
import com.azuredoom.levelingcore.compat.placeholderlib.PlaceholderLibCompat;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.config.internal.ConfigBootstrap;
import com.azuredoom.levelingcore.config.internal.ConfigManager;
import com.azuredoom.levelingcore.config.internal.LevelingCoreConfig;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;
import com.azuredoom.levelingcore.interaction.OpenSkillsInteraction;
import com.azuredoom.levelingcore.interaction.SkillPointResetInteraction;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.level.itemlevellock.ItemStatRequirement;
import com.azuredoom.levelingcore.level.itemlevellock.ItemToLevelMapping;
import com.azuredoom.levelingcore.level.itemlevellock.WeaponStatRequirementMapping;
import com.azuredoom.levelingcore.level.mobs.MobEncounterProfileResolver;
import com.azuredoom.levelingcore.level.mobs.MobLevelPersistence;
import com.azuredoom.levelingcore.level.mobs.MobLevelRegistry;
import com.azuredoom.levelingcore.level.mobs.mapping.*;
import com.azuredoom.levelingcore.level.objectives.ObjectivesXPMapping;
import com.azuredoom.levelingcore.level.rewards.LevelRewards;
import com.azuredoom.levelingcore.level.rewards.RewardEntry;
import com.azuredoom.levelingcore.level.stats.StatsPerLevelMapping;
import com.azuredoom.levelingcore.level.xp.XPValues;
import com.azuredoom.levelingcore.systems.damage.MobDamageFilter;
import com.azuredoom.levelingcore.systems.damage.PlayerDamageFilter;
import com.azuredoom.levelingcore.systems.equipment.ArmorBlockLevelSystem;
import com.azuredoom.levelingcore.systems.equipment.ArmorBlockStatSystem;
import com.azuredoom.levelingcore.systems.items.HandGateTickingSystem;
import com.azuredoom.levelingcore.systems.items.ItemBlockPacketManager;
import com.azuredoom.levelingcore.systems.items.ItemTooltipInventoryApplySystem;
import com.azuredoom.levelingcore.systems.items.ItemTooltipMetadataManager;
import com.azuredoom.levelingcore.systems.items.stats.HandStatGateTickingSystem;
import com.azuredoom.levelingcore.systems.items.stats.ItemStatBlockPacketManager;
import com.azuredoom.levelingcore.systems.level.LevelDownTickingSystem;
import com.azuredoom.levelingcore.systems.level.LevelUpTickingSystem;
import com.azuredoom.levelingcore.systems.level.MobLevelSystem;
import com.azuredoom.levelingcore.systems.nameplate.ShowLvlHeadSystem;
import com.azuredoom.levelingcore.systems.objectives.ObjectivesSystem;
import com.azuredoom.levelingcore.systems.stats.AgilitySpeedManager;
import com.azuredoom.levelingcore.systems.xp.GainXPEventSystem;
import com.azuredoom.levelingcore.systems.xp.LossXPEventSystem;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;
import com.azuredoom.levelingcore.utils.HudPlayerReady;
import com.azuredoom.levelingcore.utils.LevelDownListenerRegistrar;
import com.azuredoom.levelingcore.utils.LevelUpListenerRegistrar;

public class LevelingCore extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final Path configDataPath = Paths.get("./mods/com.azuredoom_levelingcore/data/config/");

    public static final Path configPath = Paths.get("./mods/com.azuredoom_levelingcore/data/config/");

    private final ConfigBootstrap bootstrap;

    private ConfigBootstrap.Bootstrap activeBootstrap;

    public static LevelServiceImpl levelingService;

    private static LevelingCore INSTANCE;

    private static Config<GUIConfig> config = null;

    public static final LevelingCoreConfig levelingCoreConfig = ConfigManager.loadOrCreate(LevelingCore.configPath);

    public static final Map<String, Integer> xpMapping = XPValues.loadOrCreate(LevelingCore.configPath);

    public static final Map<Integer, List<RewardEntry>> levelRewardMapping = LevelRewards.loadOrCreate(
        LevelingCore.configPath
    );

    public static final Map<String, Integer> itemLevelMapping = ItemToLevelMapping.loadOrCreate(
        LevelingCore.configPath
    );

    public static final StatsPerLevelMapping statsPerLevel = new StatsPerLevelMapping(LevelingCore.configPath);

    public static final List<MobInstanceMapping.InstanceRule> mobInstanceMapping = MobInstanceMapping.loadOrCreate(
        LevelingCore.configPath
    );

    public static final Map<String, Integer> mobZoneMapping = MobZoneMapping.loadOrCreate(LevelingCore.configPath);

    public static final Map<String, Integer> mobBiomeMapping = MobBiomeMapping.loadOrCreate(LevelingCore.configPath);

    public static final Map<String, Integer> mobEnvironmentMapping = MobEnvironmentMapping.loadOrCreate(
        LevelingCore.configPath
    );

    public static final Map<String, Integer> mobOverrideMapping = MobOverrideMapping.loadOrCreate(
        LevelingCore.configPath
    );

    public static final MobLevelRegistry mobLevelRegistry = new MobLevelRegistry();

    public static final MobLevelPersistence mobLevelPersistence = new MobLevelPersistence();

    public static final ArmorBlockLevelSystem equipBlockManager = new ArmorBlockLevelSystem();

    public static final ItemBlockPacketManager itemBlockPacketManager = new ItemBlockPacketManager();

    public static final Map<String, Integer> objectiveXPMapping = ObjectivesXPMapping.loadOrCreate(
        LevelingCore.configPath
    );

    public static final Map<String, ItemStatRequirement> itemStatRequirements =
        WeaponStatRequirementMapping.loadOrCreate(LevelingCore.configPath);

    public static final ArmorBlockStatSystem equipBlockStatManager = new ArmorBlockStatSystem();

    public static final ItemStatBlockPacketManager itemStatBlockPacketManager = new ItemStatBlockPacketManager();

    public static final List<MobBossMapping.BossRule> mobBossMapping = MobBossMapping.loadOrCreate(
        LevelingCore.configPath
    );

    public static final MobEncounterProfileResolver mobEncounterProfileResolver = new MobEncounterProfileResolver(
        mobBossMapping
    );

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
        bootstrap = new ConfigBootstrap();
    }

    @Override
    protected void start() {
        if (PluginManager.get().getPlugin(new PluginIdentifier("HelpChat", "PlaceholderAPI")) != null) {
            PlaceholderAPICompat.register();
        }

        if (PluginManager.get().getPlugin(new PluginIdentifier("PlaceholderLib", "PlaceholderLib")) != null) {
            PlaceholderLibCompat.register();
        }
    }

    /**
     * Initializes the core components of the leveling system. This method sets up necessary configurations, registers
     * commands, and configures systems to handle player leveling and experience management. It also initializes the
     * singleton instance of the {@code LevelingCore} class.
     */
    @Override
    protected void setup() {
        INSTANCE = this;
        config.save();
        LOGGER.atInfo().log("Leveling Core initializing");
        activeBootstrap = bootstrap.bootstrap(configPath);
        levelingService = activeBootstrap.service();
        levelingService.registerAgilityListener((playerId, _) -> {
            if (config.get().isAgiModifiesSpeed()) {
                AgilitySpeedManager.applyForPlayer(playerId);
            }
        });
        this.registerAllCommands();
        this.registerAllSystems();
        this.getCodecRegistry(Interaction.CODEC)
            .register("SkillPointResetInteraction", SkillPointResetInteraction.class, SkillPointResetInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC)
            .register("OpenSkillsInteraction", OpenSkillsInteraction.class, OpenSkillsInteraction.CODEC);
        // Adds the UI to the player and ensures AP stats are applied
        this.getEventRegistry()
            .registerGlobal(
                PlayerReadyEvent.class,
                (playerReadyEvent -> {
                    var player = playerReadyEvent.getPlayer();
                    var playerRef = player.getReference();
                    if (playerRef == null) {
                        LOGGER.atWarning().log("Player reference is null");
                        return;
                    }
                    var playerRefComponent = playerRef.getStore()
                        .getComponent(playerRef, PlayerRef.getComponentType());
                    if (playerRefComponent == null) {
                        LOGGER.atWarning().log("Player ref component is null");
                        return;
                    }
                    var uuid = playerRefComponent.getUuid();
                    var levelService = LevelingCore.getLevelService();
                    var level = levelService.getLevel(uuid);
                    int targetTotal;
                    if (config.get().isAgiModifiesSpeed()) {
                        AgilitySpeedManager.trackPlayer(uuid, player, playerRef.getStore(), playerRef);
                        AgilitySpeedManager.applyForPlayer(uuid);
                    }
                    if (config.get().isUseStatsPerLevelMapping()) {
                        targetTotal = statsPerLevel.getCumulativeStatsForLevel(
                            level,
                            level * config.get().getStatsPerLevel()
                        );
                    } else {
                        targetTotal = level * config.get().getStatsPerLevel();
                    }
                    var used = levelService.getUsedAbilityPoints(uuid);
                    var currentTotal = levelService.getAvailableAbilityPoints(uuid) + used;

                    if (currentTotal != targetTotal) {
                        levelService.setAbilityPoints(uuid, Math.max(0, targetTotal));
                    }
                    if (LevelingCore.getConfig().get().isEnableItemLevelRestriction())
                        LevelingCore.equipBlockManager.validateArmorOnReady(player);
                    if (LevelingCore.getConfig().get().isEnableItemStatRequirement())
                        LevelingCore.equipBlockStatManager.validateArmorOnReady(player);
                    HudPlayerReady.ready(playerReadyEvent, config);
                })
            );
        this.getEntityStoreRegistry().registerSystem(new PlayerDamageFilter(config));
        this.getEntityStoreRegistry().registerSystem(new MobDamageFilter(config));
        // Cleans up various weak hash maps and UI on player disconnect
        this.getEventRegistry()
            .registerGlobal(PlayerDisconnectEvent.class, (event) -> {
                if (config.get().isAgiModifiesSpeed())
                    AgilitySpeedManager.clear(event.getPlayerRef().getUuid());
                XPBarHud.removeHud(event.getPlayerRef());
                LevelUpListenerRegistrar.clear(event.getPlayerRef().getUuid());
                LevelDownListenerRegistrar.clear(event.getPlayerRef().getUuid());
            });

        var showLvlHeadSystem = new ShowLvlHeadSystem(config);
        var scheduled =
            HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                showLvlHeadSystem,
                0L,
                250L,
                TimeUnit.MILLISECONDS
            );
        var task = new CompletableFuture<Void>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                scheduled.cancel(mayInterruptIfRunning);
                return super.cancel(mayInterruptIfRunning);
            }
        };
        this.getTaskRegistry().registerTask(task);
        LevelingCore.mobLevelPersistence.load();
        if (LevelingCore.getConfig().get().isEnableItemLevelRestriction()) {
            LevelingCore.itemBlockPacketManager.start();
        }
        if (LevelingCore.getConfig().get().isEnableItemStatRequirement()) {
            LevelingCore.itemStatBlockPacketManager.start();
        }
        if (
            LevelingCore.getConfig().get().isEnableItemLevelRestriction()
                || LevelingCore.getConfig().get().isEnableItemStatRequirement()
        ) {
            ItemTooltipMetadataManager.register();
        }
        new HStats("937eca15-2942-44cd-b6a8-650afd1d1b39", "1.0.7", LOGGER);
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
        LevelingCore.mobLevelPersistence.save();
        if (LevelingCore.getConfig().get().isEnableItemLevelRestriction()) {
            LevelingCore.itemBlockPacketManager.shutdown();
        }
        if (LevelingCore.getConfig().get().isEnableItemStatRequirement()) {
            LevelingCore.itemStatBlockPacketManager.shutdown();
        }
        super.shutdown();
        LOGGER.atInfo().log("Leveling Core shutting down");
        try {
            if (activeBootstrap != null) {
                activeBootstrap.closeable().close();
            }
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

    public static Config<GUIConfig> getConfig() {
        return config;
    }

    public static MobEncounterProfileResolver getMobEncounterProfileResolver() {
        return mobEncounterProfileResolver;
    }

    public void registerAllCommands() {
        getCommandRegistry().registerCommand(new LevelingCoreCommands(config));
    }

    public void registerAllSystems() {
        if (config.get().isEnableItemLevelRestriction())
            getEntityStoreRegistry().registerSystem(equipBlockManager);
        if (config.get().isEnableItemStatRequirement())
            getEntityStoreRegistry().registerSystem(equipBlockStatManager);
        getEntityStoreRegistry().registerSystem(
            new HandGateTickingSystem(LevelingCore.itemBlockPacketManager.getHandGate())
        );
        getEntityStoreRegistry().registerSystem(
            new HandStatGateTickingSystem(LevelingCore.itemStatBlockPacketManager.getHandGate())
        );
        if (config.get().isEnableItemLevelRestriction() || config.get().isEnableItemStatRequirement()) {
            getEntityStoreRegistry().registerSystem(new ItemTooltipInventoryApplySystem());
        }
        getEntityStoreRegistry().registerSystem(new ObjectivesSystem());
        getEntityStoreRegistry().registerSystem(new MobLevelSystem(config));
        getEntityStoreRegistry().registerSystem(new LevelUpTickingSystem(config));
        getEntityStoreRegistry().registerSystem(new LevelDownTickingSystem(config));
        getEntityStoreRegistry().registerSystem(new GainXPEventSystem(config));
        getEntityStoreRegistry().registerSystem(new LossXPEventSystem(config));
    }
}
