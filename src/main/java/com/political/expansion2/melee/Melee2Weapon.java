package com.political.expansion2.melee;

import com.political.items.Rarity;

/** Expansion2 melee catalogue — ids prefixed {@code wpn2_}. */
public enum Melee2Weapon {

    // ---------------- CURSED theme ----------------
    WPN2_CURSED_SWORD("wpn2_cursed_sword", "Cursed Blade", "sword", Rarity.COMMON,
            new Stats().dmg(26).str(16).cc(10).cd(22), Melee2Ability.CURSED_SLASH),
    WPN2_CURSED_DAGGER("wpn2_cursed_dagger", "Cursed Dagger", "dagger", Rarity.COMMON,
            new Stats().dmg(28).str(17).cc(12).cd(24).spd(8), Melee2Ability.CURSED_STAB),
    WPN2_CURSED_MACE("wpn2_cursed_mace", "Cursed Mace", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(30).cc(16).cd(39).fer(6), Melee2Ability.CURSED_CRUSH),
    WPN2_CURSED_AXE("wpn2_cursed_axe", "Cursed Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(48).str(31).cc(12).cd(41), Melee2Ability.CURSED_REND),
    WPN2_CURSED_SPEAR("wpn2_cursed_spear", "Cursed Spear", "spear", Rarity.RARE,
            new Stats().dmg(66).str(42).cc(17).cd(53).hp(20).def(10), Melee2Ability.CURSED_LUNGE),
    WPN2_CURSED_HALBERD("wpn2_cursed_halberd", "Cursed Halberd", "halberd", Rarity.RARE,
            new Stats().dmg(68).str(43).cc(19).cd(55).intel(15).hp(20).def(10), Melee2Ability.CURSED_REACH),
    WPN2_CURSED_KATANA("wpn2_cursed_katana", "Cursed Katana", "katana", Rarity.EPIC,
            new Stats().dmg(90).str(60).cc(18).cd(70).intel(20).hp(20).def(10), Melee2Ability.CURSED_DRAW),
    WPN2_CURSED_SCYTHE("wpn2_cursed_scythe", "Cursed Scythe", "scythe", Rarity.EPIC,
            new Stats().dmg(92).str(61).cc(20).cd(72).hp(20).def(10), Melee2Ability.CURSED_REAP),
    WPN2_CURSED_GREATSWORD("wpn2_cursed_greatsword", "Cursed Greatsword", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(121).str(86).cc(26).cd(94).fer(6).intel(25).hp(35).def(15), Melee2Ability.CURSED_SMASH),
    WPN2_CURSED_CLEAVER("wpn2_cursed_cleaver", "Cursed Cleaver", "cleaver", Rarity.LEGENDARY,
            new Stats().dmg(123).str(87).cc(22).cd(96).intel(25).hp(35).def(15), Melee2Ability.CURSED_CHOP),
    WPN2_CURSED_CLAW("wpn2_cursed_claw", "Cursed Claws", "claw", Rarity.MYTHIC,
            new Stats().dmg(165).str(125).cc(30).cd(150).intel(25).hp(35).def(15).spd(14), Melee2Ability.CURSED_FRENZY),
    WPN2_CURSED_WHIP("wpn2_cursed_whip", "Cursed Whip", "whip", Rarity.MYTHIC,
            new Stats().dmg(167).str(126).cc(32).cd(152).intel(25).hp(35).def(15), Melee2Ability.CURSED_LASH),

    // ---------------- PROM theme ----------------
    WPN2_PROM_SWORD("wpn2_prom_sword", "Prominence Blade", "sword", Rarity.COMMON,
            new Stats().dmg(26).str(16).cc(10).cd(22), Melee2Ability.PROM_SLASH),
    WPN2_PROM_DAGGER("wpn2_prom_dagger", "Prominence Dagger", "dagger", Rarity.COMMON,
            new Stats().dmg(28).str(17).cc(12).cd(24).spd(8), Melee2Ability.PROM_STAB),
    WPN2_PROM_MACE("wpn2_prom_mace", "Prominence Mace", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(30).cc(16).cd(39).fer(6), Melee2Ability.PROM_CRUSH),
    WPN2_PROM_AXE("wpn2_prom_axe", "Prominence Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(48).str(31).cc(12).cd(41), Melee2Ability.PROM_REND),
    WPN2_PROM_SPEAR("wpn2_prom_spear", "Prominence Spear", "spear", Rarity.RARE,
            new Stats().dmg(66).str(42).cc(17).cd(53).hp(20).def(10), Melee2Ability.PROM_LUNGE),
    WPN2_PROM_HALBERD("wpn2_prom_halberd", "Prominence Halberd", "halberd", Rarity.RARE,
            new Stats().dmg(68).str(43).cc(19).cd(55).intel(15).hp(20).def(10), Melee2Ability.PROM_REACH),
    WPN2_PROM_KATANA("wpn2_prom_katana", "Prominence Katana", "katana", Rarity.EPIC,
            new Stats().dmg(90).str(60).cc(18).cd(70).intel(20).hp(20).def(10), Melee2Ability.PROM_DRAW),
    WPN2_PROM_SCYTHE("wpn2_prom_scythe", "Prominence Scythe", "scythe", Rarity.EPIC,
            new Stats().dmg(92).str(61).cc(20).cd(72).hp(20).def(10), Melee2Ability.PROM_REAP),
    WPN2_PROM_GREATSWORD("wpn2_prom_greatsword", "Prominence Greatsword", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(121).str(86).cc(26).cd(94).fer(6).intel(25).hp(35).def(15), Melee2Ability.PROM_SMASH),
    WPN2_PROM_CLEAVER("wpn2_prom_cleaver", "Prominence Cleaver", "cleaver", Rarity.LEGENDARY,
            new Stats().dmg(123).str(87).cc(22).cd(96).intel(25).hp(35).def(15), Melee2Ability.PROM_CHOP),
    WPN2_PROM_CLAW("wpn2_prom_claw", "Prominence Claws", "claw", Rarity.MYTHIC,
            new Stats().dmg(165).str(125).cc(30).cd(150).intel(25).hp(35).def(15).spd(14), Melee2Ability.PROM_FRENZY),
    WPN2_PROM_WHIP("wpn2_prom_whip", "Prominence Whip", "whip", Rarity.MYTHIC,
            new Stats().dmg(167).str(126).cc(32).cd(152).intel(25).hp(35).def(15), Melee2Ability.PROM_LASH),

    // ---------------- ELEM theme ----------------
    WPN2_ELEM_SWORD("wpn2_elem_sword", "Primal Blade", "sword", Rarity.COMMON,
            new Stats().dmg(26).str(16).cc(10).cd(22), Melee2Ability.ELEM_SLASH),
    WPN2_ELEM_DAGGER("wpn2_elem_dagger", "Primal Dagger", "dagger", Rarity.COMMON,
            new Stats().dmg(28).str(17).cc(12).cd(24).spd(8), Melee2Ability.ELEM_STAB),
    WPN2_ELEM_MACE("wpn2_elem_mace", "Primal Mace", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(30).cc(16).cd(39).fer(6), Melee2Ability.ELEM_CRUSH),
    WPN2_ELEM_AXE("wpn2_elem_axe", "Primal Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(48).str(31).cc(12).cd(41), Melee2Ability.ELEM_REND),
    WPN2_ELEM_SPEAR("wpn2_elem_spear", "Primal Spear", "spear", Rarity.RARE,
            new Stats().dmg(66).str(42).cc(17).cd(53).hp(20).def(10), Melee2Ability.ELEM_LUNGE),
    WPN2_ELEM_HALBERD("wpn2_elem_halberd", "Primal Halberd", "halberd", Rarity.RARE,
            new Stats().dmg(68).str(43).cc(19).cd(55).intel(15).hp(20).def(10), Melee2Ability.ELEM_REACH),
    WPN2_ELEM_KATANA("wpn2_elem_katana", "Primal Katana", "katana", Rarity.EPIC,
            new Stats().dmg(90).str(60).cc(18).cd(70).intel(20).hp(20).def(10), Melee2Ability.ELEM_DRAW),
    WPN2_ELEM_SCYTHE("wpn2_elem_scythe", "Primal Scythe", "scythe", Rarity.EPIC,
            new Stats().dmg(92).str(61).cc(20).cd(72).hp(20).def(10), Melee2Ability.ELEM_REAP),
    WPN2_ELEM_GREATSWORD("wpn2_elem_greatsword", "Primal Greatsword", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(121).str(86).cc(26).cd(94).fer(6).intel(25).hp(35).def(15), Melee2Ability.ELEM_SMASH),
    WPN2_ELEM_CLEAVER("wpn2_elem_cleaver", "Primal Cleaver", "cleaver", Rarity.LEGENDARY,
            new Stats().dmg(123).str(87).cc(22).cd(96).intel(25).hp(35).def(15), Melee2Ability.ELEM_CHOP),
    WPN2_ELEM_CLAW("wpn2_elem_claw", "Primal Claws", "claw", Rarity.MYTHIC,
            new Stats().dmg(165).str(125).cc(30).cd(150).intel(25).hp(35).def(15).spd(14), Melee2Ability.ELEM_FRENZY),
    WPN2_ELEM_WHIP("wpn2_elem_whip", "Primal Whip", "whip", Rarity.MYTHIC,
            new Stats().dmg(167).str(126).cc(32).cd(152).intel(25).hp(35).def(15), Melee2Ability.ELEM_LASH),

    // ---------------- VOID theme ----------------
    WPN2_VOID_SWORD("wpn2_void_sword", "Void Blade", "sword", Rarity.COMMON,
            new Stats().dmg(26).str(16).cc(10).cd(22), Melee2Ability.VOID_SLASH),
    WPN2_VOID_DAGGER("wpn2_void_dagger", "Void Dagger", "dagger", Rarity.COMMON,
            new Stats().dmg(28).str(17).cc(12).cd(24).spd(8), Melee2Ability.VOID_STAB),
    WPN2_VOID_MACE("wpn2_void_mace", "Void Mace", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(30).cc(16).cd(39).fer(6), Melee2Ability.VOID_CRUSH),
    WPN2_VOID_AXE("wpn2_void_axe", "Void Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(48).str(31).cc(12).cd(41), Melee2Ability.VOID_REND),
    WPN2_VOID_SPEAR("wpn2_void_spear", "Void Spear", "spear", Rarity.RARE,
            new Stats().dmg(66).str(42).cc(17).cd(53).hp(20).def(10), Melee2Ability.VOID_LUNGE),
    WPN2_VOID_HALBERD("wpn2_void_halberd", "Void Halberd", "halberd", Rarity.RARE,
            new Stats().dmg(68).str(43).cc(19).cd(55).intel(15).hp(20).def(10), Melee2Ability.VOID_REACH),
    WPN2_VOID_KATANA("wpn2_void_katana", "Void Katana", "katana", Rarity.EPIC,
            new Stats().dmg(90).str(60).cc(18).cd(70).intel(20).hp(20).def(10), Melee2Ability.VOID_DRAW),
    WPN2_VOID_SCYTHE("wpn2_void_scythe", "Void Scythe", "scythe", Rarity.EPIC,
            new Stats().dmg(92).str(61).cc(20).cd(72).hp(20).def(10), Melee2Ability.VOID_REAP),
    WPN2_VOID_GREATSWORD("wpn2_void_greatsword", "Void Greatsword", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(121).str(86).cc(26).cd(94).fer(6).intel(25).hp(35).def(15), Melee2Ability.VOID_SMASH),
    WPN2_VOID_CLEAVER("wpn2_void_cleaver", "Void Cleaver", "cleaver", Rarity.LEGENDARY,
            new Stats().dmg(123).str(87).cc(22).cd(96).intel(25).hp(35).def(15), Melee2Ability.VOID_CHOP),
    WPN2_VOID_CLAW("wpn2_void_claw", "Void Claws", "claw", Rarity.MYTHIC,
            new Stats().dmg(165).str(125).cc(30).cd(150).intel(25).hp(35).def(15).spd(14), Melee2Ability.VOID_FRENZY),
    WPN2_VOID_WHIP("wpn2_void_whip", "Void Whip", "whip", Rarity.MYTHIC,
            new Stats().dmg(167).str(126).cc(32).cd(152).intel(25).hp(35).def(15), Melee2Ability.VOID_LASH),

    // ---------------- CEL theme ----------------
    WPN2_CEL_SWORD("wpn2_cel_sword", "Celestial Blade", "sword", Rarity.COMMON,
            new Stats().dmg(26).str(16).cc(10).cd(22), Melee2Ability.CEL_SLASH),
    WPN2_CEL_DAGGER("wpn2_cel_dagger", "Celestial Dagger", "dagger", Rarity.COMMON,
            new Stats().dmg(28).str(17).cc(12).cd(24).spd(8), Melee2Ability.CEL_STAB),
    WPN2_CEL_MACE("wpn2_cel_mace", "Celestial Mace", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(30).cc(16).cd(39).fer(6), Melee2Ability.CEL_CRUSH),
    WPN2_CEL_AXE("wpn2_cel_axe", "Celestial Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(48).str(31).cc(12).cd(41), Melee2Ability.CEL_REND),
    WPN2_CEL_SPEAR("wpn2_cel_spear", "Celestial Spear", "spear", Rarity.RARE,
            new Stats().dmg(66).str(42).cc(17).cd(53).hp(20).def(10), Melee2Ability.CEL_LUNGE),
    WPN2_CEL_HALBERD("wpn2_cel_halberd", "Celestial Halberd", "halberd", Rarity.RARE,
            new Stats().dmg(68).str(43).cc(19).cd(55).intel(15).hp(20).def(10), Melee2Ability.CEL_REACH),
    WPN2_CEL_KATANA("wpn2_cel_katana", "Celestial Katana", "katana", Rarity.EPIC,
            new Stats().dmg(90).str(60).cc(18).cd(70).intel(20).hp(20).def(10), Melee2Ability.CEL_DRAW),
    WPN2_CEL_SCYTHE("wpn2_cel_scythe", "Celestial Scythe", "scythe", Rarity.EPIC,
            new Stats().dmg(92).str(61).cc(20).cd(72).hp(20).def(10), Melee2Ability.CEL_REAP),
    WPN2_CEL_GREATSWORD("wpn2_cel_greatsword", "Celestial Greatsword", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(121).str(86).cc(26).cd(94).fer(6).intel(25).hp(35).def(15), Melee2Ability.CEL_SMASH),
    WPN2_CEL_CLEAVER("wpn2_cel_cleaver", "Celestial Cleaver", "cleaver", Rarity.LEGENDARY,
            new Stats().dmg(123).str(87).cc(22).cd(96).intel(25).hp(35).def(15), Melee2Ability.CEL_CHOP),
    WPN2_CEL_CLAW("wpn2_cel_claw", "Celestial Claws", "claw", Rarity.MYTHIC,
            new Stats().dmg(165).str(125).cc(30).cd(150).intel(25).hp(35).def(15).spd(14), Melee2Ability.CEL_FRENZY),
    WPN2_CEL_WHIP("wpn2_cel_whip", "Celestial Whip", "whip", Rarity.MYTHIC,
            new Stats().dmg(167).str(126).cc(32).cd(152).intel(25).hp(35).def(15), Melee2Ability.CEL_LASH),

    // ---------------- BLOOD theme ----------------
    WPN2_BLOOD_SWORD("wpn2_blood_sword", "Sanguine Blade", "sword", Rarity.COMMON,
            new Stats().dmg(26).str(16).cc(10).cd(22), Melee2Ability.BLOOD_SLASH),
    WPN2_BLOOD_DAGGER("wpn2_blood_dagger", "Sanguine Dagger", "dagger", Rarity.COMMON,
            new Stats().dmg(28).str(17).cc(12).cd(24).spd(8), Melee2Ability.BLOOD_STAB),
    WPN2_BLOOD_MACE("wpn2_blood_mace", "Sanguine Mace", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(30).cc(16).cd(39).fer(6), Melee2Ability.BLOOD_CRUSH),
    WPN2_BLOOD_AXE("wpn2_blood_axe", "Sanguine Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(48).str(31).cc(12).cd(41), Melee2Ability.BLOOD_REND),
    WPN2_BLOOD_SPEAR("wpn2_blood_spear", "Sanguine Spear", "spear", Rarity.RARE,
            new Stats().dmg(66).str(42).cc(17).cd(53).hp(20).def(10), Melee2Ability.BLOOD_LUNGE),
    WPN2_BLOOD_HALBERD("wpn2_blood_halberd", "Sanguine Halberd", "halberd", Rarity.RARE,
            new Stats().dmg(68).str(43).cc(19).cd(55).intel(15).hp(20).def(10), Melee2Ability.BLOOD_REACH),
    WPN2_BLOOD_KATANA("wpn2_blood_katana", "Sanguine Katana", "katana", Rarity.EPIC,
            new Stats().dmg(90).str(60).cc(18).cd(70).intel(20).hp(20).def(10), Melee2Ability.BLOOD_DRAW),
    WPN2_BLOOD_SCYTHE("wpn2_blood_scythe", "Sanguine Scythe", "scythe", Rarity.EPIC,
            new Stats().dmg(92).str(61).cc(20).cd(72).hp(20).def(10), Melee2Ability.BLOOD_REAP),
    WPN2_BLOOD_GREATSWORD("wpn2_blood_greatsword", "Sanguine Greatsword", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(121).str(86).cc(26).cd(94).fer(6).intel(25).hp(35).def(15), Melee2Ability.BLOOD_SMASH),
    WPN2_BLOOD_CLEAVER("wpn2_blood_cleaver", "Sanguine Cleaver", "cleaver", Rarity.LEGENDARY,
            new Stats().dmg(123).str(87).cc(22).cd(96).intel(25).hp(35).def(15), Melee2Ability.BLOOD_CHOP),
    WPN2_BLOOD_CLAW("wpn2_blood_claw", "Sanguine Claws", "claw", Rarity.MYTHIC,
            new Stats().dmg(165).str(125).cc(30).cd(150).intel(25).hp(35).def(15).spd(14), Melee2Ability.BLOOD_FRENZY),
    WPN2_BLOOD_WHIP("wpn2_blood_whip", "Sanguine Whip", "whip", Rarity.MYTHIC,
            new Stats().dmg(167).str(126).cc(32).cd(152).intel(25).hp(35).def(15), Melee2Ability.BLOOD_LASH),

    // ---------------- TECH theme ----------------
    WPN2_TECH_SWORD("wpn2_tech_sword", "Arc Blade", "sword", Rarity.COMMON,
            new Stats().dmg(26).str(16).cc(10).cd(22), Melee2Ability.TECH_SLASH),
    WPN2_TECH_DAGGER("wpn2_tech_dagger", "Arc Dagger", "dagger", Rarity.COMMON,
            new Stats().dmg(28).str(17).cc(12).cd(24).spd(8), Melee2Ability.TECH_STAB),
    WPN2_TECH_MACE("wpn2_tech_mace", "Arc Mace", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(30).cc(16).cd(39).fer(6), Melee2Ability.TECH_CRUSH),
    WPN2_TECH_AXE("wpn2_tech_axe", "Arc Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(48).str(31).cc(12).cd(41), Melee2Ability.TECH_REND),
    WPN2_TECH_SPEAR("wpn2_tech_spear", "Arc Spear", "spear", Rarity.RARE,
            new Stats().dmg(66).str(42).cc(17).cd(53).hp(20).def(10), Melee2Ability.TECH_LUNGE),
    WPN2_TECH_HALBERD("wpn2_tech_halberd", "Arc Halberd", "halberd", Rarity.RARE,
            new Stats().dmg(68).str(43).cc(19).cd(55).intel(15).hp(20).def(10), Melee2Ability.TECH_REACH),
    WPN2_TECH_KATANA("wpn2_tech_katana", "Arc Katana", "katana", Rarity.EPIC,
            new Stats().dmg(90).str(60).cc(18).cd(70).intel(20).hp(20).def(10), Melee2Ability.TECH_DRAW),
    WPN2_TECH_SCYTHE("wpn2_tech_scythe", "Arc Scythe", "scythe", Rarity.EPIC,
            new Stats().dmg(92).str(61).cc(20).cd(72).hp(20).def(10), Melee2Ability.TECH_REAP),
    WPN2_TECH_GREATSWORD("wpn2_tech_greatsword", "Arc Greatsword", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(121).str(86).cc(26).cd(94).fer(6).intel(25).hp(35).def(15), Melee2Ability.TECH_SMASH),
    WPN2_TECH_CLEAVER("wpn2_tech_cleaver", "Arc Cleaver", "cleaver", Rarity.LEGENDARY,
            new Stats().dmg(123).str(87).cc(22).cd(96).intel(25).hp(35).def(15), Melee2Ability.TECH_CHOP),
    WPN2_TECH_CLAW("wpn2_tech_claw", "Arc Claws", "claw", Rarity.MYTHIC,
            new Stats().dmg(165).str(125).cc(30).cd(150).intel(25).hp(35).def(15).spd(14), Melee2Ability.TECH_FRENZY),
    WPN2_TECH_WHIP("wpn2_tech_whip", "Arc Whip", "whip", Rarity.MYTHIC,
            new Stats().dmg(167).str(126).cc(32).cd(152).intel(25).hp(35).def(15), Melee2Ability.TECH_LASH),

    // ---------------- NAT theme ----------------
    WPN2_NAT_SWORD("wpn2_nat_sword", "Verdant Blade", "sword", Rarity.COMMON,
            new Stats().dmg(26).str(16).cc(10).cd(22), Melee2Ability.NAT_SLASH),
    WPN2_NAT_DAGGER("wpn2_nat_dagger", "Verdant Dagger", "dagger", Rarity.COMMON,
            new Stats().dmg(28).str(17).cc(12).cd(24).spd(8), Melee2Ability.NAT_STAB),
    WPN2_NAT_MACE("wpn2_nat_mace", "Verdant Mace", "mace", Rarity.UNCOMMON,
            new Stats().dmg(46).str(30).cc(16).cd(39).fer(6), Melee2Ability.NAT_CRUSH),
    WPN2_NAT_AXE("wpn2_nat_axe", "Verdant Axe", "axe", Rarity.UNCOMMON,
            new Stats().dmg(48).str(31).cc(12).cd(41), Melee2Ability.NAT_REND),
    WPN2_NAT_SPEAR("wpn2_nat_spear", "Verdant Spear", "spear", Rarity.RARE,
            new Stats().dmg(66).str(42).cc(17).cd(53).hp(20).def(10), Melee2Ability.NAT_LUNGE),
    WPN2_NAT_HALBERD("wpn2_nat_halberd", "Verdant Halberd", "halberd", Rarity.RARE,
            new Stats().dmg(68).str(43).cc(19).cd(55).intel(15).hp(20).def(10), Melee2Ability.NAT_REACH),
    WPN2_NAT_KATANA("wpn2_nat_katana", "Verdant Katana", "katana", Rarity.EPIC,
            new Stats().dmg(90).str(60).cc(18).cd(70).intel(20).hp(20).def(10), Melee2Ability.NAT_DRAW),
    WPN2_NAT_SCYTHE("wpn2_nat_scythe", "Verdant Scythe", "scythe", Rarity.EPIC,
            new Stats().dmg(92).str(61).cc(20).cd(72).hp(20).def(10), Melee2Ability.NAT_REAP),
    WPN2_NAT_GREATSWORD("wpn2_nat_greatsword", "Verdant Greatsword", "greatsword", Rarity.LEGENDARY,
            new Stats().dmg(121).str(86).cc(26).cd(94).fer(6).intel(25).hp(35).def(15), Melee2Ability.NAT_SMASH),
    WPN2_NAT_CLEAVER("wpn2_nat_cleaver", "Verdant Cleaver", "cleaver", Rarity.LEGENDARY,
            new Stats().dmg(123).str(87).cc(22).cd(96).intel(25).hp(35).def(15), Melee2Ability.NAT_CHOP),
    WPN2_NAT_CLAW("wpn2_nat_claw", "Verdant Claws", "claw", Rarity.MYTHIC,
            new Stats().dmg(165).str(125).cc(30).cd(150).intel(25).hp(35).def(15).spd(14), Melee2Ability.NAT_FRENZY),
    WPN2_NAT_WHIP("wpn2_nat_whip", "Verdant Whip", "whip", Rarity.MYTHIC,
            new Stats().dmg(167).str(126).cc(32).cd(152).intel(25).hp(35).def(15), Melee2Ability.NAT_LASH),

    ;

    public final String id;
    public final String displayName;
    public final String archetype;
    public final Rarity rarity;
    public final Stats stats;
    public final Melee2Ability ability;

    Melee2Weapon(String id, String displayName, String archetype, Rarity rarity,
                 Stats stats, Melee2Ability ability) {
        this.id = id;
        this.displayName = displayName;
        this.archetype = archetype;
        this.rarity = rarity;
        this.stats = stats;
        this.ability = ability;
    }

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
