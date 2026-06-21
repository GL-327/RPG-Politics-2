package com.political.client.compat.jei.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A bank/economy conversion display recipe: convert {@code input} into
 * {@code output} (e.g. coin tiers, deposits, exchange rates). JEI display only.
 *
 * @param input  the source currency/item stack (count meaningful)
 * @param output the resulting currency/item stack (count meaningful)
 * @param title  short category header line for this entry
 * @param notes  descriptive lines shown as the output slot's tooltip
 */
public record EconomyConversionRecipe(ItemStack input, ItemStack output, Component title, List<Component> notes) {}
