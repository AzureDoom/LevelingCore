package com.azuredoom.levelingcore.ui.page;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.level.LevelServiceImpl;

public class LeaderboardScreen extends InteractiveCustomUIPage<LeaderboardScreen.BindingData> {

    private static final int MAX_ROWS = 50;

    private int currentOffset = 0;

    private final int pageSize = 50;

    private final LevelServiceImpl levelServiceImpl;

    private final UUID viewerId;

    public LeaderboardScreen(
        @Nonnull PlayerRef playerRef,
        @Nonnull CustomPageLifetime lifetime,
        LevelServiceImpl levelServiceImpl
    ) {
        super(playerRef, lifetime, BindingData.CODEC);
        this.levelServiceImpl = levelServiceImpl;
        this.viewerId = playerRef.getUuid();
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull UICommandBuilder uiCommandBuilder,
        @Nonnull UIEventBuilder uiEventBuilder,
        @Nonnull Store<EntityStore> store
    ) {
        uiCommandBuilder.append("Pages/LevelingCore/leaderboardpage.ui");

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#PrevButton",
            new EventData().append("Type", "PrevPage")
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#NextButton",
            new EventData().append("Type", "NextPage")
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#RefreshButton",
            new EventData().append("Type", "Refresh")
        );

        update(uiCommandBuilder);
    }

    /**
     * Updates the leaderboard display using the provided {@code UICommandBuilder}.
     * <p>
     * This method fetches leaderboard data, including player rankings, total entries, and viewer-specific details like
     * their rank. It updates the UI with player information, navigation controls, and corresponding visibility states.
     * The entries are styled dynamically based on their rank and whether the player is the viewer.
     *
     * @param uiCommandBuilder the command builder used to update the UI components of the leaderboard
     */
    public void update(UICommandBuilder uiCommandBuilder) {
        var totalEntries = levelServiceImpl.getLeaderboardCount();
        var entries = levelServiceImpl.getLeaderboardPage(pageSize, currentOffset);
        var yourRank = levelServiceImpl.getPlayerRank(viewerId);

        uiCommandBuilder.set("#LeaderboardTitle.TextSpans", Message.raw("Top Players"));

        int start = totalEntries == 0 ? 0 : currentOffset + 1;
        int end = currentOffset + entries.size();

        uiCommandBuilder.set(
            "#PageLabel.TextSpans",
            Message.raw("Showing " + start + " - " + end)
        );

        if (yourRank > 0) {
            uiCommandBuilder.set("#YourRankLabel.TextSpans", Message.raw("Your Rank: #" + yourRank));
        } else {
            uiCommandBuilder.set("#YourRankLabel.TextSpans", Message.raw("Your Rank: Unranked"));
        }

        var hasPrev = currentOffset > 0;
        var hasNext = currentOffset + pageSize < totalEntries;

        uiCommandBuilder.set("#PrevButton.Visible", hasPrev);
        uiCommandBuilder.set("#PrevButton.HitTestVisible", hasPrev);
        uiCommandBuilder.set("#NextButton.Visible", hasNext);
        uiCommandBuilder.set("#NextButton.HitTestVisible", hasNext);

        for (var i = 0; i < MAX_ROWS; i++) {
            var rowIndex = i + 1;
            var visible = i < entries.size();

            uiCommandBuilder.set("#Row" + rowIndex + ".Visible", visible);

            if (!visible) {
                continue;
            }

            var entry = entries.get(i);
            var rank = currentOffset + i + 1;

            uiCommandBuilder.set("#Rank" + rowIndex + ".TextSpans", Message.raw("#" + rank));
            uiCommandBuilder.set("#Name" + rowIndex + ".TextSpans", Message.raw(entry.name()));
            uiCommandBuilder.set("#Level" + rowIndex + ".TextSpans", Message.raw("Lv. " + entry.level()));
            uiCommandBuilder.set("#Xp" + rowIndex + ".TextSpans", Message.raw("XP: " + entry.xp()));

            applyRankStyling(uiCommandBuilder, rowIndex, rank, entry.playerId().equals(viewerId));
        }
    }

    /**
     * Handles data events triggered by changes in the leaderboard screen.
     * <p>
     * This method processes different types of data events such as "MyRank", "PrevPage", "NextPage", and "Refresh".
     * Depending on the provided data type, it adjusts internal states like the current page offset or recalculates
     * ranks for the viewer. After processing the event, the user interface is refreshed to reflect the updated state.
     *
     * @param ref   a reference to the entity store involved in the event
     * @param store the entity store instance containing relevant data
     * @param data  the binding data associated with the event, identifying the type of operation or update to process
     */
    @Override
    public void handleDataEvent(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull Store<EntityStore> store,
        @Nonnull BindingData data
    ) {
        super.handleDataEvent(ref, store, data);

        if (data.Type == null) {
            return;
        }

        switch (data.Type) {
            case "MyRank" -> {
                var yourRank = levelServiceImpl.getPlayerRank(viewerId);
                if (yourRank > 0) {
                    currentOffset = ((yourRank - 1) / pageSize) * pageSize;
                }
            }
            case "PrevPage" -> currentOffset = Math.max(0, currentOffset - pageSize);
            case "NextPage" -> currentOffset += pageSize;
            case "Refresh" -> {}
        }

        refreshUI();
    }

    /**
     * Refreshes the user interface of the leaderboard screen.
     * <p>
     * This method rebuilds the UI by creating a new instance of {@code UICommandBuilder}, updating it with the
     * necessary data, and then sending the updated commands for rendering. It ensures the displayed data is in sync
     * with the current state of the leaderboard.
     * <p>
     * The method relies on the {@link #update(UICommandBuilder)} method to populate the command builder with the
     * appropriate leaderboard data, such as player ranks and navigation controls. Finally, the updated commands are
     * sent for rendering via {@code sendUpdate}.
     */
    private void refreshUI() {
        var builder = new UICommandBuilder();
        update(builder);
        sendUpdate(builder);
    }

    /**
     * Applies styling to the rank and name text for a specific row in the leaderboard based on the player's rank and
     * viewer status.
     *
     * @param uiCommandBuilder the command builder used to set the styling properties
     * @param rowIndex         the index of the row to apply the styling to
     * @param rank             the rank of the player corresponding to the row
     * @param isViewer         whether the player associated with the row is the viewer
     */
    private void applyRankStyling(UICommandBuilder uiCommandBuilder, int rowIndex, int rank, boolean isViewer) {
        String rankColor;
        String nameColor;
        var config = LevelingCore.getConfig().get();

        if (rank == 1) {
            rankColor = config.getRankOneRankColor();
            nameColor = config.getRankOneNameColor();
        } else if (rank == 2) {
            rankColor = config.getRankTwoRankColor();
            nameColor = config.getRankTwoNameColor();
        } else if (rank == 3) {
            rankColor = config.getRankThreeRankColor();
            nameColor = config.getRankThreeNameColor();
        } else if (isViewer) {
            rankColor = config.getViewersRankColor();
            nameColor = config.getViewersNameColor();
        } else {
            rankColor = config.getDefaultRankColor();
            nameColor = config.getDefaultNameColor();
        }

        uiCommandBuilder.set("#Rank" + rowIndex + ".Style.TextColor", rankColor);
        uiCommandBuilder.set("#Name" + rowIndex + ".Style.TextColor", nameColor);
    }

    public static class BindingData {

        public String Type;

        public static final BuilderCodec<BindingData> CODEC = BuilderCodec.builder(BindingData.class, BindingData::new)
            .append(new KeyedCodec<>("Type", Codec.STRING), (d, v) -> d.Type = v, d -> d.Type)
            .add()
            .build();
    }
}
