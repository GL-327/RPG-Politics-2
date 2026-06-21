package com.political.world.structures;

import net.minecraft.ChatFormatting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * The eleven above-ground RPG worldgen archetypes scattered across the overworld surface.
 * These are <b>not</b> dungeons — they are discoverable surface sites that tie into the mod's
 * factions and lore (sorcerers, heroes, bandits, the cursed, nobility, merchants, elections).
 *
 * <p>Each type carries its own building palette, an ambient population profile (hostile mob
 * ids drawn from the existing {@code ExpansionMobs}/{@code ExpansionMobs2} rosters, or friendly
 * villager/trader counts), a faction flavour flag, and the loot table its chests roll from.
 */
public enum StructureType {

    SORCERER_WATCHTOWER("sorcerer_watchtower", "Sorcerer Watchtower", ChatFormatting.AQUA,
            Faction.ARCANE, "arcane", 0, false,
            "mob_cultist_acolyte", "mob_storm_herald", null),

    HERO_OUTPOST("hero_outpost", "Hero Outpost", ChatFormatting.GOLD,
            Faction.ORDER, "martial", 3, false,
            null, null, null),

    BANDIT_CAMP("bandit_camp", "Bandit Camp", ChatFormatting.RED,
            Faction.BANDIT, "bandit", 0, false,
            "mob_bandit_outlaw", "mob_bandit_brute", "mob2_bandit_king"),

    CURSED_SHRINE("cursed_shrine", "Cursed Shrine", ChatFormatting.DARK_PURPLE,
            Faction.CURSED, "cursed", 0, true,
            "mob_grave_revenant", "mob_wraith", null),

    ABANDONED_MANOR("abandoned_manor", "Abandoned Manor", ChatFormatting.DARK_GRAY,
            Faction.CURSED, "noble", 0, false,
            "mob_wraith", "mob_plague_bearer", null),

    TRADING_POST("trading_post", "Trading Post", ChatFormatting.GREEN,
            Faction.MERCHANT, "mercantile", 4, false,
            null, null, null),

    ELECTION_HALL_RUIN("election_hall_ruin", "Election Hall Ruin", ChatFormatting.LIGHT_PURPLE,
            Faction.CIVIC, "civic", 1, false,
            "mob_bone_legionnaire", null, null),

    MAGE_TOWER("mage_tower", "Mage Tower", ChatFormatting.BLUE,
            Faction.ARCANE, "arcane", 0, false,
            "mob_cultist_acolyte", "mob_storm_herald", "mob2_archmage_sovereign"),

    BATTLEFIELD("battlefield", "Battlefield Graveyard", ChatFormatting.DARK_RED,
            Faction.CURSED, "battlefield", 0, true,
            "mob_bone_legionnaire", "mob_grave_revenant", null),

    OBELISK("obelisk", "Ancient Obelisk", ChatFormatting.YELLOW,
            Faction.ARCANE, "arcane", 0, false,
            "mob_wraith", null, null),

    WANDERING_MERCHANT("wandering_merchant", "Wandering Merchant Camp", ChatFormatting.AQUA,
            Faction.MERCHANT, "mercantile", 2, false,
            null, null, null);

    /** Faction allegiance, used for flavour broadcasts and mob/loot association. */
    public enum Faction { ARCANE, ORDER, BANDIT, CURSED, MERCHANT, CIVIC }

    public final String id;
    public final String display;
    public final ChatFormatting color;
    public final Faction faction;
    /** Loot table id under {@code chests/structures/<lootTable>}. */
    public final String lootTable;
    /** Number of friendly villagers/traders to populate (0 for hostile sites). */
    public final int villagers;
    /** Cursed sites also summon a low-grade cursed spirit for ambience. */
    public final boolean cursed;
    public final String mobCommon;  // nullable
    public final String mobElite;   // nullable
    public final String mobBoss;    // nullable

    StructureType(String id, String display, ChatFormatting color, Faction faction, String lootTable,
                  int villagers, boolean cursed, String mobCommon, String mobElite, String mobBoss) {
        this.id = id;
        this.display = display;
        this.color = color;
        this.faction = faction;
        this.lootTable = lootTable;
        this.villagers = villagers;
        this.cursed = cursed;
        this.mobCommon = mobCommon;
        this.mobElite = mobElite;
        this.mobBoss = mobBoss;
    }

    /** Primary structural block for this archetype. */
    public Block wallBlock() {
        return switch (this) {
            case SORCERER_WATCHTOWER, MAGE_TOWER -> StructureBlocks.RUNED_STONE;
            case HERO_OUTPOST -> Blocks.STONE_BRICKS;
            case BANDIT_CAMP, WANDERING_MERCHANT -> Blocks.OAK_LOG;
            case CURSED_SHRINE -> StructureBlocks.CURSED_ALTAR;
            case ABANDONED_MANOR -> Blocks.DARK_OAK_PLANKS;
            case TRADING_POST -> Blocks.SPRUCE_PLANKS;
            case ELECTION_HALL_RUIN -> StructureBlocks.MOSSY_MARBLE;
            case BATTLEFIELD -> Blocks.MOSSY_COBBLESTONE;
            case OBELISK -> StructureBlocks.OBELISK_STONE;
        };
    }

    /** Floor / base material. */
    public Block floorBlock() {
        return switch (this) {
            case SORCERER_WATCHTOWER, MAGE_TOWER -> Blocks.POLISHED_DEEPSLATE;
            case HERO_OUTPOST, ELECTION_HALL_RUIN -> Blocks.STONE_BRICKS;
            case BANDIT_CAMP, WANDERING_MERCHANT, BATTLEFIELD -> StructureBlocks.CAMP_GROUND;
            case CURSED_SHRINE -> Blocks.POLISHED_BLACKSTONE;
            case ABANDONED_MANOR -> Blocks.SPRUCE_PLANKS;
            case TRADING_POST -> Blocks.STRIPPED_SPRUCE_LOG;
            case OBELISK -> StructureBlocks.OBELISK_STONE;
        };
    }

    /** Accent / lighting block. */
    public Block accentBlock() {
        return switch (this) {
            case SORCERER_WATCHTOWER, MAGE_TOWER, OBELISK -> Blocks.AMETHYST_BLOCK;
            case HERO_OUTPOST -> StructureBlocks.WAR_BANNER;
            case BANDIT_CAMP -> Blocks.CAMPFIRE;
            case CURSED_SHRINE, BATTLEFIELD -> Blocks.SOUL_LANTERN;
            case ABANDONED_MANOR -> Blocks.COBWEB;
            case TRADING_POST, WANDERING_MERCHANT -> Blocks.BARREL;
            case ELECTION_HALL_RUIN -> Blocks.LANTERN;
        };
    }

    public static StructureType byId(String id) {
        if (id == null) return null;
        String key = id.toLowerCase(Locale.ROOT);
        for (StructureType t : values()) if (t.id.equals(key)) return t;
        return null;
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(t -> t.id).toList();
    }

    public static StructureType roll(Random rng) {
        StructureType[] all = values();
        return all[rng.nextInt(all.length)];
    }
}
