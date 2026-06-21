package com.political.vfx;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Per-element visual themes for the {@link VfxHelper} library.
 *
 * <p>Each element bundles a coherent set of vanilla particles plus cast/impact
 * sounds so that every power, ability, trap, or boss phase of a given element
 * reads consistently to the player. All particles and sounds referenced here are
 * known-good in this code base (Minecraft 26.2, Mojang mappings) and are spawned
 * server-side via {@link net.minecraft.server.level.ServerLevel#sendParticles},
 * so the whole system is completely mixin-free.</p>
 *
 * <h2>Optional sound-agent hook</h2>
 * <p>Every {@link Theme} stores plain {@link SoundEvent} handles. When the sound
 * agent's {@code com.political.sound.ModSounds} (or similar) lands, the
 * integration agent can swap any of these for a custom event simply by passing it
 * to the {@code SoundEvent}-typed parameters on {@link VfxHelper} (e.g.
 * {@link VfxHelper#playAt}). {@code VfxHelper}/{@code VfxElement} intentionally do
 * <b>not</b> import or hard-depend on {@code ModSounds} so this package compiles
 * standalone. See {@code docs/expansion3/vfx.md} for the exact swap points.</p>
 */
public enum VfxElement {

    /** Searing fire / flame-thrower / meteor look. */
    FIRE(
            ParticleTypes.FLAME,
            ParticleTypes.LAVA,
            ParticleTypes.SMOKE,
            SoundEvents.FIRECHARGE_USE,
            SoundEvents.FIRECHARGE_USE,
            0xFF6A1A,
            1.0f),

    /** Icy frost / freeze look. */
    FROST(
            ParticleTypes.SNOWFLAKE,
            ParticleTypes.CLOUD,
            ParticleTypes.SNOWFLAKE,
            SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundEvents.AMETHYST_BLOCK_CHIME,
            0x9FE3FF,
            1.4f),

    /** Cursed / domain / void look. */
    VOID(
            ParticleTypes.REVERSE_PORTAL,
            ParticleTypes.PORTAL,
            ParticleTypes.SQUID_INK,
            SoundEvents.WARDEN_SONIC_BOOM,
            SoundEvents.WARDEN_SONIC_BOOM,
            0x7A2BD6,
            0.6f),

    /** Crackling lightning / storm look. */
    LIGHTNING(
            ParticleTypes.ELECTRIC_SPARK,
            ParticleTypes.END_ROD,
            ParticleTypes.CRIT,
            SoundEvents.WARDEN_SONIC_BOOM,
            SoundEvents.WARDEN_SONIC_BOOM,
            0xFFF066,
            1.2f),

    /** Radiant holy / smite / heal look. */
    HOLY(
            ParticleTypes.END_ROD,
            ParticleTypes.GLOW,
            ParticleTypes.ENCHANTED_HIT,
            SoundEvents.BEACON_ACTIVATE,
            SoundEvents.AMETHYST_BLOCK_CHIME,
            0xFFF4C2,
            1.1f),

    /** Verdant nature / poison-bloom look. */
    NATURE(
            ParticleTypes.HAPPY_VILLAGER,
            ParticleTypes.SPORE_BLOSSOM_AIR,
            ParticleTypes.COMPOSTER,
            SoundEvents.BEACON_AMBIENT,
            SoundEvents.BEACON_AMBIENT,
            0x6FCB3A,
            1.0f),

    /** Visceral blood / berserk / execute look. */
    BLOOD(
            ParticleTypes.DAMAGE_INDICATOR,
            ParticleTypes.CRIT,
            ParticleTypes.DAMAGE_INDICATOR,
            SoundEvents.RAVAGER_ROAR,
            SoundEvents.WARDEN_ROAR,
            0xB81B1B,
            0.8f),

    /** Mystic arcane / sorcery / enchant look. */
    ARCANE(
            ParticleTypes.ENCHANTED_HIT,
            ParticleTypes.WITCH,
            ParticleTypes.ENCHANT,
            SoundEvents.EVOKER_CAST_SPELL,
            SoundEvents.AMETHYST_BLOCK_CHIME,
            0xC06CFF,
            1.3f);

    /**
     * Immutable visual identity of an element.
     *
     * @param core    dense particle used for the main body of an effect
     * @param trail   lighter particle used for trails, halos, and lingering wisps
     * @param spark   short, snappy particle used for impacts / hit-sparks
     * @param cast    sound played when a power/ability of this element fires
     * @param impact  sound played when this element strikes a target
     * @param tintRgb packed 0xRRGGBB accent tint (used by callers/UI; particles are vanilla)
     * @param pitch   default pitch hint for cast/impact sounds
     */
    public record Theme(
            ParticleOptions core,
            ParticleOptions trail,
            ParticleOptions spark,
            SoundEvent cast,
            SoundEvent impact,
            int tintRgb,
            float pitch) {}

    private final Theme theme;

    VfxElement(ParticleOptions core, ParticleOptions trail, ParticleOptions spark,
               SoundEvent cast, SoundEvent impact, int tintRgb, float pitch) {
        this.theme = new Theme(core, trail, spark, cast, impact, tintRgb, pitch);
    }

    public Theme theme() {
        return theme;
    }

    public ParticleOptions core() {
        return theme.core();
    }

    public ParticleOptions trail() {
        return theme.trail();
    }

    public ParticleOptions spark() {
        return theme.spark();
    }

    public SoundEvent castSound() {
        return theme.cast();
    }

    public SoundEvent impactSound() {
        return theme.impact();
    }

    public int tintRgb() {
        return theme.tintRgb();
    }

    public float pitch() {
        return theme.pitch();
    }
}
