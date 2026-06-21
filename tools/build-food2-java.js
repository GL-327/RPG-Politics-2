/**
 * Generates Food2Items.java and tools/lang-fragments/food2.json from food2-roster.js.
 * Usage: node tools/build-food2-java.js
 */
const fs = require('fs');
const path = require('path');
const { ROSTER } = require('./food2-roster');

const EFFECT_MAP = {
  REGENERATION: 'MobEffects.REGENERATION',
  STRENGTH: 'MobEffects.STRENGTH',
  SPEED: 'MobEffects.SPEED',
  HASTE: 'MobEffects.HASTE',
  RESISTANCE: 'MobEffects.RESISTANCE',
  FIRE_RESISTANCE: 'MobEffects.FIRE_RESISTANCE',
  WATER_BREATHING: 'MobEffects.WATER_BREATHING',
  NIGHT_VISION: 'MobEffects.NIGHT_VISION',
  SATURATION: 'MobEffects.SATURATION',
};

function parseEffects(effects) {
  if (!effects || effects.length === 0) return [];
  return effects.map((e) => {
    const [name, sec, amp = '0'] = e.split(':');
    const mob = EFFECT_MAP[name];
    if (!mob) throw new Error('unknown effect ' + name);
    return `effect(${mob}, ${sec}, ${amp})`;
  });
}

function javaLine(entry) {
  const { id, n, s, kind, effects } = entry;
  const fx = parseEffects(effects);
  if (kind === 'drink') {
    return fx.length
      ? `        drink("${id}", ${n}, ${s}f${fx.length ? ',\n                ' + fx.join(', ') : ''});`
      : `        drink("${id}", ${n}, ${s}f);`;
  }
  if (kind === 'buff' || fx.length) {
    return `        buffFood("${id}", ${n}, ${s}f, false,\n                ${fx.join(',\n                ')});`;
  }
  return `        food("${id}", ${n}, ${s}f);`;
}

const sections = [
  { title: 'ASIAN CUISINE', filter: (e) => e.id.startsWith('food2_') && ['ramen','pho','dumpling','spring','fried_rice','teriyaki','tempura','miso','bibimbap','kimchi','pork_bun','udon','yakitori','sashimi','onigiri','curry_rice','pad_thai','green_curry','satay','dal','naan','biryani','samosa','tandoori','dim_sum','congee','hot_pot','mapo','katsu','takoyaki','okonomiyaki','bao','laksa','nasi','banh'].some(k => e.id.includes(k)) },
  { title: 'EUROPEAN CUISINE', filter: (e) => ['paella','ratatouille','quiche','croissant','baguette','gnocchi','lasagna','carbonara','goulash','pierogi','borscht','schnitzel','bratwurst','fish_chips','shepherd','moussaka','spanakopita','cottage','welsh','scotch','beef_wellington','coq','bouillabaisse','cassoulet','souvlaki','tapas','ribollita','cacciatore'].some(k => e.id.includes(k)) },
  { title: 'MIDDLE EASTERN CUISINE', filter: (e) => ['hummus','falafel','shawarma','kebab','tabouleh','baba','pita','tagine','kofta','fattoush','mansaf','kibbeh','mujadara','manakish','grape_leaves','shakshuka','ful_medames','labneh','persian','turkish_delight','pomegranate','tahini','mehze','harira','manti'].some(k => e.id.includes(k)) },
  { title: 'DESSERTS', filter: (e) => ['tiramisu','creme_brulee','macaron','baklava','knafeh','mochi','flan','pavlova','cannoli','panna_cotta','gelato','sorbet','cupcake','brownie','cheesecake','fudge','truffle','eclair','churros','sticky_toffee','banoffee','pecan','lava_cake','mille_feulle','pastry_assortment'].some(k => e.id.includes(k)) },
  { title: 'DRINKS', filter: (e) => e.kind === 'drink' },
  { title: 'STREET FOOD', filter: (e) => ['hot_dog','soft_pretzel','nachos','quesadilla','empanada','loaded_fries','corn_dog','poutine','tacos_al','arepa','gyros','pretzel_dog','street_corn','elote','jerk_skewer'].some(k => e.id.includes(k)) },
  { title: 'BUFF MEALS', filter: (e) => ['feast_platter','warrior_stew','hunter_rations','miner_breakfast','hero_sandwich','phoenix_roast','dragon_noodles','knight_feast','sea_kings','golden_curry','elven_salad','diplomat_pastries'].some(k => e.id.includes(k)) },
];

const used = new Set();
const registerBody = sections.map(({ title, filter }) => {
  const items = ROSTER.filter((e) => filter(e) && !used.has(e.id));
  items.forEach((e) => used.add(e.id));
  if (items.length === 0) return '';
  return `        // ${'-'.repeat(63)}\n        // ${title}\n        // ${'-'.repeat(63)}\n` + items.map(javaLine).join('\n');
}).join('\n\n');

const missed = ROSTER.filter((e) => !used.has(e.id));
if (missed.length) {
  console.warn('Unsectioned items:', missed.map((e) => e.id));
}

const java = `package com.political.expansion2.food;

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
 * Phase-2 global cuisine expansion: ${ROSTER.length} edible items spanning Asian,
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
${registerBody}
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
`;

const lang = {};
for (const e of ROSTER) {
  lang[`item.politicalserver.${e.id}`] = e.name;
}

const javaPath = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'political', 'expansion2', 'food', 'Food2Items.java');
const langPath = path.join(__dirname, 'lang-fragments', 'food2.json');

fs.mkdirSync(path.dirname(javaPath), { recursive: true });
fs.writeFileSync(javaPath, java);
fs.writeFileSync(langPath, JSON.stringify(lang, null, 2) + '\n');

console.log('Food2Items.java:', ROSTER.length, 'items');
console.log('lang fragment:', Object.keys(lang).length, 'keys');
