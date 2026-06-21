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
    AEGIS_BLADE("Aegis Blade", Items.NETHERITE_SWORD, Rarity.EPIC, 0, 10, 80, 0, 85,
            Ability.CRIT_STRIKE),
    EMBER_STAFF("Ember Staff", Items.BLAZE_ROD, Rarity.EPIC, 0, 0, 40, 120, 70,
            Ability.IGNITE, Ability.FIRE_IMMUNE),
    VOID_REAVER("Void Reaver", Items.NETHERITE_SWORD, Rarity.MYTHIC, 40, 0, 130, 30, 120,
            Ability.LIFESTEAL, Ability.EXECUTE, Ability.WITHER_TOUCH),
    STORM_EDGE("Storm Edge", Items.DIAMOND_SWORD, Rarity.LEGENDARY, 0, 5, 95, 20, 90,
            Ability.THUNDER_STRIKE, Ability.KNOCKBACK),
    INFERNO_BRAND("Inferno Brand", Items.NETHERITE_AXE, Rarity.LEGENDARY, 20, 0, 110, 0, 105,
            Ability.IGNITE, Ability.FIRE_AURA),
    FROSTMOURNE("Frostmourne", Items.DIAMOND_SWORD, Rarity.LEGENDARY, 30, 10, 85, 0, 88,
            Ability.FROST, Ability.CRIT_STRIKE),
    VENOM_FANG("Venom Fang", Items.IRON_SWORD, Rarity.RARE, 0, 0, 55, 10, 55,
            Ability.POISON, Ability.LIFESTEAL),
    THUNDERCALLER("Thundercaller", Items.TRIDENT, Rarity.LEGENDARY, 0, 0, 90, 40, 95,
            Ability.THUNDER_STRIKE),
    MIDAS_EDGE("Midas Edge", Items.GOLDEN_SWORD, Rarity.EPIC, 0, 0, 60, 0, 65,
            Ability.COIN_BOOST, Ability.CRIT_STRIKE),
    ABYSSAL_BLADE("Abyssal Blade", Items.NETHERITE_SWORD, Rarity.EPIC, 0, 0, 100, 25, 100,
            Ability.EXECUTE, Ability.LIFESTEAL),

    // ---------------- Legendary tools ----------------
    DANIELS_PICKAXE("Daniel's Pickaxe", Items.NETHERITE_PICKAXE, Rarity.MYTHIC, 0, 0, 0, 0,
            Ability.INSTANT_MINE, Ability.TUNNEL_3X3, Ability.VEIN_MINE, Ability.AUTO_SMELT, Ability.FORTUNE_TOUCH),
    TITAN_DRILL("Titan Drill", Items.NETHERITE_PICKAXE, Rarity.LEGENDARY, 0, 0, 0, 0,
            Ability.INSTANT_MINE, Ability.VEIN_MINE, Ability.AUTO_SMELT, Ability.FORTUNE_TOUCH),
    EXCAVATOR_SPADE("Excavator Spade", Items.NETHERITE_SHOVEL, Rarity.EPIC, 0, 0, 0, 0,
            Ability.TUNNEL_3X3, Ability.INSTANT_MINE),
    WORLDCLEAVER("Worldcleaver", Items.NETHERITE_AXE, Rarity.LEGENDARY, 0, 0, 70, 0, 80,
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
            Ability.WATER_BREATHING, Ability.SPEED),

    // ---------------- Extended arsenal ----------------
    NIGHTFALL_SCYTHE("Nightfall Scythe", Items.IRON_HOE, Rarity.LEGENDARY, 15, 0, 95, 15, 98,
            Ability.WITHER_TOUCH, Ability.LIFESTEAL),
    PHANTOM_BLADE("Phantom Blade", Items.IRON_SWORD, Rarity.EPIC, 0, 0, 75, 20, 78,
            Ability.CRIT_STRIKE, Ability.POISON),
    RADIANT_HALBERD("Radiant Halberd", Items.GOLDEN_AXE, Rarity.LEGENDARY, 10, 5, 105, 0, 110,
            Ability.EXPLOSIVE, Ability.KNOCKBACK),
    CRYSTAL_STAFF("Crystal Staff", Items.END_ROD, Rarity.EPIC, 0, 0, 30, 110, 60,
            Ability.FROST, Ability.THUNDER_STRIKE),
    SOUL_BOW("Soul Bow", Items.BOW, Rarity.EPIC, 0, 0, 70, 20, 72,
            Ability.WITHER_TOUCH, Ability.LIFESTEAL),
    DRAGONSLAYER("Dragonslayer", Items.NETHERITE_AXE, Rarity.MYTHIC, 20, 10, 125, 0, 130,
            Ability.IGNITE, Ability.EXECUTE, Ability.EXPLOSIVE),
    ARACHNO_CLAW("Arachno Claw", Items.SHEARS, Rarity.RARE, 0, 0, 50, 0, 52,
            Ability.POISON, Ability.KNOCKBACK),
    MOONLIT_KATANA("Moonlit Katana", Items.DIAMOND_SWORD, Rarity.LEGENDARY, 0, 5, 88, 10, 92,
            Ability.CRIT_STRIKE, Ability.FROST),
    GHOST_DAGGER("Ghost Dagger", Items.STONE_SWORD, Rarity.RARE, 0, 0, 45, 15, 48,
            Ability.CRIT_STRIKE),
    SOLAR_SPEAR("Solar Spear", Items.TRIDENT, Rarity.LEGENDARY, 0, 0, 92, 5, 95,
            Ability.IGNITE, Ability.THUNDER_STRIKE),
    BERSERKERS_AXE("Berserker's Axe", Items.IRON_AXE, Rarity.EPIC, 0, 0, 85, 0, 88,
            Ability.KNOCKBACK, Ability.EXPLOSIVE),
    SHADOW_SHIELD("Shadow Shield", Items.SHIELD, Rarity.RARE, 30, 40, 0, 0,
            Ability.RESISTANCE),
    OCEAN_TRIDENT("Ocean Trident", Items.TRIDENT, Rarity.EPIC, 0, 10, 78, 25, 80,
            Ability.WATER_BREATHING, Ability.FROST),
    VAMPIRE_FANG("Vampire Fang", Items.FERMENTED_SPIDER_EYE, Rarity.LEGENDARY, 0, 0, 65, 0, 68,
            Ability.LIFESTEAL, Ability.POISON),
    SKULL_MACE("Skull Mace", Items.MACE, Rarity.MYTHIC, 10, 0, 115, 0, 118,
            Ability.KNOCKBACK, Ability.EXECUTE, Ability.CRIT_STRIKE),

    // ---------------- Necrotic set ----------------
    NECRO_CROWN("Necrotic Crown", Items.WITHER_SKELETON_SKULL, Rarity.EPIC, 45, 25, 0, 30,
            Ability.NIGHT_VISION, Ability.WITHER_TOUCH),
    NECRO_GARB("Necrotic Garb", Items.LEATHER_CHESTPLATE, Rarity.EPIC, 90, 45, 0, 25,
            Ability.REGEN, Ability.RESISTANCE),
    NECRO_LEGS("Necrotic Leggings", Items.LEATHER_LEGGINGS, Rarity.EPIC, 65, 35, 0, 20),
    NECRO_BOOTS("Necrotic Boots", Items.LEATHER_BOOTS, Rarity.EPIC, 35, 20, 0, 0,
            Ability.FALL_IMMUNE),

    // ---------------- Radiant set ----------------
    RADIANT_DIADEM("Radiant Diadem", Items.GOLDEN_HELMET, Rarity.LEGENDARY, 55, 30, 10, 40),
    RADIANT_VESTMENTS("Radiant Vestments", Items.GOLDEN_CHESTPLATE, Rarity.LEGENDARY, 100, 50, 15, 50,
            Ability.REGEN, Ability.ABSORPTION),
    RADIANT_LEGGINGS("Radiant Leggings", Items.GOLDEN_LEGGINGS, Rarity.LEGENDARY, 70, 40, 0, 30),
    RADIANT_SANDALS("Radiant Sandals", Items.GOLDEN_BOOTS, Rarity.LEGENDARY, 40, 25, 0, 0,
            Ability.FALL_IMMUNE, Ability.SPEED),

    // ---------------- Extended tools ----------------
    STARFORGE_HAMMER("Starforge Hammer", Items.NETHERITE_PICKAXE, Rarity.LEGENDARY, 0, 0, 20, 0, 65,
            Ability.INSTANT_MINE, Ability.AUTO_SMELT),
    MOONLIGHT_PICK("Moonlight Pick", Items.DIAMOND_PICKAXE, Rarity.EPIC, 0, 0, 0, 0,
            Ability.VEIN_MINE, Ability.FORTUNE_TOUCH),
    ARCANE_HOE("Arcane Hoe", Items.DIAMOND_HOE, Rarity.RARE, 0, 0, 0, 40, 0,
            Ability.XP_BOOST, Ability.AUTO_SMELT),
    TIMBER_AXE("Timber Axe", Items.DIAMOND_AXE, Rarity.RARE, 0, 0, 35, 0, 40,
            Ability.TREE_FELLER),
    PROSPECTOR_SHOVEL("Prospector Shovel", Items.IRON_SHOVEL, Rarity.UNCOMMON, 0, 0, 0, 0,
            Ability.FORTUNE_TOUCH, Ability.TUNNEL_3X3);

    public final String displayName;
    public final Item baseItem;
    public final Rarity rarity;
    public final int health;
    public final int defense;
    public final int strength;
    public final int intelligence;
    public final int damage;
    public final Ability[] abilities;

    RpgItem(String displayName, Item baseItem, Rarity rarity,
            int health, int defense, int strength, int intelligence, Ability... abilities) {
        this(displayName, baseItem, rarity, health, defense, strength, intelligence, 0, abilities);
    }

    RpgItem(String displayName, Item baseItem, Rarity rarity,
            int health, int defense, int strength, int intelligence, int damage, Ability... abilities) {
        this.displayName = displayName;
        this.baseItem = baseItem;
        this.rarity = rarity;
        this.health = health;
        this.defense = defense;
        this.strength = strength;
        this.intelligence = intelligence;
        this.damage = damage;
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
}
