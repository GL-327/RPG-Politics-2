package com.political.vfx;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Reusable, parameterized particle + sound effect routines for RPG&nbsp;Politics&nbsp;2.
 *
 * <p>Everything here is server-driven through
 * {@link ServerLevel#sendParticles} and {@link ServerLevel#playSound}, so the
 * library is 100% mixin-free and safe to call from any server-side combat, power,
 * trap, or boss code. Routines are intentionally <i>additive</i>: they only spawn
 * cosmetic particles and sounds and never touch damage, movement, or game state.</p>
 *
 * <h2>Conventions</h2>
 * <ul>
 *   <li>All positions are world coordinates ({@link Vec3} or raw doubles).</li>
 *   <li>"count" is the number of particles; "spread" is the gaussian box radius
 *       passed to {@code sendParticles}; "speed" is the particle speed argument.</li>
 *   <li>Element-themed overloads pull particles/sounds from {@link VfxElement}.</li>
 *   <li>Sounds are passed as plain {@link SoundEvent} so the future
 *       {@code com.political.sound.ModSounds} can be dropped in without any
 *       dependency here (see {@code docs/expansion3/vfx.md}).</li>
 * </ul>
 *
 * <h2>Recommended call sites (for the integration agent)</h2>
 * <ul>
 *   <li>{@code PowerManager2#cast} / {@code PowerManager#...} &mdash; add an
 *       {@code elementBurst}/{@code elementBeam}/{@code domainExpansion} after the
 *       gameplay effect resolves.</li>
 *   <li>{@code AbilityEngine#applyCritAndFerocity} &mdash; replace the inline
 *       {@code CRIT} particles with {@link #critSpark} / {@link #ferocitySpark}.</li>
 *   <li>Melee/Ranged ability engines &mdash; call {@link #elementCone} /
 *       {@link #elementTrail} keyed off the weapon element.</li>
 *   <li>Dungeon traps &mdash; call {@link #groundCrack}, {@link #novaRing}, or
 *       {@link #pillar} when a trap arms/fires.</li>
 *   <li>Boss spawners / phase changes &mdash; call {@link #bossPhaseBurst}.</li>
 * </ul>
 */
public final class VfxHelper {

    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

    private VfxHelper() {}

    // ------------------------------------------------------------------
    // Low-level primitives
    // ------------------------------------------------------------------

    /** Raw particle spawn wrapper. All other routines funnel through this. */
    public static void spawn(ServerLevel level, ParticleOptions particle,
                             double x, double y, double z,
                             int count, double sx, double sy, double sz, double speed) {
        if (level == null || particle == null) return;
        level.sendParticles(particle, x, y, z, Math.max(0, count), sx, sy, sz, speed);
    }

    /** A single localized burst of particles at a point. */
    public static void burst(ServerLevel level, ParticleOptions particle, Vec3 at,
                             int count, double spread, double speed) {
        spawn(level, particle, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** Play a sound at a world position (PLAYERS category). */
    public static void playAt(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        if (level == null || sound == null) return;
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    /** Play a sound at raw coordinates (PLAYERS category). */
    public static void playAt(ServerLevel level, double x, double y, double z,
                              SoundEvent sound, float volume, float pitch) {
        if (level == null || sound == null) return;
        level.playSound(null, x, y, z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    // ------------------------------------------------------------------
    // 1. Beams & lines
    // ------------------------------------------------------------------

    /** Straight beam of particles from {@code origin} along {@code dir} for {@code length} blocks. */
    public static void beam(ServerLevel level, ParticleOptions particle, Vec3 origin, Vec3 dir,
                            double length, double step, int perStep, double jitter) {
        Vec3 d = dir.normalize();
        for (double t = 0; t <= length; t += step) {
            Vec3 at = origin.add(d.scale(t));
            spawn(level, particle, at.x, at.y, at.z, perStep, jitter, jitter, jitter, 0.0);
        }
    }

    /** Beam connecting two arbitrary points (e.g. chain-lightning / tether visuals). */
    public static void beamBetween(ServerLevel level, ParticleOptions particle, Vec3 a, Vec3 b,
                                   double perBlock, double jitter) {
        Vec3 delta = b.subtract(a);
        double len = delta.length();
        if (len < 1.0e-4) return;
        int steps = Math.max(1, (int) (len * perBlock));
        Vec3 stepVec = delta.scale(1.0 / steps);
        Vec3 cur = a;
        for (int i = 0; i <= steps; i++) {
            spawn(level, particle, cur.x, cur.y, cur.z, 1, jitter, jitter, jitter, 0.0);
            cur = cur.add(stepVec);
        }
    }

    /**
     * Sparkle trail along a path with slight randomized scatter, ideal for dashes,
     * projectiles, and blink/teleport streaks.
     */
    public static void sparkleTrail(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to) {
        beamBetween(level, particle, from, to, 2.0, 0.08);
    }

    // ------------------------------------------------------------------
    // 2. Cones
    // ------------------------------------------------------------------

    /** Cone of particles fanning out from {@code origin} along {@code dir}. */
    public static void cone(ServerLevel level, ParticleOptions particle, Vec3 origin, Vec3 dir,
                            double length, double halfAngleDeg, int rings, int perRing) {
        Vec3 forward = dir.normalize();
        Vec3 up = Math.abs(forward.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up).normalize();
        Vec3 trueUp = right.cross(forward).normalize();
        double maxR = Math.tan(Math.toRadians(halfAngleDeg));
        for (int r = 1; r <= rings; r++) {
            double dist = length * r / rings;
            double radius = maxR * dist;
            Vec3 center = origin.add(forward.scale(dist));
            for (int i = 0; i < perRing; i++) {
                double ang = (Math.PI * 2 * i) / perRing;
                Vec3 off = right.scale(Math.cos(ang) * radius).add(trueUp.scale(Math.sin(ang) * radius));
                Vec3 at = center.add(off);
                spawn(level, particle, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.01);
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. Rings, novas & shockwaves
    // ------------------------------------------------------------------

    /** Flat horizontal ring of particles at a fixed radius. */
    public static void ring(ServerLevel level, ParticleOptions particle, Vec3 center,
                            double radius, int points, double speed) {
        for (int i = 0; i < points; i++) {
            double ang = (Math.PI * 2 * i) / points;
            double x = center.x + Math.cos(ang) * radius;
            double z = center.z + Math.sin(ang) * radius;
            spawn(level, particle, x, center.y, z, 1, 0.02, 0.02, 0.02, speed);
        }
    }

    /**
     * Nova ring &mdash; an outward-bursting horizontal ring. Particles are given an
     * outward velocity so the ring visually expands for one tick.
     */
    public static void novaRing(ServerLevel level, ParticleOptions particle, Vec3 center,
                                double radius, int points, double outwardSpeed) {
        for (int i = 0; i < points; i++) {
            double ang = (Math.PI * 2 * i) / points;
            double dx = Math.cos(ang), dz = Math.sin(ang);
            spawn(level, particle, center.x + dx * radius, center.y, center.z + dz * radius,
                    0, dx, 0.0, dz, outwardSpeed);
        }
    }

    /**
     * Ground-hugging shockwave: several concentric expanding rings drawn in one
     * call, great for slams and impact craters.
     */
    public static void shockwave(ServerLevel level, ParticleOptions particle, Vec3 center,
                                 double maxRadius, int rings) {
        for (int r = 1; r <= rings; r++) {
            double radius = maxRadius * r / rings;
            int points = (int) Math.max(8, radius * 6);
            ring(level, particle, center.add(0, 0.1, 0), radius, points, 0.0);
        }
    }

    // ------------------------------------------------------------------
    // 4. Spirals, vortices, pillars & orbits
    // ------------------------------------------------------------------

    /** Ascending helix of particles around a vertical axis. */
    public static void spiral(ServerLevel level, ParticleOptions particle, Vec3 base,
                              double radius, double height, double turns, int points) {
        for (int i = 0; i < points; i++) {
            double t = (double) i / points;
            double ang = turns * Math.PI * 2 * t;
            double x = base.x + Math.cos(ang) * radius;
            double z = base.z + Math.sin(ang) * radius;
            double y = base.y + height * t;
            spawn(level, particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Inward-collapsing funnel of particles (radius shrinks as it rises). */
    public static void vortex(ServerLevel level, ParticleOptions particle, Vec3 base,
                              double radius, double height, double turns, int points) {
        for (int i = 0; i < points; i++) {
            double t = (double) i / points;
            double ang = turns * Math.PI * 2 * t;
            double r = radius * (1.0 - t);
            double x = base.x + Math.cos(ang) * r;
            double z = base.z + Math.sin(ang) * r;
            double y = base.y + height * t;
            spawn(level, particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.02);
        }
    }

    /** Vertical pillar of particles (smite marker / beam-down / strike telegraph). */
    public static void pillar(ServerLevel level, ParticleOptions particle, Vec3 base,
                              double height, int perBlock, double jitter) {
        int steps = Math.max(1, (int) (height * perBlock));
        for (int i = 0; i <= steps; i++) {
            double y = base.y + height * i / steps;
            spawn(level, particle, base.x, y, base.z, 1, jitter, 0.02, jitter, 0.0);
        }
    }

    /**
     * Single-frame orbital ring around a point at a given phase; call repeatedly
     * with an advancing {@code phase} to animate a charged/channeling look.
     */
    public static void orbit(ServerLevel level, ParticleOptions particle, Vec3 center,
                             double radius, double yOffset, int points, double phase) {
        for (int i = 0; i < points; i++) {
            double ang = phase + (Math.PI * 2 * i) / points;
            double x = center.x + Math.cos(ang) * radius;
            double z = center.z + Math.sin(ang) * radius;
            spawn(level, particle, x, center.y + yOffset, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ------------------------------------------------------------------
    // 5. Domes & domain expansion
    // ------------------------------------------------------------------

    /**
     * Hollow particle shell (full sphere or upper hemisphere) distributed evenly
     * with a golden-angle spiral. Call each tick with a growing {@code radius} to
     * animate an expanding sphere.
     */
    public static void domeShell(ServerLevel level, ParticleOptions particle, Vec3 center,
                                 double radius, int points, boolean hemisphere) {
        for (int i = 0; i < points; i++) {
            double y = hemisphere
                    ? (double) i / (points - 1)              // 0 .. 1 (upper half)
                    : 1.0 - 2.0 * i / (points - 1);          // 1 .. -1 (full sphere)
            double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
            double theta = GOLDEN_ANGLE * i;
            double x = Math.cos(theta) * r;
            double z = Math.sin(theta) * r;
            spawn(level, particle, center.x + x * radius, center.y + y * radius, center.z + z * radius,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Showcase domain-expansion visual: a few nested sphere shells (the "expansion"
     * snapshot) plus a lingering ground ring and a themed boom. Reusable directly
     * by Powers2 domains.
     *
     * <p>For a smoothly <i>animated</i> expansion, instead call {@link #domeShell}
     * once per tick with {@code radius} ramping from 0 to {@code maxRadius} and then
     * keep {@link #ring}-ing the perimeter while the domain persists.</p>
     */
    public static void domainExpansion(ServerLevel level, VfxElement element, Vec3 center, double maxRadius) {
        VfxElement.Theme t = element.theme();
        // Three snapshot shells imply outward growth.
        domeShell(level, t.core(), center, maxRadius * 0.45, 70, false);
        domeShell(level, t.trail(), center, maxRadius * 0.72, 110, false);
        domeShell(level, t.core(), center, maxRadius, 150, false);
        // Bright vertical core flash + lingering perimeter ring on the floor.
        burst(level, t.trail(), center.add(0, 1.0, 0), 60, maxRadius * 0.3, 0.04);
        ring(level, t.core(), center.add(0, 0.15, 0), maxRadius, (int) (maxRadius * 8), 0.0);
        playAt(level, center, t.cast(), 1.8f, t.pitch() * 0.85f);
        playAt(level, center, SoundEvents.BEACON_ACTIVATE, 1.2f, 0.6f);
        if (com.political.sound.VfxSounds.VFX_DOMAIN_WALL != null) {
            playAt(level, center, com.political.sound.VfxSounds.VFX_DOMAIN_WALL, 1.3f, 0.75f);
        }
    }

    /**
     * Lightweight per-tick pulse for an <i>active</i> domain: a slowly rotating
     * perimeter ring plus a few rising core wisps. Cheap enough to call every tick.
     */
    public static void domainPulse(ServerLevel level, VfxElement element, Vec3 center,
                                   double radius, double phase) {
        VfxElement.Theme t = element.theme();
        orbit(level, t.core(), center, radius, 0.3, (int) (radius * 2), phase);
        burst(level, t.trail(), center.add(0, 1.2, 0), 6, radius * 0.4, 0.02);
    }

    // ------------------------------------------------------------------
    // 6. Ground decals
    // ------------------------------------------------------------------

    /**
     * Radial ground cracks &mdash; {@code spokes} jagged lines fanning outward from
     * {@code center} along the floor. Great for earth-shatter traps and slams.
     */
    public static void groundCrack(ServerLevel level, ParticleOptions particle, Vec3 center,
                                   double length, int spokes) {
        RandomSource rng = level.getRandom();
        for (int s = 0; s < spokes; s++) {
            double baseAng = (Math.PI * 2 * s) / spokes + (rng.nextDouble() - 0.5) * 0.4;
            double dx = Math.cos(baseAng), dz = Math.sin(baseAng);
            int steps = Math.max(2, (int) (length * 2));
            for (int i = 1; i <= steps; i++) {
                double d = length * i / steps;
                double wobble = (rng.nextDouble() - 0.5) * 0.3;
                double x = center.x + dx * d - dz * wobble;
                double z = center.z + dz * d + dx * wobble;
                spawn(level, particle, x, center.y + 0.1, z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
    }

    // ------------------------------------------------------------------
    // 7. Combat feedback: hit / crit / ferocity
    // ------------------------------------------------------------------

    private static Vec3 chestOf(Entity e) {
        return new Vec3(e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ());
    }

    /** Generic element-themed impact spark on a struck entity (plus impact sound). */
    public static void hitSpark(ServerLevel level, Entity target, VfxElement element) {
        VfxElement.Theme t = element.theme();
        Vec3 at = chestOf(target);
        burst(level, t.spark(), at, 12, 0.3, 0.1);
        burst(level, t.trail(), at, 4, 0.25, 0.05);
        playAt(level, at, t.impact(), 0.7f, t.pitch());
    }

    /**
     * Critical-hit feedback: a bright burst of crit sparks scaled by how hard the
     * crit landed, plus the vanilla crit chime. Designed to be dropped straight into
     * {@code AbilityEngine#applyCritAndFerocity}.
     *
     * @param multiplier crit damage multiplier (e.g. {@code critDamage/100}); scales intensity
     */
    public static void critSpark(ServerLevel level, Entity target, double multiplier) {
        Vec3 at = chestOf(target);
        int count = (int) Math.min(60, 14 + multiplier * 8);
        burst(level, ParticleTypes.CRIT, at, count, 0.4, 0.18);
        burst(level, ParticleTypes.ENCHANTED_HIT, at, count / 2, 0.3, 0.12);
        float pitch = (float) Math.min(1.6, 0.9 + multiplier * 0.15);
        playAt(level, at, SoundEvents.PLAYER_ATTACK_CRIT, 1.1f, pitch);
    }

    /**
     * Ferocity feedback: one quick slash-spark fan per extra hit. Call from the
     * ferocity loop in {@code AbilityEngine} with the number of bonus hits.
     */
    public static void ferocitySpark(ServerLevel level, Entity target, int extraHits) {
        if (extraHits <= 0) return;
        Vec3 at = chestOf(target);
        for (int i = 0; i < extraHits; i++) {
            burst(level, ParticleTypes.SWEEP_ATTACK, at, 1, 0.2, 0.0);
            burst(level, ParticleTypes.CRIT, at, 6, 0.3, 0.15);
        }
        playAt(level, at, SoundEvents.PLAYER_ATTACK_SWEEP, 0.9f, 1.2f);
    }

    /**
     * General damage feedback used by the combat system: intensity 0..1 scales the
     * particle count, element drives the look. Cheap, no sound spam.
     */
    public static void damageFeedback(ServerLevel level, Entity target, VfxElement element, double intensity) {
        VfxElement.Theme t = element.theme();
        int count = (int) Math.min(40, 4 + intensity * 30);
        burst(level, t.spark(), chestOf(target), count, 0.3, 0.1);
    }

    // ------------------------------------------------------------------
    // 8. Auras, level-up & boss phases
    // ------------------------------------------------------------------

    /** Rising swirl of element particles around an entity (buff / charged state). */
    public static void auraColumn(ServerLevel level, Entity entity, VfxElement element, double phase) {
        VfxElement.Theme t = element.theme();
        Vec3 base = entity.position();
        double h = Math.max(1.0, entity.getBbHeight());
        orbit(level, t.core(), base.add(0, 0.1, 0), entity.getBbWidth() * 0.8 + 0.4, 0.1, 10, phase);
        orbit(level, t.trail(), base.add(0, h * 0.5, 0), entity.getBbWidth() * 0.6 + 0.3, 0.0, 8, -phase * 1.3);
        burst(level, t.trail(), base.add(0, h, 0), 3, 0.2, 0.01);
    }

    /** Celebratory level-up / awakening burst: upward column, halo ring, chime. */
    public static void levelUpBurst(ServerLevel level, Entity entity) {
        Vec3 base = entity.position();
        pillar(level, ParticleTypes.END_ROD, base, entity.getBbHeight() + 1.5, 6, 0.15);
        ring(level, ParticleTypes.GLOW, base.add(0, 0.1, 0), 1.2, 24, 0.05);
        spiral(level, ParticleTypes.HAPPY_VILLAGER, base, 1.0, entity.getBbHeight() + 1.0, 2.5, 40);
        playAt(level, base, SoundEvents.BEACON_ACTIVATE, 1.0f, 1.4f);
        playAt(level, base, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    /**
     * Dramatic boss phase-transition / enrage burst: a themed dome flash, expanding
     * shockwave, vertical pillar, and a roar. Call when a boss spawns or changes phase.
     */
    public static void bossPhaseBurst(ServerLevel level, VfxElement element, Vec3 center) {
        VfxElement.Theme t = element.theme();
        domeShell(level, t.core(), center.add(0, 1.0, 0), 3.5, 90, true);
        shockwave(level, t.trail(), center, 6.0, 3);
        pillar(level, t.core(), center, 8.0, 4, 0.2);
        burst(level, t.spark(), center.add(0, 1.5, 0), 40, 1.5, 0.15);
        playAt(level, center, SoundEvents.ENDER_DRAGON_GROWL, 1.6f, 0.7f);
        playAt(level, center, t.cast(), 1.4f, t.pitch() * 0.8f);
    }

    // ------------------------------------------------------------------
    // 9. Element-themed convenience wrappers
    // ------------------------------------------------------------------

    /** Themed point burst with a cast sound &mdash; the everyday "this power fired" effect. */
    public static void elementBurst(ServerLevel level, VfxElement element, Vec3 at, double scale) {
        VfxElement.Theme t = element.theme();
        burst(level, t.core(), at, (int) (30 * scale), 0.6 * scale, 0.05);
        burst(level, t.trail(), at, (int) (15 * scale), 0.8 * scale, 0.03);
        playAt(level, at, t.cast(), 1.0f, t.pitch());
    }

    /** Themed beam (e.g. eye-beams, bolts) with a cast sound at the origin. */
    public static void elementBeam(ServerLevel level, VfxElement element, Vec3 origin, Vec3 dir, double length) {
        VfxElement.Theme t = element.theme();
        beam(level, t.core(), origin, dir, length, 0.5, 2, 0.05);
        playAt(level, origin, t.cast(), 1.1f, t.pitch());
    }

    /** Themed frontal cone (e.g. flamethrower, frost breath, war cry). */
    public static void elementCone(ServerLevel level, VfxElement element, Vec3 origin, Vec3 dir,
                                   double length, double halfAngleDeg) {
        VfxElement.Theme t = element.theme();
        cone(level, t.core(), origin, dir, length, halfAngleDeg, 8, 8);
        playAt(level, origin, t.cast(), 1.0f, t.pitch());
    }

    /** Themed expanding nova ring (e.g. AoE pulse). */
    public static void elementNova(ServerLevel level, VfxElement element, Vec3 center, double radius) {
        VfxElement.Theme t = element.theme();
        novaRing(level, t.core(), center, radius, (int) (radius * 10), 0.4);
        burst(level, t.trail(), center.add(0, 0.5, 0), 30, radius * 0.4, 0.05);
        playAt(level, center, t.cast(), 1.2f, t.pitch());
    }

    /** Themed motion trail between two points (dashes, projectiles, blinks). */
    public static void elementTrail(ServerLevel level, VfxElement element, Vec3 from, Vec3 to) {
        sparkleTrail(level, element.trail(), from, to);
    }

    // ==================================================================
    //  EXPANSION C — maximal VFX routines
    //  (reimagined originally from alexscaves particle *behaviours*; all
    //   server-driven through sendParticles, mixin-free, additive only).
    //  None of the following remove or change any method above.
    // ==================================================================

    /** Builds an orthonormal basis {@code [forward, right, up]} from a direction. */
    private static Vec3[] basis(Vec3 dir) {
        Vec3 f = dir.normalize();
        Vec3 ref = Math.abs(f.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = f.cross(ref).normalize();
        Vec3 up = right.cross(f).normalize();
        return new Vec3[]{f, right, up};
    }

    // ------------------------------------------------------------------
    // 10. Lightning, arcs & chains
    // ------------------------------------------------------------------

    /**
     * Jagged lightning bolt: a perturbed polyline from {@code from} to {@code to}.
     * {@code jaggedness} (≈0..2) scales the lateral kink amplitude; the final node
     * always lands exactly on {@code to}.
     */
    public static void lightningBolt(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to,
                                     int segments, double jaggedness) {
        if (level == null) return;
        int segs = Math.max(1, segments);
        RandomSource rng = level.getRandom();
        Vec3 axis = to.subtract(from);
        double len = axis.length();
        if (len < 1.0e-4) return;
        Vec3[] b = basis(axis);
        Vec3 dir = b[0], right = b[1], up = b[2];
        Vec3 prev = from;
        for (int i = 1; i <= segs; i++) {
            double t = (double) i / segs;
            double mag = (i == segs) ? 0.0 : jaggedness * len * 0.12;
            double a = (rng.nextDouble() - 0.5) * 2 * mag;
            double c = (rng.nextDouble() - 0.5) * 2 * mag;
            Vec3 node = from.add(dir.scale(len * t)).add(right.scale(a)).add(up.scale(c));
            beamBetween(level, particle, prev, node, 3.0, 0.02);
            prev = node;
        }
    }

    /** Lightning bolt with {@code forks} random branches splitting off the main channel. */
    public static void forkedLightning(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to,
                                       int segments, int forks) {
        if (level == null) return;
        lightningBolt(level, particle, from, to, segments, 1.0);
        RandomSource rng = level.getRandom();
        Vec3 axis = to.subtract(from);
        double len = axis.length();
        for (int f = 0; f < forks; f++) {
            double t = 0.25 + rng.nextDouble() * 0.55;
            Vec3 start = from.add(axis.scale(t));
            Vec3 end = start.add(new Vec3(
                    (rng.nextDouble() - 0.5) * len * 0.5,
                    (rng.nextDouble() - 0.5) * len * 0.35,
                    (rng.nextDouble() - 0.5) * len * 0.5));
            lightningBolt(level, particle, start, end, Math.max(2, segments / 2), 1.3);
        }
    }

    /** Chain-lightning: jagged arcs hopping {@code source → t0 → t1 → …} with a spark at each node. */
    public static void chainLightning(ServerLevel level, ParticleOptions particle, Vec3 source, List<Vec3> targets) {
        if (level == null || targets == null) return;
        Vec3 prev = source;
        for (Vec3 tgt : targets) {
            if (tgt == null) continue;
            lightningBolt(level, particle, prev, tgt, 6, 0.9);
            burst(level, particle, tgt, 6, 0.2, 0.05);
            prev = tgt;
        }
    }

    /** Star-burst chain web: separate jagged arcs fanning from one source to every target. */
    public static void chainLightningWeb(ServerLevel level, ParticleOptions particle, Vec3 source, Vec3... targets) {
        if (level == null || targets == null) return;
        for (Vec3 tgt : targets) {
            if (tgt == null) continue;
            lightningBolt(level, particle, source, tgt, 5, 0.8);
        }
        burst(level, particle, source, 10, 0.2, 0.05);
    }

    /** Sagging arc/tether between two points (parabolic droop along −Y); great for whips and links. */
    public static void arc(ServerLevel level, ParticleOptions particle, Vec3 a, Vec3 b, double sag, int points) {
        int pts = Math.max(1, points);
        for (int i = 0; i <= pts; i++) {
            double t = (double) i / pts;
            double droop = sag * 4.0 * t * (1.0 - t);
            Vec3 p = a.add(b.subtract(a).scale(t)).add(0, -droop, 0);
            spawn(level, particle, p.x, p.y, p.z, 1, 0.01, 0.01, 0.01, 0.0);
        }
    }

    // ------------------------------------------------------------------
    // 11. Black hole, implosion, twin-orb collide & meteor
    // ------------------------------------------------------------------

    /**
     * Black-hole implosion snapshot: a swarm of {@code swirl} particles given a
     * strong INWARD velocity around {@code center} (accretion), plus a dense
     * {@code core}. Call repeatedly while charging, then {@link #implosionFlash}
     * on collapse.
     */
    public static void blackHoleImplosion(ServerLevel level, ParticleOptions swirl, ParticleOptions core,
                                          Vec3 center, double radius, int points) {
        if (level == null) return;
        RandomSource rng = level.getRandom();
        for (int i = 0; i < points; i++) {
            double t = (double) i / Math.max(1, points);
            double ang = GOLDEN_ANGLE * i;
            double r = radius * Math.sqrt(1.0 - t);
            double y = (rng.nextDouble() - 0.5) * radius * 0.6;
            double x = Math.cos(ang) * r;
            double z = Math.sin(ang) * r;
            double inward = -0.22;
            spawn(level, swirl, center.x + x, center.y + y, center.z + z,
                    0, x * inward, y * inward * 0.5, z * inward, 0.2);
        }
        burst(level, core, center, 30, 0.15, 0.0);
    }

    /** Outward detonation after an implosion: inner nova, full sphere shell and a dense core flash (no sound). */
    public static void implosionFlash(ServerLevel level, ParticleOptions particle, Vec3 center, double radius) {
        if (level == null) return;
        novaRing(level, particle, center, radius * 0.3, (int) Math.max(8, radius * 8), 0.6);
        domeShell(level, particle, center, radius, 120, false);
        burst(level, particle, center, 80, 0.1, 0.4);
    }

    /**
     * Two energy orbs streaking in from {@code a} and {@code b} to their midpoint,
     * colliding into a burst, then firing a {@code beam} outward along {@code beamDir}.
     */
    public static void twinOrbCollide(ServerLevel level, ParticleOptions orb, ParticleOptions beamP,
                                      Vec3 a, Vec3 b, Vec3 beamDir, double beamLength) {
        Vec3 mid = a.add(b).scale(0.5);
        sparkleTrail(level, orb, a, mid);
        sparkleTrail(level, orb, b, mid);
        burst(level, orb, mid, 40, 0.2, 0.1);
        beam(level, beamP, mid, beamDir, beamLength, 0.4, 2, 0.05);
    }

    /** Falling meteor streak from {@code from} to {@code to} with a fiery trail and impact splash. */
    public static void meteor(ServerLevel level, ParticleOptions head, ParticleOptions trail, Vec3 from, Vec3 to) {
        sparkleTrail(level, trail, from, to);
        beamBetween(level, head, from, to, 1.5, 0.1);
        burst(level, head, to, 50, 0.4, 0.2);
    }

    // ------------------------------------------------------------------
    // 12. Sword slashes & crescent arcs
    // ------------------------------------------------------------------

    /**
     * Crescent sword-slash arc in front of {@code origin}. The slash sweeps
     * {@code arcDeg} degrees at {@code radius} in a plane facing {@code dir},
     * rotated about the look axis by {@code tiltDeg} (so diagonal swings read right).
     */
    public static void swordSlash(ServerLevel level, ParticleOptions particle, Vec3 origin, Vec3 dir,
                                  double radius, double arcDeg, double tiltDeg, int points) {
        Vec3[] b = basis(dir);
        Vec3 forward = b[0], right = b[1], up = b[2];
        Vec3 center = origin.add(forward.scale(radius * 0.4));
        double half = Math.toRadians(arcDeg) * 0.5;
        double tilt = Math.toRadians(tiltDeg);
        Vec3 axisR = right.scale(Math.cos(tilt)).add(up.scale(Math.sin(tilt)));
        Vec3 axisU = right.scale(-Math.sin(tilt)).add(up.scale(Math.cos(tilt)));
        int pts = Math.max(2, points);
        for (int i = 0; i <= pts; i++) {
            double ang = -half + (2 * half) * i / pts;
            Vec3 p = center.add(axisR.scale(Math.cos(ang) * radius)).add(axisU.scale(Math.sin(ang) * radius));
            spawn(level, particle, p.x, p.y, p.z, 1, 0.01, 0.01, 0.01, 0.0);
        }
    }

    /** Two crossing slashes (an "X" finisher) sharing an origin/direction. */
    public static void crossSlash(ServerLevel level, ParticleOptions particle, Vec3 origin, Vec3 dir, double radius) {
        swordSlash(level, particle, origin, dir, radius, 150, 35, 26);
        swordSlash(level, particle, origin, dir, radius, 150, -35, 26);
    }

    // ------------------------------------------------------------------
    // 13. Channel / sustained beams & helices
    // ------------------------------------------------------------------

    /**
     * Sustained channel beam: a steady {@code core} line plus a bright {@code pulse}
     * travelling along it. Advance {@code phase} (0..1 looping) each tick for a
     * "charging laser" look.
     */
    public static void channelBeam(ServerLevel level, ParticleOptions core, ParticleOptions pulse,
                                   Vec3 origin, Vec3 dir, double length, double phase) {
        beam(level, core, origin, dir, length, 0.4, 1, 0.04);
        Vec3 d = dir.normalize();
        double tp = ((phase % 1.0) + 1.0) % 1.0;
        Vec3 at = origin.add(d.scale(length * tp));
        burst(level, pulse, at, 8, 0.12, 0.02);
    }

    /** Double/triple-helix beam wound around a forward axis (DNA-strand laser). */
    public static void helixBeam(ServerLevel level, ParticleOptions particle, Vec3 origin, Vec3 dir,
                                 double length, double radius, double turns, int strands, int points) {
        Vec3[] b = basis(dir);
        Vec3 forward = b[0], right = b[1], up = b[2];
        int pts = Math.max(2, points);
        int str = Math.max(1, strands);
        for (int s = 0; s < str; s++) {
            double phase0 = (Math.PI * 2 * s) / str;
            for (int i = 0; i <= pts; i++) {
                double t = (double) i / pts;
                double ang = phase0 + turns * Math.PI * 2 * t;
                Vec3 p = origin.add(forward.scale(length * t))
                        .add(right.scale(Math.cos(ang) * radius))
                        .add(up.scale(Math.sin(ang) * radius));
                spawn(level, particle, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    // ------------------------------------------------------------------
    // 14. Ground runes, sigils & extra dome shapes
    // ------------------------------------------------------------------

    /**
     * Rotating ground rune circle: an outer + inner ring with {@code glyphs}
     * evenly-spaced glyph clusters on the perimeter. Advance {@code phase} to spin it.
     */
    public static void runeCircle(ServerLevel level, ParticleOptions ringP, ParticleOptions glyph,
                                  Vec3 center, double radius, int glyphs, double phase) {
        int ringPts = (int) Math.max(24, radius * 10);
        ring(level, ringP, center.add(0, 0.05, 0), radius, ringPts, 0.0);
        ring(level, ringP, center.add(0, 0.05, 0), radius * 0.72, (int) Math.max(16, ringPts * 0.72), 0.0);
        int g = Math.max(1, glyphs);
        for (int i = 0; i < g; i++) {
            double a = phase + (Math.PI * 2 * i) / g;
            double x = center.x + Math.cos(a) * radius * 0.86;
            double z = center.z + Math.sin(a) * radius * 0.86;
            burst(level, glyph, new Vec3(x, center.y + 0.1, z), 3, 0.05, 0.0);
        }
    }

    /**
     * Star-polygon sigil ({@code n}/{@code skip} schläfli) inscribed in a circle on
     * the floor; e.g. {@code n=5, skip=2} draws a pentagram.
     */
    public static void sigil(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int n, int skip) {
        int pts = Math.max(3, n);
        Vec3[] verts = new Vec3[pts];
        for (int i = 0; i < pts; i++) {
            double a = (Math.PI * 2 * i) / pts - Math.PI / 2;
            verts[i] = new Vec3(center.x + Math.cos(a) * radius, center.y + 0.05, center.z + Math.sin(a) * radius);
        }
        int step = Math.max(1, skip);
        int idx = 0;
        for (int i = 0; i < pts; i++) {
            int next = (idx + step) % pts;
            beamBetween(level, particle, verts[idx], verts[next], 4.0, 0.01);
            idx = next;
        }
        ring(level, particle, center.add(0, 0.05, 0), radius, (int) Math.max(24, radius * 10), 0.0);
    }

    /** A single latitude "band" ring of a sphere at height fraction {@code yFrac} (−1..1). */
    public static void domeBand(ServerLevel level, ParticleOptions particle, Vec3 center,
                                double radius, double yFrac, int points) {
        double y = Math.max(-1.0, Math.min(1.0, yFrac));
        double r = Math.sqrt(Math.max(0.0, 1.0 - y * y)) * radius;
        ring(level, particle, center.add(0, y * radius, 0), r, Math.max(8, points), 0.0);
    }

    /** Vertical "ribs" of a dome: {@code ribs} half-circle arcs from base to apex. */
    public static void domeRibs(ServerLevel level, ParticleOptions particle, Vec3 center,
                                double radius, int ribs, int pointsPerRib) {
        int rb = Math.max(1, ribs);
        int pp = Math.max(2, pointsPerRib);
        for (int s = 0; s < rb; s++) {
            double az = (Math.PI * 2 * s) / rb;
            double cx = Math.cos(az), cz = Math.sin(az);
            for (int i = 0; i <= pp; i++) {
                double phi = (Math.PI * 0.5) * i / pp;
                double r = Math.cos(phi) * radius;
                double y = Math.sin(phi) * radius;
                spawn(level, particle, center.x + cx * r, center.y + y, center.z + cz * r, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    // ------------------------------------------------------------------
    // 15. Debris, craters & ground slams
    // ------------------------------------------------------------------

    /** Outward debris burst with an upward bias (rubble kicked up from an impact). */
    public static void debrisBurst(ServerLevel level, ParticleOptions particle, Vec3 center, int count, double power) {
        if (level == null) return;
        RandomSource rng = level.getRandom();
        for (int i = 0; i < count; i++) {
            double dx = (rng.nextDouble() - 0.5) * 2;
            double dy = rng.nextDouble() * 1.0 + 0.2;
            double dz = (rng.nextDouble() - 0.5) * 2;
            Vec3 v = new Vec3(dx, dy, dz).normalize().scale(power);
            spawn(level, particle, center.x, center.y + 0.2, center.z, 0, v.x, v.y, v.z, 1.0);
        }
    }

    /** Full impact crater: concentric dust shockwave + kicked-up debris + central dust puff. */
    public static void impactCrater(ServerLevel level, ParticleOptions dust, ParticleOptions debris,
                                    Vec3 center, double radius) {
        shockwave(level, dust, center, radius, 3);
        debrisBurst(level, debris, center, (int) Math.max(6, radius * 8), 0.4);
        burst(level, dust, center.add(0, 0.2, 0), (int) Math.max(10, radius * 10), radius * 0.3, 0.05);
    }

    // ------------------------------------------------------------------
    // 16. Lingering domain walls & ceilings
    // ------------------------------------------------------------------

    /**
     * Lingering cylindrical domain wall: stacked perimeter rings with a vertical
     * shimmer driven by {@code phase}. Cheap enough to call each tick while a
     * domain persists.
     */
    public static void domainWall(ServerLevel level, ParticleOptions particle, Vec3 center,
                                  double radius, double height, double phase) {
        int around = (int) Math.max(16, radius * 8);
        int rows = (int) Math.max(2, height * 2);
        for (int r = 0; r < rows; r++) {
            double y = center.y + height * r / rows;
            double wobble = Math.sin(phase + r * 0.6) * 0.06;
            ring(level, particle, new Vec3(center.x, y, center.z), radius + wobble, around, 0.0);
        }
    }

    /** Domed ceiling cap (upper hemisphere) sitting {@code height} above the floor centre. */
    public static void domainCeiling(ServerLevel level, ParticleOptions particle, Vec3 center,
                                     double radius, double height) {
        domeShell(level, particle, center.add(0, height, 0), radius, 120, true);
    }

    // ------------------------------------------------------------------
    // 17. Extra element-themed wrappers (build on the new routines above)
    // ------------------------------------------------------------------

    /** Themed forked lightning strike between two points. */
    public static void elementLightning(ServerLevel level, VfxElement element, Vec3 from, Vec3 to) {
        VfxElement.Theme t = element.theme();
        forkedLightning(level, t.core(), from, to, 8, 2);
        burst(level, t.spark(), to, 12, 0.3, 0.1);
        playAt(level, from, t.cast(), 1.1f, t.pitch());
    }

    /** Themed chain lightning hopping across a list of targets. */
    public static void elementChain(ServerLevel level, VfxElement element, Vec3 source, List<Vec3> targets) {
        chainLightning(level, element.core(), source, targets);
        playAt(level, source, element.castSound(), 1.0f, element.pitch());
    }

    /** Themed black-hole charge (inward accretion). Follow with {@link #elementBlackHoleCollapse}. */
    public static void elementBlackHole(ServerLevel level, VfxElement element, Vec3 center, double radius) {
        VfxElement.Theme t = element.theme();
        blackHoleImplosion(level, t.trail(), t.core(), center, radius, 160);
        playAt(level, center, t.cast(), 1.2f, t.pitch() * 0.7f);
    }

    /** Themed black-hole collapse detonation. */
    public static void elementBlackHoleCollapse(ServerLevel level, VfxElement element, Vec3 center, double radius) {
        VfxElement.Theme t = element.theme();
        implosionFlash(level, t.core(), center, radius);
        burst(level, t.spark(), center, 60, 0.2, 0.3);
        playAt(level, center, SoundEvents.WARDEN_SONIC_BOOM, 1.4f, 0.8f);
        playAt(level, center, t.cast(), 1.2f, t.pitch() * 0.6f);
    }

    /** Themed single crescent slash. */
    public static void elementSlash(ServerLevel level, VfxElement element, Vec3 origin, Vec3 dir, double radius) {
        VfxElement.Theme t = element.theme();
        swordSlash(level, t.core(), origin, dir, radius, 140, 25, 28);
        swordSlash(level, t.trail(), origin, dir, radius * 0.9, 130, 25, 22);
        playAt(level, origin, t.cast(), 1.0f, t.pitch() * 1.1f);
    }

    /** Themed crossing "X" slash finisher. */
    public static void elementCrossSlash(ServerLevel level, VfxElement element, Vec3 origin, Vec3 dir, double radius) {
        VfxElement.Theme t = element.theme();
        crossSlash(level, t.core(), origin, dir, radius);
        burst(level, t.spark(), origin.add(dir.normalize().scale(radius * 0.5)), 20, 0.3, 0.15);
        playAt(level, origin, t.cast(), 1.1f, t.pitch());
    }

    /** Themed sustained channel beam; advance {@code phase} per tick. */
    public static void elementChannel(ServerLevel level, VfxElement element, Vec3 origin, Vec3 dir,
                                      double length, double phase) {
        VfxElement.Theme t = element.theme();
        channelBeam(level, t.core(), t.spark(), origin, dir, length, phase);
    }

    /** Themed double-helix beam. */
    public static void elementHelixBeam(ServerLevel level, VfxElement element, Vec3 origin, Vec3 dir, double length) {
        VfxElement.Theme t = element.theme();
        helixBeam(level, t.core(), origin, dir, length, 0.4, 4, 2, 60);
        beam(level, t.trail(), origin, dir, length, 0.5, 1, 0.03);
        playAt(level, origin, t.cast(), 1.1f, t.pitch());
    }

    /** Themed rotating ground rune circle; advance {@code phase} to spin it. */
    public static void elementRuneCircle(ServerLevel level, VfxElement element, Vec3 center, double radius, double phase) {
        VfxElement.Theme t = element.theme();
        runeCircle(level, t.core(), t.trail(), center, radius, 8, phase);
    }

    /** Themed lingering domain wall + spinning floor rune (call each domain tick). */
    public static void elementDomainWall(ServerLevel level, VfxElement element, Vec3 center,
                                         double radius, double height, double phase) {
        VfxElement.Theme t = element.theme();
        domainWall(level, t.trail(), center, radius, height, phase);
        runeCircle(level, t.core(), t.trail(), center, radius, 12, phase * 0.5);
    }

    /** Themed falling meteor with impact. */
    public static void elementMeteor(ServerLevel level, VfxElement element, Vec3 from, Vec3 to) {
        VfxElement.Theme t = element.theme();
        meteor(level, t.core(), t.trail(), from, to);
        playAt(level, to, t.impact(), 1.2f, t.pitch() * 0.8f);
    }

    /** Themed twin-orb collision firing a beam. */
    public static void elementTwinOrb(ServerLevel level, VfxElement element, Vec3 a, Vec3 b,
                                      Vec3 beamDir, double beamLength) {
        VfxElement.Theme t = element.theme();
        twinOrbCollide(level, t.core(), t.trail(), a, b, beamDir, beamLength);
        playAt(level, a.add(b).scale(0.5), t.cast(), 1.2f, t.pitch());
    }

    // ------------------------------------------------------------------
    // 18. Sound category overload (Sound Physics Remastered friendliness)
    // ------------------------------------------------------------------

    /**
     * Play a sound at a position in an explicit {@link SoundSource} category.
     * Sound Physics Remastered applies reverb/occlusion to world sounds; routing
     * combat/power cues through {@code PLAYERS}/{@code HOSTILE}/{@code BLOCKS}
     * (rather than {@code MASTER}/{@code MUSIC}) lets it process them. See the
     * handoff manifest for category guidance.
     */
    public static void playAt(ServerLevel level, Vec3 at, SoundEvent sound, SoundSource source,
                              float volume, float pitch) {
        if (level == null || sound == null) return;
        level.playSound(null, at.x, at.y, at.z, sound, source, volume, pitch);
    }
}
