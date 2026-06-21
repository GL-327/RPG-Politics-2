# Expansion2 Melee Weapons (Phase 2)

Package: `com.political.expansion2.melee`
Item id prefix: `wpn2_`

**96 melee weapons** across 8 visual themes (JJK cursed tools, Prominence, elemental,
void, celestial, blood, tech, nature). Skyblock stats only — no vanilla attack modifiers.

## Integration

1. `RpgPoliticsMod.onInitialize`: `com.political.expansion2.melee.Melee2Weapons.register();`
2. Creative tab: `for (ItemStack s : Melee2Weapons.displays()) out.accept(s);`
3. Merge `tools/lang-fragments/melee2.json` into `en_us.json`.
4. Textures: `node tools/gen-melee2.js`

## Catalogue (96 weapons)

| ID | Name | Theme | Type | Rarity | Ability |
|---|---|---|---|---|---|
| `wpn2_cursed_sword` | Cursed Blade | cursed | sword | COMMON | **Domain Expansion Cut** |
| `wpn2_cursed_dagger` | Cursed Dagger | cursed | dagger | COMMON | **Cursed Energy Dagger** |
| `wpn2_cursed_mace` | Cursed Mace | cursed | mace | UNCOMMON | **Inverted Mace** |
| `wpn2_cursed_axe` | Cursed Axe | cursed | axe | UNCOMMON | **Black Flash Axe** |
| `wpn2_cursed_spear` | Cursed Spear | cursed | spear | RARE | **Shrine Spear** |
| `wpn2_cursed_halberd` | Cursed Halberd | cursed | halberd | RARE | **Ten Shadows Halberd** |
| `wpn2_cursed_katana` | Cursed Katana | cursed | katana | EPIC | **Limitless Katana** |
| `wpn2_cursed_scythe` | Cursed Scythe | cursed | scythe | EPIC | **Malevolent Scythe** |
| `wpn2_cursed_greatsword` | Cursed Greatsword | cursed | greatsword | LEGENDARY | **Shibuya Greatsword** |
| `wpn2_cursed_cleaver` | Cursed Cleaver | cursed | cleaver | LEGENDARY | **Jujutsu Cleaver** |
| `wpn2_cursed_claw` | Cursed Claws | cursed | claw | MYTHIC | **Grade One Claw** |
| `wpn2_cursed_whip` | Cursed Whip | cursed | whip | MYTHIC | **Special Grade Whip** |
| `wpn2_prom_sword` | Prominence Blade | prom | sword | COMMON | **Radiant Cut** |
| `wpn2_prom_dagger` | Prominence Dagger | prom | dagger | COMMON | **Solar Dagger** |
| `wpn2_prom_mace` | Prominence Mace | prom | mace | UNCOMMON | **Nova Mace** |
| `wpn2_prom_axe` | Prominence Axe | prom | axe | UNCOMMON | **Ascendant Axe** |
| `wpn2_prom_spear` | Prominence Spear | prom | spear | RARE | **Paragon Spear** |
| `wpn2_prom_halberd` | Prominence Halberd | prom | halberd | RARE | **Zenith Halberd** |
| `wpn2_prom_katana` | Prominence Katana | prom | katana | EPIC | **Apex Katana** |
| `wpn2_prom_scythe` | Prominence Scythe | prom | scythe | EPIC | **Luminous Scythe** |
| `wpn2_prom_greatsword` | Prominence Greatsword | prom | greatsword | LEGENDARY | **Halcyon Greatsword** |
| `wpn2_prom_cleaver` | Prominence Cleaver | prom | cleaver | LEGENDARY | **Empyrean Cleaver** |
| `wpn2_prom_claw` | Prominence Claws | prom | claw | MYTHIC | **Transcendent Claw** |
| `wpn2_prom_whip` | Prominence Whip | prom | whip | MYTHIC | **Apotheosis Whip** |
| `wpn2_elem_sword` | Primal Blade | elem | sword | COMMON | **Inferno Cut** |
| `wpn2_elem_dagger` | Primal Dagger | elem | dagger | COMMON | **Glacier Dagger** |
| `wpn2_elem_mace` | Primal Mace | elem | mace | UNCOMMON | **Tempest Mace** |
| `wpn2_elem_axe` | Primal Axe | elem | axe | UNCOMMON | **Tidal Axe** |
| `wpn2_elem_spear` | Primal Spear | elem | spear | RARE | **Quake Spear** |
| `wpn2_elem_halberd` | Primal Halberd | elem | halberd | RARE | **Plasma Halberd** |
| `wpn2_elem_katana` | Primal Katana | elem | katana | EPIC | **Magma Katana** |
| `wpn2_elem_scythe` | Primal Scythe | elem | scythe | EPIC | **Cryo Scythe** |
| `wpn2_elem_greatsword` | Primal Greatsword | elem | greatsword | LEGENDARY | **Storm Greatsword** |
| `wpn2_elem_cleaver` | Primal Cleaver | elem | cleaver | LEGENDARY | **Ember Cleaver** |
| `wpn2_elem_claw` | Primal Claws | elem | claw | MYTHIC | **Frostfire Claw** |
| `wpn2_elem_whip` | Primal Whip | elem | whip | MYTHIC | **Cataclysm Whip** |
| `wpn2_void_sword` | Void Blade | void | sword | COMMON | **Rift Cut** |
| `wpn2_void_dagger` | Void Dagger | void | dagger | COMMON | **Null Dagger** |
| `wpn2_void_mace` | Void Mace | void | mace | UNCOMMON | **Oblivion Mace** |
| `wpn2_void_axe` | Void Axe | void | axe | UNCOMMON | **Abyssal Axe** |
| `wpn2_void_spear` | Void Spear | void | spear | RARE | **Event Horizon Spear** |
| `wpn2_void_halberd` | Void Halberd | void | halberd | RARE | **Entropy Halberd** |
| `wpn2_void_katana` | Void Katana | void | katana | EPIC | **Annihilation Katana** |
| `wpn2_void_scythe` | Void Scythe | void | scythe | EPIC | **Singularity Scythe** |
| `wpn2_void_greatsword` | Void Greatsword | void | greatsword | LEGENDARY | **Eclipse Greatsword** |
| `wpn2_void_cleaver` | Void Cleaver | void | cleaver | LEGENDARY | **Dark Matter Cleaver** |
| `wpn2_void_claw` | Void Claws | void | claw | MYTHIC | **Phase Claw** |
| `wpn2_void_whip` | Void Whip | void | whip | MYTHIC | **Unmaking Whip** |
| `wpn2_cel_sword` | Celestial Blade | cel | sword | COMMON | **Starfall Cut** |
| `wpn2_cel_dagger` | Celestial Dagger | cel | dagger | COMMON | **Nebula Dagger** |
| `wpn2_cel_mace` | Celestial Mace | cel | mace | UNCOMMON | **Cosmic Mace** |
| `wpn2_cel_axe` | Celestial Axe | cel | axe | UNCOMMON | **Aurora Axe** |
| `wpn2_cel_spear` | Celestial Spear | cel | spear | RARE | **Supernova Spear** |
| `wpn2_cel_halberd` | Celestial Halberd | cel | halberd | RARE | **Orbit Halberd** |
| `wpn2_cel_katana` | Celestial Katana | cel | katana | EPIC | **Pulsar Katana** |
| `wpn2_cel_scythe` | Celestial Scythe | cel | scythe | EPIC | **Quasar Scythe** |
| `wpn2_cel_greatsword` | Celestial Greatsword | cel | greatsword | LEGENDARY | **Lunar Greatsword** |
| `wpn2_cel_cleaver` | Celestial Cleaver | cel | cleaver | LEGENDARY | **Solar Wind Cleaver** |
| `wpn2_cel_claw` | Celestial Claws | cel | claw | MYTHIC | **Constellation Claw** |
| `wpn2_cel_whip` | Celestial Whip | cel | whip | MYTHIC | **Divine Whip** |
| `wpn2_blood_sword` | Sanguine Blade | blood | sword | COMMON | **Hemorrhage Cut** |
| `wpn2_blood_dagger` | Sanguine Dagger | blood | dagger | COMMON | **Crimson Dagger** |
| `wpn2_blood_mace` | Sanguine Mace | blood | mace | UNCOMMON | **Sanguine Mace** |
| `wpn2_blood_axe` | Sanguine Axe | blood | axe | UNCOMMON | **Exsanguinate Axe** |
| `wpn2_blood_spear` | Sanguine Spear | blood | spear | RARE | **Vitae Spear** |
| `wpn2_blood_halberd` | Sanguine Halberd | blood | halberd | RARE | **Carnage Halberd** |
| `wpn2_blood_katana` | Sanguine Katana | blood | katana | EPIC | **Thirst Katana** |
| `wpn2_blood_scythe` | Sanguine Scythe | blood | scythe | EPIC | **Coagulate Scythe** |
| `wpn2_blood_greatsword` | Sanguine Greatsword | blood | greatsword | LEGENDARY | **Arterial Greatsword** |
| `wpn2_blood_cleaver` | Sanguine Cleaver | blood | cleaver | LEGENDARY | **Scarlet Cleaver** |
| `wpn2_blood_claw` | Sanguine Claws | blood | claw | MYTHIC | **Transfusion Claw** |
| `wpn2_blood_whip` | Sanguine Whip | blood | whip | MYTHIC | **Requiem Whip** |
| `wpn2_tech_sword` | Arc Blade | tech | sword | COMMON | **Pulse Cut** |
| `wpn2_tech_dagger` | Arc Dagger | tech | dagger | COMMON | **Overclock Dagger** |
| `wpn2_tech_mace` | Arc Mace | tech | mace | UNCOMMON | **Circuit Mace** |
| `wpn2_tech_axe` | Arc Axe | tech | axe | UNCOMMON | **Ion Axe** |
| `wpn2_tech_spear` | Arc Spear | tech | spear | RARE | **Plasma Spear** |
| `wpn2_tech_halberd` | Arc Halberd | tech | halberd | RARE | **Rail Halberd** |
| `wpn2_tech_katana` | Arc Katana | tech | katana | EPIC | **Photon Katana** |
| `wpn2_tech_scythe` | Arc Scythe | tech | scythe | EPIC | **Quantum Scythe** |
| `wpn2_tech_greatsword` | Arc Greatsword | tech | greatsword | LEGENDARY | **Nano Greatsword** |
| `wpn2_tech_cleaver` | Arc Cleaver | tech | cleaver | LEGENDARY | **Fusion Cleaver** |
| `wpn2_tech_claw` | Arc Claws | tech | claw | MYTHIC | **Voltage Claw** |
| `wpn2_tech_whip` | Arc Whip | tech | whip | MYTHIC | **Singularity Whip** |
| `wpn2_nat_sword` | Verdant Blade | nat | sword | COMMON | **Thorn Cut** |
| `wpn2_nat_dagger` | Verdant Dagger | nat | dagger | COMMON | **Bloom Dagger** |
| `wpn2_nat_mace` | Verdant Mace | nat | mace | UNCOMMON | **Root Mace** |
| `wpn2_nat_axe` | Verdant Axe | nat | axe | UNCOMMON | **Canopy Axe** |
| `wpn2_nat_spear` | Verdant Spear | nat | spear | RARE | **Spore Spear** |
| `wpn2_nat_halberd` | Verdant Halberd | nat | halberd | RARE | **Pollen Halberd** |
| `wpn2_nat_katana` | Verdant Katana | nat | katana | EPIC | **Grove Katana** |
| `wpn2_nat_scythe` | Verdant Scythe | nat | scythe | EPIC | **Wild Scythe** |
| `wpn2_nat_greatsword` | Verdant Greatsword | nat | greatsword | LEGENDARY | **Feral Greatsword** |
| `wpn2_nat_cleaver` | Verdant Cleaver | nat | cleaver | LEGENDARY | **Moss Cleaver** |
| `wpn2_nat_claw` | Verdant Claws | nat | claw | MYTHIC | **Petal Claw** |
| `wpn2_nat_whip` | Verdant Whip | nat | whip | MYTHIC | **Overgrowth Whip** |
