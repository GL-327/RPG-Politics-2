# Expansion 3 — Above-Ground RPG Worldgen Structures

Discoverable **surface** sites scattered across the overworld as players explore, tying loot,
mobs and flavour into the mod's existing factions and lore. This system is **separate from the
underground dungeon system** (`com.political.world.dungeons`) — it owns the package
`com.political.world.structures` and never touches dungeon code.

It reuses the mixin-free, terrain-adaptive `Build` placement layer and the streamed build-queue
pattern used by `SettlementManager` / `DungeonManager`: each structure is planned into a
`BuildBuffer`, queued, and streamed into the world a few thousand blocks per tick so generation
never stalls the server.

## Structure types (11)

| id | display | faction | population | loot table |
|----|---------|---------|------------|------------|
| `sorcerer_watchtower` | Sorcerer Watchtower | arcane | cultist spawner + storm-herald elite | `arcane` |
| `mage_tower` | Mage Tower | arcane | cultists, storm heralds, **archmage** summit | `arcane` |
| `hero_outpost` | Hero Outpost | order | 3 friendly villagers (garrison), no hostiles | `martial` |
| `bandit_camp` | Bandit Camp | bandit | outlaws + brutes, **bandit king** | `bandit` |
| `cursed_shrine` | Cursed Shrine | cursed | grave revenants, wraith, **cursed spirit** | `cursed` |
| `abandoned_manor` | Abandoned Manor | cursed | wraiths, plague bearer | `noble` |
| `trading_post` | Trading Post | merchant | 4 villagers | `mercantile` |
| `election_hall_ruin` | Election Hall Ruin | civic | bone legionnaire, 1 civic villager | `civic` |
| `battlefield` | Battlefield Graveyard | cursed | bone legionnaires, grave revenants, cursed spirit | `battlefield` |
| `obelisk` | Ancient Obelisk | arcane | lone wraith | `arcane` |
| `wandering_merchant` | Wandering Merchant Camp | merchant | wandering trader + 2 villagers | `mercantile` |

### Faction flavour
Mob and loot ties use **ids that already exist** in the rosters, so nothing dangles:

- **Cursed** sites (`cursed_shrine`, `battlefield`) summon a grade-1 cursed spirit via
  `CurseSpirits2.spawnAt`, plus `mob_grave_revenant` / `mob_wraith` from `ExpansionMobs`.
- **Bandit** camps spawn `mob_bandit_outlaw` / `mob_bandit_brute` and the boss `mob2_bandit_king`.
- **Arcane** towers spawn `mob_cultist_acolyte` / `mob_storm_herald`; the Mage Tower crowns it
  with `mob2_archmage_sovereign`.
- **Order** / **merchant** sites are friendly — populated with vanilla villagers and a wandering
  trader (no hostile spawns).

All hostile spawns route through `ExpansionMobs.spawnById` → `ExpansionMobs2.spawnById`
(same fallback chain the dungeon system uses).

## Blocks added (`StructureBlocks`)

Six new full-cube blocks (mixin-free, vanilla-backed models so they render without new PNGs):

- `structure_runed_stone` — arcane masonry (watchtowers, mage towers)
- `structure_mossy_marble` — weathered marble (manors, civic ruins)
- `structure_cursed_altar` — glowing offering altar (cursed shrines)
- `structure_obelisk_stone` — monolith stone (obelisks, monuments)
- `structure_war_banner` — faction banner marker (outposts, camps)
- `structure_camp_ground` — trodden ground (camps, battlefields)

They are added to the existing **"RPG — Blocks & Build"** creative tab via
`CreativeCatalog.build` (one additive line). Lang lives in `tools/lang-fragments/structures.json`
and is mirrored into `assets/politicalserver/lang/en_us.json`.

## Loot tables

`data/politicalserver/loot_table/chests/structures/`: `arcane`, `martial`, `bandit`, `cursed`,
`noble`, `mercantile`, `civic`, `battlefield`. Each blends confirmed mod items (e.g.
`mana_crystal`, `coin_pouch`, `cursed_essence`, `treasury_note`, `bounty_seal`, `reforge_stone`)
with vanilla rewards. `StructureLoot.fillChest` resolves `chests/structures/<table>` and falls
back to a coin/emerald stack if a table is missing, so generation never breaks.

## Commands (`/structure`)

- `/structure list` — list discovered sites (everyone)
- `/structure types` — list all archetypes (everyone)
- `/structure locate [type]` — nearest site, optionally of a given type (everyone)
- `/structure summon <type> [at <x> <z>]` — queue a build (game-masters)

## Settlement deepening (additive)

`SettlementProps` (in this package) adds decorative variety to the procedural settlements —
**market stalls, monuments, planters, benches, notice boards, banner gates** — built on the
shared `Build` layer and terrain-following. It is strictly additive: **no `CIVIC_MARKER`
placement, no `Settlement` registration, and no civic geometry is changed**, so the elected
government overlay is byte-for-byte unaffected.

### Shared-file edits (for integration awareness)
These are small, clearly-additive calls (each marked with a comment in-file):

- `SettlementGenerator.buildTown` — market row, monument, notice board, banner gate around the square.
- `SettlementGenerator.buildVillage` — planter, market stall, notice board on the green.
- `SettlementGenerator.buildCity` — monument, market row, benches, notice board in the plaza.
- `RpgPoliticsMod.onInitialize` — `StructureRegistry.register()`, `StructureCommands.register(...)`, `StructureManager.tick(...)`.
- `CreativeCatalog.build` — adds `StructureBlocks.list()` to the build tab.

## Integration hooks (public API)

- `StructureRegistry.register()` — blocks + manager (call once at init, after `ModBlocks.register()`).
- `StructureManager.tick(server)` — scatter + stream (call each server tick).
- `StructureManager.queueAt(level, x, z, type)` — programmatic summon; returns the `StructureSite`.
- `StructureManager.sites()` / `nearest(level, x, z)` / `nearestOfType(...)` — site queries.
- `StructureCommands.register(dispatcher)` — command set.
- `StructureType` — archetype enum (`ids()`, `byId(id)`, `roll(rng)`), palette + faction + loot metadata.
- `SettlementProps` — reusable decorative prop helpers.

## Scatter tuning

`StructureManager`: 144-block cell grid, 18% chance per fresh cell, 56–128 blocks from the
player, minimum 80-block gap between surface sites and a 96-block clearance from settlements.
Placement budget 9000 blocks/tick.
