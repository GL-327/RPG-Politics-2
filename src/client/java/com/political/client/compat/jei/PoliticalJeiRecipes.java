package com.political.client.compat.jei;

import com.political.client.compat.jei.recipe.CursedRelicRecipe;
import com.political.client.compat.jei.recipe.EconomyConversionRecipe;
import com.political.client.compat.jei.recipe.GearAbilityRecipe;
import com.political.items.RelicItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Builds the JEI display recipe lists for RPG&nbsp;Politics&nbsp;2's custom systems.
 *
 * <p>Items are resolved by registry id with a vanilla fallback ({@link #mod} /
 * {@link #mc}), so this class never hard-couples to another workstream's item
 * classes and never throws if an id is absent — it just shows a sensible stand-in.
 */
public final class PoliticalJeiRecipes {

    public static final String MOD_ID = "politicalserver";

    private PoliticalJeiRecipes() {}

    private static Item mod(String path) {
        return BuiltInRegistries.ITEM
                .getOptional(Identifier.fromNamespaceAndPath(MOD_ID, path))
                .orElse(Items.PAPER);
    }

    private static Item mc(String path) {
        return BuiltInRegistries.ITEM
                .getOptional(Identifier.fromNamespaceAndPath("minecraft", path))
                .orElse(Items.PAPER);
    }

    private static ItemStack modStack(String path, int count) {
        return new ItemStack(mod(path), count);
    }

    private static ItemStack mcStack(String path, int count) {
        return new ItemStack(mc(path), count);
    }

    private static ItemStack relic(Item item) {
        return item == null ? new ItemStack(Items.PAPER) : new ItemStack(item);
    }

    private static Component line(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }

    public static ItemStack gearAbilityIcon() {
        return modStack("compound_v", 1);
    }

    public static ItemStack economyIcon() {
        return mcStack("emerald", 1);
    }

    public static ItemStack cursedRelicIcon() {
        return relic(RelicItems.CURSED_ESSENCE);
    }

    public static List<GearAbilityRecipe> gearAbilities() {
        return List.of(
                new GearAbilityRecipe(
                        modStack("compound_v", 1),
                        modStack("v1", 1),
                        line("Compound V \u2192 Powered", ChatFormatting.AQUA),
                        List.of(
                                line("Drink to awaken a random super power.", ChatFormatting.GRAY),
                                line("Permanent until removed by Anti-V.", ChatFormatting.DARK_GRAY))),
                new GearAbilityRecipe(
                        modStack("temp_v", 1),
                        modStack("v1", 1),
                        line("Temp V \u2192 Surge", ChatFormatting.AQUA),
                        List.of(
                                line("Grants a temporary power surge.", ChatFormatting.GRAY),
                                line("Wears off after a short duration.", ChatFormatting.DARK_GRAY))),
                new GearAbilityRecipe(
                        modStack("anti_v", 1),
                        mcStack("glass_bottle", 1),
                        line("Anti-V \u2192 Cleanse", ChatFormatting.RED),
                        List.of(
                                line("Strips all powers from the drinker.", ChatFormatting.GRAY))));
    }

    public static List<EconomyConversionRecipe> economyConversions() {
        return List.of(
                new EconomyConversionRecipe(
                        mcStack("gold_nugget", 9),
                        mcStack("gold_ingot", 1),
                        line("Pocket Change \u2192 Coin", ChatFormatting.GOLD),
                        List.of(line("9 nuggets bank into 1 gold coin.", ChatFormatting.GRAY))),
                new EconomyConversionRecipe(
                        mcStack("gold_ingot", 8),
                        mcStack("emerald", 1),
                        line("Coins \u2192 Emerald Note", ChatFormatting.GREEN),
                        List.of(
                                line("Treasury exchange rate.", ChatFormatting.GRAY),
                                line("Rate varies with market events.", ChatFormatting.DARK_GRAY))),
                new EconomyConversionRecipe(
                        mcStack("emerald", 9),
                        mcStack("emerald_block", 1),
                        line("Notes \u2192 Bullion", ChatFormatting.GREEN),
                        List.of(line("Consolidate for vault storage.", ChatFormatting.GRAY))));
    }

    public static List<CursedRelicRecipe> cursedRelics() {
        return List.of(
                new CursedRelicRecipe(
                        List.of(relic(RelicItems.CURSED_ESSENCE), relic(RelicItems.MANA_CRYSTAL),
                                mcStack("amethyst_shard", 2), modStack("anti_v", 1)),
                        relic(RelicItems.AWAKENING_STONE),
                        line("Awakening Infusion", ChatFormatting.DARK_PURPLE),
                        List.of(
                                line("Awakens dormant cursed energy.", ChatFormatting.GRAY),
                                line("Requires sorcerer potential.", ChatFormatting.DARK_GRAY))),
                new CursedRelicRecipe(
                        List.of(relic(RelicItems.EXORCISM_TOKEN), mcStack("nether_star", 1),
                                relic(RelicItems.GRADE_SCROLL)),
                        relic(RelicItems.REFORGE_STONE),
                        line("Grade & Reforge Catalyst", ChatFormatting.LIGHT_PURPLE),
                        List.of(
                                line("Exorcism token + grade scroll fuse into a reforge stone.", ChatFormatting.GRAY),
                                line("Info-only — use items directly in-game.", ChatFormatting.DARK_GRAY))));
    }
}
