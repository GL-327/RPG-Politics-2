package com.political.expansion.ranged;

import com.political.items.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Catalogue of the ranged &amp; magic arsenal. Every entry is a Skyblock-stat item (no vanilla
 * base attack/armour): stats are carried in {@code custom_data} and resolved by the shared
 * {@code com.political.items.ItemStats} pipeline exactly like {@code RpgItem}, but this enum
 * lives in the expansion package and binds each weapon to a self-contained {@link RangedCast}.
 *
 * <p>Item ids are all prefixed {@code arc_} (e.g. {@code arc_emberbow}). The base {@link Item}
 * is irrelevant to rendering — the {@code ITEM_MODEL} component overrides it with the custom
 * texture — but is chosen to have no conflicting right-click use so the cast fires cleanly.
 */
public enum RangedWeapon {
    // ---------------- Bows ----------------
    HUNTBOW("Hunter's Recurve", Category.BOW, Items.BLAZE_ROD, Rarity.COMMON,
            40, 55, 30, 12, 30, 0, RangedCast.ARROW_VOLLEY),
    WINDBOW("Galewind Bow", Category.BOW, Items.BLAZE_ROD, Rarity.UNCOMMON,
            70, 60, 35, 14, 35, 0, RangedCast.WIND_SHEAR),
    EMBERBOW("Emberflare Bow", Category.BOW, Items.BLAZE_ROD, Rarity.RARE,
            110, 75, 45, 18, 50, 0, RangedCast.FIREBOLT),
    SOULBOW("Soulpiercer Bow", Category.BOW, Items.BLAZE_ROD, Rarity.EPIC,
            150, 85, 55, 22, 60, 3, RangedCast.SOUL_BEAM),
    FROSTBOW("Frostbite Longbow", Category.BOW, Items.BLAZE_ROD, Rarity.EPIC,
            160, 80, 50, 20, 55, 0, RangedCast.FROST_VOLLEY),
    STORMBOW("Stormcaller Bow", Category.BOW, Items.BLAZE_ROD, Rarity.LEGENDARY,
            210, 95, 65, 25, 75, 4, RangedCast.CHAIN_LIGHTNING),
    VOIDBOW("Voidrend Bow", Category.BOW, Items.BLAZE_ROD, Rarity.MYTHIC,
            280, 120, 80, 30, 90, 8, RangedCast.VOID_LANCE),

    // ---------------- Crossbows ----------------
    BOLTCASTER("Repeating Boltcaster", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.RARE,
            90, 80, 50, 20, 55, 5, RangedCast.ARROW_VOLLEY),
    VENOMBOLT("Venomspitter Crossbow", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.RARE,
            100, 78, 48, 16, 45, 0, RangedCast.POISON_BARRAGE),
    GLACIALBOLT("Glacial Arbalest", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.EPIC,
            150, 90, 58, 22, 60, 0, RangedCast.FROST_VOLLEY),
    INFERNOBOLT("Infernal Arbalest", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.EPIC,
            150, 95, 60, 24, 65, 5, RangedCast.EXPLOSIVE_SHOT),
    THUNDERBOLT("Thunderlock Crossbow", Category.CROSSBOW, Items.BLAZE_ROD, Rarity.LEGENDARY,
            200, 105, 70, 26, 80, 6, RangedCast.CHAIN_LIGHTNING),

    // ---------------- Throwing weapons ----------------
    THROWING_KNIVES("Shadow Throwing Knives", Category.THROWING, Items.BLAZE_ROD, Rarity.UNCOMMON,
            50, 60, 40, 30, 45, 10, RangedCast.KNIFE_FAN),
    CHAKRAM("Razor Chakram", Category.THROWING, Items.BLAZE_ROD, Rarity.RARE,
            70, 75, 50, 22, 55, 8, RangedCast.RICOCHET),
    THROWING_STARS("Tempest Shuriken", Category.THROWING, Items.BLAZE_ROD, Rarity.RARE,
            80, 70, 48, 28, 50, 12, RangedCast.STAR_STORM),
    KUNAI("Cursed Kunai", Category.THROWING, Items.BLAZE_ROD, Rarity.EPIC,
            120, 85, 55, 26, 60, 8, RangedCast.KNIFE_FAN),
    JAVELIN("Stormpoint Javelin", Category.THROWING, Items.BLAZE_ROD, Rarity.EPIC,
            130, 100, 62, 20, 65, 5, RangedCast.JAVELIN_PIERCE),

    // ---------------- Wands ----------------
    EMBERWAND("Cinder Wand", Category.WAND, Items.BLAZE_ROD, Rarity.COMMON,
            80, 45, 20, 14, 40, 0, RangedCast.FIREBOLT),
    FROSTWAND("Rime Wand", Category.WAND, Items.BLAZE_ROD, Rarity.UNCOMMON,
            110, 50, 22, 14, 40, 0, RangedCast.FROST_VOLLEY),
    GALEWAND("Zephyr Wand", Category.WAND, Items.BLAZE_ROD, Rarity.UNCOMMON,
            110, 48, 22, 14, 40, 0, RangedCast.WIND_SHEAR),
    LIFEWAND("Verdant Wand", Category.WAND, Items.BLAZE_ROD, Rarity.RARE,
            150, 30, 15, 10, 30, 0, RangedCast.HEAL_BEAM),
    ARCANEWAND("Arcane Scepter", Category.WAND, Items.BLAZE_ROD, Rarity.RARE,
            160, 60, 28, 18, 55, 0, RangedCast.MAGIC_MISSILE),

    // ---------------- Staves ----------------
    PYROSTAFF("Pyroclasm Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.EPIC,
            190, 70, 35, 18, 55, 0, RangedCast.METEOR),
    CRYOSTAFF("Cryostorm Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.EPIC,
            190, 68, 34, 18, 50, 0, RangedCast.BLIZZARD),
    LIFESTAFF("Staff of Renewal", Category.STAFF, Items.BLAZE_ROD, Rarity.LEGENDARY,
            240, 40, 20, 12, 35, 0, RangedCast.HOLY_NOVA),
    STORMSTAFF("Tempest Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.LEGENDARY,
            240, 85, 45, 22, 70, 4, RangedCast.STORM_FIELD),
    VOIDSTAFF("Oblivion Staff", Category.STAFF, Items.BLAZE_ROD, Rarity.MYTHIC,
            300, 100, 55, 26, 85, 6, RangedCast.VOID_LANCE),

    // ---------------- Tomes / Spellbooks ----------------
    PYROMANCY_TOME("Tome of Pyromancy", Category.TOME, Items.BOOK, Rarity.RARE,
            160, 55, 25, 16, 50, 0, RangedCast.FLAME_WAVE),
    FROST_GRIMOIRE("Grimoire of Frost", Category.TOME, Items.BOOK, Rarity.EPIC,
            200, 60, 28, 18, 50, 0, RangedCast.BLIZZARD),
    SHADOW_TOME("Tome of Shadows", Category.TOME, Items.BOOK, Rarity.EPIC,
            200, 65, 30, 18, 55, 4, RangedCast.SHADOW_BOLT),
    STORM_CODEX("Codex of Storms", Category.TOME, Items.BOOK, Rarity.LEGENDARY,
            250, 80, 40, 22, 70, 5, RangedCast.STORM_FIELD),
    HOLY_CODEX("Codex of Light", Category.TOME, Items.BOOK, Rarity.LEGENDARY,
            250, 50, 25, 14, 45, 0, RangedCast.HOLY_NOVA),
    NECRO_TOME("Necronomicon", Category.TOME, Items.BOOK, Rarity.MYTHIC,
            320, 90, 45, 24, 80, 8, RangedCast.DECAY_FIELD),

    // ---------------- Orbs ----------------
    ARCANE_ORB("Arcane Orb", Category.ORB, Items.BOOK, Rarity.RARE,
            170, 55, 26, 16, 50, 0, RangedCast.ARCANE_ORB),
    SOUL_ORB("Soul Reliquary", Category.ORB, Items.BOOK, Rarity.EPIC,
            210, 70, 35, 20, 55, 4, RangedCast.SOUL_BEAM);

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
    public final RangedCast cast;

    RangedWeapon(String displayName, Category category, Item baseItem, Rarity rarity,
                 int intelligence, int damage, int strength, int critChance, int critDamage,
                 int ferocity, RangedCast cast) {
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

    /** All ids are prefixed {@code arc_}; e.g. {@code EMBERBOW -> arc_emberbow}. */
    public String id() {
        return "arc_" + name().toLowerCase();
    }

    /** Item category, used for the rarity footer label and texture archetype. */
    public enum Category {
        BOW("BOW"), CROSSBOW("CROSSBOW"), THROWING("THROWING WEAPON"),
        WAND("WAND"), STAFF("STAFF"), TOME("SPELLBOOK"), ORB("ORB");

        public final String footer;
        Category(String footer) { this.footer = footer; }
    }

    private static final Map<String, RangedWeapon> BY_ID = new HashMap<>();
    static {
        for (RangedWeapon w : values()) BY_ID.put(w.id(), w);
    }

    public static RangedWeapon byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }
}
