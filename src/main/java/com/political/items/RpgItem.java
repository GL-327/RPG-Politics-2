package com.political.items;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Original rebranded RPG gear. Stats are carried as {@code custom_data} integer keys
 * ({@code rpg_health}, {@code rpg_defense}, {@code rpg_strength}, {@code rpg_intelligence})
 * which {@link com.political.combat.StatManager} reads when equipped, plus an
 * {@code rpg_abilities} list read by {@link com.political.combat.AbilityEngine}.
 *
 * <p>This catalogue condenses the legacy mod's legendary tool sets, block/gem armour,
 * dragon chestplates and beam weapons into the stat + ability system used by this build.
 */
public enum RpgItem {
    // ---------------- Weapons ----------------
    AEGIS_BLADE("Aegis Blade", Items.NETHERITE_SWORD, Rarity.EPIC, 0, 10, 80, 0,
            Ability.CRIT_STRIKE),
    EMBER_STAFF("Ember Staff", Items.BLAZE_ROD, Rarity.EPIC, 0, 0, 40, 120,
            Ability.IGNITE, Ability.FIRE_IMMUNE),
    VOID_REAVER("Void Reaver", Items.NETHERITE_SWORD, Rarity.MYTHIC, 40, 0, 130, 30,
            Ability.LIFESTEAL, Ability.EXECUTE, Ability.WITHER_TOUCH),
    STORM_EDGE("Storm Edge", Items.DIAMOND_SWORD, Rarity.LEGENDARY, 0, 5, 95, 20,
            Ability.THUNDER_STRIKE, Ability.KNOCKBACK),
    INFERNO_BRAND("Inferno Brand", Items.NETHERITE_AXE, Rarity.LEGENDARY, 20, 0, 110, 0,
            Ability.IGNITE, Ability.FIRE_AURA),
    FROSTMOURNE("Frostmourne", Items.DIAMOND_SWORD, Rarity.LEGENDARY, 30, 10, 85, 0,
            Ability.FROST, Ability.CRIT_STRIKE),
    VENOM_FANG("Venom Fang", Items.IRON_SWORD, Rarity.RARE, 0, 0, 55, 10,
            Ability.POISON, Ability.LIFESTEAL),
    THUNDERCALLER("Thundercaller", Items.TRIDENT, Rarity.LEGENDARY, 0, 0, 90, 40,
            Ability.THUNDER_STRIKE),
    MIDAS_EDGE("Midas Edge", Items.GOLDEN_SWORD, Rarity.EPIC, 0, 0, 60, 0,
            Ability.COIN_BOOST, Ability.CRIT_STRIKE),
    ABYSSAL_BLADE("Abyssal Blade", Items.NETHERITE_SWORD, Rarity.EPIC, 0, 0, 100, 25,
            Ability.EXECUTE, Ability.LIFESTEAL),

    // ---------------- Legendary tools ----------------
    DANIELS_PICKAXE("Daniel's Pickaxe", Items.NETHERITE_PICKAXE, Rarity.MYTHIC, 0, 0, 0, 0,
            Ability.INSTANT_MINE, Ability.TUNNEL_3X3, Ability.VEIN_MINE, Ability.AUTO_SMELT, Ability.FORTUNE_TOUCH),
    TITAN_DRILL("Titan Drill", Items.NETHERITE_PICKAXE, Rarity.LEGENDARY, 0, 0, 0, 0,
            Ability.INSTANT_MINE, Ability.VEIN_MINE, Ability.AUTO_SMELT, Ability.FORTUNE_TOUCH),
    EXCAVATOR_SPADE("Excavator Spade", Items.NETHERITE_SHOVEL, Rarity.EPIC, 0, 0, 0, 0,
            Ability.TUNNEL_3X3, Ability.INSTANT_MINE),
    WORLDCLEAVER("Worldcleaver", Items.NETHERITE_AXE, Rarity.LEGENDARY, 0, 0, 70, 0,
            Ability.TREE_FELLER, Ability.INSTANT_MINE),

    // ---------------- Sentinel set (defensive) ----------------
    SENTINEL_HELM("Sentinel Helm", Items.IRON_HELMET, Rarity.RARE, 40, 25, 0, 0,
            Ability.NIGHT_VISION),
    WARDENS_PLATE("Warden's Plate", Items.NETHERITE_CHESTPLATE, Rarity.EPIC, 100, 60, 0, 0,
            Ability.RESISTANCE),
    TITAN_LEGGINGS("Titan Leggings", Items.DIAMOND_LEGGINGS, Rarity.RARE, 50, 35, 0, 0),
    VOIDWALKER_BOOTS("Voidwalker Boots", Items.DIAMOND_BOOTS, Rarity.RARE, 30, 20, 0, 30,
            Ability.FALL_IMMUNE),

    // ---------------- Inferno (dragon) set ----------------
    INFERNO_HELM("Inferno Crown", Items.NETHERITE_HELMET, Rarity.LEGENDARY, 60, 30, 0, 0,
            Ability.FIRE_IMMUNE),
    INFERNO_PLATE("Inferno Aegis", Items.NETHERITE_CHESTPLATE, Rarity.LEGENDARY, 120, 50, 10, 0,
            Ability.FIRE_IMMUNE, Ability.FIRE_AURA),
    INFERNO_LEGS("Inferno Greaves", Items.NETHERITE_LEGGINGS, Rarity.LEGENDARY, 80, 40, 0, 0,
            Ability.FIRE_IMMUNE),
    INFERNO_BOOTS("Inferno Treads", Items.NETHERITE_BOOTS, Rarity.LEGENDARY, 50, 25, 0, 0,
            Ability.FIRE_IMMUNE),

    // ---------------- Storm set ----------------
    STORM_HELM("Storm Visor", Items.DIAMOND_HELMET, Rarity.LEGENDARY, 50, 25, 0, 20),
    STORM_PLATE("Storm Cuirass", Items.ELYTRA, Rarity.MYTHIC, 90, 30, 0, 30,
            Ability.FLIGHT),
    STORM_LEGS("Storm Greaves", Items.DIAMOND_LEGGINGS, Rarity.LEGENDARY, 60, 30, 0, 0,
            Ability.SPEED),
    STORM_BOOTS("Storm Striders", Items.DIAMOND_BOOTS, Rarity.LEGENDARY, 40, 20, 0, 0,
            Ability.SPEED, Ability.FALL_IMMUNE),

    // ---------------- Void set ----------------
    VOID_HELM("Void Shroud", Items.NETHERITE_HELMET, Rarity.EPIC, 55, 30, 0, 40,
            Ability.NIGHT_VISION),
    VOID_PLATE("Void Mantle", Items.NETHERITE_CHESTPLATE, Rarity.EPIC, 110, 55, 0, 40,
            Ability.RESISTANCE),
    VOID_LEGS("Void Greaves", Items.NETHERITE_LEGGINGS, Rarity.EPIC, 70, 40, 0, 30),
    VOID_BOOTS("Void Steps", Items.NETHERITE_BOOTS, Rarity.EPIC, 45, 25, 0, 30,
            Ability.FALL_IMMUNE),

    // ---------------- Gem armour (utility) ----------------
    LAPIS_HELM("Lapis Circlet", Items.LEATHER_HELMET, Rarity.RARE, 20, 8, 0, 30,
            Ability.XP_BOOST),
    LAPIS_PLATE("Lapis Robe", Items.LEATHER_CHESTPLATE, Rarity.RARE, 30, 10, 0, 40,
            Ability.XP_BOOST, Ability.WATER_BREATHING),
    EMERALD_HELM("Emerald Circlet", Items.LEATHER_HELMET, Rarity.RARE, 20, 8, 0, 0,
            Ability.COIN_BOOST),
    EMERALD_PLATE("Emerald Robe", Items.LEATHER_CHESTPLATE, Rarity.RARE, 30, 10, 0, 0,
            Ability.COIN_BOOST),

    // ---------------- Aquatic (tide) set ----------------
    TIDE_HELM("Tide Helm", Items.TURTLE_HELMET, Rarity.RARE, 30, 15, 0, 20,
            Ability.WATER_BREATHING),
    TIDE_BOOTS("Tide Striders", Items.GOLDEN_BOOTS, Rarity.RARE, 25, 12, 0, 0,
            Ability.WATER_BREATHING, Ability.SPEED);

    public final String displayName;
    public final Item baseItem;
    public final Rarity rarity;
    public final int health;
    public final int defense;
    public final int strength;
    public final int intelligence;
    public final Ability[] abilities;

    RpgItem(String displayName, Item baseItem, Rarity rarity,
            int health, int defense, int strength, int intelligence, Ability... abilities) {
        this.displayName = displayName;
        this.baseItem = baseItem;
        this.rarity = rarity;
        this.health = health;
        this.defense = defense;
        this.strength = strength;
        this.intelligence = intelligence;
        this.abilities = abilities;
    }

    public String id() {
        return name().toLowerCase();
    }

    public static RpgItem byId(String id) {
        try {
            return valueOf(id.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Item rarity controls display colour and is shown in lore. */
    public enum Rarity {
        COMMON(ChatFormatting.WHITE),
        RARE(ChatFormatting.BLUE),
        EPIC(ChatFormatting.LIGHT_PURPLE),
        LEGENDARY(ChatFormatting.GOLD),
        MYTHIC(ChatFormatting.RED);

        public final ChatFormatting color;

        Rarity(ChatFormatting color) {
            this.color = color;
        }
    }
}
