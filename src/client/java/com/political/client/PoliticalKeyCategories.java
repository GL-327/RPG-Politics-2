package com.political.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Dedicated keybind category so all RPG Politics controls group together in Controls. */
public final class PoliticalKeyCategories {

    public static final KeyMapping.Category RPG =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("politicalserver", "rpg"));

    private PoliticalKeyCategories() {}
}
