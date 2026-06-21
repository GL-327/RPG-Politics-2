# Expansion 2 — Powers & Techniques (76 new abilities)

Owner package: `com.political.expansion2.powers.**`  
Mixin-free Fabric / MC 26.2. Does **not** modify `Power.java` — uses parallel `Power2` enum + `PowerManager2`.

References: **viltrumitecore** (dash, mega punch, block, grab, supersonic, orbital mechanics), **JJK pack** (character techniques + named domains), existing `Power`/`PowerManager` patterns.

---

## Package layout

| Class | Role |
| --- | --- |
| `Power2` | Enum — 76 powers across 6 categories |
| `PowerManager2` | Cast logic, cooldowns, passive toggles, lingering domains |
| `Power2Effects` | Shared beam/cone/domain helpers |
| `PowerBridge` | Routes activation/GUI actions to Power2 or base Power |
| `Power2Commands` | Optional `/power2 list|learn|grant` |
| `Powers2` | `register()`, `registerClient()`, `allIds()` |
| `PowersScreen2` | Extended GUI with 8 tabs (client) |

---

## Power roster (76 total)

### Viltrumite (15) — Mana

| id | name | cost | cd(s) |
| --- | --- | --- | --- |
| `viltrumite_dash` | Viltrumite Dash | 14 | 1.5 |
| `viltrumite_mega_punch` | Mega Punch | 22 | 2.5 |
| `viltrumite_block` | Viltrumite Block | 18 | 6 |
| `viltrumite_grab_slam` | Grab & Slam | 28 | 3.5 |
| `viltrumite_orbital_strike` | Orbital Strike | 45 | 8 |
| `viltrumite_impact_crater` | Impact Crater | 38 | 7 |
| `viltrumite_rage_surge` | Rage Surge | 32 | 9 |
| `viltrumite_aerial_bombardment` | Aerial Bombardment | 40 | 6.5 |
| `viltrumite_thunderclap` | Thunderclap | 24 | 4 |
| `viltrumite_skull_crush` | Skull Crush | 36 | 5 |
| `viltrumite_suborbital_dive` | Suborbital Dive | 42 | 7.5 |
| `viltrumite_war_cry` | War Cry | 20 | 5 |
| `viltrumite_bone_shatter` | Bone Shatter | 30 | 4.5 |
| `viltrumite_momentum_ram` | Momentum Ram | 26 | 5.5 |
| `viltrumite_supersonic` | Supersonic | 34 | 10 |

### Hero (15) — Mana

| id | name | cost | cd(s) |
| --- | --- | --- | --- |
| `homelander_beam` | Homelander Beam | 30 | 2.25 |
| `atom_eve_shift` | Atom Eve: Matter Shift | 28 | 5 |
| `a_train_blur` | A-Train Blur | 22 | 4 |
| `queen_maevs_counter` | Queen Maeve's Counter | 26 | 6 |
| `translucent_vanish` | Translucent: Phase Vanish | 24 | 7 |
| `starlight_bolt` | Starlight Bolt | 20 | 3 |
| `black_noir_strike` | Black Noir: Silent Strike | 32 | 4.5 |
| `soldier_boy_charge` | Soldier Boy: Charge | 38 | 6.5 |
| `deep_tidal_crush` | The Deep: Tidal Crush | 26 | 5 |
| `mm_suppress` | Mother's Milk: Suppress | 22 | 5.5 |
| `frenchie_arsenal` | Frenchie's Arsenal | 24 | 3.5 |
| `kimiko_blade_storm` | Kimiko: Blade Storm | 28 | 4 |
| `butcher_berserk` | Butcher's Berserk | 30 | 8 |
| `ryan_outburst` | Ryan's Outburst | 50 | 10 |
| `neo_noir_echo` | Neo Noir Echo | 34 | 7.5 |

### JJK Technique II (20) — Cursed Energy, grade-gated

| id | name | cost | cd(s) | grade |
| --- | --- | --- | --- | --- |
| `straw_doll` | Straw Doll Technique | 28 | 4 | 1 |
| `hairpin` | Hairpin | 22 | 2.5 | 1 |
| `boogie_woogie` | Boogie Woogie | 24 | 3 | 2 |
| `copy_technique` | Copy | 40 | 8 | 4 |
| `rika_manifest` | Rika Manifest | 55 | 11 | 4 |
| `star_rage` | Star Rage | 35 | 6 | 3 |
| `judgeman` | Judgeman | 45 | 9 | 4 |
| `confiscation` | Confiscation | 50 | 10 | 4 |
| `cursed_spirit_manipulation` | Cursed Spirit Manipulation | 48 | 12 | 4 |
| `disaster_plants` | Disaster Plants | 32 | 5.5 | 3 |
| `flowing_red_scale` | Flowing Red Scale | 30 | 5 | 2 |
| `antigravity_system` | Antigravity System | 38 | 6.5 | 4 |
| `bird_strike` | Bird Strike | 26 | 3.5 | 2 |
| `ice_formation` | Ice Formation | 28 | 4.5 | 2 |
| `thunder_inspection` | Thunder Inspection | 34 | 5 | 3 |
| `soul_multiplicity` | Soul Multiplicity | 42 | 7.5 | 3 |
| `blood_boiling` | Blood Boiling | 30 | 4 | 2 |
| `curse_collage` | Curse Collation | 44 | 9 | 4 |
| `death_swarming` | Death Swarming | 36 | 6 | 3 |
| `gamma_ray` | Gamma Ray | 60 | 13 | 5 |

### Domain Expansion (10) — CE, visual AoE ring + 8s lingering debuff ticks

| id | name | cost | cd(s) | grade |
| --- | --- | --- | --- | --- |
| `domain_infinite_void` | Domain: Infinite Void | 110 | 35 | 5 |
| `domain_time_cell` | Domain: Time Cell Moon Palace | 100 | 32.5 | 5 |
| `domain_handmade` | Domain: Captivating Handmade | 95 | 30 | 4 |
| `domain_mutual_love` | Domain: Authentic Mutual Love | 105 | 34 | 5 |
| `domain_deadly_sentencing` | Domain: Deadly Sentencing | 90 | 31 | 4 |
| `domain_cerebral` | Domain: Cerebral Binding | 85 | 29 | 4 |
| `domain_idle_gamble` | Domain: Idle Death Gamble | 100 | 32 | 5 |
| `domain_womb` | Domain: Womb Profusion | 95 | 30 | 5 |
| `domain_thunder_gaais` | Domain: Thunder Gaia | 88 | 28 | 4 |
| `domain_horizon` | Domain: Horizon of the Grau | 92 | 29.5 | 4 |

Domains: instant burst damage + particle ring on cast; `DOMAINS` map ticks debuff/damage every second for 8s (mixin-free).

### Ultimate (8)

| id | name | cost | cd(s) | resource |
| --- | --- | --- | --- | --- |
| `ultimate_purple_storm` | Ultimate: Purple Storm | 100 | 25 | CE (grade 5) |
| `ultimate_maximum_uzumaki` | Ultimate: Maximum Uzumaki | 95 | 24 | CE (grade 5) |
| `ultimate_merged_beast` | Ultimate: Merger Beast | 85 | 21 | CE (grade 4) |
| `ultimate_meteor_storm` | Ultimate: Meteor Storm | 105 | 26 | CE (grade 5) |
| `ultimate_open_shrine` | Ultimate: Open Shrine | 115 | 28 | CE (grade 5) |
| `ultimate_star_fall` | Ultimate: Star Fall | 90 | 23 | CE (grade 4) |
| `ultimate_viltrumite_apocalypse` | Ultimate: Viltrumite Apocalypse | 80 | 20 | Mana |
| `ultimate_hero_squad` | Ultimate: Hero Squad | 70 | 19 | Mana |

### Passive toggles (8) — activate again to disable

| id | name | effect |
| --- | --- | --- |
| `passive_ce_efficiency` | CE Efficiency | +1 CE/s, −15% technique cost (grade 2) |
| `passive_battle_frenzy` | Battle Frenzy | Strength I below 50% HP |
| `passive_shadow_affinity` | Shadow Affinity | −15% CD on JJK/domain powers |
| `passive_regen_aura` | Regenerative Aura | +0.5 HP/s |
| `passive_grade_pressure` | Grade Pressure | Weakness aura vs lower-grade foes |
| `passive_flight_mastery` | Flight Mastery | (doc hook for FlightClient throttle) |
| `passive_curse_resist` | Curse Resistance | Clears poison/wither/weakness |
| `passive_black_flash_mastery` | Black Flash Mastery | Strength I while active |

---

## Integration checklist (required)

### 1. Common init — `RpgPoliticsMod.onInitialize()`

```java
com.political.expansion2.powers.Powers2.register();
```

Inside command registration:

```java
com.political.expansion2.powers.Powers2.registerCommands(dispatcher);
```

### 2. Client init — `PoliticalClient.onInitializeClient()`

```java
com.political.expansion2.powers.Powers2.registerClient();
```

**Option A — extended GUI:** in the `PowerMenuS2C` receiver, open `PowersScreen2` instead of `PowersScreen`:

```java
if (PowersScreen2.OPEN != null) PowersScreen2.OPEN.apply(payload);
else context.client().setScreenAndShow(new PowersScreen2(payload));
```

**Option B — merge tabs:** copy tab + `Power2.ofCategory()` loop from `PowersScreen2` into `PowersScreen` (append-only).

### 3. PowerManager bridge

In `PowerManager.activateSelected()` **first line**:

```java
Component p2 = com.political.expansion2.powers.PowerManager2.activateSelected(player);
if (p2 != null) return p2;
```

Or replace calls with `PowerBridge.activateSelected(player)`.

In `ModNetworking` `ActivatePowerC2S` receiver and `PowerManager.handleAction()`:

```java
com.political.expansion2.powers.PowerBridge.handleAction(player, action, powerId);
```

### 4. PowerCommands

- `/power list` and `/power info`: also iterate `Power2.values()` or delegate to `/power2 list`.
- `/cursed learn <id>`: if `Power2.byId(id) != null`, check `Power2.minGrade` instead of `TECHNIQUE_GRADE`.
- `/power grant`: accept Power2 ids via `Power2.byId`.
- `PowerCommands.known`: show Power2 names when id not in base `Power`.

### 5. StatManager CE bonus

Where `Power.cursedEnergyBonus()` is summed, add:

```java
bonus += com.political.expansion2.powers.Power2.cursedEnergyBonus(knownPowerIds);
```

### 6. Disconnect cleanup — `RpgPoliticsMod` player disconnect

```java
com.political.expansion2.powers.PowerManager2.onPlayerRemoved(uuid);
```

### 7. Lang

Merge `tools/lang-fragments/powers2.json` → `assets/politicalserver/lang/en_us.json`.

### 8. Compound V pool (optional)

Add Viltrumite/Hero Power2 ids to serum roll pool in `Serums` or grant on `/v compound` with low weight.

---

## All power ids (`Powers2.allIds()`)

```
viltrumite_dash, viltrumite_mega_punch, viltrumite_block, viltrumite_grab_slam,
viltrumite_orbital_strike, viltrumite_impact_crater, viltrumite_rage_surge,
viltrumite_aerial_bombardment, viltrumite_thunderclap, viltrumite_skull_crush,
viltrumite_suborbital_dive, viltrumite_war_cry, viltrumite_bone_shatter,
viltrumite_momentum_ram, viltrumite_supersonic,
homelander_beam, atom_eve_shift, a_train_blur, queen_maevs_counter, translucent_vanish,
starlight_bolt, black_noir_strike, soldier_boy_charge, deep_tidal_crush, mm_suppress,
frenchie_arsenal, kimiko_blade_storm, butcher_berserk, ryan_outburst, neo_noir_echo,
straw_doll, hairpin, boogie_woogie, copy_technique, rika_manifest, star_rage, judgeman,
confiscation, cursed_spirit_manipulation, disaster_plants, flowing_red_scale,
antigravity_system, bird_strike, ice_formation, thunder_inspection, soul_multiplicity,
blood_boiling, curse_collage, death_swarming, gamma_ray,
domain_infinite_void, domain_time_cell, domain_handmade, domain_mutual_love,
domain_deadly_sentencing, domain_cerebral, domain_idle_gamble, domain_womb,
domain_thunder_gaais, domain_horizon,
ultimate_purple_storm, ultimate_maximum_uzumaki, ultimate_merged_beast, ultimate_meteor_storm,
ultimate_open_shrine, ultimate_star_fall, ultimate_viltrumite_apocalypse, ultimate_hero_squad,
passive_ce_efficiency, passive_battle_frenzy, passive_shadow_affinity, passive_regen_aura,
passive_grade_pressure, passive_flight_mastery, passive_curse_resist, passive_black_flash_mastery
```

---

## Testing

1. `/power2 grant viltrumite_dash` → select → R key activates blink.
2. `/cursed awaken` then `/power2 learn straw_doll` (grade 1).
3. `/power2 grant domain_infinite_void` → activate → particle ring + blind/slow on mobs for ~8s.
4. `/power2 grant passive_battle_frenzy` → activate twice toggles on/off.
5. Open Powers menu — Expansion 2 nodes appear when using `PowersScreen2`.

No Gradle changes. No mixin. No edits to forbidden client files unless integration agent merges GUI tabs.
