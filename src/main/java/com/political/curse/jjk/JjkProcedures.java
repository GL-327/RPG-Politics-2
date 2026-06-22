package com.political.curse.jjk;

import com.political.curse.technique.TechniqueContext;
import com.political.vfx.VfxElement;
import com.political.vfx.VfxHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Reusable JJK combat procedure helpers ported in spirit from reference mods (JujutsuCraft /
 * cursedfate radial domain pulses, life-drain rings, inward vortex setups). All logic is
 * reimplemented here on vanilla APIs with original naming — no external mod dependency.
 */
public final class JjkProcedures {

    private JjkProcedures() {}

    /**
     * Radial cursed-energy burst: damages every living entity within {@code radius} and plays
     * element VFX from the caster outward.
     */
    public static void radialBurst(TechniqueContext ctx, double radius, float damage, VfxElement element) {
        for (LivingEntity e : ctx.around(radius)) {
            ctx.hurt(e, damage);
            VfxHelper.hitSpark(ctx.level, e, element);
        }
        VfxHelper.elementNova(ctx.level, element, ctx.caster.position(), radius);
    }

    /**
     * Life-siphon ring: drains nearby foes and heals the caster for a fraction of damage dealt.
     */
    public static float lifeSiphonRing(TechniqueContext ctx, double radius, float damagePerTarget, float healRatio) {
        List<LivingEntity> targets = ctx.around(radius);
        if (targets.isEmpty()) return 0f;
        float total = 0f;
        for (LivingEntity e : targets) {
            ctx.hurt(e, damagePerTarget);
            total += damagePerTarget;
            VfxHelper.beamBetween(ctx.level, VfxElement.BLOOD.core(),
                    e.getBoundingBox().getCenter(), ctx.caster.getEyePosition(), 1.5, 0.04);
            VfxHelper.hitSpark(ctx.level, e, VfxElement.BLOOD);
        }
        float heal = total * healRatio;
        if (heal > 0f) ctx.caster.heal(heal);
        VfxHelper.elementNova(ctx.level, VfxElement.BLOOD, ctx.caster.position(), radius * 0.85);
        return heal;
    }

    /**
     * Inward vortex: pulls nearby foes toward the caster, then applies slowness (setup for follow-up).
     */
    public static int vortexPull(TechniqueContext ctx, double radius, double pullStrength) {
        List<LivingEntity> targets = ctx.around(radius);
        if (targets.isEmpty()) return 0;
        Vec3 center = ctx.caster.position().add(0, 1, 0);
        int count = 0;
        for (LivingEntity e : targets) {
            Vec3 toCenter = center.subtract(e.position());
            double len = toCenter.length();
            if (len < 0.5) continue;
            Vec3 pull = toCenter.normalize().scale(pullStrength);
            e.push(pull.x, pull.y * 0.35 + 0.05, pull.z);
            e.hurtMarked = true;
            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
            count++;
        }
        VfxHelper.elementNova(ctx.level, VfxElement.VOID, ctx.caster.position(), radius);
        return count;
    }

    /**
     * Barrier pulse: short resistance on the caster plus a damaging shockwave at the edge.
     */
    public static void barrierPulse(TechniqueContext ctx, double radius, float outwardDamage) {
        ctx.caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0));
        ctx.caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, 0));
        for (LivingEntity e : ctx.around(radius)) {
            ctx.hurt(e, outwardDamage);
            pushFrom(ctx.caster.position(), e, 0.9, 0.35);
            VfxHelper.hitSpark(ctx.level, e, VfxElement.HOLY);
        }
        VfxHelper.auraColumn(ctx.level, ctx.caster, VfxElement.HOLY, ctx.caster.tickCount * 0.25);
        VfxHelper.elementNova(ctx.level, VfxElement.HOLY, ctx.caster.position(), radius);
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
