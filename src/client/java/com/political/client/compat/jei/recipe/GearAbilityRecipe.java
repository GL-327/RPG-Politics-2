package com.political.client.compat.jei.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A "gear ability" display recipe: a piece of gear/relic ({@code gear}) that, when
 * equipped or consumed, yields an ability/output ({@code result}). Purely a JEI
 * display model — it carries no crafting logic.
 *
 * @param gear   the input gear / serum / relic item shown on the left
 * @param result the granted ability token / upgraded item shown on the right
 * @param title  short category header line for this entry
 * @param notes  descriptive lines shown as the result slot's tooltip
 */
public record GearAbilityRecipe(ItemStack gear, ItemStack result, Component title, List<Component> notes) {}
