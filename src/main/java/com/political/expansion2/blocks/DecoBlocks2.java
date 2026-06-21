package com.political.expansion2.blocks;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Expansion 2 building &amp; decoration palette — 120 decorative full-cube blocks.
 *
 * <p>Categories: bricks, wood trims, metals, gems/glass, roofing, paths, banners,
 * ornate stone, modern facades, and light sources. All ids are prefixed {@code dec2_}.
 *
 * <p>Integration: call {@link #register()} during mod init, and feed {@link #blocks()}
 * into the "RPG — Settlements &amp; Build" creative tab.
 */
public final class DecoBlocks2 {

    public static final String MOD_ID = "politicalserver";

    public static final Map<String, Block> ALL = new LinkedHashMap<>();

    private record Spec(String id, MapColor color, float hard, float resist, int light) {}

    private static final Spec[] BLOCKS = {
        // ---- 20 brick variants ----
        spec("dec2_rust_brick", MapColor.TERRACOTTA_RED, 2.0f, 6.0f, 0),
        spec("dec2_sage_brick", MapColor.COLOR_GREEN, 2.0f, 6.0f, 0),
        spec("dec2_navy_brick", MapColor.COLOR_BLUE, 2.0f, 6.0f, 0),
        spec("dec2_wine_brick", MapColor.TERRACOTTA_RED, 2.0f, 6.0f, 0),
        spec("dec2_pearl_brick", MapColor.QUARTZ, 2.0f, 6.0f, 0),
        spec("dec2_ash_brick", MapColor.STONE, 2.0f, 6.0f, 0),
        spec("dec2_sunset_brick", MapColor.TERRACOTTA_ORANGE, 2.0f, 6.0f, 0),
        spec("dec2_frost_brick", MapColor.ICE, 2.0f, 6.0f, 0),
        spec("dec2_olive_brick", MapColor.TERRACOTTA_GREEN, 2.0f, 6.0f, 0),
        spec("dec2_plum_brick", MapColor.COLOR_PURPLE, 2.0f, 6.0f, 0),
        spec("dec2_coral_brick", MapColor.COLOR_PINK, 2.0f, 6.0f, 0),
        spec("dec2_midnight_brick", MapColor.COLOR_BLACK, 2.0f, 6.0f, 0),
        spec("dec2_honey_brick", MapColor.COLOR_YELLOW, 2.0f, 6.0f, 0),
        spec("dec2_storm_brick", MapColor.WARPED_STEM, 2.0f, 6.0f, 0),
        spec("dec2_dust_brick", MapColor.SAND, 2.0f, 6.0f, 0),
        spec("dec2_mint_brick", MapColor.COLOR_LIGHT_GREEN, 2.0f, 6.0f, 0),
        spec("dec2_lavender_brick", MapColor.COLOR_PURPLE, 2.0f, 6.0f, 0),
        spec("dec2_burnt_brick", MapColor.NETHER, 2.0f, 6.0f, 0),
        spec("dec2_weathered_brick", MapColor.COLOR_GREEN, 2.5f, 8.0f, 0),
        spec("dec2_glazed_brick", MapColor.TERRACOTTA_WHITE, 2.0f, 6.0f, 0),

        // ---- 15 wood trims ----
        spec("dec2_oak_trim", MapColor.WOOD, 1.5f, 5.0f, 0),
        spec("dec2_spruce_trim", MapColor.PODZOL, 1.5f, 5.0f, 0),
        spec("dec2_birch_trim", MapColor.QUARTZ, 1.5f, 5.0f, 0),
        spec("dec2_dark_oak_trim", MapColor.DIRT, 1.5f, 5.0f, 0),
        spec("dec2_acacia_trim", MapColor.COLOR_ORANGE, 1.5f, 5.0f, 0),
        spec("dec2_cherry_trim", MapColor.TERRACOTTA_PINK, 1.5f, 5.0f, 0),
        spec("dec2_mangrove_trim", MapColor.NETHER, 1.5f, 5.0f, 0),
        spec("dec2_walnut_trim", MapColor.COLOR_BROWN, 1.5f, 5.0f, 0),
        spec("dec2_teak_trim", MapColor.TERRACOTTA_YELLOW, 1.5f, 5.0f, 0),
        spec("dec2_cedar_trim", MapColor.TERRACOTTA_RED, 1.5f, 5.0f, 0),
        spec("dec2_pine_trim", MapColor.WOOD, 1.5f, 5.0f, 0),
        spec("dec2_mahogany_trim", MapColor.COLOR_RED, 1.5f, 5.0f, 0),
        spec("dec2_bamboo_trim", MapColor.COLOR_YELLOW, 1.5f, 5.0f, 0),
        spec("dec2_ebony_trim", MapColor.COLOR_BLACK, 1.5f, 5.0f, 0),
        spec("dec2_maple_trim", MapColor.TERRACOTTA_ORANGE, 1.5f, 5.0f, 0),

        // ---- 15 metals ----
        spec("dec2_steel_plate", MapColor.METAL, 3.0f, 9.0f, 0),
        spec("dec2_bronze_plate", MapColor.TERRACOTTA_ORANGE, 3.0f, 9.0f, 0),
        spec("dec2_tin_plate", MapColor.GLOW_LICHEN, 3.0f, 9.0f, 0),
        spec("dec2_lead_plate", MapColor.STONE, 3.0f, 9.0f, 0),
        spec("dec2_silver_plate", MapColor.QUARTZ, 3.0f, 9.0f, 0),
        spec("dec2_platinum_plate", MapColor.QUARTZ, 3.0f, 9.0f, 0),
        spec("dec2_nickel_plate", MapColor.GLOW_LICHEN, 3.0f, 9.0f, 0),
        spec("dec2_brass_plate", MapColor.GOLD, 3.0f, 9.0f, 0),
        spec("dec2_pewter_plate", MapColor.STONE, 3.0f, 9.0f, 0),
        spec("dec2_rusty_iron", MapColor.TERRACOTTA_ORANGE, 2.5f, 8.0f, 0),
        spec("dec2_oxidized_copper", MapColor.WARPED_STEM, 2.5f, 8.0f, 0),
        spec("dec2_titanium_plate", MapColor.QUARTZ, 3.5f, 10.0f, 0),
        spec("dec2_chrome_plate", MapColor.QUARTZ, 3.0f, 9.0f, 0),
        spec("dec2_iron_weave", MapColor.METAL, 3.0f, 9.0f, 0),
        spec("dec2_gold_plate", MapColor.GOLD, 3.0f, 9.0f, 0),

        // ---- 15 gems / glass ----
        spec("dec2_ruby_glass", MapColor.COLOR_RED, 1.5f, 4.0f, 5),
        spec("dec2_sapphire_glass", MapColor.COLOR_BLUE, 1.5f, 4.0f, 4),
        spec("dec2_emerald_glass", MapColor.EMERALD, 1.5f, 4.0f, 5),
        spec("dec2_amethyst_glass", MapColor.COLOR_PURPLE, 1.5f, 4.0f, 4),
        spec("dec2_topaz_glass", MapColor.COLOR_YELLOW, 1.5f, 4.0f, 4),
        spec("dec2_opal_glass", MapColor.QUARTZ, 1.5f, 4.0f, 6),
        spec("dec2_onyx_glass", MapColor.COLOR_BLACK, 1.5f, 4.0f, 0),
        spec("dec2_aquamarine_glass", MapColor.COLOR_LIGHT_BLUE, 1.5f, 4.0f, 4),
        spec("dec2_garnet_glass", MapColor.TERRACOTTA_RED, 1.5f, 4.0f, 4),
        spec("dec2_citrine_glass", MapColor.COLOR_ORANGE, 1.5f, 4.0f, 5),
        spec("dec2_peridot_glass", MapColor.COLOR_LIGHT_GREEN, 1.5f, 4.0f, 4),
        spec("dec2_tourmaline_glass", MapColor.COLOR_PINK, 1.5f, 4.0f, 4),
        spec("dec2_frosted_glass", MapColor.QUARTZ, 1.5f, 4.0f, 0),
        spec("dec2_cobalt_glass", MapColor.COLOR_BLUE, 1.5f, 4.0f, 4),
        spec("dec2_prism_glass", MapColor.DIAMOND, 1.5f, 4.0f, 10),

        // ---- 15 roof types ----
        spec("dec2_charcoal_shingles", MapColor.COLOR_BLACK, 1.5f, 5.0f, 0),
        spec("dec2_forest_shingles", MapColor.COLOR_GREEN, 1.5f, 5.0f, 0),
        spec("dec2_ocean_shingles", MapColor.COLOR_BLUE, 1.5f, 5.0f, 0),
        spec("dec2_amber_shingles", MapColor.COLOR_ORANGE, 1.5f, 5.0f, 0),
        spec("dec2_violet_shingles", MapColor.COLOR_PURPLE, 1.5f, 5.0f, 0),
        spec("dec2_zinc_roof", MapColor.GLOW_LICHEN, 1.5f, 5.0f, 0),
        spec("dec2_iron_roof", MapColor.METAL, 1.5f, 5.0f, 0),
        spec("dec2_terracotta_roof", MapColor.TERRACOTTA_ORANGE, 1.5f, 5.0f, 0),
        spec("dec2_cedar_shakes", MapColor.WOOD, 1.0f, 3.0f, 0),
        spec("dec2_palm_thatch", MapColor.TERRACOTTA_YELLOW, 1.0f, 3.0f, 0),
        spec("dec2_clay_roof", MapColor.TERRACOTTA_RED, 1.5f, 5.0f, 0),
        spec("dec2_snow_roof", MapColor.SNOW, 1.0f, 3.0f, 0),
        spec("dec2_moss_roof", MapColor.COLOR_GREEN, 1.0f, 3.0f, 0),
        spec("dec2_obsidian_roof", MapColor.COLOR_BLACK, 2.0f, 6.0f, 0),
        spec("dec2_golden_roof", MapColor.GOLD, 1.5f, 5.0f, 0),

        // ---- 10 paths ----
        spec("dec2_cobble_path", MapColor.STONE, 2.0f, 6.0f, 0),
        spec("dec2_brick_path", MapColor.TERRACOTTA_RED, 2.0f, 6.0f, 0),
        spec("dec2_gravel_path", MapColor.DIRT, 1.5f, 5.0f, 0),
        spec("dec2_slate_path", MapColor.DEEPSLATE, 2.0f, 6.0f, 0),
        spec("dec2_sandstone_path", MapColor.SAND, 2.0f, 6.0f, 0),
        spec("dec2_mossy_path", MapColor.COLOR_GREEN, 2.0f, 6.0f, 0),
        spec("dec2_cracked_path", MapColor.STONE, 2.0f, 6.0f, 0),
        spec("dec2_hex_path", MapColor.QUARTZ, 2.0f, 6.0f, 0),
        spec("dec2_flagstone_path", MapColor.STONE, 2.0f, 6.0f, 0),
        spec("dec2_marble_path", MapColor.QUARTZ, 2.0f, 6.0f, 0),

        // ---- 10 banners / flags ----
        spec("dec2_crimson_banner", MapColor.COLOR_RED, 1.0f, 3.0f, 0),
        spec("dec2_azure_banner", MapColor.COLOR_BLUE, 1.0f, 3.0f, 0),
        spec("dec2_emerald_banner", MapColor.EMERALD, 1.0f, 3.0f, 0),
        spec("dec2_gold_banner", MapColor.GOLD, 1.0f, 3.0f, 0),
        spec("dec2_violet_banner", MapColor.COLOR_PURPLE, 1.0f, 3.0f, 0),
        spec("dec2_snow_banner", MapColor.SNOW, 1.0f, 3.0f, 0),
        spec("dec2_onyx_banner", MapColor.COLOR_BLACK, 1.0f, 3.0f, 0),
        spec("dec2_copper_banner", MapColor.TERRACOTTA_ORANGE, 1.0f, 3.0f, 0),
        spec("dec2_silver_banner", MapColor.QUARTZ, 1.0f, 3.0f, 0),
        spec("dec2_royal_banner", MapColor.COLOR_PURPLE, 1.0f, 3.0f, 0),

        // ---- 10 ornate (includes light sources) ----
        spec("dec2_ornate_limestone", MapColor.SAND, 2.5f, 8.0f, 0),
        spec("dec2_ornate_sandstone", MapColor.SAND, 2.5f, 8.0f, 0),
        spec("dec2_ornate_marble", MapColor.QUARTZ, 2.5f, 8.0f, 0),
        spec("dec2_ornate_obsidian", MapColor.COLOR_BLACK, 2.5f, 8.0f, 0),
        spec("dec2_carved_limestone", MapColor.SAND, 2.5f, 8.0f, 0),
        spec("dec2_carved_sandstone", MapColor.SAND, 2.5f, 8.0f, 0),
        spec("dec2_brazier", MapColor.NETHER, 2.5f, 8.0f, 14),
        spec("dec2_wall_sconce", MapColor.GOLD, 2.5f, 8.0f, 13),
        spec("dec2_crystal_lamp", MapColor.DIAMOND, 2.5f, 8.0f, 15),
        spec("dec2_oil_lantern", MapColor.TERRACOTTA_ORANGE, 2.5f, 8.0f, 12),

        // ---- 10 modern facade ----
        spec("dec2_concrete_white", MapColor.QUARTZ, 2.0f, 6.0f, 0),
        spec("dec2_concrete_grey", MapColor.STONE, 2.0f, 6.0f, 0),
        spec("dec2_concrete_charcoal", MapColor.COLOR_BLACK, 2.0f, 6.0f, 0),
        spec("dec2_panel_blue", MapColor.COLOR_BLUE, 1.5f, 5.0f, 0),
        spec("dec2_panel_red", MapColor.COLOR_RED, 1.5f, 5.0f, 0),
        spec("dec2_panel_green", MapColor.COLOR_GREEN, 1.5f, 5.0f, 0),
        spec("dec2_panel_yellow", MapColor.COLOR_YELLOW, 1.5f, 5.0f, 0),
        spec("dec2_glass_wall", MapColor.QUARTZ, 1.5f, 4.0f, 0),
        spec("dec2_metal_cladding", MapColor.METAL, 2.0f, 6.0f, 0),
        spec("dec2_neon_panel", MapColor.COLOR_CYAN, 1.5f, 5.0f, 12),
    };

    private DecoBlocks2() {}

    private static Spec spec(String id, MapColor color, float hard, float resist, int light) {
        return new Spec(id, color, hard, resist, light);
    }

    public static void register() {
        for (Spec s : BLOCKS) {
            reg(s.id(), props(s.color(), s.hard(), s.resist(), s.light()));
        }
    }

    public static List<Block> blocks() {
        return new ArrayList<>(ALL.values());
    }

    private static BlockBehaviour.Properties props(MapColor color, float hardness, float resist, int light) {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resist);
        if (light > 0) {
            final int l = light;
            p = p.lightLevel(state -> l);
        }
        return p;
    }

    private static Block reg(String name, BlockBehaviour.Properties props) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new Block(props.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        ALL.put(name, block);
        return block;
    }
}
