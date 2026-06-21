package com.political.expansion2.ranged;

import com.political.items.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/** Phase-2 ranged/magic arsenal — 92 weapons, ids prefixed {@code arc2_}. */
public enum RangedWeapon2 {
    SOLAR_BOW("Helios Longbow", Category.BOW, Items.BLAZE_ROD, Rarity.COMMON,
            58, 70, 39, 14, 37, 0, RangedCast2.SOLAR_BEAM),
    CINDER_BOW("Cinder Recurve", Category.BOW, Items.BLAZE_ROD, Rarity.UNCOMMON,
            81, 98, 54, 16, 42, 0, RangedCast2.CINDER_CHAIN),
    INFERNO_BOW("Inferno Greatbow", Category.BOW, Items.BLAZE_ROD, Rarity.RARE,
            110, 133, 73, 18, 48, 1, RangedCast2.INFERNO_NOVA),
    MAGMA_BOW("Magma Strider Bow", Category.BOW, Items.BLAZE_ROD, Rarity.RARE,
            110, 133, 73, 18, 48, 1, RangedCast2.MAGMA_DOMAIN),
    PHOENIX_BOW("Phoenix Wing Bow", Category.BOW, Items.BLAZE_ROD, Rarity.EPIC,
            144, 175, 96, 20, 55, 3, RangedCast2.PHOENIX_BURST),
    ASH_BOW("Ashwind Bow", Category.BOW, Items.BLAZE_ROD, Rarity.EPIC,
            144, 175, 96, 20, 55, 3, RangedCast2.ASH_WAVE),
    BLAZE_BOW("Blazering Bow", Category.BOW, Items.BLAZE_ROD, Rarity.LEGENDARY,
            185, 224, 123, 23, 63, 5, RangedCast2.BLAZE_RING),
    GLACIAL_BOW("Glacial Moonbow", Category.BOW, Items.BLAZE_ROD, Rarity.RARE,
            110, 133, 73, 18, 48, 1, RangedCast2.ICE_LANCE),
    BLIZZARD_BOW("Blizzard Hunter Bow", Category.BOW, Items.BLAZE_ROD, Rarity.EPIC,
            144, 175, 96, 20, 55, 3, RangedCast2.BLIZZARD_VORTEX),
    PERMA_BOW("Permafrost Warbow", Category.BOW, Items.BLAZE_ROD, Rarity.LEGENDARY,
            185, 224, 123, 23, 63, 5, RangedCast2.PERMAFROST_DOMAIN),
    STORM_BOW("Tempest Arc Bow", Category.BOW, Items.BLAZE_ROD, Rarity.LEGENDARY,
            185, 224, 123, 23, 63, 5, RangedCast2.STATIC_CHAIN),
    VOID_BOW("Voidstring Bow", Category.BOW, Items.BLAZE_ROD, Rarity.MYTHIC,
            243, 294, 162, 27, 75, 8, RangedCast2.VOID_BEAM),
    SNIPER_XBOW("Arcane Sniper", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.UNCOMMON,
            110, 119, 65, 16, 42, 0, RangedCast2.BOLT_SNIPER),
    VENOM_XBOW("Viper Crossbow", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.RARE,
            150, 162, 89, 18, 48, 1, RangedCast2.POISON_BEAM),
    FROST_XBOW("Rime Arbalest", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.RARE,
            150, 162, 89, 18, 48, 1, RangedCast2.SHATTER_BOLT),
    THUNDER_XBOW("Voltaic Arbalest", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.EPIC,
            197, 213, 117, 20, 55, 3, RangedCast2.BALL_LIGHTNING),
    SHADOW_XBOW("Nightbolt Crossbow", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.EPIC,
            197, 213, 117, 20, 55, 3, RangedCast2.SHADOW_BEAM),
    HOLY_XBOW("Radiant Crossbow", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.LEGENDARY,
            252, 272, 150, 23, 63, 5, RangedCast2.RADIANT_BEAM),
    NECRO_XBOW("Gravebolt Crossbow", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.LEGENDARY,
            252, 272, 150, 23, 63, 5, RangedCast2.NECROTIC_BEAM),
    PLASMA_XBOW("Plasma Caster", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.EPIC,
            197, 213, 117, 20, 55, 3, RangedCast2.PLASMA_LANCE),
    COSMIC_XBOW("Starfall Crossbow", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.MYTHIC,
            331, 357, 196, 27, 75, 8, RangedCast2.ASTRAL_CHAIN),
    CHRONO_XBOW("Chrono Arbalest", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.LEGENDARY,
            252, 272, 150, 23, 63, 5, RangedCast2.STASIS_BOLT),
    SOLAR_CHAKRAM("Sun Disc", Category.CHAKRAM, Items.BLAZE_ROD, Rarity.UNCOMMON,
            96, 101, 55, 16, 42, 0, RangedCast2.CHAKRAM_VOLLEY),
    FROST_CHAKRAM("Rime Ring", Category.CHAKRAM, Items.BLAZE_ROD, Rarity.RARE,
            130, 137, 75, 18, 48, 1, RangedCast2.CHAKRAM_ORBIT),
    STORM_CHAKRAM("Thunder Ring", Category.CHAKRAM, Items.BLAZE_ROD, Rarity.RARE,
            130, 137, 75, 18, 48, 1, RangedCast2.RICOCHET_STORM),
    SHADOW_CHAKRAM("Umbra Chakram", Category.CHAKRAM, Items.BLAZE_ROD, Rarity.EPIC,
            171, 180, 99, 20, 55, 3, RangedCast2.PHANTOM_STRIKE),
    VENOM_CHAKRAM("Toxin Wheel", Category.CHAKRAM, Items.BLAZE_ROD, Rarity.EPIC,
            171, 180, 99, 20, 55, 3, RangedCast2.VENOM_CHAIN),
    BLOOD_CHAKRAM("Crimson Slicer", Category.CHAKRAM, Items.BLAZE_ROD, Rarity.LEGENDARY,
            218, 230, 127, 23, 63, 5, RangedCast2.VAMPIRE_CHAIN),
    ARCANE_CHAKRAM("Rune Chakram", Category.CHAKRAM, Items.BLAZE_ROD, Rarity.LEGENDARY,
            218, 230, 127, 23, 63, 5, RangedCast2.ENCHANT_CHAIN),
    VOID_CHAKRAM("Abyss Ring", Category.CHAKRAM, Items.BLAZE_ROD, Rarity.MYTHIC,
            287, 302, 166, 27, 75, 8, RangedCast2.ABYSS_CHAIN),
    EMBER_GUN("Emberlock Pistol", Category.GUN, Items.BLAZE_ROD, Rarity.COMMON,
            89, 95, 52, 14, 37, 0, RangedCast2.SHOTGUN_SPRAY),
    VOLT_GUN("Volt Rifle", Category.GUN, Items.BLAZE_ROD, Rarity.UNCOMMON,
            125, 133, 73, 16, 42, 0, RangedCast2.BOLT_SNIPER),
    FROST_GUN("Cryo Carbine", Category.GUN, Items.BLAZE_ROD, Rarity.RARE,
            170, 181, 99, 18, 48, 1, RangedCast2.ICE_LANCE),
    TOXIN_GUN("Chem Spitter", Category.GUN, Items.BLAZE_ROD, Rarity.RARE,
            170, 181, 99, 18, 48, 1, RangedCast2.ACID_SPRAY),
    VOID_GUN("Null Cannon", Category.GUN, Items.BLAZE_ROD, Rarity.EPIC,
            223, 238, 131, 20, 55, 3, RangedCast2.PHASE_LANCE),
    ARC_GUN("Arc Repeater", Category.GUN, Items.BLAZE_ROD, Rarity.EPIC,
            223, 238, 131, 20, 55, 3, RangedCast2.PLASMA_LANCE),
    SOUL_GUN("Soul Harpoon Gun", Category.GUN, Items.BLAZE_ROD, Rarity.LEGENDARY,
            286, 304, 167, 23, 63, 5, RangedCast2.SOUL_CHAIN),
    STAR_GUN("Nova Blaster", Category.GUN, Items.BLAZE_ROD, Rarity.LEGENDARY,
            286, 304, 167, 23, 63, 5, RangedCast2.COSMIC_NOVA),
    EMP_GUN("EMP Launcher", Category.GUN, Items.BLAZE_ROD, Rarity.EPIC,
            223, 238, 131, 20, 55, 3, RangedCast2.EMP_BLAST),
    METEOR_GUN("Meteor Launcher", Category.GUN, Items.BLAZE_ROD, Rarity.MYTHIC,
            375, 399, 219, 27, 75, 8, RangedCast2.METEOR_SHOWER),
    SPARK_WAND("Spark Wand", Category.WAND, Items.BLAZE_ROD, Rarity.COMMON,
            100, 48, 26, 14, 37, 0, RangedCast2.ARCANE_BEAM),
    GALE_WAND("Gale Wand", Category.WAND, Items.BLAZE_ROD, Rarity.UNCOMMON,
            140, 67, 37, 16, 42, 0, RangedCast2.GALE_BEAM),
    FLAME_WAND("Flame Wand", Category.WAND, Items.BLAZE_ROD, Rarity.UNCOMMON,
            140, 67, 37, 16, 42, 0, RangedCast2.SOLAR_BEAM),
    RIME_WAND("Rime Wand", Category.WAND, Items.BLAZE_ROD, Rarity.RARE,
            190, 91, 50, 18, 48, 1, RangedCast2.CRYO_PULSE),
    VENOM_WAND("Venom Wand", Category.WAND, Items.BLAZE_ROD, Rarity.RARE,
            190, 91, 50, 18, 48, 1, RangedCast2.POISON_BEAM),
    MEND_WAND("Mend Wand", Category.WAND, Items.BLAZE_ROD, Rarity.RARE,
            190, 91, 50, 18, 48, 1, RangedCast2.MEND_BEAM),
    BLOOD_WAND("Sanguine Wand", Category.WAND, Items.BLAZE_ROD, Rarity.EPIC,
            249, 120, 66, 20, 55, 3, RangedCast2.BLOOD_BEAM),
    THUNDER_WAND("Storm Wand", Category.WAND, Items.BLAZE_ROD, Rarity.EPIC,
            249, 120, 66, 20, 55, 3, RangedCast2.THUNDER_BEAM),
    SHADOW_WAND("Shadow Wand", Category.WAND, Items.BLAZE_ROD, Rarity.EPIC,
            249, 120, 66, 20, 55, 3, RangedCast2.SHADOW_BEAM),
    TIME_WAND("Chrono Wand", Category.WAND, Items.BLAZE_ROD, Rarity.LEGENDARY,
            319, 154, 84, 23, 63, 5, RangedCast2.CHRONO_BEAM),
    STAR_WAND("Star Wand", Category.WAND, Items.BLAZE_ROD, Rarity.LEGENDARY,
            319, 154, 84, 23, 63, 5, RangedCast2.STAR_BEAM),
    VOID_WAND("Void Wand", Category.WAND, Items.BLAZE_ROD, Rarity.MYTHIC,
            419, 202, 111, 27, 75, 8, RangedCast2.SINGULARITY),
    PYRO_STAFF("Pyre Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.RARE,
            259, 137, 75, 18, 48, 1, RangedCast2.MAGMA_DOMAIN),
    CRYO_STAFF("Cryo Monarch Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.RARE,
            259, 137, 75, 18, 48, 1, RangedCast2.GLACIAL_NOVA),
    GALE_STAFF("Tempest Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.EPIC,
            341, 180, 99, 20, 55, 3, RangedCast2.TORNADO_DOMAIN),
    STORM_STAFF("Stormcaller Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.EPIC,
            341, 180, 99, 20, 55, 3, RangedCast2.VOLT_DOMAIN),
    LIFE_STAFF("Verdant Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.LEGENDARY,
            437, 230, 127, 23, 63, 5, RangedCast2.VERDANT_NOVA),
    HOLY_STAFF("Sanctified Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.LEGENDARY,
            437, 230, 127, 23, 63, 5, RangedCast2.BLESSING_NOVA),
    NECRO_STAFF("Gravecaller Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.EPIC,
            341, 180, 99, 20, 55, 3, RangedCast2.ROT_NOVA),
    BLOOD_STAFF("Hemomancer Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.EPIC,
            341, 180, 99, 20, 55, 3, RangedCast2.CRIMSON_NOVA),
    NATURE_STAFF("Grovekeeper Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.LEGENDARY,
            437, 230, 127, 23, 63, 5, RangedCast2.GROVE_DOMAIN),
    COSMIC_STAFF("Cosmos Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.MYTHIC,
            573, 302, 166, 27, 75, 8, RangedCast2.ECLIPSE_DOMAIN),
    GOLEM_STAFF("Aegis Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.LEGENDARY,
            437, 230, 127, 23, 63, 5, RangedCast2.GOLEM_SUMMON),
    VEX_STAFF("Hexweaver Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.MYTHIC,
            573, 302, 166, 27, 75, 8, RangedCast2.VEX_SWARM),
    FIRE_TOME("Tome of Embers", Category.TOME, Items.BOOK, Rarity.RARE,
            299, 110, 61, 18, 48, 1, RangedCast2.INFERNO_NOVA),
    FROST_TOME("Tome of Rime", Category.TOME, Items.BOOK, Rarity.RARE,
            299, 110, 61, 18, 48, 1, RangedCast2.PERMAFROST_DOMAIN),
    GALE_TOME("Tome of Gales", Category.TOME, Items.BOOK, Rarity.EPIC,
            394, 145, 80, 20, 55, 3, RangedCast2.HURRICANE_CONE),
    STORM_TOME("Tome of Thunder", Category.TOME, Items.BOOK, Rarity.EPIC,
            394, 145, 80, 20, 55, 3, RangedCast2.STORM_NOVA),
    ARCANE_TOME("Tome of Runes", Category.TOME, Items.BOOK, Rarity.RARE,
            299, 110, 61, 18, 48, 1, RangedCast2.RUNE_DOMAIN),
    SHADOW_TOME("Tome of Night", Category.TOME, Items.BOOK, Rarity.EPIC,
            394, 145, 80, 20, 55, 3, RangedCast2.UMBRA_DOMAIN),
    POISON_TOME("Tome of Plagues", Category.TOME, Items.BOOK, Rarity.EPIC,
            394, 145, 80, 20, 55, 3, RangedCast2.PLAGUE_DOMAIN),
    HOLY_TOME("Tome of Light", Category.TOME, Items.BOOK, Rarity.LEGENDARY,
            504, 186, 102, 23, 63, 5, RangedCast2.SANCTUM_DOMAIN),
    NECRO_TOME("Tome of Graves", Category.TOME, Items.BOOK, Rarity.LEGENDARY,
            504, 186, 102, 23, 63, 5, RangedCast2.GRAVE_DOMAIN),
    BLOOD_TOME("Tome of Sanguis", Category.TOME, Items.BOOK, Rarity.LEGENDARY,
            504, 186, 102, 23, 63, 5, RangedCast2.SANGUINE_RITE),
    COSMIC_TOME("Tome of Stars", Category.TOME, Items.BOOK, Rarity.MYTHIC,
            662, 244, 134, 27, 75, 8, RangedCast2.METEOR_SHOWER),
    TIME_TOME("Tome of Hours", Category.TOME, Items.BOOK, Rarity.LEGENDARY,
            504, 186, 102, 23, 63, 5, RangedCast2.TIME_WARP),
    ARCANE_ORB("Prism Orb", Category.ORB, Items.BOOK, Rarity.RARE,
            239, 99, 54, 18, 48, 1, RangedCast2.PRISMATIC_NOVA),
    FROST_ORB("Glacier Orb", Category.ORB, Items.BOOK, Rarity.RARE,
            239, 99, 54, 18, 48, 1, RangedCast2.FROST_CHAIN),
    STORM_ORB("Stormheart Orb", Category.ORB, Items.BOOK, Rarity.EPIC,
            315, 130, 72, 20, 55, 3, RangedCast2.BALL_LIGHTNING),
    VOID_ORB("Voidheart Orb", Category.ORB, Items.BOOK, Rarity.EPIC,
            315, 130, 72, 20, 55, 3, RangedCast2.ENTROPY_NOVA),
    LIFE_ORB("Lifeheart Orb", Category.ORB, Items.BOOK, Rarity.LEGENDARY,
            403, 166, 92, 23, 63, 5, RangedCast2.MEND_BEAM),
    BLOOD_ORB("Crimson Orb", Category.ORB, Items.BOOK, Rarity.LEGENDARY,
            403, 166, 92, 23, 63, 5, RangedCast2.CRIMSON_NOVA),
    CRYSTAL_ORB("Crystal Orb", Category.ORB, Items.BOOK, Rarity.EPIC,
            315, 130, 72, 20, 55, 3, RangedCast2.GEM_NOVA),
    COSMIC_ORB("Nebula Orb", Category.ORB, Items.BOOK, Rarity.MYTHIC,
            529, 218, 120, 27, 75, 8, RangedCast2.COSMIC_NOVA),
    EMBER_FOCUS("Ember Domain Focus", Category.DOMAIN_FOCUS, Items.BLAZE_ROD, Rarity.EPIC,
            368, 163, 89, 20, 55, 3, RangedCast2.MAGMA_DOMAIN),
    FROST_FOCUS("Frost Domain Focus", Category.DOMAIN_FOCUS, Items.BLAZE_ROD, Rarity.EPIC,
            368, 163, 89, 20, 55, 3, RangedCast2.PERMAFROST_DOMAIN),
    STORM_FOCUS("Storm Domain Focus", Category.DOMAIN_FOCUS, Items.BLAZE_ROD, Rarity.LEGENDARY,
            470, 208, 114, 23, 63, 5, RangedCast2.VOLT_DOMAIN),
    VOID_FOCUS("Void Domain Focus", Category.DOMAIN_FOCUS, Items.BLAZE_ROD, Rarity.LEGENDARY,
            470, 208, 114, 23, 63, 5, RangedCast2.RIFT_DOMAIN),
    GROVE_FOCUS("Grove Domain Focus", Category.DOMAIN_FOCUS, Items.BLAZE_ROD, Rarity.LEGENDARY,
            470, 208, 114, 23, 63, 5, RangedCast2.GROVE_DOMAIN),
    PLAGUE_FOCUS("Plague Domain Focus", Category.DOMAIN_FOCUS, Items.BLAZE_ROD, Rarity.EPIC,
            368, 163, 89, 20, 55, 3, RangedCast2.PLAGUE_DOMAIN),
    SANCTUM_FOCUS("Sanctum Domain Focus", Category.DOMAIN_FOCUS, Items.BLAZE_ROD, Rarity.LEGENDARY,
            470, 208, 114, 23, 63, 5, RangedCast2.SANCTUM_DOMAIN),
    ECLIPSE_FOCUS("Eclipse Domain Focus", Category.DOMAIN_FOCUS, Items.BLAZE_ROD, Rarity.MYTHIC,
            617, 273, 150, 27, 75, 8, RangedCast2.ECLIPSE_DOMAIN);

    public final String displayName;
    public final Category category;
    public final Item baseItem;
    public final Rarity rarity;
    public final int intelligence;
    public final int damage;
    public final int strength;
    public final int critChance;
    public final int critDamage;
    public final int ferocity;
    public final RangedCast2 cast;

    RangedWeapon2(String displayName, Category category, Item baseItem, Rarity rarity,
                  int intelligence, int damage, int strength, int critChance, int critDamage,
                  int ferocity, RangedCast2 cast) {
        this.displayName = displayName;
        this.category = category;
        this.baseItem = baseItem;
        this.rarity = rarity;
        this.intelligence = intelligence;
        this.damage = damage;
        this.strength = strength;
        this.critChance = critChance;
        this.critDamage = critDamage;
        this.ferocity = ferocity;
        this.cast = cast;
    }

    public String id() {
        return "arc2_" + name().toLowerCase();
    }

    public enum Category {
        BOW("BOW"), CROSSBOW("CROSSBOW"), CHAKRAM("CHAKRAM"), GUN("ARC GUN"),
        WAND("WAND"), STAFF("STAFF"), TOME("SPELLBOOK"), ORB("ORB"), DOMAIN_FOCUS("DOMAIN FOCUS");

        public final String footer;
        Category(String footer) { this.footer = footer; }
    }

    private static final Map<String, RangedWeapon2> BY_ID = new HashMap<>();
    static {
        for (RangedWeapon2 w : values()) BY_ID.put(w.id(), w);
    }

    public static RangedWeapon2 byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }
}
