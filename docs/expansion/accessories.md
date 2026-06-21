# Accessories & Consumables Expansion

New package: `com.political.expansion.accessories`
Item id prefix: `acc_`
No mixins. Nothing in the shared stat pipeline (`StatManager`, `ItemStats`) is edited.

## Overview

This expansion adds **26 Skyblock-style accessories** (talismans / rings / amulets /
charms / bands / totems / artifacts / relics) and **24 consumables** (potions / elixirs /
vials / foods / scrolls) — 50 new items total.

Accessories grant their bonuses **passively while simply held anywhere in the player's
inventory** (Prominence-II / Hypixel-Skyblock style — no equip slot required). A
self-contained inventory-tick handler registered in `Accessories.register()` scans every
online player's inventory once per second and applies the aggregated bonuses:

- **Stats** → vanilla attribute modifiers using this package's **own** modifier IDs
  (`acc_health`, `acc_armor`, `acc_toughness`, `acc_knockback`, `acc_strength`,
  `acc_speed`, `acc_attack_speed`, `acc_luck`). These never clash with `StatManager`'s
  `rpg_*` modifiers, so both systems stack cleanly and removing an accessory removes its
  bonus on the next tick.
- **Effects** → vanilla `MobEffects`, refreshed each second (60-tick duration so they
  decay shortly after the accessory leaves the inventory).
- **Mana / Cursed Energy regen** → trickled via the existing public
  `StatManager.addMana(...)` / `StatManager.addCursedEnergy(...)` API.

Duplicate copies of the same accessory only count **once** (no stacking exploit).

Consumables fire server-side through `UseItemCallback` (right-click), then the stack is
shrunk (unless creative), mirroring `RelicItems` / `ModItems`.

Item names + Skyblock-style lore are stamped onto stacks lazily during the inventory scan
(and via `Accessories.display(item)` for creative tabs), flagged with `acc_dec` in custom
data so decoration only happens once per stack.

## Stat → attribute mapping

| Accessory stat   | Vanilla attribute      | Operation            | Scale          |
|------------------|------------------------|----------------------|----------------|
| health           | `MAX_HEALTH`           | ADD_VALUE            | x1             |
| defense          | `ARMOR`                | ADD_VALUE            | x0.15          |
| toughness        | `ARMOR_TOUGHNESS`      | ADD_VALUE            | x1             |
| knockbackResist  | `KNOCKBACK_RESISTANCE` | ADD_VALUE            | x1 (0..1)      |
| strength         | `ATTACK_DAMAGE`        | ADD_VALUE            | x0.05          |
| speed            | `MOVEMENT_SPEED`       | ADD_MULTIPLIED_BASE  | x0.005         |
| attackSpeed      | `ATTACK_SPEED`         | ADD_MULTIPLIED_BASE  | x0.01          |
| luck             | `LUCK`                 | ADD_VALUE            | x1             |
| critChance/critDamage/ferocity | *(tooltip only)*  | —          | informational  |
| manaRegen        | `StatManager.addMana`  | per second           | flat/s         |
| cursedRegen      | `StatManager.addCursedEnergy` | per second    | flat/s         |

## Accessories (id · type · rarity · effect)

| Id | Type | Rarity | Effect |
|----|------|--------|--------|
| `acc_warding_talisman` | Talisman | Uncommon | +20 Health, +15 Defense |
| `acc_vigor_ring` | Ring | Uncommon | +40 Health |
| `acc_berserker_band` | Band | Rare | +25 Strength, +8 Ferocity |
| `acc_swiftness_charm` | Charm | Uncommon | +30 Speed, Speed I |
| `acc_scholar_amulet` | Amulet | Rare | +4 Mana/s |
| `acc_cursed_seal` | Talisman | Epic | +3 Cursed Energy/s |
| `acc_titan_heart` | Artifact | Epic | +80 Health, +30 Defense, +4 Toughness |
| `acc_assassin_ring` | Ring | Rare | +15% Crit Chance, +40% Crit Damage, +10 Speed |
| `acc_bruiser_totem` | Totem | Rare | +50 Health, +15 Strength, +40% Knockback Resist |
| `acc_phoenix_charm` | Charm | Epic | +30 Health, Fire Resistance |
| `acc_aqua_pendant` | Amulet | Uncommon | Water Breathing, Dolphin's Grace |
| `acc_owl_talisman` | Talisman | Uncommon | Night Vision |
| `acc_feather_charm` | Charm | Rare | +10 Speed, Slow Falling, Jump Boost II |
| `acc_miners_band` | Band | Uncommon | +2 Luck, Haste II |
| `acc_lucky_clover` | Charm | Rare | +5 Luck, Luck II |
| `acc_guardian_artifact` | Artifact | Legendary | +60 Health, +60 Defense, Resistance I |
| `acc_warlords_signet` | Ring | Legendary | +40 Strength, +60% Crit Damage, +12 Ferocity |
| `acc_arcane_orb` | Artifact | Epic | +10 Mana/s |
| `acc_vampiric_charm` | Charm | Epic | +20 Strength, Regeneration I |
| `acc_golem_core` | Artifact | Legendary | +120 Health, +50 Defense, +60% Knockback Resist, +6 Toughness |
| `acc_windrunner_anklet` | Ring | Rare | +40 Speed, +20% Attack Speed, Jump Boost I |
| `acc_dragon_scale` | Talisman | Legendary | +50 Health, +40 Defense, Fire Resistance |
| `acc_soul_lantern_charm` | Charm | Epic | +2 Cursed Energy/s, Night Vision |
| `acc_executioners_emblem` | Talisman | Epic | +20 Strength, +80% Crit Damage |
| `acc_sentinel_aegis` | Artifact | Mythic | +150 Health, +80 Defense, +8 Toughness, Resistance I |
| `acc_godslayer_relic` | Relic | Mythic | +60 Strength, +20% Crit Chance, +100% Crit Damage, +20 Ferocity, Strength I |

## Consumables (id · type · rarity · effect)

| Id | Type | Rarity | Effect |
|----|------|--------|--------|
| `acc_minor_healing_potion` | Potion | Common | Heal 40% max Health |
| `acc_greater_healing_potion` | Potion | Uncommon | Heal to full |
| `acc_mana_potion` | Potion | Common | Restore 40% Mana |
| `acc_greater_mana_potion` | Potion | Uncommon | Restore 100% Mana |
| `acc_cursed_energy_vial` | Potion | Rare | Restore 60% Cursed Energy (fails if none) |
| `acc_elixir_of_strength` | Elixir | Rare | Strength III, 90s |
| `acc_elixir_of_swiftness` | Elixir | Uncommon | Speed III, 120s |
| `acc_elixir_of_iron_skin` | Elixir | Rare | Resistance II, 60s |
| `acc_elixir_of_haste` | Elixir | Uncommon | Haste III, 120s |
| `acc_elixir_of_the_phoenix` | Elixir | Epic | Fire Resistance + Regeneration II, 60s |
| `acc_berserk_brew` | Elixir | Epic | Strength IV + Speed II, 90s |
| `acc_invisibility_draught` | Potion | Rare | Invisibility, 45s |
| `acc_night_owl_tonic` | Potion | Common | Night Vision, 5min |
| `acc_gills_brew` | Potion | Uncommon | Water Breathing + Dolphin's Grace, 3min |
| `acc_titan_tonic` | Elixir | Epic | Health Boost II + Absorption II, 2min |
| `acc_hearty_stew` | Food | Common | Saturation + heal 15% |
| `acc_golden_feast` | Food | Rare | Saturation + Regeneration + Absorption |
| `acc_spirit_bread` | Food | Uncommon | Saturation + restore 30% Mana |
| `acc_cursed_jerky` | Food | Rare | Saturation + restore 30% Cursed Energy (fails if none) |
| `acc_scroll_of_recall` | Scroll | Rare | Teleport to world spawn |
| `acc_scroll_of_blink` | Scroll | Uncommon | Blink 8 blocks forward |
| `acc_scroll_of_ascension` | Scroll | Rare | Teleport up to surface + Slow Falling |
| `acc_scroll_of_warding` | Scroll | Epic | Resistance II + Absorption II, 60s |
| `acc_scroll_of_haste` | Scroll | Uncommon | Speed II + Haste II, 60s |

## What the integration agent must wire

1. **Initialization** — call `com.political.expansion.accessories.Accessories.register();`
   in `RpgPoliticsMod.onInitialize()` (alongside the other `*.register()` calls, e.g. next
   to `RelicItems.register()`). This single call registers all 50 items, the inventory-tick
   handler (via `ServerTickEvents.END_SERVER_TICK`) and the `UseItemCallback`.

2. **Lang** — merge `tools/lang-fragments/accessories.json` into
   `src/main/resources/assets/politicalserver/lang/en_us.json` (50 `item.politicalserver.acc_*`
   keys).

3. **Creative tab** (optional) — to surface the items in a creative tab, add a tab in
   `ModTabs` / a method in `CreativeCatalog` that iterates `Accessories.items()` and accepts
   `Accessories.display(item)` for each (decorated stacks with name + lore). Suggested:
   ```java
   public static void accessories(CreativeModeTab.Output out) {
       for (Item item : Accessories.items()) out.accept(Accessories.display(item));
   }
   ```
   A reasonable tab icon is `Accessories.display(<the godslayer relic item>)`.

4. **Assets** — textures, item models and item-definition JSON for every `acc_*` id are
   already generated under `src/main/resources/assets/politicalserver/{textures/item,models/item,items}`
   by `tools/gen-accessories.js` (already run; rerun with `node tools/gen-accessories.js`).

No other wiring is required — bonuses, effects and consumable behavior are fully
self-contained within the package.

## Public API surface

- `Accessories.register()` — register everything (call once on init).
- `Accessories.items()` → `List<Item>` — all registered items.
- `Accessories.display(Item)` → decorated `ItemStack` for tabs / giving.
- `Accessories.accessoryDefs()` / `Accessories.consumableDefs()` — definition metadata.
