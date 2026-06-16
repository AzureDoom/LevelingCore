package com.azuredoom.levelingcore.systems.xp;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.Objects;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.compat.party.PartyPluginCompat;
import com.azuredoom.levelingcore.compat.party.PartyProCompat;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.lang.CommandLang;
import com.azuredoom.levelingcore.ui.hud.XPBarHud;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;
import com.azuredoom.levelingcore.utils.NotificationsUtil;

/**
 * The {@code GainXPEventSystem} class handles the process of awarding experience points (XP) to players based on
 * specific actions or events in the game world, particularly when an entity dies. This system is invoked upon the
 * addition of a {@code DeathComponent} to an entity.
 * <ul>
 * <li>XP gain is calculated based on the maximum health of the defeated entity and a configurable percentage provided
 * by the {@code GUIConfig}.</li>
 * <li>The system ensures that XP is awarded only if the default XP gain system is enabled in the configuration.</li>
 * <li>Supports interaction with the leveling service to update player levels and send appropriate messages upon
 * leveling up.</li>
 * </ul>
 * The class extends {@code DeathSystems.OnDeathSystem} to seamlessly integrate with death-related events in the game.
 */
public class GainXPEventSystem extends DeathSystems.OnDeathSystem {

    private final Config<GUIConfig> config;

    public GainXPEventSystem(Config<GUIConfig> config) {
        this.config = config;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(DeathComponent.getComponentType());
    }

    @Override
    public void onComponentAdded(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull DeathComponent component,
        @Nonnull Store<EntityStore> store,
        @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        var deathInfo = component.getDeathInfo();
        if (deathInfo == null)
            return;

        if (!this.config.get().isEnableDefaultXPGainSystem()) {
            return;
        }

        if (store.getComponent(ref, PlayerRef.getComponentType()) != null) {
            return;
        }

        if (deathInfo.getSource() instanceof Damage.EntitySource entitySource) {
            var attackerRef = entitySource.getRef();
            if (attackerRef.isValid()) {
                var player = store.getComponent(attackerRef, Player.getComponentType());
                if (player == null)
                    return;
                var playerRef = player.getReference();
                if (playerRef == null) {
                    return;
                }
                var playerRefComponent = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
                if (playerRefComponent == null) {
                    return;
                }
                var playerUUID = playerRefComponent.getUuid();
                var statMap = store.getComponent(ref, EntityStatMap.getComponentType());
                if (statMap == null)
                    return;
                var entity = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
                if (entity == null)
                    return;
                var xpMap = LevelingCore.xpMapping;
                var healthIndex = EntityStatType.getAssetMap().getIndex("Health");
                var healthStat = statMap.get(healthIndex);
                if (healthStat == null)
                    return;
                var maxHealth = healthStat.getMax();
                var uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
                if (uuidComponent == null)
                    return;
                var entityUuid = uuidComponent.getUuid();
                var mobLevel = LevelingCore.mobLevelRegistry.getOrCreateWithPersistence(
                    entityUuid,
                    () -> MobLevelingUtil.computeSpawnLevel(commandBuffer, entity),
                    () -> LevelingCore.mobEncounterProfileResolver.resolve(store, entity),
                    LevelingCore.mobLevelPersistence
                );
                if (mobLevel == null)
                    return;
                var hpScale = Math.sqrt(maxHealth);
                var xpAmountHealth = Math.max(0, Math.round(hpScale * config.get().getDefaultXPGainPercentage()));
                var getXPMapping = xpMap.getOrDefault(entity.getNPCTypeId(), Math.toIntExact(xpAmountHealth));
                var levelScale = Math.pow(mobLevel.level, config.get().getMobLevelMultiplier());
                var base = config.get().isUseConfigXPMappingsInsteadOfHealthDefaults()
                    ? (long) (getXPMapping)
                    : xpAmountHealth;
                var xpScale = Math.pow(mobLevel.xpMultiplier, config.get().getMobLevelMultiplier());
                var xpAmount = Math.round(base * levelScale * xpScale);
                if (xpAmount <= 0)
                    return;
                var levelService = LevelingCore.getLevelService();
                store.getExternalData().getWorld().execute(() -> {
                    var levelBefore = levelService.getLevel(playerUUID);
                    var penalizedXp = applyLevelPenalty(xpAmount, levelBefore, mobLevel.level);
                    if (penalizedXp <= 0)
                        return;
                    if (
                        PluginManager.get()
                            .getPlugin(new PluginIdentifier("tsumori", "partypro")) != null
                    ) {
                        PartyProCompat.onXPGain(penalizedXp, playerUUID, levelService, config, playerRefComponent);
                    } else if (
                        PluginManager.get()
                            .getPlugin(new PluginIdentifier("com.carsonk", "Party Plugin")) != null
                    ) {
                        PartyPluginCompat.onXPGain(
                            penalizedXp,
                            playerUUID,
                            levelService,
                            config,
                            playerRefComponent
                        );
                    } else {
                        // Fallback to default XP gain if supported Party mods are not installed
                        if (
                            !config.get().isDisableXPGainNotification() && !levelService.isMaxLevel(
                                playerUUID
                            )
                        )
                            NotificationsUtil.sendXPGainNotification(
                                playerRefComponent,
                                penalizedXp
                            );
                        levelService.addXp(playerUUID, penalizedXp);
                        XPBarHud.updateHud(playerRefComponent);
                    }
                    LevelingCore.mobLevelRegistry.remove(entityUuid);
                    LevelingCore.mobLevelPersistence.remove(entityUuid);
                    var levelAfter = levelService.getLevel(playerUUID);
                    if (levelAfter > levelBefore) {
                        if (config.get().isEnableLevelChatMsgs())
                            playerRefComponent.sendMessage(CommandLang.LEVEL_UP.param("level", levelAfter));
                    }
                });
            }
        }
    }

    /**
     * Applies a level-difference XP penalty based on the gap between the player's level and the mob's level.
     * <p>
     * The multiplier is determined by {@code diff = mobLevel - playerLevel}:
     * <ul>
     * <li>-5 to +10 → 100%</li>
     * <li>-6 to -10 → 80%</li>
     * <li>-11 to -15 → 60%</li>
     * <li>-16 to -20 → 20%</li>
     * <li>below -20 → 10%</li>
     * <li>+11 to +15 → 80%</li>
     * <li>+16 to +20 → 20%</li>
     * <li>above +20 → 10%</li>
     * </ul>
     *
     * @param xp          the base XP amount before penalty
     * @param playerLevel the current level of the player
     * @param mobLevel    the level of the defeated mob
     * @return the penalized XP amount, always non-negative
     */
    private static long applyLevelPenalty(long xp, int playerLevel, int mobLevel) {
        if (LevelingCore.getConfig().get().isLevelPenaltyDisabled()) {
            return xp;
        }

        var diff = mobLevel - playerLevel;
        double multiplier;
        if (diff >= -5 && diff <= 10) {
            multiplier = 1.0;
        } else if (diff > 10 && diff <= 15) {
            multiplier = 0.8;
        } else if (diff > 15 && diff <= 20) {
            multiplier = 0.2;
        } else if (diff > 20) {
            multiplier = 0.1;
        } else if (diff >= -10) {
            multiplier = 0.8;
        } else if (diff >= -15) {
            multiplier = 0.6;
        } else if (diff >= -20) {
            multiplier = 0.2;
        } else {
            multiplier = 0.1;
        }
        return Math.max(0, Math.round(xp * multiplier));
    }
}
