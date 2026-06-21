package com.political.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Additive sound events for the Workstream-C VFX library (Expansion C).
 *
 * <p>This is a <b>new, non-destructive</b> companion to {@link ModSounds}: it adds
 * a handful of dedicated cues for the big new {@code VfxHelper} routines
 * (chain-lightning, black-hole collapse, sustained channel, rune-circle hum,
 * domain-wall shimmer, meteor impact, sword-slash) without touching {@code ModSounds}
 * or its {@code sounds.json}. Definitions for these ids live in the fragment
 * {@code assets/politicalserver/sounds.vfx.json}; the integration agent merges that
 * fragment into the single {@code assets/politicalserver/sounds.json} the game reads
 * (Minecraft only loads one {@code sounds.json} per namespace — see the handoff
 * manifest). Until merged, the events register fine and simply have no audio mapped.</p>
 *
 * <p><b>Sound Physics Remastered:</b> all of these are intended to be played through
 * world categories ({@link SoundSource#PLAYERS} / {@link SoundSource#HOSTILE} /
 * {@link SoundSource#BLOCKS}) so SPR applies reverb &amp; occlusion. Use the
 * {@link #play} helpers (default {@code PLAYERS}) or {@code VfxHelper.playAt(..., SoundSource, ...)}.</p>
 */
public final class VfxSounds {

    public static final String MOD_ID = "politicalserver";

    private static final Map<String, SoundEvent> BY_ID = new LinkedHashMap<>();

    public static SoundEvent VFX_CHAIN_LIGHTNING;
    public static SoundEvent VFX_BLACK_HOLE_CHARGE;
    public static SoundEvent VFX_BLACK_HOLE_COLLAPSE;
    public static SoundEvent VFX_CHANNEL_LOOP;
    public static SoundEvent VFX_RUNE_HUM;
    public static SoundEvent VFX_DOMAIN_WALL;
    public static SoundEvent VFX_METEOR_IMPACT;
    public static SoundEvent VFX_SLASH;
    public static SoundEvent VFX_IMPLOSION;

    private VfxSounds() {}

    /** Registers every VFX sound event. Idempotent. Call during common mod init. */
    public static void register() {
        if (VFX_CHAIN_LIGHTNING != null) return;
        VFX_CHAIN_LIGHTNING = make("vfx_chain_lightning");
        VFX_BLACK_HOLE_CHARGE = make("vfx_black_hole_charge");
        VFX_BLACK_HOLE_COLLAPSE = make("vfx_black_hole_collapse");
        VFX_CHANNEL_LOOP = make("vfx_channel_loop");
        VFX_RUNE_HUM = make("vfx_rune_hum");
        VFX_DOMAIN_WALL = make("vfx_domain_wall");
        VFX_METEOR_IMPACT = make("vfx_meteor_impact");
        VFX_SLASH = make("vfx_slash");
        VFX_IMPLOSION = make("vfx_implosion");
    }

    private static SoundEvent make(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        SoundEvent registered = Registry.register(
                BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        BY_ID.put(name, registered);
        return registered;
    }

    /** Resolves a VFX sound by its short id; {@code null} if unknown / not yet registered. */
    public static SoundEvent byId(String name) {
        return BY_ID.get(name);
    }

    /** Immutable-ish view of every registered VFX sound, keyed by short id. */
    public static Map<String, SoundEvent> all() {
        return BY_ID;
    }

    /** Plays a VFX cue at a position in the PLAYERS category (SPR-friendly). */
    public static void play(ServerLevel level, Vec3 at, SoundEvent event, float volume, float pitch) {
        play(level, at, event, SoundSource.PLAYERS, volume, pitch);
    }

    /** Plays a VFX cue at a position in an explicit category (route world cues through PLAYERS/HOSTILE/BLOCKS for SPR). */
    public static void play(ServerLevel level, Vec3 at, SoundEvent event, SoundSource source, float volume, float pitch) {
        if (level == null || event == null) return;
        level.playSound(null, at.x, at.y, at.z, event, source, volume, pitch);
    }
}
