package com.political.curse.limb;

import java.util.EnumMap;
import java.util.Map;

/** Routes each cursed technique to the limb that channels it. */
public final class LimbTechniqueMap {

    private static final Map<String, CursedLimb> BY_TECHNIQUE = new java.util.HashMap<>();
    private static final EnumMap<CursedLimb, java.util.List<String>> BY_LIMB = new EnumMap<>(CursedLimb.class);

    static {
        for (CursedLimb limb : CursedLimb.values()) {
            BY_LIMB.put(limb, new java.util.ArrayList<>());
        }
        map("severing_edge", CursedLimb.HANDS);
        map("hollow_lance", CursedLimb.UPPER_ARM_R);
        map("riftpalm", CursedLimb.HANDS);
        map("ashen_pyre", CursedLimb.TORSO);
        map("frostbind_coil", CursedLimb.LOWER_ARM_L);
        map("stormcall_brand", CursedLimb.EYES);
        map("grave_tether", CursedLimb.HEART);
        map("warding_sigil", CursedLimb.TORSO);
        map("shade_step", CursedLimb.LOWER_LEG_R);
        map("verdant_snare", CursedLimb.LOWER_BODY);
        map("chain_sigil", CursedLimb.HANDS);
        map("obsidian_mantle", CursedLimb.TORSO);
        map("sunder_ring", CursedLimb.HANDS);
        map("soul_vortex", CursedLimb.LOWER_BODY);
        map("crimson_harvest", CursedLimb.HEART);
        map("sanctum_pulse", CursedLimb.TORSO);
        map("void_lance_storm", CursedLimb.UPPER_ARM_L);
    }

    private LimbTechniqueMap() {}

    private static void map(String techniqueId, CursedLimb limb) {
        BY_TECHNIQUE.put(techniqueId, limb);
        BY_LIMB.get(limb).add(techniqueId);
    }

    public static CursedLimb limbFor(String techniqueId) {
        return BY_TECHNIQUE.getOrDefault(techniqueId, CursedLimb.TORSO);
    }

    public static java.util.List<String> techniquesFor(CursedLimb limb) {
        return BY_LIMB.getOrDefault(limb, java.util.List.of());
    }
}
