package com.political.expansion.melee;

import com.political.items.Rarity;

/**
 * The melee weapon expansion catalogue. Every entry is a Skyblock-stat weapon: there is
 * <b>no vanilla base attack damage/speed</b> — all combat power is carried as {@code custom_data}
 * stats (damage / strength / crit chance / crit damage / ferocity / intelligence / health /
 * defense / speed) exactly like {@link com.political.items.RpgItem}, plus a unique right-click
 * {@link MeleeAbility}.
 *
 * <p>Item ids are prefixed {@code wpn_} and each resolves its own texture via the
 * {@code ITEM_MODEL} component / registered item model
 * ({@code assets/politicalserver/items/<id>.json}).
 */
public enum MeleeWeapon {
    // ---------------- Common ----------------
    IRON_CUTLASS("wpn_iron_cutlass", "Iron Cutlass", "sword", Rarity.COMMON,
            new Stats().dmg(24).str(14).cc(10).cd(20), MeleeAbility.CLEAVING_SLASH),
    BRONZE_DIRK("wpn_bronze_dirk", "Bronze Dirk", "dagger", Rarity.COMMON,
            new Stats().dmg(20).str(10).cc(14).cd(25).spd(4), MeleeAbility.QUICK_JAB),
    STONE_BLUDGEON("wpn_stone_bludgeon", "Stone Bludgeon", "mace", Rarity.COMMON,
            new Stats().dmg(28).str(18).cd(30), MeleeAbility.CRUSHING_BLOW),
    WOODSMAN_HATCHET("wpn_woodsman_hatchet", "Woodsman Hatchet", "axe", Rarity.COMMON,
            new Stats().dmg(26).str(16).cd(25).fer(3), MeleeAbility.TIMBER_HACK),
    MILITIA_SPEAR("wpn_militia_spear", "Militia Spear", "spear", Rarity.COMMON,
            new Stats().dmg(22).str(12).cc(8).cd(20), MeleeAbility.BRACED_LUNGE),

    // ---------------- Uncommon ----------------
    STEEL_SABER("wpn_steel_saber", "Steel Saber", "sword", Rarity.UNCOMMON,
            new Stats().dmg(40).str(26).cc(12).cd(35), MeleeAbility.RIPOSTE),
    HUNTERS_KRIS("wpn_hunters_kris", "Hunter's Kris", "dagger", Rarity.UNCOMMON,
            new Stats().dmg(34).str(20).cc(18).cd(40).spd(6), MeleeAbility.BLEEDING_STAB),
    BEARDED_AXE("wpn_bearded_axe", "Bearded Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(44).str(30).cd(40).fer(4), MeleeAbility.REND),
    WAR_PIKE("wpn_war_pike", "War Pike", "spear", Rarity.UNCOMMON,
            new Stats().dmg(38).str(22).cc(10).cd(30), MeleeAbility.SKEWER),
    IRON_MORNINGSTAR("wpn_iron_morningstar", "Iron Morningstar", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(32).cd(45), MeleeAbility.CONCUSSION),

    // ---------------- Rare ----------------
    KNIGHTS_LONGSWORD("wpn_knights_longsword", "Knight's Longsword", "sword", Rarity.RARE,
            new Stats().dmg(58).str(38).cc(15).cd(45).hp(20).def(10), MeleeAbility.VALIANT_SWEEP),
    SHADOW_KUNAI("wpn_shadow_kunai", "Shadow Kunai", "dagger", Rarity.RARE,
            new Stats().dmg(52).str(34).cc(20).cd(55).spd(8).intel(15), MeleeAbility.SHADOW_FLICKER),
    STORM_GLAIVE("wpn_storm_glaive", "Storm Glaive", "halberd", Rarity.RARE,
            new Stats().dmg(62).str(40).cc(14).cd(40).intel(20), MeleeAbility.STATIC_REACH),
    TEMPEST_KATANA("wpn_tempest_katana", "Tempest Katana", "katana", Rarity.RARE,
            new Stats().dmg(56).str(36).cc(18).cd(50).spd(6), MeleeAbility.IAIDO_DRAW),
    GRAVE_SCYTHE("wpn_grave_scythe", "Grave Scythe", "scythe", Rarity.RARE,
            new Stats().dmg(60).str(42).cc(12).cd(45).intel(15), MeleeAbility.REAPING_ARC),

    // ---------------- Epic ----------------
    DRAGONBONE_GREATSWORD("wpn_dragonbone_greatsword", "Dragonbone Greatsword", "greatsword", Rarity.EPIC,
            new Stats().dmg(82).str(58).cc(18).cd(60).fer(5).hp(30).def(15), MeleeAbility.EARTHSHATTER),
    VENOM_CLAWS("wpn_venom_claws", "Venom Claws", "claw", Rarity.EPIC,
            new Stats().dmg(74).str(50).cc(24).cd(55).fer(8).spd(10), MeleeAbility.RENDING_FRENZY),
    FROST_CLEAVER("wpn_frost_cleaver", "Frost Cleaver", "cleaver", Rarity.EPIC,
            new Stats().dmg(88).str(64).cc(16).cd(55).intel(20), MeleeAbility.PERMAFROST_CHOP),
    EMBER_WARAXE("wpn_ember_waraxe", "Ember Waraxe", "axe", Rarity.EPIC,
            new Stats().dmg(90).str(66).cd(70).fer(6).intel(15), MeleeAbility.MAGMA_CLEAVE),
    SERPENT_WHIP("wpn_serpent_whip", "Serpent Whip", "whip", Rarity.EPIC,
            new Stats().dmg(70).str(48).cc(20).cd(60).spd(8), MeleeAbility.LASHING_COIL),
    THUNDERSPIKE_SPEAR("wpn_thunderspike_spear", "Thunderspike Spear", "spear", Rarity.EPIC,
            new Stats().dmg(80).str(54).cc(20).cd(58).intel(25), MeleeAbility.LEVIN_THROW),

    // ---------------- Legendary ----------------
    CELESTIAL_CLAYMORE("wpn_celestial_claymore", "Celestial Claymore", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(110).str(82).cc(22).cd(80).fer(10).hp(40).def(20).intel(20), MeleeAbility.COMET_SMASH),
    MOONSHADOW_KATANA("wpn_moonshadow_katana", "Moonshadow Katana", "katana", Rarity.LEGENDARY,
            new Stats().dmg(100).str(74).cc(28).cd(90).spd(12).intel(25), MeleeAbility.CRESCENT_ECLIPSE),
    SOULREAPER_SCYTHE("wpn_soulreaper_scythe", "Soulreaper Scythe", "scythe", Rarity.LEGENDARY,
            new Stats().dmg(108).str(80).cc(20).cd(80).intel(40), MeleeAbility.SOUL_HARVEST),
    WARLORDS_HALBERD("wpn_warlords_halberd", "Warlord's Halberd", "halberd", Rarity.LEGENDARY,
            new Stats().dmg(112).str(86).cc(18).cd(75).fer(10).intel(20), MeleeAbility.SWEEPING_VORTEX),
    TITANBREAKER_MAUL("wpn_titanbreaker_maul", "Titanbreaker Maul", "mace", Rarity.LEGENDARY,
            new Stats().dmg(120).str(95).cd(95).fer(12).hp(30).def(15), MeleeAbility.SEISMIC_SLAM),
    PHANTOM_DAGGERS("wpn_phantom_daggers", "Phantom Daggers", "dagger", Rarity.LEGENDARY,
            new Stats().dmg(96).str(72).cc(30).cd(95).spd(16).intel(30), MeleeAbility.THOUSAND_CUTS),

    // ---------------- Mythic ----------------
    GODSLAYER_BLADE("wpn_godslayer_blade", "Godslayer Blade", "sword", Rarity.MYTHIC,
            new Stats().dmg(150).str(120).cc(30).cd(140).fer(20).intel(60), MeleeAbility.DIVINE_EXECUTION),
    VOIDREND_GREATSWORD("wpn_voidrend_greatsword", "Voidrend Greatsword", "greatsword", Rarity.MYTHIC,
            new Stats().dmg(158).str(130).cc(28).cd(130).fer(18).intel(50).hp(40).def(20), MeleeAbility.OBLIVION_RIFT),
    RAGNAROK_AXE("wpn_ragnarok_axe", "Ragnarok Axe", "axe", Rarity.MYTHIC,
            new Stats().dmg(160).str(128).cd(150).fer(25).intel(40), MeleeAbility.CATACLYSM),
    ETERNITY_SCYTHE("wpn_eternity_scythe", "Eternity Scythe", "scythe", Rarity.MYTHIC,
            new Stats().dmg(152).str(122).cc(26).cd(135).intel(70), MeleeAbility.REAP_ETERNAL);

    public final String id;
    public final String displayName;
    public final String archetype;
    public final Rarity rarity;
    public final Stats stats;
    public final MeleeAbility ability;

    MeleeWeapon(String id, String displayName, String archetype, Rarity rarity,
                Stats stats, MeleeAbility ability) {
        this.id = id;
        this.displayName = displayName;
        this.archetype = archetype;
        this.rarity = rarity;
        this.stats = stats;
        this.ability = ability;
    }

    /** Fluent Skyblock-stat bundle for a single weapon (all values default to 0). */
    public static final class Stats {
        public int damage, strength, critChance, critDamage, ferocity, intelligence, health, defense, speed, attackSpeed;

        public Stats dmg(int v) { this.damage = v; return this; }
        public Stats str(int v) { this.strength = v; return this; }
        public Stats cc(int v) { this.critChance = v; return this; }
        public Stats cd(int v) { this.critDamage = v; return this; }
        public Stats fer(int v) { this.ferocity = v; return this; }
        public Stats intel(int v) { this.intelligence = v; return this; }
        public Stats hp(int v) { this.health = v; return this; }
        public Stats def(int v) { this.defense = v; return this; }
        public Stats spd(int v) { this.speed = v; return this; }
        public Stats atkSpd(int v) { this.attackSpeed = v; return this; }
    }
}
