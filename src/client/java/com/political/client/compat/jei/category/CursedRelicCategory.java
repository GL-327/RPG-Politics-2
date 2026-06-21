package com.political.client.compat.jei.category;

import com.political.RpgPoliticsMod;
import com.political.client.compat.jei.recipe.CursedRelicRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI category showing cursed-energy / relic crafting: up to four ingredients
 * (2×2 grid) combine into a relic. Real category against the stable JEI API.
 */
public class CursedRelicCategory extends AbstractRecipeCategory<CursedRelicRecipe> {

    public static final RecipeType<CursedRelicRecipe> TYPE = new RecipeType<>(
            Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, "cursed_relic"),
            CursedRelicRecipe.class);

    private static final int[][] GRID = {{8, 8}, {30, 8}, {8, 30}, {30, 30}};

    public CursedRelicCategory(IGuiHelper gui, ItemStack icon) {
        super(TYPE,
                Component.translatable("jei.politicalserver.category.cursed_relic"),
                gui.createDrawableItemStack(icon),
                132, 60);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CursedRelicRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = recipe.inputs();
        for (int i = 0; i < inputs.size() && i < GRID.length; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, GRID[i][0], GRID[i][1]).addItemStack(inputs.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 21).addItemStack(recipe.result())
                .addRichTooltipCallback((view, tip) -> recipe.notes().forEach(tip::add));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, CursedRelicRecipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(66, 21);
        builder.addText(recipe.title(), 124, 16).setPosition(4, 50);
    }
}
