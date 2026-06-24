package com.political.items;

import com.political.combat.StatManager;
import com.political.net.ModNetworking;
import com.political.net.SbsApplyC2S;
import com.political.net.SbsOpenS2C;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Server-authoritative backend for the {@code /sbs} (Skyblock-stats) editor. Builds a snapshot of
 * the player's held item ({@link SbsOpenS2C}) and applies edits from the client ({@link SbsApplyC2S})
 * straight onto the item's {@code custom_data}, then re-decorates it and refreshes the player's stat
 * sheet so changes take effect immediately.
 */
public final class ItemEditor {

    private ItemEditor() {}

    /** Reads the held item's current state and opens (or refreshes) the editor on the client. */
    public static void sendMenu(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack == null || stack.isEmpty()) {
            ModNetworking.send(player, new SbsOpenS2C(false, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    Rarity.COMMON.name(), Variant.NONE.name(), 0, "", ""));
            return;
        }
        CompoundTag tag = ItemStats.tagOf(stack);
        ModNetworking.send(player, new SbsOpenS2C(
                true,
                stack.getHoverName().getString(),
                tag.getIntOr(ItemStats.HEALTH, 0),
                tag.getIntOr(ItemStats.DEFENSE, 0),
                tag.getIntOr(ItemStats.STRENGTH, 0),
                tag.getIntOr(ItemStats.INTELLIGENCE, 0),
                tag.getIntOr(ItemStats.DAMAGE, 0),
                tag.getIntOr(ItemStats.CRIT_CHANCE, 0),
                tag.getIntOr(ItemStats.CRIT_DAMAGE, 0),
                tag.getIntOr(ItemStats.FEROCITY, 0),
                tag.getIntOr(ItemStats.SPEED, 0),
                tag.getIntOr(ItemStats.ATTACK_SPEED, 0),
                ItemStats.rarityOf(stack).name(),
                ItemStats.variantOf(stack).name(),
                ItemStats.cursedGradeOf(stack),
                ItemStats.prefixPowerId(stack),
                ItemStats.prefixAbilityId(stack)));
    }

    /** Applies a client edit onto the held item, re-decorates it, and refreshes player stats. */
    public static void apply(ServerPlayer player, SbsApplyC2S e) {
        ItemStack stack = player.getMainHandItem();
        if (stack == null || stack.isEmpty()) {
            player.sendSystemMessage(Component.literal("Hold an item to edit its Skyblock stats.")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        ItemStats.writeStats(stack,
                e.health(), e.defense(), e.strength(), e.intelligence(), e.damage(),
                e.critChance(), e.critDamage(), e.ferocity(), e.speed(), e.attackSpeed());

        Rarity rarity = Rarity.byId(e.rarity());
        if (rarity != null) ItemStats.setRarity(stack, rarity);

        Variant variant = Variant.byId(e.variant());
        if (variant == null) variant = Variant.NONE;
        if (variant == Variant.CURSED) {
            ItemStats.setCursedGrade(stack, Math.max(1, e.cursedGrade()));
        } else {
            ItemStats.setVariant(stack, variant);
        }

        // Bind/clear the prefix abilities (only meaningful for the matching prefix, but storing the
        // raw ids is harmless and lets the editor round-trip cleanly).
        ItemStats.setPrefixPower(stack, variant == Variant.CURSED ? e.prefixPower() : "");
        ItemStats.setPrefixAbility(stack, variant == Variant.UNIQUE ? e.prefixAbility() : "");

        ItemStats.decorate(stack);
        StatManager.apply(player);

        player.sendSystemMessage(Component.literal("Updated ").withStyle(ChatFormatting.GREEN)
                .append(stack.getHoverName()), true);
        sendMenu(player);
    }
}
