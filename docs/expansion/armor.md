# Armor Expansion

A big roster of **14 full 4-piece armour sets (56 pieces)** spanning every rarity
from Common to Mythic, in the style of Prominence II / RAD2 armour packs. Every
set has themed Skyblock stats, per-piece passive abilities, and a **Full Set
Bonus** that activates only while all four pieces are worn.

- **Package:** `com.political.expansion.armor`
- **Entry class:** `ArmorExpansion` — exposes `register()` and `items()`
- **Item id prefix:** `arm_` (e.g. `arm_celestial_chestplate`)
- **Mod id / namespace:** `politicalserver`
- No mixins. No edits to shared files (`StatManager`, `AbilityEngine`,
  `RpgItem`, `CreativeCatalog`, `lang/en_us.json`, etc.).

## Skyblock stats only (no vanilla armour)

Exactly like the core `RpgItem` gear, each piece:

- carries its stats as `custom_data` integer keys (`rpg_health`, `rpg_defense`,
  `rpg_strength`, `rpg_intelligence`, `rpg_speed`, `rpg_crit_chance`,
  `rpg_crit_damage`, `rpg_ferocity`) read by `ItemStats`/`StatManager`;
- has its vanilla attribute modifiers stripped to
  `ItemAttributeModifiers.EMPTY`, so it never grants vanilla armour/toughness;
- carries its passive abilities in `rpg_abilities`, which the **existing**
  `AbilityEngine` equipment tick applies automatically (it scans all armour
  slots regardless of item id).

Because the pieces reuse the same NBT contract as `RpgItem`, the stat HUD, gear
score, reforge/rarity colouring and ability passives all work with **zero**
changes to shared code.

## Full Set Bonus (self-contained)

`ArmorSetBonusHandler` registers its **own** `ServerTickEvents.END_SERVER_TICK`
listener (≈1×/second). Each tick it reads the player's four worn pieces; if they
all share the same `arm_set` `custom_data` key it applies that set's bonus
(vanilla mob effects / heal / nearby-mob effects only — no shared-manager edits).
Per-piece passives keep flowing through the existing `AbilityEngine`; the handler
only layers the extra full-set bonus on top.

## Integration (what the integration agent must wire)

1. **Register** during mod init, next to the other `register()` calls:

   ```java
   com.political.expansion.armor.ArmorExpansion.register();
   ```

2. **Creative tab** — feed the pieces into a tab (mirrors how `CreativeCatalog.gear`
   accepts `RpgItems.create(def)` stacks). `items()` returns ready-built stacks:

   ```java
   for (net.minecraft.world.item.ItemStack stack : com.political.expansion.armor.ArmorExpansion.items()) {
       out.accept(stack);
   }
   ```

   `ArmorExpansion.items()` returns all 56 pieces (14 sets × 4) in set/slot order.

3. **Translations** — merge `tools/lang-fragments/armor.json` into
   `assets/politicalserver/lang/en_us.json`. (Display names are also set as a
   literal `CUSTOM_NAME`, so this is for completeness/localisation.) Keys are
   `item.politicalserver.arm_*`.

4. **Textures/models** — `tools/gen-armor.js` already generated the icon PNGs,
   `models/item/<id>.json` and `items/<id>.json` for every piece. If they are
   missing (e.g. `tools/node_modules` was absent on this machine), run:

   ```powershell
   cd tools ; node gen-armor.js
   ```

   (Worn armour-layer textures are not provided — pieces render the vanilla base
   material's armour layer on the body; inventory icons are custom.)

## Set roster

Stats below are the **base** per-piece values (helmet / chest / legs / boots);
the tooltip also shows the rarity-scaled total. `H`=Health, `D`=Defense,
`S`=Strength, `I`=Intelligence (Mana), `Spd`=Speed, plus Crit/Ferocity where noted.

| Set | Rarity | Theme | Piece passives | Full Set Bonus |
|---|---|---|---|---|
| **Recruit** | Common | Militia leather | — | *Basic Training:* Speed I, Saturation |
| **Guardian** | Uncommon | Iron defender | Chest: Fortified | *Bulwark:* Resistance I, Absorption I |
| **Ranger** | Uncommon | Scout/agility | Helm: Night Vision; Boots: Swiftness + Featherfall | *Pathfinder:* Speed II, Jump Boost II |
| **Frostguard** | Rare | Ice tank/mana | Helm: Aquatic; Boots: Featherfall | *Permafrost:* Resistance I, Water Breathing, slows nearby foes |
| **Emberforge** | Rare | Flame bruiser | Fire Immunity (all); Chest: Fire Aura | *Inferno Heart:* Fire Resistance, Strength I, ignites nearby foes |
| **Tempest** | Epic | Storm speedster | Legs/Boots: Swiftness; Boots: Featherfall | *Eye of the Storm:* Speed II, Jump Boost III, Haste I, Slow Falling |
| **Verdant** | Epic | Nature regen | Helm/Chest: Regeneration; Boots: Aquatic | *Wild Growth:* Regeneration II, Saturation, Health Boost II |
| **Abyssal** | Epic | Deep-sea mage | Helm: Aquatic + Night Vision; Chest/Boots: Aquatic | *Tides of the Deep:* Water Breathing, Night Vision, Regen I, Resistance I |
| **Shadowstalker** | Epic | Assassin (crit) | Helm: Night Vision; Boots: Swiftness | *Night's Veil:* Speed II, Strength I, Jump Boost II, Night Vision |
| **Bloodmoon** | Legendary | Vampiric bruiser | Chest: Battle Wards | *Crimson Feast:* Strength II, Absorption II, Regen I, lifedraw heal |
| **Solaris** | Legendary | Holy paladin | Helm: NV + Fire Immune; Chest: Regen + Wards; Boots: Featherfall | *Solar Blessing:* Regen II, Resistance I, Fire Res, Health Boost II, Saturation |
| **Wraith** | Legendary | Dark juggernaut | Helm: Night Vision; Chest: Fortified; Boots: Featherfall | *Curse of the Wraith:* Resistance II, Strength II, NV, withers nearby foes |
| **Titanforge** | Mythic | Unbreakable tank | Chest: Fortified + Wards; Boots: Fire Immune | *Unbreakable:* Resistance II, Absorption IV, Health Boost IV, Fire Res |
| **Celestial** | Mythic | Ascendant all-round | Helm: NV; Chest: Flight + Fire Immune; Legs: Fortified; Boots: Swiftness + Featherfall | *Ascendant:* Resistance II, Strength II, Speed II, Regen III, Health Boost IV, Fire Res, NV |

### Per-piece base stats

| Set | Helmet | Chest | Legs | Boots |
|---|---|---|---|---|
| Recruit | H20 D10 | H35 D18 | H28 D14 | H20 D10 Spd2 |
| Guardian | H35 D22 | H60 D40 | H45 D30 | H30 D20 |
| Ranger | H22 D10 I10 | H35 D16 I15 | H28 D12 Spd3 | H20 D8 Spd5 |
| Frostguard | H45 D28 I20 | H75 D50 I25 | H58 D38 I15 | H40 D24 I10 |
| Emberforge | H40 D22 S20 | H65 D35 S30 | H50 D28 S22 | H35 D18 S15 |
| Tempest | H45 D24 I25 Spd5 | H70 D40 I30 Spd8 | H55 D30 Spd10 | H40 D20 Spd14 |
| Verdant | H60 D26 I20 | H100 D44 I25 | H78 D34 I18 | H52 D22 I12 |
| Abyssal | H45 D24 I40 | H70 D40 I55 | H55 D30 I40 | H40 D22 I30 |
| Shadowstalker | H45 D22 S20 Spd6 (5% CC, 15 CD) | H70 D38 S30 Spd8 (8% CC, 25 CD) | H55 D28 S22 Spd10 (6% CC, 15 CD) | H40 D18 S15 Spd14 |
| Bloodmoon | H55 D26 S35 (5% CC, 20 CD, 10 Fer) | H95 D48 S55 (8% CC, 30 CD, 15 Fer) | H72 D36 S40 (6% CC, 20 CD, 10 Fer) | H50 D24 S28 Spd6 (8 Fer) |
| Solaris | H65 D34 I30 | H110 D58 I40 | H82 D44 I28 | H56 D30 I20 |
| Wraith | H58 D30 S40 | H98 D52 S60 | H74 D40 S44 | H52 D28 S30 |
| Titanforge | H90 D55 | H160 D95 | H120 D70 | H80 D48 |
| Celestial | H95 D50 S40 I40 | H170 D85 S60 I60 | H130 D65 S45 I45 | H90 D45 S30 I30 Spd12 |

> Reference quality target: Prominence II / RAD2 armour and the
> *cosmeticarmorreworked* presentation — recognisable per-piece silhouettes,
> rarity-coloured names, and a clearly highlighted full-set bonus block.
