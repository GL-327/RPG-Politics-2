# Melee Weapon Expansion

Package: `com.political.expansion.melee`
Item id prefix: `wpn_`

A self-contained set of **31 melee weapons** built entirely on the mod's existing
**Skyblock stat system** — there is **no vanilla base attack damage/speed** on any of them.
Every weapon:

- is registered as its own `net.minecraft.world.item.Item` (id `politicalserver:wpn_*`);
- carries all combat power as `custom_data` stats (`rpg_damage`, `rpg_strength`,
  `rpg_crit_chance`, `rpg_crit_damage`, `rpg_ferocity`, `rpg_intelligence`, `rpg_health`,
  `rpg_defense`, `rpg_speed`), resolved by `com.political.items.ItemStats` exactly like
  `RpgItem`s. `rpg_item_id` is stamped so `ItemStats` always uses the explicit-stat path;
- has `ATTRIBUTE_MODIFIERS` set to `EMPTY` (no `+N Attack Damage` / armor lines);
- sets `ITEM_MODEL` to its own texture model;
- has a rich Hypixel-Skyblock-style tooltip (`MeleeTooltipBuilder`, reusing
  `SkyblockTooltipBuilder`'s gear-score maths) including a unique **RIGHT CLICK** active ability.

## How it works

- `MeleeWeapon` — enum catalogue (name, archetype, rarity, stat bundle, ability).
- `MeleeAbility` — enum of the 31 unique active abilities (display, description, mana, cooldown).
  Kept in-package so the shared `ItemActiveAbility` enum is never edited.
- `MeleeWeapons` — registers the items, builds decorated stacks, exposes `register()` /
  `items()` / `displays()`.
- `MeleeAbilityEngine` — a self-contained `UseItemCallback` handler (registered from
  `MeleeWeapons.register()`) that resolves the held weapon's `MeleeAbility`, checks cooldown,
  spends Mana via `StatManager.spendMana`, and casts the effect. For any non-expansion item it
  returns `PASS`, so it coexists with the shared `ItemActiveAbilityEngine`.

## Integration checklist (for the integration agent)

1. In the mod initializer (`RpgPoliticsMod.onInitialize`), add:
   `com.political.expansion.melee.MeleeWeapons.register();`
   This also wires the right-click ability handler (no separate call needed).
2. In a creative tab (e.g. add to `CreativeCatalog.gear` or a new tab in `ModTabs`), emit the
   decorated display stacks:
   ```java
   for (net.minecraft.world.item.ItemStack s : com.political.expansion.melee.MeleeWeapons.displays())
       out.accept(s);
   ```
   (`MeleeWeapons.items()` returns the raw `List<Item>` if needed.)
3. Merge `tools/lang-fragments/melee.json` into `assets/politicalserver/lang/en_us.json`.
4. Textures/models/item-defs are already generated under
   `assets/politicalserver/{textures/item,models/item,items}/wpn_*`. Re-run
   `node tools/gen-melee.js` after any art change.

## Weapon catalogue

Stats shown are the authoritative `custom_data` base values (tooltips additionally show a
rarity-scaled total). DMG = damage, STR = strength, CC = crit chance %, CD = crit damage %,
FER = ferocity, INT = mana, HP/DEF = bonus health/defense.

### Common
| Weapon | Type | Stats | Ability (right click) |
|---|---|---|---|
| Iron Cutlass | sword | DMG 24, STR 14, CC 10, CD 20 | **Cleaving Slash** — cone slash |
| Bronze Dirk | dagger | DMG 20, STR 10, CC 14, CD 25, SPD 4 | **Quick Jab** — dash-stab |
| Stone Bludgeon | mace | DMG 28, STR 18, CD 30 | **Crushing Blow** — single hit + slow |
| Woodsman Hatchet | axe | DMG 26, STR 16, CD 25, FER 3 | **Timber Hack** — tight cone |
| Militia Spear | spear | DMG 22, STR 12, CC 8, CD 20 | **Braced Lunge** — lunge + knockback |

### Uncommon
| Weapon | Type | Stats | Ability (right click) |
|---|---|---|---|
| Steel Saber | sword | DMG 40, STR 26, CC 12, CD 35 | **Riposte** — cone + Resistance |
| Hunter's Kris | dagger | DMG 34, STR 20, CC 18, CD 40, SPD 6 | **Bleeding Stab** — heavy Poison |
| Bearded Axe | axe | DMG 44, STR 30, CD 40, FER 4 | **Rend** — cone + Weakness |
| War Pike | spear | DMG 38, STR 22, CC 10, CD 30 | **Skewer** — ranged impale + pull-in |
| Iron Morningstar | mace | DMG 46, STR 32, CD 45 | **Concussion** — AoE Slowness |

### Rare
| Weapon | Type | Stats | Ability (right click) |
|---|---|---|---|
| Knight's Longsword | sword | DMG 58, STR 38, CC 15, CD 45, HP 20, DEF 10 | **Valiant Sweep** — wide arc + knockback |
| Shadow Kunai | dagger | DMG 52, STR 34, CC 20, CD 55, SPD 8, INT 15 | **Shadow Flicker** — blink behind + strike |
| Storm Glaive | halberd | DMG 62, STR 40, CC 14, CD 40, INT 20 | **Static Reach** — long line shock |
| Tempest Katana | katana | DMG 56, STR 36, CC 18, CD 50, SPD 6 | **Iaido Draw** — draw-cut bleed + slow |
| Grave Scythe | scythe | DMG 60, STR 42, CC 12, CD 45, INT 15 | **Reaping Arc** — withering crescent |

### Epic
| Weapon | Type | Stats | Ability (right click) |
|---|---|---|---|
| Dragonbone Greatsword | greatsword | DMG 82, STR 58, CC 18, CD 60, FER 5, HP 30, DEF 15 | **Earthshatter** — AoE slam + launch |
| Venom Claws | claw | DMG 74, STR 50, CC 24, CD 55, FER 8, SPD 10 | **Rending Frenzy** — double-hit Poison cone |
| Frost Cleaver | cleaver | DMG 88, STR 64, CC 16, CD 55, INT 20 | **Permafrost Chop** — AoE freeze |
| Ember Waraxe | axe | DMG 90, STR 66, CD 70, FER 6, INT 15 | **Magma Cleave** — igniting cone |
| Serpent Whip | whip | DMG 70, STR 48, CC 20, CD 60, SPD 8 | **Lashing Coil** — reel-in cone |
| Thunderspike Spear | spear | DMG 80, STR 54, CC 20, CD 58, INT 25 | **Levin Throw** — lightning at target |

### Legendary
| Weapon | Type | Stats | Ability (right click) |
|---|---|---|---|
| Celestial Claymore | greatsword | DMG 110, STR 82, CC 22, CD 80, FER 10, HP 40, DEF 20, INT 20 | **Comet Smash** — massive AoE + launch |
| Moonshadow Katana | katana | DMG 100, STR 74, CC 28, CD 90, SPD 12, INT 25 | **Crescent Eclipse** — heavy crit arc + slow |
| Soulreaper Scythe | scythe | DMG 108, STR 80, CC 20, CD 80, INT 40 | **Soul Harvest** — AoE wither + lifesteal |
| Warlord's Halberd | halberd | DMG 112, STR 86, CC 18, CD 75, FER 10, INT 20 | **Sweeping Vortex** — pull + cone |
| Titanbreaker Maul | mace | DMG 120, STR 95, CD 95, FER 12, HP 30, DEF 15 | **Seismic Slam** — quake stun + launch |
| Phantom Daggers | dagger | DMG 96, STR 72, CC 30, CD 95, SPD 16, INT 30 | **Thousand Cuts** — blink-strike all nearby |

### Mythic
| Weapon | Type | Stats | Ability (right click) |
|---|---|---|---|
| Godslayer Blade | sword | DMG 150, STR 120, CC 30, CD 140, FER 20, INT 60 | **Divine Execution** — execute + holy nova |
| Voidrend Greatsword | greatsword | DMG 158, STR 130, CC 28, CD 130, FER 18, INT 50, HP 40, DEF 20 | **Oblivion Rift** — void implosion + wither |
| Ragnarok Axe | axe | DMG 160, STR 128, CD 150, FER 25, INT 40 | **Cataclysm** — fire eruption + launch |
| Eternity Scythe | scythe | DMG 152, STR 122, CC 26, CD 135, INT 70 | **Reap the Eternal** — massive wither + lifesteal |
