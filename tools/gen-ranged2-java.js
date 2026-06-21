/**
 * Generates RangedCast2.java and RangedWeapon2.java from ranged2-catalog.js
 *   cd tools && node gen-ranged2-java.js
 */
const fs = require('fs');
const path = require('path');
const { CASTS, WEAPONS } = require('./ranged2-catalog');

const OUT = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'political', 'expansion2', 'ranged');

function effectArgs(c) {
  const parts = [];
  if (c.fire) parts.push(`ParticleKind.FLAME, ${c.fire}`);
  else if (c.pattern === 'BEAM' || c.pattern === 'CHAIN') parts.push('ParticleKind.BEAM, 0');
  else if (c.pattern === 'HEAL' || c.pattern === 'SANCTUM' || c.pattern === 'HOLY_NOVA') parts.push('ParticleKind.HEART, 0');
  else if (c.summon) parts.push('ParticleKind.SOUL, 0');
  else parts.push('ParticleKind.ARCANE, 0');

  parts.push(String(c.aimR ?? 0));
  parts.push(String(c.chainR ?? 5));
  parts.push(String(c.chainMult ?? 0.6));
  parts.push(String(c.coneT ?? 0.45));

  const slow = c.slow ? `new int[]{${c.slow[0]}, ${c.slow[1]}}` : 'new int[]{0, 0}';
  const poison = c.poison ? `new int[]{${c.poison[0]}, ${c.poison[1]}}` : 'new int[]{0, 0}';
  const wither = c.wither ? `new int[]{${c.wither[0]}, ${c.wither[1]}}` : 'new int[]{0, 0}';
  const weakness = c.weakness ? `new int[]{${c.weakness[0]}, ${c.weakness[1]}}` : 'new int[]{0, 0}';
  parts.push(slow, poison, wither, weakness);
  parts.push(String(c.blind ?? 0));
  parts.push(String(c.healSelf ?? 0));
  parts.push(String(c.healAllies ?? 0));
  parts.push(String(c.drain ?? 0));
  parts.push(String(c.selfCost ?? 0));
  parts.push(String(!!c.lightning));
  parts.push(String(!!c.launch));
  parts.push(String(!!c.launchFrom));
  parts.push(c.summon ? `SummonKind.${c.summon}` : 'SummonKind.NONE');
  parts.push(String(c.count ?? 0));
  return parts.join(', ');
}

function genCasts() {
  const entries = CASTS.map(c => `    ${c.name}("${c.display}", "${c.desc.replace(/"/g, '\\"')}",
            ${c.mana}, ${c.cd}, ${c.power}f, ${c.range}, ChatFormatting.${c.color},
            CastPattern.${c.pattern}, CastEffects.of(${effectArgs(c)}))`);
  return `package com.political.expansion2.ranged;

import net.minecraft.ChatFormatting;

/**
 * Phase-2 spell catalogue — ${CASTS.length} unique RIGHT CLICK casts for {@code arc2_} weapons.
 * Behaviour is executed by {@link RangedAbilityEngine2} from {@link CastPattern} + {@link CastEffects}.
 */
public enum RangedCast2 {
${entries.join(',\n')};

    public final String displayName;
    public final String description;
    public final int manaCost;
    public final int cooldownSeconds;
    public final float power;
    public final int range;
    public final ChatFormatting color;
    public final CastPattern pattern;
    public final CastEffects effects;

    RangedCast2(String displayName, String description, int manaCost, int cooldownSeconds,
                float power, int range, ChatFormatting color, CastPattern pattern, CastEffects effects) {
        this.displayName = displayName;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownSeconds = cooldownSeconds;
        this.power = power;
        this.range = range;
        this.color = color;
        this.pattern = pattern;
        this.effects = effects;
    }

    /** Targeted beams/chains require a foe in sight or Mana is refunded. */
    public boolean requiresTarget() {
        return pattern == CastPattern.BEAM || pattern == CastPattern.CHAIN;
    }
}
`;
}

const CAT_MAP = {
  bow: 'Category.BOW',
  crossbow: 'Category.CROSSBOW',
  chakram: 'Category.CHAKRAM',
  gun: 'Category.GUN',
  wand: 'Category.WAND',
  staff: 'Category.STAFF',
  tome: 'Category.TOME',
  orb: 'Category.ORB',
  focus: 'Category.DOMAIN_FOCUS',
};

const BASE_ITEM = {
  bow: 'Items.BLAZE_ROD', crossbow: 'Items.BLAZE_ROD', chakram: 'Items.BLAZE_ROD',
  gun: 'Items.BLAZE_ROD', wand: 'Items.BLAZE_ROD', staff: 'Items.BLAZE_ROD',
  tome: 'Items.BOOK', orb: 'Items.BOOK', focus: 'Items.BLAZE_ROD',
};

function genWeapons() {
  const entries = WEAPONS.map(w => `    ${w.name}("${w.display}", ${CAT_MAP[w.cat]}, ${BASE_ITEM[w.cat]}, Rarity.${w.rarity},
            ${w.int}, ${w.dmg}, ${w.str}, ${w.cc}, ${w.cd}, ${w.fer}, RangedCast2.${w.cast})`);
  return `package com.political.expansion2.ranged;

import com.political.items.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/** Phase-2 ranged/magic arsenal — ${WEAPONS.length} weapons, ids prefixed {@code arc2_}. */
public enum RangedWeapon2 {
${entries.join(',\n')};

    public final String displayName;
    public final Category category;
    public final Item baseItem;
    public final Rarity rarity;
    public final int intelligence;
    public final int damage;
    public final int strength;
    public final int critChance;
    public final int critDamage;
    public final int ferocity;
    public final RangedCast2 cast;

    RangedWeapon2(String displayName, Category category, Item baseItem, Rarity rarity,
                  int intelligence, int damage, int strength, int critChance, int critDamage,
                  int ferocity, RangedCast2 cast) {
        this.displayName = displayName;
        this.category = category;
        this.baseItem = baseItem;
        this.rarity = rarity;
        this.intelligence = intelligence;
        this.damage = damage;
        this.strength = strength;
        this.critChance = critChance;
        this.critDamage = critDamage;
        this.ferocity = ferocity;
        this.cast = cast;
    }

    public String id() {
        return "arc2_" + name().toLowerCase();
    }

    public enum Category {
        BOW("BOW"), CROSSBOW("CROSSBOW"), CHAKRAM("CHAKRAM"), GUN("ARC GUN"),
        WAND("WAND"), STAFF("STAFF"), TOME("SPELLBOOK"), ORB("ORB"), DOMAIN_FOCUS("DOMAIN FOCUS");

        public final String footer;
        Category(String footer) { this.footer = footer; }
    }

    private static final Map<String, RangedWeapon2> BY_ID = new HashMap<>();
    static {
        for (RangedWeapon2 w : values()) BY_ID.put(w.id(), w);
    }

    public static RangedWeapon2 byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }
}
`;
}

function genLang() {
  const lang = {};
  for (const w of WEAPONS) {
    lang[`item.politicalserver.arc2_${w.name.toLowerCase()}`] = w.display;
  }
  return lang;
}

fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'RangedCast2.java'), genCasts());
fs.writeFileSync(path.join(OUT, 'RangedWeapon2.java'), genWeapons());
fs.writeFileSync(path.join(__dirname, 'lang-fragments', 'ranged2.json'), JSON.stringify(genLang(), null, 2) + '\n');
console.log('casts:', CASTS.length, 'weapons:', WEAPONS.length);
console.log('wrote', OUT);
