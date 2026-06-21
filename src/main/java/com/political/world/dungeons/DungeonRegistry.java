package com.political.world.dungeons;

import com.political.RpgPoliticsMod;

/** Registers dungeon blocks, items, and hooks into the mod lifecycle. */
public final class DungeonRegistry {

    private DungeonRegistry() {}

    public static void register() {
        DungeonBlocks.register();
        StructureCompassItem.register();
        DungeonManager.register();
        RpgPoliticsMod.LOGGER.info("Dungeon system registered ({} types).", DungeonType.values().length);
    }
}
