package com.political.items;

import java.util.HashMap;
import java.util.Map;

/** Unique RIGHT CLICK active ability bound to each {@link RpgItem} (except JJK cursed gear). */
public enum ItemActiveAbility {
    GUARDIAN_AEGIS("Aegis Ward", "Grant yourself Absorption and Resistance.", 50, 30),
    EMBER_STAFF("Ember Nova", "Ignite and scorch enemies in a cone.", 60, 25),
    VOID_RIFT("Void Collapse", "Tear a rift that withers nearby foes.", 80, 45),
    STORM_CALL("Storm Call", "Call lightning where you look.", 70, 35),
    FLAME_BURST("Inferno Burst", "Unleash a cone of fire.", 55, 20),
    FROST_NOVA("Frost Nova", "Freeze and damage nearby enemies.", 65, 28),
    VENOM_STRIKE("Venom Lunge", "Poison the enemy you are looking at.", 40, 15),
    THUNDER_DASH("Thunder Dash", "Blink forward and shock nearby foes.", 75, 40),
    MIDAS_TOUCH("Midas Touch", "Bless yourself with luck and coins.", 30, 60),
    ABYSSAL_WAVE("Abyssal Wave", "Launch a wave of soul damage.", 85, 50),
    SHADOWSTEP("Shadowstep", "Teleport behind your target and gain Strength.", 70, 60),
    INSTANT_TUNNEL("Tunnel Burst", "Carve an instant 1x3 tunnel ahead.", 45, 8),
    VEIN_SURGE("Vein Surge", "Gain extreme Haste for mining.", 35, 20),
    EXCAVATE("Excavator", "Gain Haste to dig a wide area.", 30, 15),
    WORLD_CLEAVE("World Cleave", "Slash everything in a short cone.", 55, 18),
    WARDEN_FORTIFY("Fortify", "Gain Resistance for a time.", 40, 45),
    VOID_SHROUD("Void Shroud", "Vanish briefly with Night Vision.", 50, 35),
    INFERNO_CROWN("Inferno Aura", "Become fire-immune and burn nearby mobs.", 60, 40),
    STORM_SURGE("Storm Surge", "Surge forward with Speed and sparks.", 45, 25),
    TIDE_BLESSING("Tide Blessing", "Breathe underwater and swim faster.", 25, 30),
    SCHOLAR_FOCUS("Scholar Focus", "Restore Mana and regenerate.", 20, 45),
    MERCHANTS_LUCK("Merchant's Luck", "Receive a coin windfall.", 0, 120),
    GUARD_BLESSING("Guardian Blessing", "Gain Strength and Resistance.", 35, 50),
    EXECUTE_MARK("Executioner's Mark", "Deal heavy damage to wounded targets.", 50, 22),
    MOON_SLASH("Moon Slash", "Slash in an arc, slowing all foes hit.", 55, 18),
    RADIANT_NOVA("Radiant Nova", "Blind and smite nearby enemies with holy light.", 65, 30),
    SOUL_VOLLEY("Soul Volley", "Launch soul bolts at nearby foes.", 70, 25),
    DRAGON_ROAR("Dragon Roar", "Knock back and ignite everything nearby.", 80, 40),
    ARACHNO_WEB("Arachno Web", "Poison and slow enemies in a cone.", 45, 20),
    NECROTIC_AURA("Necrotic Aura", "Spread wither to nearby enemies.", 60, 35),
    STARFALL("Starfall", "Smash the ground for area damage.", 50, 22),
    LUNAR_MINE("Lunar Mine", "Gain Haste and Night Vision for mining.", 30, 25),
    ARCANE_HARVEST("Arcane Harvest", "Restore Mana and gain Haste.", 25, 30),
    SOLAR_FLARE("Solar Flare", "Call sunlight and ignite your target.", 60, 28),
    BERSERK_RAGE("Berserker Rage", "Gain Strength and Speed.", 40, 45),
    SHIELD_BASH("Shield Bash", "Knock back and damage your target.", 35, 15),
    TIDAL_LANCE("Tidal Lance", "Impale your target with freezing water.", 55, 20),
    BLOOD_DRAIN("Blood Drain", "Steal health from your target.", 45, 18),
    SKULL_CRUSH("Skull Crush", "Devastating blow to a single target.", 65, 25),
    CRYSTAL_BEAM("Crystal Beam", "Fire a frost-lightning beam.", 75, 30),
    GHOST_STRIKE("Ghost Strike", "Phase through and strike your target.", 40, 12),
    TIMBER_CHOP("Timber Chop", "Fell nearby trees instantly.", 20, 10),
    PROSPECTOR_DIG("Prospector Dig", "Gain Haste and Luck while digging.", 15, 20);

    public final String displayName;
    public final String description;
    public final int manaCost;
    public final int cooldownSeconds;

    private static final Map<String, ItemActiveAbility> BY_ITEM = new HashMap<>();

    static {
        // Weapons
        map(RpgItem.AEGIS_BLADE, GUARDIAN_AEGIS);
        map(RpgItem.EMBER_STAFF, EMBER_STAFF);
        map(RpgItem.VOID_REAVER, VOID_RIFT);
        map(RpgItem.STORM_EDGE, STORM_CALL);
        map(RpgItem.INFERNO_BRAND, FLAME_BURST);
        map(RpgItem.FROSTMOURNE, FROST_NOVA);
        map(RpgItem.VENOM_FANG, VENOM_STRIKE);
        map(RpgItem.THUNDERCALLER, THUNDER_DASH);
        map(RpgItem.MIDAS_EDGE, MIDAS_TOUCH);
        map(RpgItem.ABYSSAL_BLADE, ABYSSAL_WAVE);
        map(RpgItem.NIGHTFALL_SCYTHE, MOON_SLASH);
        map(RpgItem.PHANTOM_BLADE, SHADOWSTEP);
        map(RpgItem.RADIANT_HALBERD, RADIANT_NOVA);
        map(RpgItem.CRYSTAL_STAFF, CRYSTAL_BEAM);
        map(RpgItem.SOUL_BOW, SOUL_VOLLEY);
        map(RpgItem.DRAGONSLAYER, DRAGON_ROAR);
        map(RpgItem.ARACHNO_CLAW, ARACHNO_WEB);
        map(RpgItem.MOONLIT_KATANA, MOON_SLASH);
        map(RpgItem.GHOST_DAGGER, GHOST_STRIKE);
        map(RpgItem.SOLAR_SPEAR, SOLAR_FLARE);
        map(RpgItem.BERSERKERS_AXE, BERSERK_RAGE);
        map(RpgItem.SHADOW_SHIELD, SHIELD_BASH);
        map(RpgItem.OCEAN_TRIDENT, TIDAL_LANCE);
        map(RpgItem.VAMPIRE_FANG, BLOOD_DRAIN);
        map(RpgItem.SKULL_MACE, SKULL_CRUSH);

        // Tools
        map(RpgItem.DANIELS_PICKAXE, INSTANT_TUNNEL);
        map(RpgItem.TITAN_DRILL, VEIN_SURGE);
        map(RpgItem.EXCAVATOR_SPADE, EXCAVATE);
        map(RpgItem.WORLDCLEAVER, WORLD_CLEAVE);
        map(RpgItem.STARFORGE_HAMMER, STARFALL);
        map(RpgItem.MOONLIGHT_PICK, LUNAR_MINE);
        map(RpgItem.ARCANE_HOE, ARCANE_HARVEST);
        map(RpgItem.TIMBER_AXE, TIMBER_CHOP);
        map(RpgItem.PROSPECTOR_SHOVEL, PROSPECTOR_DIG);

        // Sentinel / Warden
        map(RpgItem.SENTINEL_HELM, GUARDIAN_AEGIS);
        map(RpgItem.WARDENS_PLATE, WARDEN_FORTIFY);
        map(RpgItem.TITAN_LEGGINGS, WARDEN_FORTIFY);
        map(RpgItem.VOIDWALKER_BOOTS, VOID_SHROUD);

        // Inferno
        map(RpgItem.INFERNO_HELM, INFERNO_CROWN);
        map(RpgItem.INFERNO_PLATE, INFERNO_CROWN);
        map(RpgItem.INFERNO_LEGS, INFERNO_CROWN);
        map(RpgItem.INFERNO_BOOTS, INFERNO_CROWN);

        // Storm
        map(RpgItem.STORM_HELM, STORM_SURGE);
        map(RpgItem.STORM_PLATE, STORM_SURGE);
        map(RpgItem.STORM_LEGS, STORM_SURGE);
        map(RpgItem.STORM_BOOTS, STORM_SURGE);

        // Void
        map(RpgItem.VOID_HELM, VOID_SHROUD);
        map(RpgItem.VOID_PLATE, VOID_SHROUD);
        map(RpgItem.VOID_LEGS, VOID_SHROUD);
        map(RpgItem.VOID_BOOTS, VOID_SHROUD);

        // Gem utility
        map(RpgItem.LAPIS_HELM, SCHOLAR_FOCUS);
        map(RpgItem.LAPIS_PLATE, SCHOLAR_FOCUS);
        map(RpgItem.EMERALD_HELM, MERCHANTS_LUCK);
        map(RpgItem.EMERALD_PLATE, MERCHANTS_LUCK);

        // Tide
        map(RpgItem.TIDE_HELM, TIDE_BLESSING);
        map(RpgItem.TIDE_BOOTS, TIDE_BLESSING);

        // Necrotic
        map(RpgItem.NECRO_CROWN, NECROTIC_AURA);
        map(RpgItem.NECRO_GARB, NECROTIC_AURA);
        map(RpgItem.NECRO_LEGS, NECROTIC_AURA);
        map(RpgItem.NECRO_BOOTS, NECROTIC_AURA);

        // Radiant
        map(RpgItem.RADIANT_DIADEM, RADIANT_NOVA);
        map(RpgItem.RADIANT_VESTMENTS, RADIANT_NOVA);
        map(RpgItem.RADIANT_LEGGINGS, RADIANT_NOVA);
        map(RpgItem.RADIANT_SANDALS, RADIANT_NOVA);
        // JJK cursed weapons intentionally omitted — stats only.
    }

    ItemActiveAbility(String displayName, String description, int manaCost, int cooldownSeconds) {
        this.displayName = displayName;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownSeconds = cooldownSeconds;
    }

    private static void map(RpgItem item, ItemActiveAbility ability) {
        BY_ITEM.put(item.id(), ability);
    }

    public static ItemActiveAbility forItem(String rpgItemId) {
        return BY_ITEM.get(rpgItemId);
    }
}
