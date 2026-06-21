package com.political.client.compat.jei.category;

import com.political.RpgPoliticsMod;
import com.political.client.compat.jei.recipe.EconomyConversionRecipe;
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

/**
 * JEI category showing bank/economy conversions (coin tiers, deposits, exchange
 * rates). Real category implemented against the stable JEI API.
 */
public class EconomyConversionCategory extends AbstractRecipeCategory<EconomyConversionRecipe> {

    public static final RecipeType<EconomyConversionRecipe> TYPE = new RecipeType<>(
            Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, "economy_conversion"),
            EconomyConversionRecipe.class);

    public EconomyConversionCategory(IGuiHelper gui, ItemStack icon) {
        super(TYPE,
                Component.translatable("jei.politicalserver.category.economy_conversion"),
                gui.createDrawableItemStack(icon),
                132, 56);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EconomyConversionRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 26).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 26).addItemStack(recipe.output())
                .addRichTooltipCallback((view, tip) -> recipe.notes().forEach(tip::add));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, EconomyConversionRecipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(64, 26);
        builder.addText(recipe.title(), 124, 18).setPosition(4, 4);
    }
}
