package com.political.client.tex;

import com.mojang.blaze3d.platform.NativeImage;
import com.political.client.model.Archetype;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates and registers per-creature entity textures at runtime, so every custom mob / cursed
 * spirit has a fitting, UV-aligned skin without shipping hand-painted PNG art.
 *
 * <p>A texture is painted directly onto the standard 64×64 humanoid UV layout from a family palette
 * (chosen by {@link Archetype}) plus a deterministic per-id hue/shade perturbation, giving each
 * creature its own look while keeping its family read. The finished {@link NativeImage} is wrapped in
 * a {@link DynamicTexture} and registered under the exact {@link Identifier} the renderers request
 * ({@code politicalserver:textures/entity/<id>.png}), so the render pipeline picks it up transparently.
 *
 * <p>Mixin-free and idempotent: each identifier is only built once.
 */
public final class ProceduralTextures {

    private static final Map<Identifier, Pending> PENDING = new LinkedHashMap<>();
    private static final Set<Identifier> UPLOADED = new HashSet<>();

    private record Pending(Archetype archetype, String seedKey) {}

    static {
        // DynamicTexture needs RenderSystem.getDevice(); client entrypoints run before GPU init.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> uploadPending());
    }

    private ProceduralTextures() {}

    /** Queues a procedural texture for upload once the render device is ready. Idempotent. */
    public static void register(Identifier id, Archetype archetype, String seedKey) {
        if (UPLOADED.contains(id) || PENDING.containsKey(id)) return;
        PENDING.put(id, new Pending(archetype, seedKey));
    }

    private static void uploadPending() {
        if (PENDING.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        List<Map.Entry<Identifier, Pending>> batch = new ArrayList<>(PENDING.entrySet());
        for (Map.Entry<Identifier, Pending> entry : batch) {
            Identifier id = entry.getKey();
            if (UPLOADED.contains(id)) continue;
            Pending pending = entry.getValue();
            Palette palette = paletteFor(pending.archetype(), pending.seedKey());
            NativeImage image = paint(pending.archetype(), palette);
            DynamicTexture texture = new DynamicTexture(id::toString, image);
            mc.getTextureManager().register(id, texture);
            UPLOADED.add(id);
            PENDING.remove(id);
        }
    }

    // ─────────────────────────────────── painting ───────────────────────────────────

    private static NativeImage paint(Archetype archetype, Palette p) {
        NativeImage img = new NativeImage(NativeImage.Format.RGBA, 64, 64, false);

        // 1) Base coat: limb/skin colour over the whole sheet with subtle vertical shading + grain.
        for (int y = 0; y < 64; y++) {
            float shade = 0.85f + 0.30f * (1.0f - y / 64.0f);
            for (int x = 0; x < 64; x++) {
                int n = hash(x * 374761393 + y * 668265263) & 0x0F;
                set(img, x, y, mul(p.skin, shade) , -8 + (n)); // tiny per-pixel grain
            }
        }

        // 2) Torso costume / armour / robe colour (body cube net: u16..39, v16..31 plus skirt rows).
        rect(img, 16, 16, 40, 48, p.torso, 10);
        // A vertical accent stripe down the chest (emblem / sash / spine).
        rect(img, 26, 20, 30, 44, p.accent, 0);

        // 3) Head: recolour the whole head net (u0..31, v0..15) to the head tone.
        rect(img, 0, 0, 32, 16, p.head, 8);

        // 4) Face on the head's front face (u8..15, v8..15): glowing eyes + a mouth line.
        rect(img, 8, 8, 16, 16, mul(p.head, 0.8f), 4);
        // Eyes.
        plot(img, 10, 11, p.glow);
        plot(img, 13, 11, p.glow);
        // Many-mouthed / many-eyed curses get extra staring eyes around the head net.
        if (archetype == Archetype.AMORPHOUS_CURSE || archetype == Archetype.SPECIAL_GRADE) {
            plot(img, 9, 9, p.glow);
            plot(img, 14, 9, p.glow);
            plot(img, 11, 13, p.glow);
            plot(img, 12, 13, p.glow);
            // a toothy mouth band on the torso, too
            for (int x = 18; x < 38; x += 2) plot(img, x, 30, p.glow);
        } else {
            // a simple mouth line
            for (int x = 11; x <= 13; x++) plot(img, x, 14, mul(p.head, 0.5f));
        }

        // 5) Family flourishes painted into the spare lower-left region (used by appendage UVs).
        switch (archetype) {
            case HERO_CAPED -> rect(img, 0, 32, 32, 50, p.accent, 6);            // cape uses accent
            case ELEMENTAL_BEING -> rect(img, 0, 32, 16, 44, p.glow, 0);          // shards glow
            case DEMON_FIEND -> rect(img, 0, 32, 16, 46, mul(p.accent, 0.7f), 6); // bat-wings
            case WINGED -> rect(img, 0, 32, 16, 48, mul(p.head, 0.9f), 6);        // feather wings
            case ARMORED_KNIGHT, CONSTRUCT_GOLEM -> rect(img, 0, 32, 16, 48, p.accent, 6); // plates
            default -> { /* base coat already covers appendage UVs */ }
        }
        // Horn/crest/antenna strip in the far corner (u56..63, v0..15) for headgear UVs.
        rect(img, 56, 0, 64, 16, p.accent, 6);

        return img;
    }

    // ─────────────────────────────────── palettes ───────────────────────────────────

    private record Palette(int skin, int head, int torso, int accent, int glow) {}

    private static Palette paletteFor(Archetype a, String seedKey) {
        int seed = seedKey == null ? 0 : seedKey.hashCode();
        // Deterministic per-id hue rotation + brightness wobble for intra-family variety.
        float hue = ((seed & 0xFF) / 255.0f - 0.5f) * 0.10f;
        float bri = 0.92f + ((seed >> 8 & 0xFF) / 255.0f) * 0.18f;

        Palette base = switch (a) {
            case GAUNT_HUMANOID -> new Palette(rgb(150, 140, 165), rgb(170, 160, 185), rgb(70, 60, 90), rgb(120, 40, 130), rgb(220, 80, 255));
            case HULKING_BRUTE  -> new Palette(rgb(120, 95, 110), rgb(135, 105, 120), rgb(60, 45, 60), rgb(150, 50, 60), rgb(255, 120, 90));
            case SERPENTINE     -> new Palette(rgb(70, 130, 120), rgb(90, 150, 135), rgb(40, 90, 85), rgb(180, 200, 90), rgb(150, 255, 200));
            case MULTI_ARMED    -> new Palette(rgb(150, 120, 70), rgb(170, 140, 85), rgb(90, 70, 40), rgb(210, 180, 90), rgb(255, 220, 120));
            case WINGED         -> new Palette(rgb(95, 110, 150), rgb(110, 125, 165), rgb(55, 65, 95), rgb(200, 210, 230), rgb(180, 220, 255));
            case QUADRUPED      -> new Palette(rgb(120, 90, 60), rgb(135, 100, 70), rgb(80, 60, 40), rgb(60, 45, 30), rgb(240, 200, 120));
            case CRAWLER        -> new Palette(rgb(70, 85, 55), rgb(85, 100, 65), rgb(45, 55, 35), rgb(120, 140, 60), rgb(180, 255, 120));
            case CLOAKED_SPIRIT -> new Palette(rgb(60, 55, 85), rgb(70, 65, 100), rgb(35, 30, 55), rgb(120, 60, 160), rgb(190, 120, 255));
            case BOSS_COLOSSUS  -> new Palette(rgb(70, 40, 45), rgb(85, 45, 50), rgb(40, 20, 25), rgb(180, 40, 40), rgb(255, 90, 60));
            case TINY_SWARM     -> new Palette(rgb(95, 120, 70), rgb(110, 135, 80), rgb(60, 80, 45), rgb(160, 190, 70), rgb(200, 255, 120));
            case AMORPHOUS_CURSE-> new Palette(rgb(165, 130, 140), rgb(180, 145, 150), rgb(110, 70, 85), rgb(150, 50, 80), rgb(255, 70, 120));
            case SPECIAL_GRADE  -> new Palette(rgb(120, 130, 140), rgb(140, 150, 160), rgb(70, 50, 55), rgb(160, 40, 40), rgb(255, 60, 60));
            case HERO_CAPED     -> new Palette(rgb(40, 70, 150), rgb(225, 195, 130), rgb(35, 60, 140), rgb(190, 40, 50), rgb(255, 230, 120));
            case ARMORED_KNIGHT -> new Palette(rgb(130, 135, 145), rgb(150, 155, 165), rgb(95, 100, 110), rgb(190, 170, 90), rgb(140, 200, 255));
            case UNDEAD_HUSK    -> new Palette(rgb(190, 185, 165), rgb(205, 200, 180), rgb(120, 115, 100), rgb(90, 80, 60), rgb(150, 255, 170));
            case DEMON_FIEND    -> new Palette(rgb(140, 45, 40), rgb(155, 55, 45), rgb(70, 25, 25), rgb(30, 20, 20), rgb(255, 140, 40));
            case CONSTRUCT_GOLEM-> new Palette(rgb(120, 120, 125), rgb(135, 135, 140), rgb(95, 95, 100), rgb(150, 130, 80), rgb(120, 220, 255));
            case ELEMENTAL_BEING-> new Palette(rgb(60, 90, 160), rgb(80, 120, 200), rgb(40, 60, 120), rgb(255, 150, 60), rgb(255, 220, 120));
        };
        return new Palette(
                shift(base.skin, hue, bri), shift(base.head, hue, bri),
                shift(base.torso, hue, bri), shift(base.accent, hue, bri), base.glow);
    }

    // ─────────────────────────────────── pixel helpers ───────────────────────────────────

    private static void rect(NativeImage img, int x0, int y0, int x1, int y1, int rgb, int grainAmt) {
        for (int y = Math.max(0, y0); y < Math.min(64, y1); y++) {
            for (int x = Math.max(0, x0); x < Math.min(64, x1); x++) {
                int n = grainAmt == 0 ? 0 : (hash(x * 9176 + y * 233 + rgb) % (grainAmt * 2 + 1)) - grainAmt;
                set(img, x, y, rgb, n);
            }
        }
    }

    private static void plot(NativeImage img, int x, int y, int rgb) {
        if (x >= 0 && x < 64 && y >= 0 && y < 64) set(img, x, y, rgb, 0);
    }

    /** Writes an opaque pixel (rgb + per-channel delta) in NativeImage's ABGR byte order. */
    private static void set(NativeImage img, int x, int y, int rgb, int delta) {
        int r = clamp(((rgb >> 16) & 0xFF) + delta);
        int g = clamp(((rgb >> 8) & 0xFF) + delta);
        int b = clamp((rgb & 0xFF) + delta);
        int abgr = (0xFF << 24) | (b << 16) | (g << 8) | r;
        img.setPixelABGR(x, y, abgr);
    }

    private static int mul(int rgb, float f) {
        int r = clamp((int) (((rgb >> 16) & 0xFF) * f));
        int g = clamp((int) (((rgb >> 8) & 0xFF) * f));
        int b = clamp((int) ((rgb & 0xFF) * f));
        return (r << 16) | (g << 8) | b;
    }

    /** Rotate hue a little and scale brightness, staying in RGB space (cheap, good enough for tints). */
    private static int shift(int rgb, float hue, float bri) {
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        // Cheap hue rotation by cross-mixing channels.
        float rr = r + hue * (g - b);
        float gg = g + hue * (b - r);
        float bb = b + hue * (r - g);
        return (clamp((int) (rr * 255 * bri)) << 16)
                | (clamp((int) (gg * 255 * bri)) << 8)
                | clamp((int) (bb * 255 * bri));
    }

    private static int rgb(int r, int g, int b) { return (r << 16) | (g << 8) | b; }

    private static int clamp(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }

    private static int hash(int x) {
        x ^= x >>> 16; x *= 0x7feb352d; x ^= x >>> 15; x *= 0x846ca68b; x ^= x >>> 16;
        return x & 0x7FFFFFFF;
    }
}
