package com.political.expansion2.accessories;

import com.political.items.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

/** Definition of a Phase-2 consumable ({@code acc2_*} potions, scrolls, bombs, etc.). */
public final class ConsumableDef2 {

    public enum Type { POTION, ELIXIR, FOOD, SCROLL, BOMB }

    @FunctionalInterface
    public interface Action {
        boolean apply(ServerPlayer player);
    }

    public final String id;
    public final String displayName;
    public final Type type;
    public final Rarity rarity;
    public final String flavor;
    public final String successMsg;
    public final ChatFormatting msgColor;
    public final int stackSize;
    public final Action action;

    public ConsumableDef2(String id, String displayName, Type type, Rarity rarity, String flavor,
                          String successMsg, ChatFormatting msgColor, int stackSize, Action action) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.rarity = rarity;
        this.flavor = flavor;
        this.successMsg = successMsg;
        this.msgColor = msgColor;
        this.stackSize = stackSize;
        this.action = action;
    }
}
