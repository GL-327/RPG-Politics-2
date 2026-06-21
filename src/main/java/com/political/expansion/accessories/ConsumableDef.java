package com.political.expansion.accessories;

import com.political.items.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

/**
 * Definition of a single consumable (potion / elixir / food / scroll). The actual
 * behaviour lives in an {@link Action} lambda which runs server-side from
 * {@link Accessories}' {@code UseItemCallback}. An action returns {@code true} when it
 * successfully fired (the stack is then shrunk), or {@code false} to leave the item
 * untouched (e.g. nothing to restore).
 */
public final class ConsumableDef {

    public enum Type { POTION, ELIXIR, FOOD, SCROLL }

    /** Server-side effect of using the consumable. */
    @FunctionalInterface
    public interface Action {
        boolean apply(ServerPlayer player);
    }

    public final String id;            // registry path, e.g. "acc_greater_healing_potion"
    public final String displayName;
    public final Type type;
    public final Rarity rarity;
    public final String flavor;        // lore line shown under the stats
    public final String successMsg;    // chat feedback on a successful use
    public final ChatFormatting msgColor;
    public final int stackSize;
    public final Action action;

    public ConsumableDef(String id, String displayName, Type type, Rarity rarity, String flavor,
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
