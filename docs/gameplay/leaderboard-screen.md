---
title: "Leaderboard Screen"
order: 8
published: true
draft: false
---

# Leaderboard Screen

![leaderboard](https://github.com/user-attachments/assets/ee5594ab-6470-41f6-b1ed-38adbcb4e63e)

The **Leaderboard Screen** displays the top players on the server based
on their level and experience (XP). It allows you to see how you rank
compared to others and navigate through all ranked players.

------------------------------------------------------------------------

## Overview

-   Players are ranked from **the highest level to the lowest level**
-   If levels are equal, players are sorted by **highest XP**
-   Supports **pagination** for large player counts
-   Highlights the **Top 3 players**
-   Shows **your personal rank**, even if you are not on the current
    page

------------------------------------------------------------------------

## Opening the Leaderboard

Use the command:

    /leaderboard

This will open the leaderboard UI.

------------------------------------------------------------------------

## Navigation

The leaderboard displays up to **50 players per page**.

### Controls

-   **Next** → Go to the next page
-   **Prev** → Go to the previous page
-   **Refresh** → Reload the leaderboard

------------------------------------------------------------------------

## Ranking System

Players are ranked using the following rules:

1.  Higher **Level** ranks first
2.  If tied, higher **XP** ranks first
3.  If still tied, player ID is used as a fallback

------------------------------------------------------------------------

## Visual Highlights

### 🥇 Rank 1 (Gold)

-   Gold-colored row (configurable by server admins)
-   Highest ranked player

### 🥈 Rank 2 (Silver)

-   Silver-colored row (configurable by server admins)

### 🥉 Rank 3 (Bronze)

-   Bronze-colored row (configurable by server admins)

### 👤 Your Rank

-   Highlighted row (blue tint)
-   Always visible in Header

------------------------------------------------------------------------

## Your Rank Display

At the top of the screen:

    Your Rank: #X

------------------------------------------------------------------------

## Notes

-   Player names are shown even if offline (based on last known name)
-   Pagination ensures smooth performance with large player counts

------------------------------------------------------------------------

## Tips

-   Use **My Rank** to quickly find yourself
-   Compete for the Top 3 to stand out visually
-   Check regularly to track progress

------------------------------------------------------------------------

Enjoy competing and climbing the leaderboard!
