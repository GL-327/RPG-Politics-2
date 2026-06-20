package com.political.politics;

import net.minecraft.ChatFormatting;

/** Government offices. */
public enum Role {
    NONE("Citizen", ChatFormatting.GRAY),
    CHAIR("Chair", ChatFormatting.RED),
    VICE_CHAIR("Vice Chair", ChatFormatting.BLUE),
    JUDGE("Judge", ChatFormatting.YELLOW),
    DICTATOR("Dictator", ChatFormatting.DARK_RED);

    public final String title;
    public final ChatFormatting color;

    Role(String title, ChatFormatting color) {
        this.title = title;
        this.color = color;
    }
}
