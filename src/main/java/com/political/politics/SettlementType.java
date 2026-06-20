package com.political.politics;

import net.minecraft.ChatFormatting;

/** Tier of a settlement. Sovereign tiers govern nearby non-sovereign settlements. */
public enum SettlementType {
    CAPITAL("Capital", true, ChatFormatting.GOLD),
    CITY("City", true, ChatFormatting.AQUA),
    TOWN("Town", false, ChatFormatting.GREEN),
    VILLAGE("Village", false, ChatFormatting.GRAY);

    public final String display;
    public final boolean sovereign;
    public final ChatFormatting color;

    SettlementType(String display, boolean sovereign, ChatFormatting color) {
        this.display = display;
        this.sovereign = sovereign;
        this.color = color;
    }
}
