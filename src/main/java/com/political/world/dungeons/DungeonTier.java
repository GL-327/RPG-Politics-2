package com.political.world.dungeons;

import net.minecraft.ChatFormatting;

/** Loot and difficulty tier for dungeon chests and boss chambers. */
public enum DungeonTier {
    COMMON(1, ChatFormatting.GRAY, "common"),
    UNCOMMON(2, ChatFormatting.GREEN, "uncommon"),
    RARE(3, ChatFormatting.BLUE, "rare"),
    EPIC(4, ChatFormatting.LIGHT_PURPLE, "epic");

    public final int level;
    public final ChatFormatting color;
    public final String lootId;

    DungeonTier(int level, ChatFormatting color, String lootId) {
        this.level = level;
        this.color = color;
        this.lootId = lootId;
    }
}
