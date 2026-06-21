package com.political.expansion2.accessories;

import com.political.items.Rarity;

import java.util.List;

/**
 * The artifacts-inspired ability accessory catalogue (original names + art). Nine wearable
 * accessories, each mapping one {@link AccessoryAbility2} onto a vanilla-API effect handled by
 * {@link AbilityAccessories2}.
 */
final class AbilityAccessoriesCatalog2 {

    private AbilityAccessoriesCatalog2() {}

    static void build(List<AbilityAccessoryDef2> out) {
        out.add(new AbilityAccessoryDef2("acc2_ab_aetherwing", "Aetherwing Charm",
                AccessoryDef2.Type.CHARM, Rarity.MYTHIC,
                "Bound starling feathers that never tire of the open sky.",
                AccessoryAbility2.FLIGHT, 0, "Creative-style flight"));
        out.add(new AbilityAccessoryDef2("acc2_ab_lodestone_locket", "Lodestone Locket",
                AccessoryDef2.Type.AMULET, Rarity.RARE,
                "A sliver of magnetized sky-iron that calls treasure to your hand.",
                AccessoryAbility2.MAGNET, 7.0, "Pulls in dropped items & XP (7 blocks)"));
        out.add(new AbilityAccessoryDef2("acc2_ab_cinderheart", "Cinderheart Band",
                AccessoryDef2.Type.BAND, Rarity.EPIC,
                "A coal that burned out an age ago, yet still remembers warmth.",
                AccessoryAbility2.FIRE_WARD, 0, "Immunity to fire & lava"));
        out.add(new AbilityAccessoryDef2("acc2_ab_bulwark_totem", "Bulwark Totem",
                AccessoryDef2.Type.TOTEM, Rarity.UNCOMMON,
                "Carved from a mountain's root; it simply will not be moved.",
                AccessoryAbility2.ANTI_KNOCKBACK, 0, "Immune to knockback"));
        out.add(new AbilityAccessoryDef2("acc2_ab_tidecaller", "Tidecaller Pendant",
                AccessoryDef2.Type.AMULET, Rarity.RARE,
                "A drop of the first ocean, sealed in living coral.",
                AccessoryAbility2.AQUATIC, 0, "Water breathing, dolphin's grace, deep sight"));
        out.add(new AbilityAccessoryDef2("acc2_ab_highstep", "Highstep Anklet",
                AccessoryDef2.Type.BAND, Rarity.UNCOMMON,
                "Tiny wings at the heel lift each stride over the rubble.",
                AccessoryAbility2.STEP_ASSIST, 1.0, "Step up full blocks (+1.0)"));
        out.add(new AbilityAccessoryDef2("acc2_ab_owls_eye", "Owl's Eye Talisman",
                AccessoryDef2.Type.TALISMAN, Rarity.COMMON,
                "An owl's patient gaze, captured in polished amber.",
                AccessoryAbility2.NIGHT_SIGHT, 0, "Permanent night vision"));
        out.add(new AbilityAccessoryDef2("acc2_ab_verdant_idol", "Verdant Sustenance Idol",
                AccessoryDef2.Type.RELIC, Rarity.UNCOMMON,
                "Moss-veined jade that feeds you from the living earth.",
                AccessoryAbility2.GRAZING, 0, "Replenishes hunger on grass"));
        out.add(new AbilityAccessoryDef2("acc2_ab_featherfall", "Featherfall Sigil",
                AccessoryDef2.Type.RUNE, Rarity.EPIC,
                "A rune that argues, gently, with gravity.",
                AccessoryAbility2.FEATHERFALL, 0, "Negates fall damage"));
    }
}
