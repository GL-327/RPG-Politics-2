/**
 * Generates expansion2 melee Java sources from melee2-catalog.js.
 * Usage: node gen-melee2-java.js
 */
const fs = require('fs');
const path = require('path');
const { buildCatalog } = require('./melee2-catalog');

const OUT = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'political', 'expansion2', 'melee');
const weapons = buildCatalog();

function statChain(s) {
  const parts = [];
  if (s.damage) parts.push(`.dmg(${s.damage})`);
  if (s.strength) parts.push(`.str(${s.strength})`);
  if (s.critChance) parts.push(`.cc(${s.critChance})`);
  if (s.critDamage) parts.push(`.cd(${s.critDamage})`);
  if (s.ferocity) parts.push(`.fer(${s.ferocity})`);
  if (s.intelligence) parts.push(`.intel(${s.intelligence})`);
  if (s.health) parts.push(`.hp(${s.health})`);
  if (s.defense) parts.push(`.def(${s.defense})`);
  if (s.speed) parts.push(`.spd(${s.speed})`);
  return `new Stats()${parts.join('')}`;
}

function genAbilityEnum() {
  const seen = new Set();
  let body = '';
  for (const w of weapons) {
    const a = w.ability;
    if (seen.has(a.enumName)) continue;
    seen.add(a.enumName);
    body += `    ${a.enumName}("${esc(a.displayName)}", "${esc(a.description)}", ${a.manaCost}, ${a.cooldownSeconds}),\n`;
  }
  return `package com.political.expansion2.melee;

/**
 * Self-contained RIGHT CLICK active abilities for expansion2 melee weapons ({@code wpn2_*}).
 */
public enum Melee2Ability {
${body}
    ;

    public final String displayName;
    public final String description;
    public final int manaCost;
    public final int cooldownSeconds;

    Melee2Ability(String displayName, String description, int manaCost, int cooldownSeconds) {
        this.displayName = displayName;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownSeconds = cooldownSeconds;
    }
}
`;
}

function genWeaponEnum() {
  let body = '';
  let curTheme = '';
  for (const w of weapons) {
    if (w.theme !== curTheme) {
      curTheme = w.theme;
      body += `\n    // ---------------- ${curTheme.toUpperCase()} theme ----------------\n`;
    }
    body += `    ${toConst(w.id)}("${w.id}", "${esc(w.displayName)}", "${w.archetype}", Rarity.${w.rarity},\n`;
    body += `            ${statChain(w.stats)}, Melee2Ability.${w.ability.enumName}),\n`;
  }
  return `package com.political.expansion2.melee;

import com.political.items.Rarity;

/** Expansion2 melee catalogue — ids prefixed {@code wpn2_}. */
public enum Melee2Weapon {
${body}
    ;

    public final String id;
    public final String displayName;
    public final String archetype;
    public final Rarity rarity;
    public final Stats stats;
    public final Melee2Ability ability;

    Melee2Weapon(String id, String displayName, String archetype, Rarity rarity,
                 Stats stats, Melee2Ability ability) {
        this.id = id;
        this.displayName = displayName;
        this.archetype = archetype;
        this.rarity = rarity;
        this.stats = stats;
        this.ability = ability;
    }

    public static final class Stats {
        public int damage, strength, critChance, critDamage, ferocity, intelligence, health, defense, speed, attackSpeed;

        public Stats dmg(int v) { this.damage = v; return this; }
        public Stats str(int v) { this.strength = v; return this; }
        public Stats cc(int v) { this.critChance = v; return this; }
        public Stats cd(int v) { this.critDamage = v; return this; }
        public Stats fer(int v) { this.ferocity = v; return this; }
        public Stats intel(int v) { this.intelligence = v; return this; }
        public Stats hp(int v) { this.health = v; return this; }
        public Stats def(int v) { this.defense = v; return this; }
        public Stats spd(int v) { this.speed = v; return this; }
        public Stats atkSpd(int v) { this.attackSpeed = v; return this; }
    }
}
`;
}

function particleExpr(name) {
  return `ParticleTypes.${name}`;
}

function effectExpr(name, dur, amp) {
  return `new MobEffectInstance(MobEffects.${name}, ${dur}, ${amp})`;
}

function genCastCase(w) {
  const a = w.ability;
  const p = a.pattern;
  const baseDmg = Math.round(8 + weapons.indexOf(w) * 0.35);
  const dmg = (p.dmg || 1) * baseDmg;
  const dmgLit = Number.isInteger(dmg) ? `${dmg}f` : `${dmg.toFixed(1)}f`;
  let lines = [`            case ${a.enumName} -> {`];

  const applyEffects = [];
  if (p.effect) applyEffects.push(`e.addEffect(${effectExpr(p.effect, p.dur || 100, p.amp || 1)})`);
  if (p.ignite) applyEffects.push(`e.setRemainingFireTicks(120)`);
  const effectBlock = applyEffects.length
    ? applyEffects.map(l => `                    ${l};`).join('\n')
    : '';

  switch (p.kind) {
    case 'CONE':
      lines.push(`                for (LivingEntity e : cone(p, ${p.range}, ${p.tight})) {`);
      lines.push(`                    hurt(level, p, e, ${dmgLit});`);
      if (effectBlock) lines.push(effectBlock);
      if (p.knock) lines.push(`                    launchAway(e, p, ${p.knock});`);
      if (p.pull) {
        lines.push(`                    Vec3 d = p.position().subtract(e.position()).normalize().scale(${p.pull});`);
        lines.push(`                    e.push(d.x, 0.2, d.z); e.hurtMarked = true;`);
      }
      lines.push(`                }`);
      lines.push(`                particleCone(level, p, ${particleExpr(p.particle)});`);
      if (p.heal) lines.push(`                p.heal(${p.heal}f * cone(p, ${p.range}, ${p.tight}).size());`);
      break;
    case 'AROUND':
      lines.push(`                for (LivingEntity e : around(p, ${p.range})) {`);
      lines.push(`                    hurt(level, p, e, ${dmgLit});`);
      if (effectBlock) lines.push(effectBlock);
      if (p.knock) lines.push(`                    launchAway(e, p, ${p.knock});`);
      lines.push(`                }`);
      lines.push(`                level.sendParticles(${particleExpr(p.particle)}, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);`);
      if (p.heal) {
        lines.push(`                float healed = around(p, ${p.range}).size() * ${p.heal}f;`);
        lines.push(`                if (healed > 0) p.heal(Math.min(healed, 15f));`);
      }
      break;
    case 'TARGET':
      lines.push(`                LivingEntity t = lookTarget(p, ${p.range});`);
      lines.push(`                if (t == null) return false;`);
      lines.push(`                hurt(level, p, t, ${dmgLit});`);
      if (p.effect) lines.push(`                t.addEffect(${effectExpr(p.effect, p.dur || 100, p.amp || 1)});`);
      if (p.ignite) lines.push(`                t.setRemainingFireTicks(120);`);
      if (p.lightning) lines.push(`                strike(level, t.position());`);
      lines.push(`                level.sendParticles(${particleExpr(p.particle)}, t.getX(), t.getY() + 1, t.getZ(), 20, 0.4, 0.4, 0.4, 0.05);`);
      break;
    case 'BLINK_STRIKE':
      lines.push(`                blink(p, ${p.blink || 3});`);
      lines.push(`                LivingEntity t = lookTarget(p, ${p.range});`);
      lines.push(`                if (t == null) return false;`);
      lines.push(`                hurt(level, p, t, ${dmgLit});`);
      lines.push(`                particleCone(level, p, ${particleExpr(p.particle)});`);
      break;
    case 'MULTI_BLINK':
      lines.push(`                List<LivingEntity> targets = around(p, ${p.range});`);
      lines.push(`                for (LivingEntity e : targets) {`);
      lines.push(`                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));`);
      lines.push(`                    p.teleportTo(behind.x, behind.y, behind.z);`);
      lines.push(`                    hurt(level, p, e, ${dmgLit});`);
      lines.push(`                    level.sendParticles(${particleExpr(p.particle)}, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);`);
      lines.push(`                }`);
      lines.push(`                if (targets.isEmpty()) return false;`);
      break;
    default:
      lines.push(`                for (LivingEntity e : cone(p, 6, 0.5)) hurt(level, p, e, ${dmgLit});`);
  }
  lines.push(`            }`);
  return lines.join('\n');
}

function genAbilityEngine() {
  const cases = weapons.map(genCastCase).join('\n');
  return `package com.political.expansion2.melee;

import com.political.combat.StatManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Self-contained RIGHT CLICK handler for {@code wpn2_*} weapons. */
public final class Melee2AbilityEngine {

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private Melee2AbilityEngine() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            Melee2Weapon weapon = Melee2Weapons.byStack(sp.getMainHandItem());
            if (weapon == null) return InteractionResult.PASS;
            Melee2Ability ability = weapon.ability;

            if (onCooldown(sp, ability)) {
                sp.sendSystemMessage(Component.literal(ability.displayName + " is on cooldown.")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (ability.manaCost > 0 && !StatManager.spendMana(sp, ability.manaCost)) {
                sp.sendSystemMessage(Component.literal("Not enough Mana.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (!cast(sp, (ServerLevel) world, ability)) return InteractionResult.PASS;
            setCooldown(sp, ability);
            return InteractionResult.SUCCESS;
        });
    }

    private static boolean onCooldown(ServerPlayer p, Melee2Ability a) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + a.name());
        return ready != null && System.currentTimeMillis() < ready;
    }

    private static void setCooldown(ServerPlayer p, Melee2Ability a) {
        COOLDOWNS.put(p.getStringUUID() + "|" + a.name(),
                System.currentTimeMillis() + a.cooldownSeconds * 1000L);
    }

    private static boolean cast(ServerPlayer p, ServerLevel level, Melee2Ability a) {
        switch (a) {
${cases}
        }
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1.2f);
        return true;
    }

    private static void hurt(ServerLevel level, ServerPlayer p, LivingEntity e, float dmg) {
        e.hurtServer(level, level.damageSources().playerAttack(p), dmg);
    }

    private static LivingEntity lookTarget(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        LivingEntity best = null;
        double bestAlong = range;
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(2);
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, box, x -> x != p && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            if (to.subtract(view.scale(along)).lengthSqr() <= 2.5 && along < bestAlong) {
                best = e;
                bestAlong = along;
            }
        }
        return best;
    }

    private static List<LivingEntity> cone(ServerPlayer p, double range, double tightness) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        List<LivingEntity> out = new ArrayList<>();
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(range * 0.5 + 1);
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, box, x -> x != p && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            if (along / to.length() >= tightness) out.add(e);
        }
        return out;
    }

    private static List<LivingEntity> around(ServerPlayer p, double r) {
        return p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r), x -> x != p && x.isAlive());
    }

    private static void blink(ServerPlayer p, double dist) {
        Vec3 v = p.getViewVector(1f).scale(dist);
        p.teleportTo(p.getX() + v.x, p.getY() + Math.max(0, v.y), p.getZ() + v.z);
        p.fallDistance = 0;
    }

    private static void launchAway(LivingEntity e, ServerPlayer p, double s) {
        Vec3 d = e.position().subtract(p.position()).normalize().scale(s);
        e.push(d.x, 0.3, d.z);
        e.hurtMarked = true;
    }

    private static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.x, at.y, at.z);
            level.addFreshEntity(bolt);
        }
    }

    private static void particleCone(ServerLevel level, ServerPlayer p, SimpleParticleType pt) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= 8; i++) {
            Vec3 at = eye.add(view.scale(i * 1.2));
            level.sendParticles(pt, at.x, at.y, at.z, 4, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
`;
}

function toConst(id) {
  return id.toUpperCase();
}

function esc(s) {
  return s.replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/\n/g, ' ');
}

function genLang() {
  const obj = {};
  for (const w of weapons) {
    obj[`item.politicalserver.${w.id}`] = w.displayName;
  }
  return JSON.stringify(obj, null, 2) + '\n';
}

function genDoc() {
  let md = `# Expansion2 Melee Weapons (Phase 2)

Package: \`com.political.expansion2.melee\`
Item id prefix: \`wpn2_\`

**${weapons.length} melee weapons** across 8 visual themes (JJK cursed tools, Prominence, elemental,
void, celestial, blood, tech, nature). Skyblock stats only — no vanilla attack modifiers.

## Integration

1. \`RpgPoliticsMod.onInitialize\`: \`com.political.expansion2.melee.Melee2Weapons.register();\`
2. Creative tab: \`for (ItemStack s : Melee2Weapons.displays()) out.accept(s);\`
3. Merge \`tools/lang-fragments/melee2.json\` into \`en_us.json\`.
4. Textures: \`node tools/gen-melee2.js\`

## Catalogue (${weapons.length} weapons)

| ID | Name | Theme | Type | Rarity | Ability |
|---|---|---|---|---|---|
`;
  for (const w of weapons) {
    md += `| \`${w.id}\` | ${w.displayName} | ${w.theme} | ${w.archetype} | ${w.rarity} | **${w.ability.displayName}** |\n`;
  }
  return md;
}

fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'Melee2Ability.java'), genAbilityEnum());
fs.writeFileSync(path.join(OUT, 'Melee2Weapon.java'), genWeaponEnum());
fs.writeFileSync(path.join(OUT, 'Melee2AbilityEngine.java'), genAbilityEngine());
fs.writeFileSync(path.join(__dirname, 'lang-fragments', 'melee2.json'), genLang());
fs.mkdirSync(path.join(__dirname, '..', 'docs', 'expansion2'), { recursive: true });
fs.writeFileSync(path.join(__dirname, '..', 'docs', 'expansion2', 'melee.md'), genDoc());
console.log('Generated', weapons.length, 'melee2 weapons');
