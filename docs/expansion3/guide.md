# In-Game Guide / Encyclopedia (Field Manual)

A self-contained, mixin-free in-game encyclopedia so players can learn the mod's enormous
content from inside the game. Lives entirely in the new package `com.political.guide` (common +
client) plus generated assets and a lang fragment.

## What it is

- **Item:** `politicalserver:guide_field_manual` ("Field Manual") — a one-stack handheld tome.
- **Screen:** `GuideScreen`, a paginated book built on the shared `RpgScreen` 26.2 render
  pipeline (no mixins, no GUI textures). A left **chapter sidebar** switches chapters; **Prev /
  Next** buttons flip pages; a page indicator shows `Page X / Y`.
- **Content:** `GuideContent` builds chapters live from the mod's own data (`Power`, `Power2`,
  `DungeonType`, `Rarity`, the grade ladder) mixed with curated player-facing prose, so the
  guide stays accurate as content evolves rather than going stale.

## Chapters

1. **Getting Started** — first-hour checklist, the two paths of power, menus vs commands.
2. **Cursed Energy & Grades** — CE, the Grade 4 → Special Grade ladder and exorcism thresholds.
3. **Powers & Techniques** — every core cursed technique (cost + cooldown) and Expansion II
   counts, plus the full Domain and Ultimate lists.
4. **Compound V & Flight** — serums, all hero powers, Viltrumite powers, and how flight works.
5. **Weapons & Rarities** — the Common → Mythic rarity ladder with multipliers, weapon families.
6. **Armor & Set Bonuses** — how 4-piece sets and set bonuses work.
7. **Accessories** — passive trinkets, relics and consumables.
8. **Dungeons** — all dungeon archetypes with tier/theme, their bosses and signature loot.
9. **Mobs & Spirits Bestiary** — cursed spirits, RPG mob tiers, sample dungeon bosses, bounties.
10. **Economy** — bank/interest, shop/market/auction, currencies and transfers.
11. **Politics** — offices, elections, taxes/treasury, justice and perks.
12. **Commands Reference** — a categorised cheat-sheet of the player-facing commands.

## How it opens

Three ways, all routing through the same `GuideOpenS2C` clientbound payload:

- **Right-click** the Field Manual item (`GuideItem` `UseItemCallback`, server-side) → sends
  `GuideOpenS2C(0)` → client opens `GuideScreen`.
- **`/guide`** opens chapter 1; **`/guide <n>`** opens chapter *n* directly.
- The item is available in **Creative** under its own tab **"RPG — Field Manual"**.

No recipe is required (creative + command access), matching how the mod's other utility items
(e.g. the Dev Menu) are distributed.

## Files (new, owned by this feature)

Common (`src/main/java/com/political/guide/`):

- `GuideRegistry.java` — single `register()` entrypoint: registers the item, the clientbound
  payload type, the `/guide` command (via `CommandRegistrationCallback`), and the creative tab.
- `GuideItem.java` — the Field Manual item + `UseItemCallback` open handler + `open(player, ch)`.
- `GuideOpenS2C.java` — `record(int chapter)` clientbound payload.
- `GuideCommands.java` — the `/guide [chapter]` command.

Client (`src/client/java/com/political/guide/`):

- `GuideScreen.java` — the paginated encyclopedia UI.
- `GuideContent.java` — chapter/content builder (data only).
- `GuideClient.java` — `registerClient()` registers the `GuideOpenS2C` receiver.

Assets / tooling:

- `tools/gen-guide.js` — Node + jimp generator for the book icon (same pattern as the other
  `gen-*.js` tools). Produces `textures/item/guide_field_manual.png`,
  `models/item/guide_field_manual.json`, `items/guide_field_manual.json`.
- `tools/lang-fragments/guide.json` — the lang fragment for this feature.

## Integration hooks

All hooks are additive; nothing in other packages is modified except the two shared entrypoints.

- **`register()`** — `RpgPoliticsMod.onInitialize()` calls
  `com.political.guide.GuideRegistry.register();` (added, alongside `DungeonRegistry.register()`).
  This also self-registers the `/guide` command and the creative tab.
- **`registerClient()`** — `PoliticalClient.onInitializeClient()` calls
  `com.political.guide.GuideClient.registerClient();` (added). `GuideClient` also implements
  `ClientModInitializer`, so it can instead be listed as a client entrypoint in
  `fabric.mod.json` if the integration agent prefers wiring it that way.
- **Command** — `/guide` is registered inside `GuideRegistry.register()` via
  `CommandRegistrationCallback`, so no edit to `RpgPoliticsMod`'s command block is needed.
- **Creative tab entry** — `GuideRegistry` registers a dedicated `politicalserver:guide` tab.
  Alternative: add `out.accept(GuideItem.stack())` to an existing `CreativeCatalog` generator in
  `ModTabs` if you'd rather fold the manual into an existing tab.
- **Lang merge** — merge `tools/lang-fragments/guide.json` into
  `assets/politicalserver/lang/en_us.json` (already applied: `item.…guide_field_manual`,
  its `.tooltip`, `gui.…guide.title`, and `itemGroup.…guide`).

## Regenerating the icon

```
node tools/gen-guide.js
```
