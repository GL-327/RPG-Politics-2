package com.political.world.structures;

import com.political.RpgPoliticsMod;

/**
 * Registers the above-ground structure system into the mod lifecycle: themed blocks/items and
 * the scatter manager. Call {@link #register()} from the common initializer (after
 * {@code ModBlocks.register()}), wire {@link StructureManager#tick} into the server tick loop,
 * and register {@link StructureCommands} in the command callback.
 */
public final class StructureRegistry {

    private StructureRegistry() {}

    public static void register() {
        StructureBlocks.register();
        StructureManager.register();
        RpgPoliticsMod.LOGGER.info("Surface structure system registered ({} types).",
                StructureType.values().length);
    }
}
