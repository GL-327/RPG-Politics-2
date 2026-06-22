package com.political.curse.technique;

import com.political.curse.SorcererGrade;
import com.political.sound.VfxSounds;
import com.political.vfx.VfxElement;
import com.political.vfx.VfxHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The ten original cursed techniques of the JJK overhaul. Every name and description is original to
 * this mod; only the genre <i>mechanics</i> are ported. Visuals all run through {@code VfxHelper}.
 *
 * <p>Call {@link #bootstrap()} once during common init (done by {@code JjkBootstrap.init}).</p>
 */
public final class CursedTechniques {

    private static boolean bootstrapped;

    private CursedTechniques() {}

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        com.political.curse.jjk.JjkPortedTechniques.bootstrap();

        // 1 — Severing Edge: a close cursed-energy slash fan.
        define("severing_edge", "Severing Edge",
                "A fanning cursed-energy slash that rends everything in front of you.",
                VfxElement.BLOOD, 6, 12, SorcererGrade.GRADE_4, ctx -> {
                    float dmg = 7f + ctx.grade() * 2.2f;
                    for (LivingEntity e : ctx.cone(4.5, 0.55)) {
                        ctx.hurt(e, dmg);
                        VfxHelper.hitSpark(ctx.level, e, VfxElement.BLOOD);
                    }
                    VfxHelper.elementCone(ctx.level, VfxElement.BLOOD, ctx.eye, ctx.view, 4.5, 35);
                    return true;
                });

        // 2 — Hollow Lance: a long piercing void lance.
        define("hollow_lance", "Hollow Lance",
                "A tight lance of hollow cursed energy that pierces a line of foes.",
                VfxElement.VOID, 14, 30, SorcererGrade.GRADE_3, ctx -> {
                    float dmg = 9f + ctx.grade() * 3f;
                    for (LivingEntity e : ctx.cone(20.0, 0.97)) {
                        ctx.hurt(e, dmg);
                        VfxHelper.hitSpark(ctx.level, e, VfxElement.VOID);
                    }
                    VfxHelper.elementBeam(ctx.level, VfxElement.VOID, ctx.eye, ctx.view, 20.0);
                    return true;
                });

        // 3 — Riftpalm: an outward shove of cursed pressure.
        define("riftpalm", "Riftpalm",
                "Releases a burst of cursed pressure, hurling nearby foes away.",
                VfxElement.ARCANE, 10, 24, SorcererGrade.GRADE_4, ctx -> {
                    float dmg = 4f + ctx.grade() * 1.5f;
                    for (LivingEntity e : ctx.around(5.0)) {
                        ctx.hurt(e, dmg);
                        pushFrom(ctx.caster.position(), e, 1.1, 0.5);
                    }
                    VfxHelper.elementNova(ctx.level, VfxElement.ARCANE, ctx.caster.position(), 5.0);
                    return true;
                });

        // 4 — Ashen Pyre: a searing cone that ignites.
        define("ashen_pyre", "Ashen Pyre",
                "A breath of cursed flame that scorches and ignites a wide cone.",
                VfxElement.FIRE, 12, 40, SorcererGrade.GRADE_4, ctx -> {
                    float dmg = 6f + ctx.grade() * 2f;
                    for (LivingEntity e : ctx.cone(6.0, 0.5)) {
                        ctx.hurt(e, dmg);
                        e.setRemainingFireTicks(80);
                    }
                    VfxHelper.elementCone(ctx.level, VfxElement.FIRE, ctx.eye, ctx.view, 6.0, 40);
                    return true;
                });

        // 5 — Frostbind Coil: a chilling line that locks foes down.
        define("frostbind_coil", "Frostbind Coil",
                "A coil of frozen cursed energy that slows and saps those it strikes.",
                VfxElement.FROST, 12, 50, SorcererGrade.GRADE_3, ctx -> {
                    float dmg = 3f + ctx.grade() * 1.5f;
                    for (LivingEntity e : ctx.cone(8.0, 0.85)) {
                        ctx.hurt(e, dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                        e.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 1));
                    }
                    VfxHelper.elementBeam(ctx.level, VfxElement.FROST, ctx.eye, ctx.view, 8.0);
                    return true;
                });

        // 6 — Stormcall Brand: brands a target and calls down lightning.
        define("stormcall_brand", "Stormcall Brand",
                "Brands the foe you face and calls a bolt of charged cursed energy onto them.",
                VfxElement.LIGHTNING, 18, 60, SorcererGrade.GRADE_2, ctx -> {
                    LivingEntity t = ctx.lookTarget(24.0);
                    if (t == null) return false;
                    LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(ctx.level, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.setPos(t.getX(), t.getY(), t.getZ());
                        ctx.level.addFreshEntity(bolt);
                    }
                    ctx.hurt(t, 8f + ctx.grade() * 3f);
                    VfxHelper.elementBeam(ctx.level, VfxElement.LIGHTNING, ctx.eye, ctx.view, ctx.eye.distanceTo(t.getEyePosition()));
                    return true;
                });

        // 7 — Grave Tether: drains a nearby foe and mends the caster.
        define("grave_tether", "Grave Tether",
                "Tethers the nearest foe, draining their vitality to mend your own.",
                VfxElement.BLOOD, 14, 45, SorcererGrade.GRADE_3, ctx -> {
                    LivingEntity t = ctx.nearest(12.0);
                    if (t == null) return false;
                    float dmg = 6f + ctx.grade() * 2f;
                    ctx.hurt(t, dmg);
                    ctx.caster.heal(dmg * 0.5f);
                    VfxHelper.beamBetween(ctx.level, VfxElement.BLOOD.core(),
                            ctx.caster.getEyePosition(), t.getBoundingBox().getCenter(), 2.0, 0.05);
                    VfxHelper.hitSpark(ctx.level, t, VfxElement.BLOOD);
                    VfxHelper.hitSpark(ctx.level, ctx.caster, VfxElement.HOLY);
                    return true;
                });

        // 8 — Warding Sigil: a protective cursed-energy ward on the self.
        define("warding_sigil", "Warding Sigil",
                "Weaves a protective sigil of cursed energy, hardening and mending you.",
                VfxElement.HOLY, 10, 120, SorcererGrade.GRADE_4, ctx -> {
                    ctx.caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 1));
                    ctx.caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
                    ctx.caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                    VfxHelper.auraColumn(ctx.level, ctx.caster, VfxElement.HOLY, ctx.caster.tickCount * 0.3);
                    VfxHelper.elementBurst(ctx.level, VfxElement.HOLY, ctx.caster.position().add(0, 1, 0), 0.8);
                    return true;
                });

        // 9 — Shade Step: a short reinforced blink.
        define("shade_step", "Shade Step",
                "Reinforces your body and blinks a short distance in the direction you look.",
                VfxElement.VOID, 8, 20, SorcererGrade.GRADE_4, ctx -> {
                    Vec3 from = ctx.caster.position();
                    Vec3 dest = from.add(ctx.view.x * 6.0, Math.max(0.0, ctx.view.y * 4.0), ctx.view.z * 6.0);
                    ctx.caster.teleportTo(dest.x, dest.y, dest.z);
                    ctx.caster.fallDistance = 0;
                    VfxHelper.elementTrail(ctx.level, VfxElement.VOID, from.add(0, 1, 0), ctx.caster.position().add(0, 1, 0));
                    VfxHelper.elementBurst(ctx.level, VfxElement.VOID, ctx.caster.position().add(0, 1, 0), 0.6);
                    return true;
                });

        // 10 — Verdant Snare: a barbed bloom that roots and poisons an area.
        define("verdant_snare", "Verdant Snare",
                "Erupts a barbed cursed bloom that roots and poisons everything around you.",
                VfxElement.NATURE, 16, 70, SorcererGrade.GRADE_2, ctx -> {
                    float dmg = 4f + ctx.grade() * 1.5f;
                    for (LivingEntity e : ctx.around(7.0)) {
                        ctx.hurt(e, dmg);
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 4));
                        e.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                        e.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 120, 1));
                    }
                    VfxHelper.elementNova(ctx.level, VfxElement.NATURE, ctx.caster.position(), 7.0);
                    return true;
                });

        // 11 — Chain Sigil: arcing cursed links between nearby foes.
        define("chain_sigil", "Chain Sigil",
                "Etches sigils that leap between nearby foes, chaining cursed damage.",
                VfxElement.LIGHTNING, 15, 35, SorcererGrade.GRADE_3, ctx -> {
                    List<LivingEntity> targets = ctx.around(10.0);
                    if (targets.isEmpty()) return false;
                    float dmg = 5f + ctx.grade() * 1.8f;
                    LivingEntity prev = ctx.caster;
                    int links = 0;
                    for (LivingEntity e : targets) {
                        if (links >= 4) break;
                        ctx.hurt(e, dmg);
                        VfxHelper.beamBetween(ctx.level, VfxElement.LIGHTNING.core(),
                                prev.getEyePosition(), e.getBoundingBox().getCenter(), 2.0, 0.04);
                        VfxHelper.hitSpark(ctx.level, e, VfxElement.LIGHTNING);
                        prev = e;
                        links++;
                    }
                    if (VfxSounds.VFX_CHAIN_LIGHTNING != null) {
                        VfxSounds.play(ctx.level, ctx.caster.position().add(0, 1, 0),
                                VfxSounds.VFX_CHAIN_LIGHTNING, 1.0f, 1.1f);
                    }
                    return true;
                });

        // 12 — Obsidian Mantle: a brief shell of hardened cursed energy.
        define("obsidian_mantle", "Obsidian Mantle",
                "Wraps you in obsidian cursed energy, granting resistance and a reactive burst.",
                VfxElement.ARCANE, 18, 90, SorcererGrade.GRADE_2, ctx -> {
                    ctx.caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 2));
                    ctx.caster.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0));
                    VfxHelper.auraColumn(ctx.level, ctx.caster, VfxElement.ARCANE, ctx.caster.tickCount * 0.2);
                    for (LivingEntity e : ctx.around(4.0)) {
                        ctx.hurt(e, 3f + ctx.grade());
                        pushFrom(ctx.caster.position(), e, 0.6, 0.25);
                    }
                    VfxHelper.elementNova(ctx.level, VfxElement.ARCANE, ctx.caster.position(), 4.0);
                    if (VfxSounds.VFX_RUNE_HUM != null) {
                        VfxSounds.play(ctx.level, ctx.caster.position().add(0, 1, 0),
                                VfxSounds.VFX_RUNE_HUM, 0.9f, 0.8f);
                    }
                    return true;
                });
    }

    private static void define(String id, String name, String desc, VfxElement element,
                               double ceCost, int cooldownTicks, int requiredGrade, TechniqueResolver resolver) {
        TechniqueRegistry.register(new CursedTechnique(id, name, desc, element, ceCost, cooldownTicks, requiredGrade, resolver));
    }

    private static void pushFrom(Vec3 origin, LivingEntity victim, double horizontal, double vertical) {
        double dx = victim.getX() - origin.x;
        double dz = victim.getZ() - origin.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 1.0e-4) {
            victim.push(dx / len * horizontal, vertical, dz / len * horizontal);
            victim.hurtMarked = true;
        }
    }
}
