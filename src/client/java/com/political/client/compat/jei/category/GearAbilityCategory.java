package com.political.client.compat.jei.category;

import com.political.RpgPoliticsMod;
import com.political.client.compat.jei.recipe.GearAbilityRecipe;
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
 * JEI category showing how a piece of gear / serum / relic grants an ability.
 * Real category implemented against the stable JEI API
 * ({@link AbstractRecipeCategory}); the host plugin is a no-op without JEI present.
 */
public class GearAbilityCategory extends AbstractRecipeCategory<GearAbilityRecipe> {

    public static final RecipeType<GearAbilityRecipe> TYPE = new RecipeType<>(
            Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, "gear_ability"),
            GearAbilityRecipe.class);

    public GearAbilityCategory(IGuiHelper gui, ItemStack icon) {
        super(TYPE,
                Component.translatable("jei.politicalserver.category.gear_ability"),
                gui.createDrawableItemStack(icon),
                132, 56);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GearAbilityRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 26).addItemStack(recipe.gear());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 26).addItemStack(recipe.result())
                .addRichTooltipCallback((view, tip) -> recipe.notes().forEach(tip::add));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, GearAbilityRecipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(64, 26);
        builder.addText(recipe.title(), 124, 18).setPosition(4, 4);
    }
}
