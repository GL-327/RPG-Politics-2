package com.political.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * The heart of the Skyblock-style stat system. Every item — vanilla or custom — resolves
 * to a {@link Sheet} of stats here. Custom {@link RpgItem}s carry authoritative stat NBT;
 * plain vanilla items have their stats inferred from their kind + material tier and scaled
 * by {@link Rarity}. A {@link Variant} (Unique / Cursed) can be layered on top.
 */
public final class ItemStats {

    // NBT keys (the first five match the legacy keys StatManager already used).
    public static final String DAMAGE = "rpg_damage";
    public static final String HEALTH = "rpg_health";
    public static final String DEFENSE = "rpg_defense";
    public static final String STRENGTH = "rpg_strength";
    public static final String INTELLIGENCE = "rpg_intelligence";
    public static final String CURSED = "rpg_cursed";
    public static final String CRIT_CHANCE = "rpg_crit_chance";
    public static final String CRIT_DAMAGE = "rpg_crit_damage";
    public static final String FEROCITY = "rpg_ferocity";
    public static final String SPEED = "rpg_speed";
    public static final String ATTACK_SPEED = "rpg_attack_speed";
    public static final String RARITY = "rpg_rarity";
    public static final String VARIANT = "rpg_variant";
    public static final String CURSED_GRADE = "rpg_cursed_grade";

    private ItemStats() {}

    public enum Kind { SWORD, AXE, TOOL, RANGED, HELMET, CHEST, LEGS, BOOTS, OTHER }

    /** A computed stat contribution from a single item. */
    public static final class Sheet {
        public double health, defense, strength, intelligence, cursed, damage;
        public double critChance, critDamage, ferocity, speed, attackSpeed;

        void scale(double m) {
            health *= m; defense *= m; strength *= m; intelligence *= m; cursed *= m; damage *= m;
            critChance *= m; critDamage *= m; ferocity *= m; speed *= m; attackSpeed *= m;
        }

        boolean isEmpty() {
            return health == 0 && defense == 0 && strength == 0 && intelligence == 0 && cursed == 0
                    && damage == 0 && critChance == 0 && critDamage == 0 && ferocity == 0 && speed == 0 && attackSpeed == 0;
        }
    }

    // ------------------------------------------------------------------
    // Resolution
    // ------------------------------------------------------------------

    public static CompoundTag tagOf(ItemStack stack) {
        CustomData data = stack == null ? null : stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static String idPath(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    public static Kind kindOf(Item item) {
        String p = idPath(item);
        if (p.endsWith("_sword")) return Kind.SWORD;
        if (p.endsWith("_axe")) return Kind.AXE;
        if (p.endsWith("_pickaxe") || p.endsWith("_shovel") || p.endsWith("_hoe")) return Kind.TOOL;
        if (p.equals("bow") || p.equals("crossbow") || p.equals("trident")) return Kind.RANGED;
        if (p.endsWith("_helmet") || p.equals("turtle_helmet")) return Kind.HELMET;
        if (p.endsWith("_chestplate") || p.equals("elytra")) return Kind.CHEST;
        if (p.endsWith("_leggings")) return Kind.LEGS;
        if (p.endsWith("_boots")) return Kind.BOOTS;
        return Kind.OTHER;
    }

    /** Material tier 0 (wood/leather) .. 5 (netherite). */
    public static int tierOf(Item item) {
        String p = idPath(item);
        if (p.startsWith("netherite_")) return 5;
        if (p.startsWith("diamond_")) return 4;
        if (p.equals("turtle_helmet") || p.equals("trident")) return 3;
        if (p.startsWith("iron_") || p.startsWith("chainmail_")) return 2;
        if (p.equals("bow") || p.equals("crossbow")) return 2;
        if (p.startsWith("stone_") || p.startsWith("golden_")) return 1;
        if (p.startsWith("wooden_") || p.startsWith("leather_")) return 0;
        if (p.endsWith("_sword") || p.endsWith("_axe")) return 1;
        return 0;
    }

    public static Rarity rarityOf(ItemStack stack) {
        CompoundTag tag = tagOf(stack);
        Rarity r = Rarity.byId(tag.getStringOr(RARITY, ""));
        if (r != null) return r;
        String itemId = tag.getStringOr(RpgItems.ITEM_ID_KEY, "");
        if (!itemId.isEmpty()) {
            RpgItem it = RpgItem.byId(itemId);
            if (it != null) return it.rarity;
        }
        return defaultRarity(stack.getItem());
    }

    public static Rarity defaultRarity(Item item) {
        return switch (tierOf(item)) {
            case 0, 1 -> Rarity.COMMON;
            case 2 -> Rarity.UNCOMMON;
            case 3 -> Rarity.RARE;
            case 4 -> Rarity.EPIC;
            default -> Rarity.LEGENDARY;
        };
    }

    public static int cursedGradeOf(ItemStack stack) {
        return tagOf(stack).getIntOr(CURSED_GRADE, 0);
    }

    public static Variant variantOf(ItemStack stack) {
        CompoundTag tag = tagOf(stack);
        if (tag.getIntOr(CURSED_GRADE, 0) > 0) return Variant.CURSED;
        Variant v = Variant.byId(tag.getStringOr(VARIANT, ""));
        return v == null ? Variant.NONE : v;
    }

    private static boolean hasExplicitStats(CompoundTag tag) {
        return !tag.getStringOr(RpgItems.ITEM_ID_KEY, "").isEmpty()
                || tag.getIntOr(HEALTH, 0) != 0 || tag.getIntOr(DEFENSE, 0) != 0
                || tag.getIntOr(STRENGTH, 0) != 0 || tag.getIntOr(INTELLIGENCE, 0) != 0
                || tag.getIntOr(CURSED, 0) != 0;
    }

    // ------------------------------------------------------------------
    // Computation
    // ------------------------------------------------------------------

    public static Sheet compute(ItemStack stack) {
        Sheet s = new Sheet();
        if (stack == null || stack.isEmpty()) return s;
        CompoundTag tag = tagOf(stack);
        Item item = stack.getItem();
        Kind kind = kindOf(item);
        int tier = tierOf(item);

        if (hasExplicitStats(tag)) {
            // Custom gear: explicit stats are authoritative and already balanced.
            s.health += tag.getIntOr(HEALTH, 0);
            s.defense += tag.getIntOr(DEFENSE, 0);
            s.strength += tag.getIntOr(STRENGTH, 0);
            s.intelligence += tag.getIntOr(INTELLIGENCE, 0);
            s.cursed += tag.getIntOr(CURSED, 0);
            s.damage += tag.getIntOr(DAMAGE, 0);
            s.critChance += tag.getIntOr(CRIT_CHANCE, 0);
            s.critDamage += tag.getIntOr(CRIT_DAMAGE, 0);
            s.ferocity += tag.getIntOr(FEROCITY, 0);
            s.speed += tag.getIntOr(SPEED, 0);
            s.attackSpeed += tag.getIntOr(ATTACK_SPEED, 0);
            addKindCombat(s, kind, tier);
        } else {
            inferBase(s, kind, tier);
            s.scale(rarityOf(stack).mult);
        }

        applyVariant(s, variantOf(stack), cursedGradeOf(stack));
        return s;
    }

    private static void inferBase(Sheet s, Kind kind, int tier) {
        int t = tier + 1;
        switch (kind) {
            case SWORD -> { s.damage += 8 * t; s.strength += 5 * t; s.critChance += 5 + tier * 2; s.critDamage += 10 + tier * 5; }
            case AXE -> { s.damage += 10 * t; s.strength += 6 * t; s.critDamage += tier * 6; s.ferocity += 3; }
            case RANGED -> { s.damage += 7 * t; s.strength += 4 * t; s.critChance += 5 + tier; s.critDamage += tier * 4; }
            case TOOL -> s.defense += t;
            case HELMET -> { s.health += 8 * t; s.defense += 4 * t; }
            case CHEST -> { s.health += 14 * t; s.defense += 8 * t; }
            case LEGS -> { s.health += 12 * t; s.defense += 6 * t; }
            case BOOTS -> { s.health += 8 * t; s.defense += 4 * t; s.speed += tier; }
            case OTHER -> { }
        }
    }

    /** Custom weapons still crit, even though their explicit stats don't list it. */
    private static void addKindCombat(Sheet s, Kind kind, int tier) {
        switch (kind) {
            case SWORD -> { s.critChance += 10; s.critDamage += 30; }
            case AXE -> { s.critDamage += 40; s.ferocity += 5; }
            case RANGED -> { s.critChance += 8; s.critDamage += 20; }
            default -> { }
        }
    }

    private static void applyVariant(Sheet s, Variant variant, int grade) {
        switch (variant) {
            case UNIQUE -> {
                s.scale(Variant.UNIQUE_MULT);
                s.ferocity += 5;
            }
            case CURSED -> {
                int g = Math.max(1, grade);
                s.cursed += 20 * g;
                s.strength += 6 * g;
                s.critDamage += 10 * g;
            }
            case NONE -> { }
        }
    }

    // ------------------------------------------------------------------
    // Stamping rarity / variant onto a stack + visuals
    // ------------------------------------------------------------------

    private static void putTag(ItemStack stack, java.util.function.Consumer<CompoundTag> edit) {
        CompoundTag tag = tagOf(stack);
        edit.accept(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setRarity(ItemStack stack, Rarity rarity) {
        putTag(stack, t -> t.putString(RARITY, rarity.name()));
    }

    public static void setVariant(ItemStack stack, Variant variant) {
        putTag(stack, t -> {
            if (variant == Variant.NONE) t.remove(VARIANT);
            else t.putString(VARIANT, variant.name());
            if (variant != Variant.CURSED) t.remove(CURSED_GRADE);
        });
    }

    /** Marks a stack as a Cursed tool of the given grade (1-4). */
    public static void setCursedGrade(ItemStack stack, int grade) {
        putTag(stack, t -> {
            t.putInt(CURSED_GRADE, Math.max(1, grade));
            t.putString(VARIANT, Variant.CURSED.name());
        });
    }

    /** Rewrites a stack's name colour + lore from its resolved rarity/variant/stats. */
    public static void decorate(ItemStack stack) {
        Rarity rarity = rarityOf(stack);
        Variant variant = variantOf(stack);

        Component baseName = Component.translatable(stack.getItem().getDescriptionId());
        String rpgId = tagOf(stack).getStringOr(RpgItems.ITEM_ID_KEY, "");
        if (!rpgId.isEmpty()) {
            RpgItem def = RpgItem.byId(rpgId);
            if (def != null) baseName = Component.literal(def.displayName);
        }
        String prefix = variant == Variant.NONE ? "" : variant.display + " ";
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(prefix).append(baseName).withStyle(rarity.color, ChatFormatting.BOLD));
        // Skyblock-governed gear carries no vanilla attribute modifiers (armor / attack damage).
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.LORE, new ItemLore(SkyblockTooltipBuilder.build(stack)));
    }
}
