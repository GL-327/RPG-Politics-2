package com.political.curse;

import com.political.combat.StatManager;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cursed weapons. Anyone can swing them, but their true power answers to cursed energy:
 * sorcerers pour cursed energy into the strike for extra distorted damage, while those
 * under a Heavenly Restriction (or with only a sliver of cursed energy) wield them with
 * monstrous physical force and need no energy at all. Built as plain items whose bite is
 * delivered through {@link com.political.combat.AbilityEngine} (no mixins, no tool-material plumbing).
 */
public final class CursedGear {

    public static final String MOD_ID = "politicalserver";

    /** name -> base bonus damage. */
    private static final Map<String, Float> BASE = new LinkedHashMap<>();
    static {
        BASE.put("cursed_dagger", 4f);
        BASE.put("cursed_blade", 7f);
        BASE.put("soul_cleaver", 9f);
        BASE.put("cursed_polearm", 11f); // the tool-specialist's reach weapon
        BASE.put("cursed_greatsword", 12f);
        BASE.put("cursed_whip", 5f);
    }

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    private CursedGear() {}

    public static List<Item> items() {
        return new ArrayList<>(ITEMS.values());
    }

    public static void register() {
        for (String name : BASE.keySet()) {
            ITEMS.put(name, register(name, new Item.Properties().stacksTo(1).durability(1800)));
        }
    }

    /**
     * Extra Skyblock hit damage for cursed tools. Consumed by {@link com.political.combat.AbilityEngine}
     * so cursed strikes are folded into the single Skyblock attack (no double-hit callback chain).
     *
     * @return bonus damage, or {@code 0} if {@code weapon} is not a cursed tool
     */
    public static float attackBonus(ServerPlayer sp, ItemStack weapon) {
        Float base = baseFor(weapon);
        if (base == null) return 0f;
        var trait = DataManager.cursedTrait(sp.getStringUUID());
        if (trait.isToolSpecialist()) {
            return base * 2.0f;
        }
        if (trait.canUseTechniques() && StatManager.getCursedEnergy(sp) >= 5) {
            StatManager.spendCursedEnergy(sp, 5);
            return base + (float) (StatManager.getCursedEnergy(sp) * 0.04);
        }
        return base * 0.6f;
    }

    public static void playHitFx(ServerLevel level, LivingEntity target) {
        level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1, target.getZ(),
                16, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.8f, 0.7f);
    }

    public static boolean isCursedTool(ItemStack stack) {
        return baseFor(stack) != null;
    }

    private static Float baseFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        for (var e : ITEMS.entrySet()) {
            if (stack.is(e.getValue())) return BASE.get(e.getKey());
        }
        return null;
    }

    private static Item register(String name, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(props.setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        return registered;
    }

    /** Builds a display stack with cursed lore for creative tabs / gifts. */
    public static ItemStack display(String name) {
        Item item = ITEMS.get(name);
        if (item == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, Boolean.TRUE);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Cursed Tool").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                Component.literal("Sorcerers fuel it with cursed energy.").withStyle(ChatFormatting.GRAY),
                Component.literal("The restricted wield it with raw might.").withStyle(ChatFormatting.DARK_GRAY))));
        return stack;
    }
}
