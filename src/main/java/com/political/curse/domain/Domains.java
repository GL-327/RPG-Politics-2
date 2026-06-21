package com.political.curse.domain;

import com.political.curse.SorcererGrade;
import com.political.vfx.VfxElement;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * The three original Domain Expansions of the JJK overhaul. All names/descriptions are original; only
 * the genre mechanics (a timed, anchored, sure-hit zone) are ported. Call {@link #bootstrap()} once.
 */
public final class Domains {

    private static boolean bootstrapped;

    private Domains() {}

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        // Mid-grade: a barbed garden that bleeds and roots.
        DomainRegistry.register(new CursedDomain(
                "garden_of_severing_bloom", "Garden of Severing Bloom",
                "A barbed cursed garden unfurls; thorns bleed and root all who stand within.",
                VfxElement.NATURE, 35, SorcererGrade.GRADE_3, 140, 7.0, 12,
                (level, caster, victim, age) -> {
                    victim.hurtServer(level, level.damageSources().playerAttack(caster), 3.0f);
                    victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
                }));

        // High-grade: a hollow void tomb that smothers and saps.
        DomainRegistry.register(new CursedDomain(
                "tomb_of_the_hollow_maw", "Tomb of the Hollow Maw",
                "A yawning void seals the area; its hollow gravity smothers everything inside.",
                VfxElement.VOID, 50, SorcererGrade.GRADE_1, 160, 8.0, 10,
                (level, caster, victim, age) -> {
                    victim.hurtServer(level, level.damageSources().playerAttack(caster), 4.5f);
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                    victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 1));
                    victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0));
                }));

        // Special grade: a radiant cathedral that scours foes and mends the caster.
        DomainRegistry.register(new CursedDomain(
                "cathedral_of_still_light", "Cathedral of Still Light",
                "A silent cathedral of light manifests; its radiance scours foes and mends its master.",
                VfxElement.HOLY, 60, SorcererGrade.SPECIAL, 180, 9.0, 8,
                (level, caster, victim, age) -> {
                    victim.hurtServer(level, level.damageSources().playerAttack(caster), 6.0f);
                    victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0));
                    victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
                    caster.heal(1.0f);
                }));
    }
}
