package com.political.expansion2.ranged;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public enum ParticleKind {
    FLAME(ParticleTypes.FLAME),
    BEAM(ParticleTypes.WITCH),
    HEART(ParticleTypes.HEART),
    SOUL(ParticleTypes.SOUL),
    ARCANE(ParticleTypes.ENCHANTED_HIT),
    FROST(ParticleTypes.SNOWFLAKE),
    WIND(ParticleTypes.CLOUD),
    STORM(ParticleTypes.ELECTRIC_SPARK),
    VOID(ParticleTypes.REVERSE_PORTAL),
    SHADOW(ParticleTypes.SQUID_INK),
    POISON(ParticleTypes.SNEEZE),
    HOLY(ParticleTypes.END_ROD),
    NATURE(ParticleTypes.HAPPY_VILLAGER),
    BLOOD(ParticleTypes.DAMAGE_INDICATOR),
    CRYSTAL(ParticleTypes.GLOW),
    METEOR(ParticleTypes.LAVA);

    public final ParticleOptions particle;

    ParticleKind(ParticleOptions particle) {
        this.particle = particle;
    }
}
