package com.political.curse.jjk;

import com.political.curse.SorcererGrade;
import com.political.curse.technique.CursedTechnique;
import com.political.curse.technique.TechniqueRegistry;
import com.political.curse.technique.TechniqueResolver;
import com.political.vfx.VfxElement;
import com.political.vfx.VfxHelper;

/**
 * Additional cursed techniques ported from reference JJK procedure patterns (cursedfate /
 * JujutsuCraft radial bursts, vortex setups, barrier pulses). Registered alongside the core
 * ten techniques in {@link com.political.curse.technique.CursedTechniques#bootstrap()}.
 */
public final class JjkPortedTechniques {

    private static boolean bootstrapped;

    private JjkPortedTechniques() {}

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        define("sunder_ring", "Sunder Ring",
                "Unleashes a ring of cursed force that shatters the air around you.",
                VfxElement.ARCANE, 13, 45, SorcererGrade.GRADE_3, ctx -> {
                    JjkProcedures.radialBurst(ctx, 6.0, 5f + ctx.grade() * 2f, VfxElement.ARCANE);
                    return true;
                });

        define("soul_vortex", "Soul Vortex",
                "Twists space inward, dragging nearby foes into your reach.",
                VfxElement.VOID, 16, 55, SorcererGrade.GRADE_3, ctx ->
                        JjkProcedures.vortexPull(ctx, 8.0, 0.55 + ctx.grade() * 0.05) > 0);

        define("crimson_harvest", "Crimson Harvest",
                "A blood sigil that siphons vitality from every foe in range.",
                VfxElement.BLOOD, 18, 70, SorcererGrade.GRADE_2, ctx ->
                        JjkProcedures.lifeSiphonRing(ctx, 7.0, 4f + ctx.grade() * 1.5f, 0.45f) > 0f);

        define("sanctum_pulse", "Sanctum Pulse",
                "Briefly hardens your cursed shell, then erupts with a holy shockwave.",
                VfxElement.HOLY, 20, 100, SorcererGrade.GRADE_2, ctx -> {
                    JjkProcedures.barrierPulse(ctx, 5.5, 4f + ctx.grade() * 2f);
                    return true;
                });

        define("void_lance_storm", "Void Lance Storm",
                "Three staggered void lances rip through targets in a wide cone.",
                VfxElement.VOID, 22, 80, SorcererGrade.GRADE_1, ctx -> {
                    float dmg = 6f + ctx.grade() * 2.5f;
                    for (int i = 0; i < 3; i++) {
                        for (var e : ctx.cone(14.0 - i * 2, 0.88 - i * 0.05)) {
                            ctx.hurt(e, dmg * (1f - i * 0.12f));
                            VfxHelper.hitSpark(ctx.level, e, VfxElement.VOID);
                        }
                        VfxHelper.elementBeam(ctx.level, VfxElement.VOID, ctx.eye, ctx.view, 14.0 - i * 2);
                    }
                    return true;
                });
    }

    private static void define(String id, String name, String desc, VfxElement element,
                               double ceCost, int cooldownTicks, int requiredGrade, TechniqueResolver resolver) {
        TechniqueRegistry.register(new CursedTechnique(id, name, desc, element, ceCost, cooldownTicks, requiredGrade, resolver));
    }
}
