package com.azuredoom.levelingcore.systems;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.logging.Level;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.compat.MultipleHudCompat;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.hud.XPBarHud;

public class XPTickSystem extends EntityTickingSystem<EntityStore> {

    private final Config<GUIConfig> config;

    public XPTickSystem(Config<GUIConfig> config) {
        this.config = config;
    }

    @Override
    public void tick(
        float var1,
        int index,
        @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
        @NonNullDecl Store<EntityStore> store,
        @NonNullDecl CommandBuffer<EntityStore> commandBuffer
    ) {
        final Holder<EntityStore> holder = EntityUtils.toHolder(index, archetypeChunk);
        final Player player = holder.getComponent(Player.getComponentType());
        final PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        if (player == null || playerRef == null) {
            return;
        }

        player.getWorld().execute(() -> {
            LevelingCoreApi.getLevelServiceIfPresent().ifPresent(levelService1 -> {
                var xpHud = new XPBarHud(playerRef, levelService1, config);
                if (PluginManager.get().getPlugin(new PluginIdentifier("Buuz135", "MultipleHUD")) != null) {
                    MultipleHudCompat.showHud(player, playerRef, xpHud);
                } else {
                    player.sendMessage(
                        Message.raw(
                            "LevelingCore Error: MultipleHUD not found, XP HUD will not work correctly with other mods adding custom UI"
                        )
                    );
                    LevelingCore.LOGGER.at(Level.WARNING)
                        .log("MultipleHUD not found, XP HUD will not work correctly with other mods adding custom UI");
                    player.getHudManager().setCustomHud(playerRef, xpHud);
                }
            });
        });
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}
