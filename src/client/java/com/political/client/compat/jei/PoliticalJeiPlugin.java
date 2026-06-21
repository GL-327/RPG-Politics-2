package com.political.client.compat.jei;

import com.political.RpgPoliticsMod;
import com.political.client.compat.jei.category.CursedRelicCategory;
import com.political.client.compat.jei.category.EconomyConversionCategory;
import com.political.client.compat.jei.category.GearAbilityCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

/**
 * JEI integration entry point for RPG Politics 2.
 *
 * <p>This class is compiled against the (very stable) JEI API only
 * ({@code compileOnly}); the full JEI jar is present only at dev runtime. JEI
 * discovers this plugin by scanning for the {@link JeiPlugin} annotation, so the
 * class is never loaded when JEI is absent — making the whole integration a safe
 * no-op without JEI installed. No hard dependency is declared in fabric.mod.json.
 *
 * <p>Besides the automatic item listing, this plugin contributes three real custom
 * recipe categories for RPG&nbsp;Politics&nbsp;2 systems:</p>
 * <ul>
 *   <li>{@link GearAbilityCategory} — gear/serum/relic &rarr; granted ability.</li>
 *   <li>{@link EconomyConversionCategory} — bank/economy coin &amp; note conversions.</li>
 *   <li>{@link CursedRelicCategory} — cursed-energy / relic crafting (2×2 ingredients).</li>
 * </ul>
 *
 * <p>The display rows are assembled in {@link PoliticalJeiRecipes}, which resolves
 * items defensively by registry id (vanilla fallback), so this integration never
 * hard-couples to another workstream's item classes.</p>
 */
@JeiPlugin
public class PoliticalJeiPlugin implements IModPlugin {

    public static final Identifier PLUGIN_UID =
            Identifier.fromNamespaceAndPath(RpgPoliticsMod.MOD_ID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new GearAbilityCategory(gui, PoliticalJeiRecipes.gearAbilityIcon()),
                new EconomyConversionCategory(gui, PoliticalJeiRecipes.economyIcon()),
                new CursedRelicCategory(gui, PoliticalJeiRecipes.cursedRelicIcon()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(GearAbilityCategory.TYPE, PoliticalJeiRecipes.gearAbilities());
        registration.addRecipes(EconomyConversionCategory.TYPE, PoliticalJeiRecipes.economyConversions());
        registration.addRecipes(CursedRelicCategory.TYPE, PoliticalJeiRecipes.cursedRelics());
    }
}
