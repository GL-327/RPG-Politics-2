package com.political.world.structures;

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
 * Themed building palette for the above-ground RPG worldgen structures. Mirrors the
 * mixin-free registration pattern of {@code ModBlocks} / {@code DungeonBlocks}: every block
 * is a plain full cube (no custom render layers) so it generates without client config, with
 * its look coming from a vanilla-backed model. Kept entirely inside the structures package so
 * the surface-site system owns its own materials.
 */
public final class StructureBlocks {

    public static final String MOD_ID = "politicalserver";

    /** Carved arcane masonry used by Sorcerer Watchtowers and Mage Towers. */
    public static Block RUNED_STONE;
    /** Weathered marble for noble manors and ruined civic halls. */
    public static Block MOSSY_MARBLE;
    /** Dark offering altar at Cursed Shrines (faintly glowing). */
    public static Block CURSED_ALTAR;
    /** Monolithic stone for Obelisks and battlefield monuments. */
    public static Block OBELISK_STONE;
    /** Faction war-banner block marking outposts, camps and shrines (glowing). */
    public static Block WAR_BANNER;
    /** Trodden camp ground for bandit camps and merchant stops. */
    public static Block CAMP_GROUND;

    /** Ordered registry of every block, for creative-tab population / integration. */
    public static final Map<String, Block> ALL = new LinkedHashMap<>();

    private StructureBlocks() {}

    public static void register() {
        RUNED_STONE   = reg("structure_runed_stone", props(MapColor.DEEPSLATE, 2.5f, 8.0f, 3));
        MOSSY_MARBLE  = reg("structure_mossy_marble", props(MapColor.QUARTZ, 2.0f, 6.0f, 0));
        CURSED_ALTAR  = reg("structure_cursed_altar", props(MapColor.COLOR_BLACK, 3.0f, 10.0f, 7));
        OBELISK_STONE = reg("structure_obelisk_stone", props(MapColor.COLOR_GRAY, 3.0f, 10.0f, 0));
        WAR_BANNER    = reg("structure_war_banner", props(MapColor.COLOR_RED, 1.0f, 3.0f, 8));
        CAMP_GROUND   = reg("structure_camp_ground", props(MapColor.DIRT, 1.5f, 4.0f, 0));
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
