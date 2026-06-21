package com.political.expansion2.food;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase-2 global cuisine expansion: 160 edible items spanning Asian,
 * European, Middle Eastern, dessert, drink, street-food and hero-tier buff meals.
 * Plain vanilla {@link Item} with {@link FoodProperties}; buff foods and drinks
 * attach a {@link Consumable} for short vanilla status effects.
 *
 * <p>Self-contained under {@code com.political.expansion2.food}. Call
 * {@link #register()} once during mod init and feed {@link #items()} into a
 * creative tab.
 */
public final class Food2Items {

    public static final String MOD_ID = "politicalserver";

    private static final Map<String, Item> ALL = new LinkedHashMap<>();

    private Food2Items() {}

    public static List<Item> items() {
        return new ArrayList<>(ALL.values());
    }

    public static Item get(String id) {
        return ALL.get(id);
    }

    public static void register() {
        // ---------------------------------------------------------------
        // ASIAN CUISINE
        // ---------------------------------------------------------------
        buffFood("food2_ramen_bowl", 8, 0.8f, false,
                effect(MobEffects.REGENERATION, 10, 0));
        buffFood("food2_pho", 8, 0.8f, false,
                effect(MobEffects.REGENERATION, 12, 0));
        food("food2_dumplings", 6, 0.6f);
        food("food2_spring_roll", 5, 0.5f);
        food("food2_fried_rice", 7, 0.7f);
        buffFood("food2_teriyaki_chicken", 8, 0.8f, false,
                effect(MobEffects.STRENGTH, 30, 0));
        food("food2_tempura", 6, 0.6f);
        buffFood("food2_miso_soup", 5, 0.5f, false,
                effect(MobEffects.REGENERATION, 8, 0));
        buffFood("food2_bibimbap", 8, 0.8f, false,
                effect(MobEffects.SPEED, 40, 0));
        buffFood("food2_kimchi", 3, 0.3f, false,
                effect(MobEffects.FIRE_RESISTANCE, 20, 0));
        food("food2_pork_bun", 6, 0.6f);
        food("food2_udon", 7, 0.7f);
        food("food2_yakitori", 6, 0.6f);
        buffFood("food2_sashimi", 5, 0.5f, false,
                effect(MobEffects.WATER_BREATHING, 30, 0));
        food("food2_onigiri", 5, 0.5f);
        buffFood("food2_curry_rice", 8, 0.8f, false,
                effect(MobEffects.RESISTANCE, 30, 0));
        buffFood("food2_pad_thai", 7, 0.7f, false,
                effect(MobEffects.SPEED, 35, 0));
        buffFood("food2_green_curry", 8, 0.8f, false,
                effect(MobEffects.FIRE_RESISTANCE, 25, 0));
        food("food2_satay", 6, 0.6f);
        food("food2_dal", 6, 0.6f);
        food("food2_naan", 5, 0.5f);
        buffFood("food2_biryani", 9, 0.9f, false,
                effect(MobEffects.SATURATION, 2, 0));
        food("food2_samosa", 5, 0.5f);
        buffFood("food2_tandoori_chicken", 8, 0.8f, false,
                effect(MobEffects.STRENGTH, 35, 0));
        food("food2_dim_sum", 6, 0.6f);
        buffFood("food2_congee", 6, 0.5f, false,
                effect(MobEffects.REGENERATION, 6, 0));
        buffFood("food2_hot_pot", 9, 0.9f, false,
                effect(MobEffects.FIRE_RESISTANCE, 20, 0),
                effect(MobEffects.REGENERATION, 8, 0));
        buffFood("food2_mapo_tofu", 7, 0.7f, false,
                effect(MobEffects.FIRE_RESISTANCE, 30, 0));
        food("food2_katsu", 7, 0.7f);
        food("food2_takoyaki", 5, 0.5f);
        food("food2_okonomiyaki", 7, 0.7f);
        food("food2_bao_burger", 8, 0.8f);
        buffFood("food2_laksa", 8, 0.8f, false,
                effect(MobEffects.WATER_BREATHING, 35, 0));
        food("food2_nasi_lemak", 8, 0.8f);
        buffFood("food2_banh_mi", 7, 0.7f, false,
                effect(MobEffects.SPEED, 30, 0));
        buffFood("food2_phoenix_roast", 11, 1.1f, false,
                effect(MobEffects.FIRE_RESISTANCE, 60, 0),
                effect(MobEffects.REGENERATION, 12, 0));

        // ---------------------------------------------------------------
        // EUROPEAN CUISINE
        // ---------------------------------------------------------------
        food("food2_paella", 9, 0.9f);
        buffFood("food2_ratatouille", 6, 0.6f, false,
                effect(MobEffects.SPEED, 25, 0));
        food("food2_quiche", 6, 0.6f);
        food("food2_croissant", 4, 0.4f);
        food("food2_baguette", 5, 0.5f);
        food("food2_gnocchi", 7, 0.7f);
        buffFood("food2_lasagna", 9, 0.9f, false,
                effect(MobEffects.SATURATION, 2, 0));
        food("food2_carbonara", 8, 0.8f);
        buffFood("food2_goulash", 8, 0.8f, false,
                effect(MobEffects.RESISTANCE, 25, 0));
        food("food2_pierogi", 6, 0.6f);
        buffFood("food2_borscht", 6, 0.6f, false,
                effect(MobEffects.REGENERATION, 8, 0));
        buffFood("food2_schnitzel", 8, 0.8f, false,
                effect(MobEffects.STRENGTH, 30, 0));
        food("food2_bratwurst", 7, 0.7f);
        food("food2_fish_chips", 8, 0.8f);
        food("food2_shepherd_pie", 9, 0.9f);
        food("food2_moussaka", 8, 0.8f);
        food("food2_spanakopita", 6, 0.6f);
        food("food2_cottage_pie", 8, 0.8f);
        buffFood("food2_welsh_rarebit", 6, 0.6f, false,
                effect(MobEffects.HASTE, 60, 0));
        food("food2_scotch_egg", 5, 0.5f);
        buffFood("food2_beef_wellington", 10, 1f, false,
                effect(MobEffects.STRENGTH, 40, 0),
                effect(MobEffects.SATURATION, 2, 0));
        buffFood("food2_coq_au_vin", 9, 0.9f, false,
                effect(MobEffects.REGENERATION, 10, 0));
        buffFood("food2_bouillabaisse", 8, 0.8f, false,
                effect(MobEffects.WATER_BREATHING, 40, 0));
        food("food2_cassoulet", 9, 0.9f);
        food("food2_souvlaki", 7, 0.7f);
        food("food2_tapas_platter", 7, 0.7f);
        food("food2_ribollita", 7, 0.7f);
        buffFood("food2_cacciatore", 8, 0.8f, false,
                effect(MobEffects.NIGHT_VISION, 120, 0));

        // ---------------------------------------------------------------
        // MIDDLE EASTERN CUISINE
        // ---------------------------------------------------------------
        food("food2_hummus", 4, 0.4f);
        food("food2_falafel", 5, 0.5f);
        buffFood("food2_shawarma", 8, 0.8f, false,
                effect(MobEffects.STRENGTH, 30, 0));
        buffFood("food2_kebab", 8, 0.8f, false,
                effect(MobEffects.STRENGTH, 35, 0));
        buffFood("food2_tabouleh", 5, 0.5f, false,
                effect(MobEffects.SPEED, 30, 0));
        food("food2_baba_ganoush", 4, 0.4f);
        food("food2_pita_bread", 4, 0.4f);
        buffFood("food2_lamb_tagine", 9, 0.9f, false,
                effect(MobEffects.REGENERATION, 12, 0));
        food("food2_kofta", 7, 0.7f);
        buffFood("food2_fattoush", 5, 0.5f, false,
                effect(MobEffects.SPEED, 25, 0));
        buffFood("food2_mansaf", 10, 1f, false,
                effect(MobEffects.SATURATION, 3, 0));
        food("food2_kibbeh", 6, 0.6f);
        food("food2_mujadara", 7, 0.7f);
        food("food2_manakish", 5, 0.5f);
        food("food2_stuffed_grape_leaves", 5, 0.5f);
        buffFood("food2_shakshuka", 7, 0.7f, false,
                effect(MobEffects.FIRE_RESISTANCE, 25, 0));
        food("food2_ful_medames", 6, 0.6f);
        food("food2_labneh", 4, 0.4f);
        buffFood("food2_persian_rice", 7, 0.7f, false,
                effect(MobEffects.NIGHT_VISION, 180, 0));
        food("food2_turkish_delight", 3, 0.3f);
        buffFood("food2_pomegranate_seeds", 3, 0.3f, false,
                effect(MobEffects.REGENERATION, 5, 0));
        food("food2_tahini", 3, 0.3f);
        food("food2_mehze_platter", 7, 0.7f);
        buffFood("food2_harira", 7, 0.7f, false,
                effect(MobEffects.REGENERATION, 10, 0));
        food("food2_manti", 6, 0.6f);

        // ---------------------------------------------------------------
        // DESSERTS
        // ---------------------------------------------------------------
        buffFood("food2_tiramisu", 5, 0.5f, false,
                effect(MobEffects.SPEED, 50, 0));
        food("food2_creme_brulee", 5, 0.5f);
        food("food2_macaron", 3, 0.3f);
        buffFood("food2_baklava", 5, 0.5f, false,
                effect(MobEffects.HASTE, 70, 0));
        food("food2_knafeh", 5, 0.5f);
        food("food2_mochi", 4, 0.4f);
        food("food2_flan", 4, 0.4f);
        food("food2_pavlova", 5, 0.5f);
        food("food2_cannoli", 4, 0.4f);
        food("food2_panna_cotta", 4, 0.4f);
        food("food2_gelato", 4, 0.5f);
        food("food2_sorbet", 3, 0.3f);
        buffFood("food2_cupcake", 4, 0.4f, false,
                effect(MobEffects.HASTE, 60, 0));
        food("food2_brownie", 4, 0.4f);
        buffFood("food2_cheesecake", 6, 0.6f, false,
                effect(MobEffects.REGENERATION, 8, 0));
        food("food2_fudge", 4, 0.4f);
        buffFood("food2_truffle", 3, 0.3f, false,
                effect(MobEffects.SPEED, 30, 0));
        food("food2_eclair", 4, 0.4f);
        food("food2_churros", 4, 0.4f);
        buffFood("food2_sticky_toffee", 6, 0.6f, false,
                effect(MobEffects.REGENERATION, 6, 0));
        food("food2_banoffee_pie", 6, 0.6f);
        food("food2_pecan_pie", 6, 0.6f);
        buffFood("food2_lava_cake", 5, 0.5f, false,
                effect(MobEffects.FIRE_RESISTANCE, 15, 0));
        food("food2_mille_feulle", 5, 0.5f);
        food("food2_pastry_assortment", 5, 0.5f);

        // ---------------------------------------------------------------
        // DRINKS
        // ---------------------------------------------------------------
        drink("food2_bubble_tea", 3, 0.3f,
                effect(MobEffects.SPEED, 90, 0));
        drink("food2_matcha_latte", 2, 0.2f,
                effect(MobEffects.HASTE, 90, 0));
        drink("food2_thai_iced_tea", 3, 0.3f);
        drink("food2_horchata", 3, 0.3f);
        drink("food2_ayran", 2, 0.2f);
        drink("food2_mint_tea", 2, 0.2f,
                effect(MobEffects.SPEED, 60, 0));
        drink("food2_kombucha", 2, 0.2f,
                effect(MobEffects.REGENERATION, 8, 0));
        drink("food2_ginger_ale", 2, 0.2f);
        drink("food2_coconut_water", 3, 0.3f,
                effect(MobEffects.WATER_BREATHING, 30, 0));
        drink("food2_sangria", 3, 0.3f);
        drink("food2_cider", 3, 0.3f);
        drink("food2_chai", 2, 0.2f,
                effect(MobEffects.SPEED, 90, 0), effect(MobEffects.HASTE, 90, 0));
        drink("food2_oolong_tea", 1, 0.1f,
                effect(MobEffects.NIGHT_VISION, 120, 0));
        drink("food2_sake", 2, 0.2f);
        drink("food2_egg_cream", 3, 0.3f);
        drink("food2_mango_lassi", 3, 0.4f,
                effect(MobEffects.REGENERATION, 6, 0));
        drink("food2_yuzu_ade", 2, 0.2f);
        drink("food2_rose_water", 2, 0.2f,
                effect(MobEffects.REGENERATION, 5, 0));
        drink("food2_turkish_coffee", 1, 0.1f,
                effect(MobEffects.SPEED, 60, 0), effect(MobEffects.HASTE, 60, 0));
        drink("food2_iced_coffee", 2, 0.2f,
                effect(MobEffects.SPEED, 80, 0));

        // ---------------------------------------------------------------
        // STREET FOOD
        // ---------------------------------------------------------------
        food("food2_hot_dog", 6, 0.6f);
        food("food2_soft_pretzel", 5, 0.5f);
        buffFood("food2_nachos", 6, 0.6f, false,
                effect(MobEffects.FIRE_RESISTANCE, 15, 0));
        food("food2_quesadilla", 6, 0.6f);
        food("food2_empanada", 5, 0.5f);
        food("food2_loaded_fries", 7, 0.7f);
        food("food2_corn_dog", 6, 0.6f);
        food("food2_poutine", 8, 0.8f);
        buffFood("food2_tacos_al_pastor", 7, 0.7f, false,
                effect(MobEffects.FIRE_RESISTANCE, 20, 0));
        food("food2_arepa", 6, 0.6f);
        food("food2_gyros_wrap", 7, 0.7f);
        food("food2_pretzel_dog", 7, 0.7f);
        buffFood("food2_street_corn", 5, 0.5f, false,
                effect(MobEffects.FIRE_RESISTANCE, 12, 0));
        food("food2_elote", 5, 0.5f);
        buffFood("food2_jerk_skewer", 6, 0.6f, false,
                effect(MobEffects.STRENGTH, 25, 0));

        // ---------------------------------------------------------------
        // BUFF MEALS
        // ---------------------------------------------------------------
        buffFood("food2_feast_platter", 12, 1.2f, false,
                effect(MobEffects.STRENGTH, 45, 0),
                effect(MobEffects.SATURATION, 3, 0));
        buffFood("food2_warrior_stew", 11, 1.1f, false,
                effect(MobEffects.STRENGTH, 40, 0),
                effect(MobEffects.RESISTANCE, 30, 0));
        buffFood("food2_hunter_rations", 9, 0.9f, false,
                effect(MobEffects.SPEED, 60, 0),
                effect(MobEffects.NIGHT_VISION, 180, 0));
        buffFood("food2_miner_breakfast", 10, 1f, false,
                effect(MobEffects.HASTE, 120, 0),
                effect(MobEffects.STRENGTH, 30, 0));
        buffFood("food2_hero_sandwich", 10, 1f, false,
                effect(MobEffects.STRENGTH, 35, 0),
                effect(MobEffects.REGENERATION, 10, 0));
        buffFood("food2_dragon_noodles", 10, 1f, false,
                effect(MobEffects.STRENGTH, 40, 0),
                effect(MobEffects.FIRE_RESISTANCE, 30, 0));
        buffFood("food2_knight_feast", 11, 1.1f, false,
                effect(MobEffects.RESISTANCE, 45, 0),
                effect(MobEffects.SATURATION, 3, 0));
        buffFood("food2_sea_kings_bounty", 10, 1f, false,
                effect(MobEffects.WATER_BREATHING, 60, 0),
                effect(MobEffects.REGENERATION, 10, 0));
        buffFood("food2_golden_curry", 11, 1.1f, false,
                effect(MobEffects.STRENGTH, 35, 0),
                effect(MobEffects.HASTE, 60, 0),
                effect(MobEffects.SATURATION, 2, 0));
        buffFood("food2_elven_salad", 8, 0.8f, false,
                effect(MobEffects.SPEED, 60, 0),
                effect(MobEffects.REGENERATION, 8, 0));
        buffFood("food2_diplomat_pastries", 7, 0.7f, false,
                effect(MobEffects.HASTE, 90, 0),
                effect(MobEffects.SPEED, 30, 0));
    }

    private static MobEffectInstance effect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                                            int seconds, int amplifier) {
        return new MobEffectInstance(effect, seconds * 20, amplifier);
    }

    private static Item food(String id, int nutrition, float saturation) {
        FoodProperties props = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .build();
        return reg(id, new Item.Properties().food(props));
    }

    private static Item buffFood(String id, int nutrition, float saturation, boolean alwaysEdible,
                                 MobEffectInstance... effects) {
        FoodProperties.Builder fb = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation);
        if (alwaysEdible) fb.alwaysEdible();
        Consumable consumable = consumable(Consumables.defaultFood(), effects);
        return reg(id, new Item.Properties().food(fb.build(), consumable));
    }

    private static Item drink(String id, int nutrition, float saturation, MobEffectInstance... effects) {
        FoodProperties props = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .alwaysEdible()
                .build();
        Consumable consumable = consumable(Consumables.defaultDrink(), effects);
        return reg(id, new Item.Properties().food(props, consumable));
    }

    private static Consumable consumable(Consumable.Builder builder, MobEffectInstance... effects) {
        for (MobEffectInstance e : effects) {
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(List.of(e), 1.0f));
        }
        return builder.build();
    }

    private static Item reg(String id, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, id));
        Item item = new Item(props.setId(key));
        Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
        ALL.put(id, registered);
        return registered;
    }
}
