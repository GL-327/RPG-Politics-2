package com.political.world.structures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * Resolves vanilla blocks by id. MC 26.2 moved many colored blocks off {@code Blocks};
 * registry lookup keeps structure builders stable across registry refactors.
 */
public final class VanillaBlocks {

    public static final Block RED_WOOL = block("red_wool");
    public static final Block WHITE_WOOL = block("white_wool");
    public static final Block BROWN_WOOL = block("brown_wool");
    public static final Block GRAY_WOOL = block("gray_wool");
    public static final Block LIGHT_BLUE_WOOL = block("light_blue_wool");
    public static final Block RED_BED = block("red_bed");
    public static final Block IRON_CHAIN = block("iron_chain");

    private VanillaBlocks() {}

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft", path));
    }
}
