package com.political.world.dungeons;

import net.minecraft.ChatFormatting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Ten distinct world-generated RPG dungeon archetypes. */
public enum DungeonType {
    CURSED_CRYPT("cursed_crypt", "Cursed Crypt", ChatFormatting.DARK_PURPLE, DungeonTier.UNCOMMON,
            true, true, false,
            "mob_grave_revenant", "mob_wraith", "mob_lich_sovereign",
            "cursed_essence", "exorcism_token", "wpn_grave_scythe"),
    BANDIT_HIDEOUT("bandit_hideout", "Bandit Hideout", ChatFormatting.GOLD, DungeonTier.COMMON,
            false, false, false,
            "mob_bandit_outlaw", "mob_bandit_brute", "mob2_bandit_king",
            "coin_pouch", "treasury_note", "wpn_serpent_whip"),
    ANCIENT_RUINS("ancient_ruins", "Ancient Ruins", ChatFormatting.YELLOW, DungeonTier.RARE,
            false, true, false,
            "mob_bone_legionnaire", "mob_cultist_acolyte", "mob_warlord_kael",
            "reforge_stone", "bounty_seal", "acc_godslayer_relic"),
    VILTRUMITE_LAB("viltrumite_lab", "Viltrumite Lab", ChatFormatting.RED, DungeonTier.RARE,
            true, false, false,
            "mob_ashen_knight", "mob_ironclad_champion", "mob2_grand_marshal",
            "compound_v", "temp_v", "v1"),
    SORCERER_SANCTUM("sorcerer_sanctum", "Sorcerer Sanctum", ChatFormatting.AQUA, DungeonTier.EPIC,
            true, true, false,
            "mob_cultist_acolyte", "mob_storm_herald", "mob2_archmage_sovereign",
            "mana_crystal", "grade_scroll", "arc_arcane_orb"),
    DRAGONS_VAULT("dragons_vault", "Dragon's Vault", ChatFormatting.DARK_RED, DungeonTier.EPIC,
            true, true, false,
            "mob_ember_fiend", "mob_blighted_ogre", "mob2_infernal_sovereign",
            "wpn_dragonbone_greatsword", "wpn_ragnarok_axe", "acc_godslayer_relic"),
    FLOODED_TEMPLE("flooded_temple", "Flooded Temple", ChatFormatting.BLUE, DungeonTier.UNCOMMON,
            false, false, true,
            "mob_frost_revenant", "mob_venom_cultist", "mob2_frost_queen",
            "food_seaweed_salad", "acc_minor_mana_potion", "arc_frost_grimoire"),
    NETHERITE_VAULT("netherite_vault", "Netherite Vault", ChatFormatting.DARK_GRAY, DungeonTier.EPIC,
            true, false, false,
            "mob_ashen_knight", "mob_ironclad_champion", "mob2_pit_commander",
            "wpn_titanbreaker_maul", "arc_thunderbolt", "wpn_godslayer_blade"),
    OVERGROWN_CATACOMBS("overgrown_catacombs", "Overgrown Catacombs", ChatFormatting.DARK_GREEN, DungeonTier.UNCOMMON,
            true, true, false,
            "mob_plague_bearer", "mob_grave_revenant", "mob2_dread_lich",
            "cursed_essence", "wpn_soulreaper_scythe", "acc_warding_talisman"),
    CRYSTAL_CAVERNS("crystal_caverns", "Crystal Caverns", ChatFormatting.LIGHT_PURPLE, DungeonTier.RARE,
            true, false, false,
            "mob_wraith", "mob_storm_herald", "mob2_arcane_titan",
            "acc_mana_ring", "acc_arcane_focus", "acc_celestial_charm");

    public final String id;
    public final String display;
    public final ChatFormatting color;
    public final DungeonTier tier;
    public final boolean underground;
    public final boolean cursed;
    public final boolean flooded;
    public final String mobCommon;
    public final String mobElite;
    public final String mobBoss;
    public final String lootA;
    public final String lootB;
    public final String lootC;

    DungeonType(String id, String display, ChatFormatting color, DungeonTier tier,
                boolean underground, boolean cursed, boolean flooded,
                String mobCommon, String mobElite, String mobBoss,
                String lootA, String lootB, String lootC) {
        this.id = id;
        this.display = display;
        this.color = color;
        this.tier = tier;
        this.underground = underground;
        this.cursed = cursed;
        this.flooded = flooded;
        this.mobCommon = mobCommon;
        this.mobElite = mobElite;
        this.mobBoss = mobBoss;
        this.lootA = lootA;
        this.lootB = lootB;
        this.lootC = lootC;
    }

    public Block wallBlock() {
        return switch (this) {
            case CURSED_CRYPT, OVERGROWN_CATACOMBS -> DungeonBlocks.CURSED_BRICK;
            case BANDIT_HIDEOUT -> Blocks.COBBLESTONE;
            case ANCIENT_RUINS -> DungeonBlocks.CRACKED_STONE;
            case VILTRUMITE_LAB -> DungeonBlocks.OBSIDIAN_BRICK;
            case SORCERER_SANCTUM -> Blocks.DEEPSLATE_BRICKS;
            case DRAGONS_VAULT -> Blocks.POLISHED_BLACKSTONE_BRICKS;
            case FLOODED_TEMPLE -> DungeonBlocks.FLOODED_MOSAIC;
            case NETHERITE_VAULT -> Blocks.NETHER_BRICKS;
            case CRYSTAL_CAVERNS -> DungeonBlocks.CRYSTAL_TILE;
        };
    }

    public Block floorBlock() {
        return switch (this) {
            case CURSED_CRYPT -> DungeonBlocks.MOSSY_CRYPT;
            case BANDIT_HIDEOUT -> Blocks.MOSSY_COBBLESTONE;
            case ANCIENT_RUINS -> DungeonBlocks.CRACKED_STONE;
            case VILTRUMITE_LAB -> Blocks.IRON_BLOCK;
            case SORCERER_SANCTUM -> Blocks.POLISHED_DEEPSLATE;
            case DRAGONS_VAULT -> Blocks.GILDED_BLACKSTONE;
            case FLOODED_TEMPLE -> DungeonBlocks.FLOODED_MOSAIC;
            case NETHERITE_VAULT -> Blocks.NETHERITE_BLOCK;
            case OVERGROWN_CATACOMBS -> DungeonBlocks.MOSSY_CRYPT;
            case CRYSTAL_CAVERNS -> DungeonBlocks.CRYSTAL_TILE;
        };
    }

    public Block accentBlock() {
        return switch (this) {
            case CURSED_CRYPT, OVERGROWN_CATACOMBS -> DungeonBlocks.SOUL_LANTERN;
            case BANDIT_HIDEOUT -> Blocks.OAK_PLANKS;
            case ANCIENT_RUINS -> Blocks.CHISELED_STONE_BRICKS;
            case VILTRUMITE_LAB -> Blocks.REDSTONE_BLOCK;
            case SORCERER_SANCTUM -> Blocks.AMETHYST_BLOCK;
            case DRAGONS_VAULT -> DungeonBlocks.BOSS_ALTAR;
            case FLOODED_TEMPLE -> Blocks.PRISMARINE_BRICKS;
            case NETHERITE_VAULT -> Blocks.ANCIENT_DEBRIS;
            case CRYSTAL_CAVERNS -> Blocks.GLOWSTONE;
        };
    }

    public static DungeonType byId(String id) {
        if (id == null) return null;
        String key = id.toLowerCase(Locale.ROOT);
        for (DungeonType t : values()) if (t.id.equals(key)) return t;
        return null;
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(t -> t.id).toList();
    }

    public static DungeonType roll(Random rng) {
        DungeonType[] all = values();
        return all[rng.nextInt(all.length)];
    }
}
