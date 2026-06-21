package com.political.items;

import com.political.combat.StatManager;
import com.political.politics.DataManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consumable relics, currencies and progression tokens. */
public final class RelicItems {

    public static final String MOD_ID = "politicalserver";

    public static Item MANA_CRYSTAL;
    public static Item CURSED_ESSENCE;
    public static Item EXORCISM_TOKEN;
    public static Item GRADE_SCROLL;
    public static Item AWAKENING_STONE;
    public static Item BOUNTY_SEAL;
    public static Item REFORGE_STONE;

    private static final Map<String, Item> ALL = new LinkedHashMap<>();

    private RelicItems() {}

    public static List<Item> items() {
        return new ArrayList<>(ALL.values());
    }

    public static void register() {
        MANA_CRYSTAL = reg("mana_crystal", "Mana Crystal", "Restores 50% of your max Mana.");
        CURSED_ESSENCE = reg("cursed_essence", "Cursed Essence", "Restores 40% of your max Cursed Energy.");
        EXORCISM_TOKEN = reg("exorcism_token", "Exorcism Token", "Your next curse exorcism grants double rewards.");
        GRADE_SCROLL = reg("grade_scroll", "Grade Scroll", "Raises your Sorcerer Grade by 1 (max 5).");
        AWAKENING_STONE = reg("awakening_stone", "Awakening Stone", "Awakens innate cursed energy if dormant.");
        BOUNTY_SEAL = reg("bounty_seal", "Bounty Seal", "Instantly grants 150 coins.");
        REFORGE_STONE = reg("reforge_stone", "Reforge Stone", "Reforges held item to the next rarity tier.");

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemStack stack = sp.getItemInHand(hand);
            if (stack.is(MANA_CRYSTAL)) return consume(sp, stack, useManaCrystal(sp));
            if (stack.is(CURSED_ESSENCE)) return consume(sp, stack, useCursedEssence(sp));
            if (stack.is(EXORCISM_TOKEN)) return consume(sp, stack, useExorcismToken(sp));
            if (stack.is(GRADE_SCROLL)) return consume(sp, stack, useGradeScroll(sp));
            if (stack.is(AWAKENING_STONE)) return consume(sp, stack, useAwakening(sp));
            if (stack.is(BOUNTY_SEAL)) return consume(sp, stack, useBountySeal(sp));
            if (stack.is(REFORGE_STONE)) return consume(sp, stack, useReforge(sp));
            return InteractionResult.PASS;
        });
    }

    private static InteractionResult consume(ServerPlayer sp, ItemStack stack, boolean ok) {
        if (!ok) return InteractionResult.FAIL;
        if (!sp.isCreative()) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private static boolean useManaCrystal(ServerPlayer sp) {
        StatManager.addMana(sp, StatManager.getMaxMana(sp) * 0.5);
        msg(sp, "Mana surges through you.", ChatFormatting.AQUA);
        return true;
    }

    private static boolean useCursedEssence(ServerPlayer sp) {
        if (StatManager.getMaxCursedEnergy(sp) <= 0) {
            msg(sp, "You have no cursed energy to restore.", ChatFormatting.GRAY);
            return false;
        }
        StatManager.addCursedEnergy(sp, StatManager.getMaxCursedEnergy(sp) * 0.4);
        msg(sp, "Cursed energy floods your veins.", ChatFormatting.DARK_PURPLE);
        return true;
    }

    private static boolean useExorcismToken(ServerPlayer sp) {
        DataManager.data().playerBuffs.computeIfAbsent(sp.getStringUUID(), u -> new ArrayList<>()).add("double_exorcism");
        msg(sp, "The next exorcism will yield double rewards.", ChatFormatting.LIGHT_PURPLE);
        return true;
    }

    private static boolean useGradeScroll(ServerPlayer sp) {
        String uuid = sp.getStringUUID();
        int grade = DataManager.sorcererGrade(uuid);
        if (grade >= 5) {
            msg(sp, "You are already Special Grade.", ChatFormatting.GOLD);
            return false;
        }
        DataManager.setSorcererGrade(uuid, grade + 1);
        msg(sp, "Your sorcerer grade rises to " + DataManager.gradeLabel(grade + 1) + ".", ChatFormatting.GOLD);
        return true;
    }

    private static boolean useAwakening(ServerPlayer sp) {
        String uuid = sp.getStringUUID();
        if (DataManager.sorcererGrade(uuid) >= 1) {
            msg(sp, "Your cursed energy is already awakened.", ChatFormatting.GRAY);
            return false;
        }
        DataManager.setSorcererGrade(uuid, 1);
        StatManager.refillCursedEnergy(sp);
        msg(sp, "Cursed energy awakens within you!", ChatFormatting.DARK_PURPLE);
        return true;
    }

    private static boolean useBountySeal(ServerPlayer sp) {
        DataManager.addCoins(sp.getStringUUID(), 150);
        msg(sp, "Redeemed a Bounty Seal for 150 coins.", ChatFormatting.GOLD);
        return true;
    }

    private static boolean useReforge(ServerPlayer sp) {
        ItemStack held = sp.getMainHandItem();
        if (held.isEmpty()) {
            msg(sp, "Hold an item to reforge.", ChatFormatting.RED);
            return false;
        }
        Rarity r = ItemStats.rarityOf(held);
        Rarity next = r.up();
        ItemStats.setRarity(held, next);
        ItemStats.decorate(held);
        msg(sp, "Reforged to " + next.display + ".", next.color);
        return true;
    }

    private static void msg(ServerPlayer sp, String text, ChatFormatting color) {
        sp.sendSystemMessage(Component.literal(text).withStyle(color));
    }

    private static Item reg(String name, String title, String desc) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().stacksTo(16).setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        ALL.put(name, registered);
        return registered;
    }

    /** Display stack with lore for creative tabs. */
    public static ItemStack display(Item item, String desc) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(desc).withStyle(ChatFormatting.GRAY))));
        return stack;
    }
}
