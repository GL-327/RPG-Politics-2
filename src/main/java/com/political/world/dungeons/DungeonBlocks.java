package com.political.world.dungeons;

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

/** Dungeon-themed building palette for procedural structure generators. */
public final class DungeonBlocks {

    public static final String MOD_ID = "politicalserver";

    public static Block CRACKED_STONE;
    public static Block CURSED_BRICK;
    public static Block SOUL_LANTERN;
    public static Block BOSS_ALTAR;
    public static Block MOSSY_CRYPT;
    public static Block OBSIDIAN_BRICK;
    public static Block CRYSTAL_TILE;
    public static Block FLOODED_MOSAIC;

    public static final Map<String, Block> ALL = new LinkedHashMap<>();

    private DungeonBlocks() {}

    public static void register() {
        CRACKED_STONE   = reg("dungeon_cracked_stone", props(MapColor.STONE, 2.0f, 6.0f, 0));
        CURSED_BRICK    = reg("dungeon_cursed_brick", props(MapColor.COLOR_PURPLE, 2.5f, 8.0f, 0));
        SOUL_LANTERN    = reg("dungeon_soul_lantern", props(MapColor.COLOR_CYAN, 1.5f, 5.0f, 12));
        BOSS_ALTAR      = reg("dungeon_boss_altar", props(MapColor.COLOR_PURPLE, 3.0f, 10.0f, 8));
        MOSSY_CRYPT     = reg("dungeon_mossy_crypt", props(MapColor.COLOR_GREEN, 2.0f, 6.0f, 0));
        OBSIDIAN_BRICK  = reg("dungeon_obsidian_brick", props(MapColor.COLOR_BLACK, 3.0f, 10.0f, 0));
        CRYSTAL_TILE    = reg("dungeon_crystal_tile", props(MapColor.COLOR_MAGENTA, 1.5f, 4.0f, 6));
        FLOODED_MOSAIC  = reg("dungeon_flooded_mosaic", props(MapColor.WATER, 2.0f, 6.0f, 0));
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
