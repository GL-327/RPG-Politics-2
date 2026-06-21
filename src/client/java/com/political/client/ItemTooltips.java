package com.political.client;

import com.political.items.ItemStats;
import com.political.items.RpgItems;
import com.political.items.SkyblockTooltipBuilder;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
 * Skyblock-style tooltips for every item; strips vanilla Attack Damage / Attack Speed lines.
 */
public final class ItemTooltips {

    private ItemTooltips() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            stripVanillaCombat(lines);
            if (stack == null || stack.isEmpty()) return;
            var tag = ItemStats.tagOf(stack);
            if (!tag.getStringOr(RpgItems.ITEM_ID_KEY, "").isEmpty()) return;
            if (!tag.getStringOr(ItemStats.RARITY, "").isEmpty()
                    && stack.get(net.minecraft.core.component.DataComponents.LORE) != null) return;
            lines.add(Component.empty());
            lines.addAll(SkyblockTooltipBuilder.build(stack));
        });
    }

    /**
     * Removes vanilla attribute lines so only Skyblock stats remain: attack damage/speed,
     * armor, armor toughness, knockback resistance, plus the "When in / on ..." group headers.
     * Our Skyblock lines never contain these phrases, so they are untouched.
     */
    private static void stripVanillaCombat(List<Component> lines) {
        Iterator<Component> it = lines.iterator();
        while (it.hasNext()) {
            String s = it.next().getString().toLowerCase();
            if (s.contains("attack damage") || s.contains("attack speed")
                    || s.contains("armor toughness") || s.contains("knockback resistance")
                    || s.startsWith("when in main hand") || s.startsWith("when in off hand")
                    || s.startsWith("when on ") || s.startsWith("when in ")
                    || s.equals("armor") || s.startsWith("+") && s.contains(" armor")) {
                it.remove();
            }
        }
    }
}
