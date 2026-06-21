package com.political.client.compat.jei.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A cursed-energy / relic crafting display recipe: combine up to four
 * {@code inputs} to produce {@code result}. JEI display only.
 *
 * @param inputs up to four ingredient stacks (laid out in a 2×2 grid)
 * @param result the crafted relic / cursed item
 * @param title  short category header line for this entry
 * @param notes  descriptive lines shown as the result slot's tooltip
 */
public record CursedRelicRecipe(List<ItemStack> inputs, ItemStack result, Component title, List<Component> notes) {}
