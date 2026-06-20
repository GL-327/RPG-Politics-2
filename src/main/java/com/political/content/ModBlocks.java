package com.political.content;

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
 * The "neo-medieval modern" building palette: custom blocks (with custom textures)
 * used by the procedural settlement generators. All blocks are plain full cubes to
 * avoid client-side render-layer configuration; the look comes from the textures.
 */
public final class ModBlocks {

    public static final String MOD_ID = "politicalserver";

    public static Block CASTLE_BRICKS;
    public static Block CASTLE_PILLAR;
    public static Block ROYAL_BANNER_BLOCK;
    public static Block TOWN_HALL_BRICKS;
    public static Block PAVED_ROAD;
    public static Block COBBLE_STREET;
    public static Block STREET_LAMP;
    public static Block MODERN_FACADE;
    public static Block MODERN_WINDOW;
    public static Block CIVIC_MARKER;

    /** Ordered registry of every block we register, for creative-tab population. */
    public static final Map<String, Block> ALL = new LinkedHashMap<>();

    private ModBlocks() {}

    public static void register() {
        CASTLE_BRICKS      = reg("castle_bricks", props(MapColor.STONE, 2.5f, 8.0f, 0));
        CASTLE_PILLAR      = reg("castle_pillar", props(MapColor.STONE, 2.5f, 8.0f, 0));
        ROYAL_BANNER_BLOCK = reg("royal_banner_block", props(MapColor.COLOR_RED, 1.0f, 3.0f, 0));
        TOWN_HALL_BRICKS   = reg("town_hall_bricks", props(MapColor.TERRACOTTA_ORANGE, 2.0f, 6.0f, 0));
        PAVED_ROAD         = reg("paved_road", props(MapColor.COLOR_BLACK, 1.5f, 6.0f, 0));
        COBBLE_STREET      = reg("cobble_street", props(MapColor.STONE, 2.0f, 6.0f, 0));
        STREET_LAMP        = reg("street_lamp", props(MapColor.METAL, 1.5f, 5.0f, 15));
        MODERN_FACADE      = reg("modern_facade", props(MapColor.QUARTZ, 1.5f, 6.0f, 0));
        MODERN_WINDOW      = reg("modern_window", props(MapColor.COLOR_LIGHT_BLUE, 1.0f, 3.0f, 4));
        CIVIC_MARKER       = reg("civic_marker", props(MapColor.COLOR_PURPLE, 2.0f, 6.0f, 10));
    }

    public static List<Block> list() {
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
