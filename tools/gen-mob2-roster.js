/**
 * Generates MobRoster2.java, mobs2.json, and MOB2_IDS for gen-mobs2.js from tools/mob2-roster.json
 */
const fs = require('fs');
const path = require('path');

const rosterPath = path.join(__dirname, 'mob2-roster.json');
const roster = JSON.parse(fs.readFileSync(rosterPath, 'utf8'));

function javaEscape(s) { return s.replace(/\\/g, '\\\\').replace(/"/g, '\\"'); }

const imports = `package com.political.expansion2.mobs;

import net.minecraft.tags.BiomeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;

import java.util.function.Predicate;

final class MobRoster2 {

    private MobRoster2() {}

    private static Predicate<BiomeSelectionContext> ow() {
        return BiomeSelectors.foundInOverworld();
    }

    private static Predicate<BiomeSelectionContext> tag(net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> t) {
        return BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(t));
    }

    static void bootstrap() {
`;

const lines = [imports];

for (const m of roster) {
  let chain = `        ExpansionMobs2.add(MobSpec2.of("${m.id}", "${javaEscape(m.name)}", MobRole2.${m.role})`;
  if (m.brute) chain += '.brute()';
  if (m.raids) chain += '.raidsVillages()';
  if (m.fireproof) chain += '.fireproof()';
  if (m.ignite) chain += '.ignites()';
  if (m.lightning) chain += '.lightning()';
  if (m.lifesteal) chain += `.lifesteal(${m.lifesteal}f)`;
  chain += `.stats(${m.hp}, ${m.dmg}, ${m.armor}, ${m.speed})`;
  chain += `.resist(${m.kb}, ${m.range})`;
  chain += `.size(${m.w}f, ${m.h}f, ${m.scale}f)`;
  if (m.onHit) chain += `.onHit(MobEffects.${m.onHit}, ${m.onHitDur || 100}, ${m.onHitAmp || 0})`;
  if (m.aura) chain += `.aura(MobEffects.${m.aura}, ${m.auraAmp || 0})`;
  if (m.summon) chain += `.summons("${m.summon}", ${m.summonCount})`;
  chain += `.coins(${m.coinMin}, ${m.coinMax})`;
  for (const d of (m.drops || [])) {
    chain += `.drop(${d.chance}f, MobRoster2.stack(Items.${d.item}, ${d.min}, ${d.max}))`;
  }
  if (m.spawn) {
    const sel = m.spawn.biome === 'overworld' ? 'ow()' : `tag(BiomeTags.${m.spawn.biome})`;
    chain += `.spawn(${sel}, ${m.spawn.weight}, ${m.spawn.min}, ${m.spawn.max})`;
  }
  chain += ');';
  lines.push(chain);
}

lines.push(`    }

    private static final java.util.Random RNG = new java.util.Random();

    private static java.util.function.Supplier<net.minecraft.world.item.ItemStack> stack(
            net.minecraft.world.item.Item item, int min, int max) {
        return () -> new net.minecraft.world.item.ItemStack(item,
                max > min ? min + RNG.nextInt(max - min + 1) : min);
    }
}
`);

const outJava = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'political', 'expansion2', 'mobs', 'MobRoster2.java');
fs.mkdirSync(path.dirname(outJava), { recursive: true });
fs.writeFileSync(outJava, lines.join('\n'));

const lang = {};
for (const m of roster) {
  lang[`entity.politicalserver.${m.id}`] = m.name;
}
fs.writeFileSync(path.join(__dirname, 'lang-fragments', 'mobs2.json'), JSON.stringify(lang, null, 2) + '\n');

const idsJs = `// AUTO-GENERATED from tools/mob2-roster.json — do not edit by hand\nmodule.exports = ${JSON.stringify(roster.map(m => m.id), null, 2)};\n`;
fs.writeFileSync(path.join(__dirname, 'mob2-ids.js'), idsJs);

console.log('Generated', roster.length, 'mobs ->', outJava);
