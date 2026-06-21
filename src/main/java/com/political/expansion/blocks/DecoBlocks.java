package com.political.expansion.blocks;

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
 * Expansion building &amp; decoration palette ("neo-medieval modern", expanded).
 *
 * <p>A large set of decorative full-cube blocks — bricks, tiles, marble/granite
 * variants, stained facades, pillars/columns, light-emitting lamps &amp; lanterns,
 * banners, roofing and ornate stone — used to flesh out settlement building.
 *
 * <p>Mirrors {@code com.political.content.ModBlocks} exactly: every block is a
 * plain {@link Block} full cube (so no client-side render-layer setup is needed;
 * the look comes entirely from the textures). All registry ids are prefixed
 * {@code dec_} to avoid clashing with the core palette.
 *
 * <p>Integration: call {@link #register()} during mod init, and feed
 * {@link #blocks()} into the "RPG — Settlements &amp; Build" creative tab.
 */
public final class DecoBlocks {

    public static final String MOD_ID = "politicalserver";

    /** Ordered registry of every block we register, for creative-tab population. */
    public static final Map<String, Block> ALL = new LinkedHashMap<>();

    private DecoBlocks() {}

    public static void register() {
        // ---- Decorative bricks ----
        reg("dec_red_brick",          props(MapColor.TERRACOTTA_RED, 2.0f, 6.0f, 0));
        reg("dec_blonde_brick",       props(MapColor.TERRACOTTA_YELLOW, 2.0f, 6.0f, 0));
        reg("dec_charcoal_brick",     props(MapColor.COLOR_BLACK, 2.0f, 6.0f, 0));
        reg("dec_ivory_brick",        props(MapColor.QUARTZ, 2.0f, 6.0f, 0));
        reg("dec_mossy_castle_brick", props(MapColor.COLOR_GREEN, 2.5f, 8.0f, 0));
        reg("dec_herringbone_brick",  props(MapColor.TERRACOTTA_ORANGE, 2.0f, 6.0f, 0));

        // ---- Tiles ----
        reg("dec_checkered_tile",     props(MapColor.QUARTZ, 2.0f, 6.0f, 0));
        reg("dec_terracotta_tile",    props(MapColor.TERRACOTTA_ORANGE, 2.0f, 6.0f, 0));
        reg("dec_azure_tile",         props(MapColor.COLOR_LIGHT_BLUE, 2.0f, 6.0f, 0));
        reg("dec_emerald_tile",       props(MapColor.EMERALD, 2.0f, 6.0f, 0));

        // ---- Marble ----
        reg("dec_white_marble",       props(MapColor.QUARTZ, 2.0f, 7.0f, 0));
        reg("dec_black_marble",       props(MapColor.COLOR_BLACK, 2.0f, 7.0f, 0));
        reg("dec_rose_marble",        props(MapColor.COLOR_PINK, 2.0f, 7.0f, 0));
        reg("dec_cobalt_marble",      props(MapColor.COLOR_BLUE, 2.0f, 7.0f, 0));
        reg("dec_marble_pillar",      props(MapColor.QUARTZ, 2.0f, 7.0f, 0));

        // ---- Granite ----
        reg("dec_grey_granite",       props(MapColor.STONE, 2.5f, 8.0f, 0));
        reg("dec_pink_granite",       props(MapColor.COLOR_PINK, 2.5f, 8.0f, 0));

        // ---- Stained facades (rich colours) ----
        reg("dec_crimson_facade",     props(MapColor.COLOR_RED, 1.5f, 6.0f, 0));
        reg("dec_cobalt_facade",      props(MapColor.COLOR_BLUE, 1.5f, 6.0f, 0));
        reg("dec_jade_facade",        props(MapColor.COLOR_GREEN, 1.5f, 6.0f, 0));
        reg("dec_amber_facade",       props(MapColor.COLOR_ORANGE, 1.5f, 6.0f, 0));
        reg("dec_violet_facade",      props(MapColor.COLOR_PURPLE, 1.5f, 6.0f, 0));

        // ---- Columns / pillars ----
        reg("dec_fluted_column",      props(MapColor.QUARTZ, 2.5f, 8.0f, 0));
        reg("dec_sandstone_column",   props(MapColor.SAND, 2.0f, 6.0f, 0));

        // ---- Lamps & lanterns (light-emitting) ----
        reg("dec_iron_lantern",       props(MapColor.METAL, 1.5f, 5.0f, 14));
        reg("dec_gold_lantern",       props(MapColor.GOLD, 1.5f, 5.0f, 15));
        reg("dec_glowstone_lamp",     props(MapColor.COLOR_YELLOW, 1.0f, 3.0f, 15));
        reg("dec_paper_lantern",      props(MapColor.COLOR_RED, 1.0f, 3.0f, 13));

        // ---- Roofing ----
        reg("dec_slate_shingles",     props(MapColor.DEEPSLATE, 1.5f, 5.0f, 0));
        reg("dec_red_shingles",       props(MapColor.COLOR_RED, 1.5f, 5.0f, 0));
        reg("dec_copper_roof",        props(MapColor.COLOR_ORANGE, 1.5f, 5.0f, 0));
        reg("dec_thatch_roof",        props(MapColor.TERRACOTTA_YELLOW, 1.0f, 3.0f, 0));

        // ---- Ornate stone ----
        reg("dec_ornate_stone",       props(MapColor.STONE, 2.5f, 8.0f, 0));
        reg("dec_carved_stone",       props(MapColor.STONE, 2.5f, 8.0f, 0));
        reg("dec_runic_stone",        props(MapColor.COLOR_CYAN, 2.5f, 8.0f, 8));

        // ---- Plaster & structural building blocks ----
        reg("dec_cream_plaster",      props(MapColor.TERRACOTTA_WHITE, 1.5f, 5.0f, 0));
        reg("dec_timber_frame",       props(MapColor.WOOD, 1.5f, 5.0f, 0));
        reg("dec_sandstone_block",    props(MapColor.SAND, 2.0f, 6.0f, 0));
        reg("dec_polished_basalt",    props(MapColor.COLOR_BLACK, 2.5f, 8.0f, 0));

        // ---- Banners ----
        reg("dec_blue_banner_block",  props(MapColor.COLOR_BLUE, 1.0f, 3.0f, 0));
        reg("dec_green_banner_block", props(MapColor.COLOR_GREEN, 1.0f, 3.0f, 0));
        reg("dec_gold_banner_block",  props(MapColor.COLOR_YELLOW, 1.0f, 3.0f, 0));
    }

    /** Every block registered by this expansion module, in declaration order. */
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
