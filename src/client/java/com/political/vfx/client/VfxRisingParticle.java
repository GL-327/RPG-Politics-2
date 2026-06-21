package com.political.vfx.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * Lightweight animated quad particle backing the {@code com.political.vfx} custom
 * particle types. Built on the vanilla {@link SimpleAnimatedParticle} so it picks
 * up the MC&nbsp;26 quad render-state pipeline automatically (no custom render
 * mixin required).
 *
 * <p>Behaviour is parameterised by the {@link Provider}: tint, scale, lifetime,
 * gravity and whether the mote drifts upward. This is the originally-authored
 * reimplementation of the "floaty coloured ember/mote" behaviour common to many
 * effect mods — using our own textures and our own motion constants.</p>
 */
public class VfxRisingParticle extends SimpleAnimatedParticle {

    protected VfxRisingParticle(ClientLevel level, double x, double y, double z,
                                double xd, double yd, double zd, SpriteSet sprites,
                                int rgb, float scale, int lifetime, float gravity) {
        super(level, x, y, z, sprites, gravity);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.friction = 0.96F;
        this.lifetime = Math.max(1, lifetime);
        this.quadSize *= Math.max(0.05F, scale);
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        this.setColor(r, g, b);
        this.setSpriteFromAge(sprites);
    }

    /**
     * Parameterised provider. One instance is registered per custom particle type
     * in {@link VfxClientBootstrap}.
     */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final int rgb;
        private final float scale;
        private final int lifetime;
        private final float gravity;
        private final boolean rises;

        public Provider(SpriteSet sprites, int rgb, float scale, int lifetime, float gravity, boolean rises) {
            this.sprites = sprites;
            this.rgb = rgb;
            this.scale = scale;
            this.lifetime = lifetime;
            this.gravity = gravity;
            this.rises = rises;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd, RandomSource random) {
            double vy = rises ? Math.abs(yd) + 0.02 : yd;
            return new VfxRisingParticle(level, x, y, z, xd, vy, zd, sprites, rgb, scale, lifetime, gravity);
        }
    }
}
