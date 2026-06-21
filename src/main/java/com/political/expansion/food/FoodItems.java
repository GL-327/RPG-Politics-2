package com.political.expansion.food;

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
 * A large Croptopia-inspired roster of edible items: fruits, vegetables, staple
 * crops, cooked meals, desserts and drinks. Everything is a plain vanilla
 * {@link Item} carrying {@link FoodProperties} (hunger + saturation) and, where
 * relevant, a {@link Consumable} component that applies short vanilla status
 * effects on eat/drink.
 *
 * <p>Self-contained: nothing here touches other agents' packages. Integration
 * only needs to call {@link #register()} once during mod init and feed
 * {@link #items()} into a creative tab.
 */
public final class FoodItems {

    public static final String MOD_ID = "politicalserver";

    /** Insertion-ordered id -> registered item, so {@link #items()} is stable. */
    private static final Map<String, Item> ALL = new LinkedHashMap<>();

    private FoodItems() {}

    /** All registered food items, in declaration order (for creative tabs). */
    public static List<Item> items() {
        return new ArrayList<>(ALL.values());
    }

    /** Look up a registered food item by its short id (without the {@code food_} prefix is NOT accepted). */
    public static Item get(String id) {
        return ALL.get(id);
    }

    public static void register() {
        // ---------------------------------------------------------------
        // FRUITS — light, snackable, low saturation
        // ---------------------------------------------------------------
        food("food_strawberry", 3, 0.3f);
        food("food_blueberries", 2, 0.2f);
        food("food_grapes", 3, 0.3f);
        food("food_orange", 4, 0.4f);
        food("food_peach", 4, 0.4f);
        food("food_banana", 4, 0.4f);
        food("food_mango", 4, 0.4f);
        food("food_lemon", 2, 0.1f);
        food("food_pear", 4, 0.4f);
        food("food_cherries", 2, 0.2f);
        food("food_pineapple", 4, 0.4f);
        food("food_coconut", 4, 0.4f);

        // ---------------------------------------------------------------
        // VEGETABLES & CROPS
        // ---------------------------------------------------------------
        food("food_tomato", 3, 0.3f);
        food("food_corn", 4, 0.4f);
        food("food_lettuce", 2, 0.2f);
        food("food_onion", 3, 0.3f);
        food("food_cucumber", 2, 0.2f);
        food("food_pumpkin_wedge", 3, 0.3f);
        // Chili gives brief fire resistance; garlic a touch of resistance.
        buffFood("food_chili_pepper", 2, 0.2f, false,
                effect(MobEffects.FIRE_RESISTANCE, 12, 0));
        buffFood("food_garlic", 2, 0.2f, false,
                effect(MobEffects.RESISTANCE, 12, 0));

        // ---------------------------------------------------------------
        // STAPLES / GRAINS
        // ---------------------------------------------------------------
        food("food_rice_bowl", 5, 0.5f);
        food("food_bread_roll", 5, 0.6f);
        food("food_cheese_wheel", 6, 0.6f);

        // ---------------------------------------------------------------
        // COOKED MEALS — hearty, most grant a short buff
        // ---------------------------------------------------------------
        buffFood("food_roasted_chicken_meal", 8, 0.8f, false,
                effect(MobEffects.REGENERATION, 8, 0));
        buffFood("food_beef_stew", 9, 0.9f, false,
                effect(MobEffects.SATURATION, 1, 0));
        buffFood("food_grilled_steak", 9, 0.9f, false,
                effect(MobEffects.STRENGTH, 30, 0));
        buffFood("food_fish_taco", 7, 0.7f, false,
                effect(MobEffects.WATER_BREATHING, 30, 0));
        buffFood("food_veggie_salad", 6, 0.6f, false,
                effect(MobEffects.SPEED, 30, 0));
        food("food_cheese_pizza_slice", 7, 0.7f);
        food("food_burger", 8, 0.8f);
        buffFood("food_noodle_soup", 7, 0.7f, false,
                effect(MobEffects.REGENERATION, 6, 0));
        buffFood("food_sushi_roll", 6, 0.6f, false,
                effect(MobEffects.WATER_BREATHING, 25, 0));
        buffFood("food_mushroom_risotto", 7, 0.7f, false,
                effect(MobEffects.NIGHT_VISION, 200, 0));

        // ---------------------------------------------------------------
        // DESSERTS
        // ---------------------------------------------------------------
        buffFood("food_chocolate_bar", 4, 0.4f, false,
                effect(MobEffects.SPEED, 40, 0));
        food("food_ice_cream", 4, 0.5f);
        buffFood("food_apple_pie_slice", 6, 0.6f, false,
                effect(MobEffects.REGENERATION, 5, 0));
        buffFood("food_glazed_donut", 5, 0.5f, false,
                effect(MobEffects.HASTE, 80, 0));
        food("food_cookie_jar", 5, 0.5f);
        food("food_caramel", 3, 0.4f);

        // ---------------------------------------------------------------
        // DRINKS — always edible (drinkable on a full bar), drink animation
        // ---------------------------------------------------------------
        drink("food_apple_juice", 3, 0.4f,
                effect(MobEffects.REGENERATION, 4, 0));
        drink("food_lemonade", 2, 0.3f);
        drink("food_coffee", 1, 0.1f,
                effect(MobEffects.SPEED, 120, 0), effect(MobEffects.HASTE, 120, 0));
        drink("food_milkshake", 4, 0.5f);
        drink("food_berry_smoothie", 3, 0.4f,
                effect(MobEffects.REGENERATION, 5, 0));
        drink("food_hot_cocoa", 3, 0.4f,
                effect(MobEffects.FIRE_RESISTANCE, 15, 0));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Builds a status-effect instance from seconds (converted to ticks). */
    private static MobEffectInstance effect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                                            int seconds, int amplifier) {
        return new MobEffectInstance(effect, seconds * 20, amplifier);
    }

    /** Plain food, no effects, eat animation. */
    private static Item food(String id, int nutrition, float saturation) {
        FoodProperties props = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .build();
        return reg(id, new Item.Properties().food(props));
    }

    /**
     * Food granting one or more short status effects on consume.
     *
     * @param alwaysEdible if true the item can be eaten with a full hunger bar
     */
    private static Item buffFood(String id, int nutrition, float saturation, boolean alwaysEdible,
                                 MobEffectInstance... effects) {
        FoodProperties.Builder fb = new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation);
        if (alwaysEdible) fb.alwaysEdible();
        Consumable consumable = consumable(Consumables.defaultFood(), effects);
        return reg(id, new Item.Properties().food(fb.build(), consumable));
    }

    /** Drink: always edible, drink animation, optional short effects. */
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
