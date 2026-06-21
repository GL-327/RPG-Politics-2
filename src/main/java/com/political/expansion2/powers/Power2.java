package com.political.expansion2.powers;

import net.minecraft.ChatFormatting;
import java.util.ArrayList;
import java.util.List;

public enum Power2 {
    VILTRUMITE_DASH("Viltrumite Dash", Category.VILTRUMITE, 14, 30, 0, "Rocket forward in a blur of Viltrumite momentum.", false, false, 0),
    VILTRUMITE_MEGA_PUNCH("Mega Punch", Category.VILTRUMITE, 22, 50, 0, "A single earth-shattering punch on whatever you look at.", false, false, 0),
    VILTRUMITE_BLOCK("Viltrumite Block", Category.VILTRUMITE, 18, 120, 0, "Brace yourself — incoming harm is heavily reduced.", false, false, 0),
    VILTRUMITE_GRAB_SLAM("Grab & Slam", Category.VILTRUMITE, 28, 70, 0, "Seize a foe and slam them into the ground.", false, false, 0),
    VILTRUMITE_ORBITAL_STRIKE("Orbital Strike", Category.VILTRUMITE, 45, 160, 0, "Dive from above onto your mark, detonating the impact zone.", false, false, 0),
    VILTRUMITE_IMPACT_CRATER("Impact Crater", Category.VILTRUMITE, 38, 140, 0, "Slam down to crater the ground and launch everything away.", false, false, 0),
    VILTRUMITE_RAGE_SURGE("Rage Surge", Category.VILTRUMITE, 32, 180, 0, "Let fury rewrite your biology — strength, speed, and resilience.", false, false, 0),
    VILTRUMITE_AERIAL_BOMBARDMENT("Aerial Bombardment", Category.VILTRUMITE, 40, 130, 0, "Rain concussive blows on everything below while aloft.", false, false, 0),
    VILTRUMITE_THUNDERCLAP("Thunderclap", Category.VILTRUMITE, 24, 80, 0, "Clap the air into a shockwave that hurls foes back.", false, false, 0),
    VILTRUMITE_SKULL_CRUSH("Skull Crush", Category.VILTRUMITE, 36, 100, 0, "Focus crushing force on a single target's head.", false, false, 0),
    VILTRUMITE_SUBORBITAL_DIVE("Suborbital Dive", Category.VILTRUMITE, 42, 150, 0, "Ascend briefly, then dive like a living missile.", false, false, 0),
    VILTRUMITE_WAR_CRY("War Cry", Category.VILTRUMITE, 20, 100, 0, "A bellow that weakens and slows all who hear it.", false, false, 0),
    VILTRUMITE_BONE_SHATTER("Bone Shatter", Category.VILTRUMITE, 30, 90, 0, "Shatter bone and will in a tight cone ahead.", false, false, 0),
    VILTRUMITE_MOMENTUM_RAM("Momentum Ram", Category.VILTRUMITE, 26, 110, 0, "Barrel through everything in your path at boost speed.", false, false, 0),
    VILTRUMITE_SUPERSONIC("Supersonic", Category.VILTRUMITE, 34, 200, 0, "Break the sound barrier — extreme speed and flight.", false, false, 0),
    HOMELANDER_BEAM("Homelander Beam", Category.HERO, 30, 45, 0, "A searing chest-and-eye beam that scorches everything ahead.", false, false, 0),
    ATOM_EVE_SHIFT("Atom Eve: Matter Shift", Category.HERO, 28, 100, 0, "Reshape matter around you — heal allies, harm foes.", false, false, 0),
    A_TRAIN_BLUR("A-Train Blur", Category.HERO, 22, 80, 0, "Move faster than the eye can follow.", false, false, 0),
    QUEEN_MAEVES_COUNTER("Queen Maeve's Counter", Category.HERO, 26, 120, 0, "Parry incoming harm and riposte with crushing force.", false, false, 0),
    TRANSLUCENT_VANISH("Translucent: Phase Vanish", Category.HERO, 24, 140, 0, "Turn fully invisible and slip through harm.", false, false, 0),
    STARLIGHT_BOLT("Starlight Bolt", Category.HERO, 20, 60, 0, "Fire a concentrated bolt of radiant energy.", false, false, 0),
    BLACK_NOIR_STRIKE("Black Noir: Silent Strike", Category.HERO, 32, 90, 0, "A lethal ambush strike from the shadows.", false, false, 0),
    SOLDIER_BOY_CHARGE("Soldier Boy: Charge", Category.HERO, 38, 130, 0, "Shoulder-charge forward, stunning and burning all you hit.", false, false, 0),
    DEEP_TIDAL_CRUSH("The Deep: Tidal Crush", Category.HERO, 26, 100, 0, "Summon a crushing wave that slows and damages.", false, false, 0),
    MM_SUPPRESS("Mother's Milk: Suppress", Category.HERO, 22, 110, 0, "Ground yourself and resist debuffs while striking back.", false, false, 0),
    FRENCHIE_ARSENAL("Frenchie's Arsenal", Category.HERO, 24, 70, 0, "Detonate a cluster of improvised explosives ahead.", false, false, 0),
    KIMIKO_BLADE_STORM("Kimiko: Blade Storm", Category.HERO, 28, 80, 0, "A whirlwind of blades shreds everything nearby.", false, false, 0),
    BUTCHER_BERSERK("Butcher's Berserk", Category.HERO, 30, 160, 0, "Temporary Compound-V fury — strength and lifesteal.", false, false, 0),
    RYAN_OUTBURST("Ryan's Outburst", Category.HERO, 50, 200, 0, "Uncontrolled power burst — massive AoE, hard to aim.", false, false, 0),
    NEO_NOIR_ECHO("Neo Noir Echo", Category.HERO, 34, 150, 0, "Split an afterimage that strikes your target twice.", false, false, 0),
    STRAW_DOLL("Straw Doll Technique", Category.JJK_TECHNIQUE, 28, 80, 1, "Drive nails through an effigy — resonance damages your mark.", false, false, 0),
    HAIRPIN("Hairpin", Category.JJK_TECHNIQUE, 22, 50, 1, "Launch cursed nails in a fan ahead.", false, false, 0),
    BOOGIE_WOOGIE("Boogie Woogie", Category.JJK_TECHNIQUE, 24, 60, 2, "Clap to swap places with your target instantly.", false, false, 0),
    COPY_TECHNIQUE("Copy", Category.JJK_TECHNIQUE, 40, 160, 4, "Briefly mimic a foe's last technique as a burst of cursed energy.", false, false, 0),
    RIKA_MANIFEST("Rika Manifest", Category.JJK_TECHNIQUE, 55, 220, 4, "Summon Rika's phantom to maul everything nearby.", false, false, 0),
    STAR_RAGE("Star Rage", Category.JJK_TECHNIQUE, 35, 120, 3, "Condense mass into overwhelming physical might.", false, false, 0),
    JUDGEMAN("Judgeman", Category.JJK_TECHNIQUE, 45, 180, 4, "Invoke trial — heavy debuffs rain on all ahead.", false, false, 0),
    CONFISCATION("Confiscation", Category.JJK_TECHNIQUE, 50, 200, 4, "Strip a foe's strength and resistance for a time.", false, false, 0),
    CURSED_SPIRIT_MANIPULATION("Cursed Spirit Manipulation", Category.JJK_TECHNIQUE, 48, 240, 4, "Bind nearby curses to fight at your side.", false, false, 0),
    DISASTER_PLANTS("Disaster Plants", Category.JJK_TECHNIQUE, 32, 110, 3, "Erupt thorned vegetation that poisons and slows.", false, false, 0),
    FLOWING_RED_SCALE("Flowing Red Scale", Category.JJK_TECHNIQUE, 30, 100, 2, "Harden skin with cursed scales — resistance and thorns.", false, false, 0),
    ANTIGRAVITY_SYSTEM("Antigravity System", Category.JJK_TECHNIQUE, 38, 130, 4, "Reverse local gravity, launching foes upward.", false, false, 0),
    BIRD_STRIKE("Bird Strike", Category.JJK_TECHNIQUE, 26, 70, 2, "Send cursed crows diving through everything in line.", false, false, 0),
    ICE_FORMATION("Ice Formation", Category.JJK_TECHNIQUE, 28, 90, 2, "Flash-freeze and shatter foes in a cone.", false, false, 0),
    THUNDER_INSPECTION("Thunder Inspection", Category.JJK_TECHNIQUE, 34, 100, 3, "Electrocute a target and chain to nearby foes.", false, false, 0),
    SOUL_MULTIPLICITY("Soul Multiplicity", Category.JJK_TECHNIQUE, 42, 150, 3, "Split souls in the area, multiplying your damage.", false, false, 0),
    BLOOD_BOILING("Blood Boiling", Category.JJK_TECHNIQUE, 30, 80, 2, "Superheat blood in a cone (costs a little life).", false, false, 0),
    CURSE_COLLAGE("Curse Collation", Category.JJK_TECHNIQUE, 44, 180, 4, "Absorb ambient curses to heal and buff yourself.", false, false, 0),
    DEATH_SWARMING("Death Swarming", Category.JJK_TECHNIQUE, 36, 120, 3, "Release a swarm of insect curses that wither foes.", false, false, 0),
    GAMMA_RAY("Gamma Ray", Category.JJK_TECHNIQUE, 60, 260, 5, "A focused beam of cursed radiation.", false, false, 0),
    DOMAIN_INFINITE_VOID("Domain: Infinite Void", Category.DOMAIN, 110, 700, 5, "Floods minds with infinite information — blind, slow, and harm all within.", true, false, 0),
    DOMAIN_TIME_CELL("Domain: Time Cell Moon Palace", Category.DOMAIN, 100, 650, 5, "A temple of slowness — enemies crawl while you strike freely.", true, false, 0),
    DOMAIN_HANDMADE("Domain: Captivating Handmade", Category.DOMAIN, 95, 600, 4, "A workshop of idle transfiguration — reshape all inside.", true, false, 0),
    DOMAIN_MUTUAL_LOVE("Domain: Authentic Mutual Love", Category.DOMAIN, 105, 680, 5, "A shrine of cleave and dismantle — relentless slashes within.", true, false, 0),
    DOMAIN_DEADLY_SENTENCING("Domain: Deadly Sentencing", Category.DOMAIN, 90, 620, 4, "Court is in session — confiscate strength from all judged.", true, false, 0),
    DOMAIN_CEREBRAL("Domain: Cerebral Binding", Category.DOMAIN, 85, 580, 4, "Bind minds and bodies in a web of cursed threads.", true, false, 0),
    DOMAIN_IDLE_GAMBLE("Domain: Idle Death Gamble", Category.DOMAIN, 100, 640, 5, "A casino of cursed fate — random bursts of fortune or ruin.", true, false, 0),
    DOMAIN_WOMB("Domain: Womb Profusion", Category.DOMAIN, 95, 600, 5, "Antigravity womb — float helpless foes while you attack.", true, false, 0),
    DOMAIN_THUNDER_GAAIS("Domain: Thunder Gaia", Category.DOMAIN, 88, 560, 4, "A storm shrine — lightning and paralysis within.", true, false, 0),
    DOMAIN_HORIZON("Domain: Horizon of the Grau", Category.DOMAIN, 92, 590, 4, "An ocean domain — drown, slow, and crush all inside.", true, false, 0),
    ULTIMATE_PURPLE_STORM("Ultimate: Purple Storm", Category.ULTIMATE, 100, 500, 5, "Unleash a storm of colliding cursed forces.", false, false, 0),
    ULTIMATE_MAXIMUM_UZUMAKI("Ultimate: Maximum Uzumaki", Category.ULTIMATE, 95, 480, 5, "Spiral all cursed energy into one annihilating orb.", false, false, 0),
    ULTIMATE_MERGED_BEAST("Ultimate: Merger Beast", Category.ULTIMATE, 85, 420, 4, "Fuse shikigami into a single devastating assault.", false, false, 0),
    ULTIMATE_METEOR_STORM("Ultimate: Meteor Storm", Category.ULTIMATE, 105, 520, 5, "Call a barrage of cursed meteors across a wide area.", false, false, 0),
    ULTIMATE_OPEN_SHRINE("Ultimate: Open Shrine", Category.ULTIMATE, 115, 560, 5, "The shrine without a barrier — endless cleave in every direction.", false, false, 0),
    ULTIMATE_STAR_FALL("Ultimate: Star Fall", Category.ULTIMATE, 90, 460, 4, "Drop stellar mass — gravity, fire, and impact.", false, false, 0),
    ULTIMATE_VILTRUMITE_APOCALYPSE("Ultimate: Viltrumite Apocalypse", Category.ULTIMATE, 80, 400, 0, "Unleash full Viltrumite might — flight, beam, and ground zero.", false, false, 0),
    ULTIMATE_HERO_SQUAD("Ultimate: Hero Squad", Category.ULTIMATE, 70, 380, 0, "Channel the combined fury of every hero you've studied.", false, false, 0),
    PASSIVE_CE_EFFICIENCY("Passive: CE Efficiency", Category.PASSIVE, 0, 60, 2, "Toggle: cursed techniques cost slightly less CE over time.", false, true, 10),
    PASSIVE_BATTLE_FRENZY("Passive: Battle Frenzy", Category.PASSIVE, 0, 60, 1, "Toggle: gain strength while below half health.", false, true, 0),
    PASSIVE_SHADOW_AFFINITY("Passive: Shadow Affinity", Category.PASSIVE, 0, 60, 2, "Toggle: shadow techniques recharge faster.", false, true, 0),
    PASSIVE_REGEN_AURA("Passive: Regenerative Aura", Category.PASSIVE, 0, 60, 1, "Toggle: slowly regenerate health out of combat.", false, true, 0),
    PASSIVE_GRADE_PRESSURE("Passive: Grade Pressure", Category.PASSIVE, 0, 60, 3, "Toggle: weaken nearby foes below your grade.", false, true, 0),
    PASSIVE_FLIGHT_MASTERY("Passive: Flight Mastery", Category.PASSIVE, 0, 60, 0, "Toggle: Viltrumite flight throttle ramps faster.", false, true, 0),
    PASSIVE_CURSE_RESIST("Passive: Curse Resistance", Category.PASSIVE, 0, 60, 2, "Toggle: resist poison, wither, and weakness.", false, true, 0),
    PASSIVE_BLACK_FLASH_MASTERY("Passive: Black Flash Mastery", Category.PASSIVE, 0, 60, 3, "Toggle: bonus distorted damage on critical hits.", false, true, 0);

    public enum Category {
        VILTRUMITE("Viltrumite", ChatFormatting.RED),
        HERO("Hero", ChatFormatting.GOLD),
        JJK_TECHNIQUE("Cursed Technique II", ChatFormatting.DARK_PURPLE),
        DOMAIN("Domain Expansion", ChatFormatting.LIGHT_PURPLE),
        ULTIMATE("Ultimate", ChatFormatting.YELLOW),
        PASSIVE("Passive", ChatFormatting.GREEN);
        public final String label;
        public final ChatFormatting color;
        Category(String label, ChatFormatting color) { this.label = label; this.color = color; }
    }

    public final String displayName;
    public final Category category;
    public final int energyCost;
    public final int cooldownTicks;
    public final int minGrade;
    public final String description;
    public final boolean isDomain;
    public final boolean isPassive;
    public final int cursedEnergyBonus;

    Power2(String displayName, Category category, int energyCost, int cooldownTicks, int minGrade,
           String description, boolean isDomain, boolean isPassive, int cursedEnergyBonus) {
        this.displayName = displayName;
        this.category = category;
        this.energyCost = energyCost;
        this.cooldownTicks = cooldownTicks;
        this.minGrade = minGrade;
        this.description = description;
        this.isDomain = isDomain;
        this.isPassive = isPassive;
        this.cursedEnergyBonus = cursedEnergyBonus;
    }

    public boolean usesCursedEnergy() {
        return category == Category.JJK_TECHNIQUE || category == Category.DOMAIN
                || (category == Category.ULTIMATE && minGrade > 0);
    }

    public String id() { return name().toLowerCase(); }

    public static Power2 byId(String id) {
        if (id == null) return null;
        try { return valueOf(id.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static List<Power2> ofCategory(Category category) {
        List<Power2> out = new ArrayList<>();
        for (Power2 p : values()) if (p.category == category) out.add(p);
        return out;
    }

    public static List<String> allIds() {
        List<String> ids = new ArrayList<>(values().length);
        for (Power2 p : values()) ids.add(p.id());
        return ids;
    }

    public static double cursedEnergyBonus(List<String> knownPowerIds) {
        double sum = 0;
        for (String id : knownPowerIds) {
            Power2 p = byId(id);
            if (p != null) sum += p.cursedEnergyBonus;
        }
        return sum;
    }
}
