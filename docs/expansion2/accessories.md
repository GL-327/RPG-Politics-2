# Accessories2 Expansion

Package: `com.political.expansion2.accessories`
Item id prefix: `acc2_`
No mixins. Separate attribute modifier IDs (`acc2_*`) from Phase-1 `acc_*`.

## Totals

| Category | Count |
|----------|------:|
| Accessories (passive inventory) | 105 |
| Consumables (right-click) | 60 |
| **Total** | **165** |

Consumables include potions, elixirs, scrolls, foods-on-use, and throwable bombs.

## Behaviour

Accessories aggregate bonuses once per second via inventory scan (Prominence / Hypixel style).
Consumables fire through `UseItemCallback`. Tooltips use Skyblock layout via `AccessoryTooltip2`.

## Public API

- `Accessories2.register()`
- `Accessories2.items()`
- `Accessories2.display(Item)`

## Integration

1. Call `Accessories2.register()` in `RpgPoliticsMod.onInitialize()`.
2. Merge `tools/lang-fragments/accessories2.json` into `en_us.json`.
3. Creative tab: iterate `Accessories2.items()` with `Accessories2.display(item)`.
4. Rerun `node tools/gen-accessories2.js` for textures/models.

## Accessories (105)

| Id | Type | Rarity | Notes |
|----|------|--------|-------|
| `acc2_ward_talisman` | TALISMAN | COMMON | Turns aside blows with quiet resolve. |
| `acc2_ward_ring` | RING | COMMON | Turns aside blows with quiet resolve. |
| `acc2_ward_charm` | CHARM | UNCOMMON | Turns aside blows with quiet resolve. |
| `acc2_ward_band` | BAND | RARE | Turns aside blows with quiet resolve. |
| `acc2_ward_amulet` | AMULET | EPIC | Turns aside blows with quiet resolve. |
| `acc2_ward_badge` | BADGE | EPIC | Turns aside blows with quiet resolve. |
| `acc2_ward_rune` | RUNE | MYTHIC | Turns aside blows with quiet resolve. |
| `acc2_ward_totem` | TOTEM | MYTHIC | Turns aside blows with quiet resolve. |
| `acc2_ward_artifact` | ARTIFACT | MYTHIC | Turns aside blows with quiet resolve. |
| `acc2_ward_relic` | RELIC | MYTHIC | Turns aside blows with quiet resolve. |
| `acc2_vigor_talisman` | TALISMAN | COMMON | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_ring` | RING | COMMON | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_charm` | CHARM | UNCOMMON | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_band` | BAND | RARE | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_amulet` | AMULET | EPIC | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_badge` | BADGE | EPIC | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_rune` | RUNE | MYTHIC | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_totem` | TOTEM | MYTHIC | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_artifact` | ARTIFACT | MYTHIC | Life pulses through the metal like a second heartbeat. |
| `acc2_vigor_relic` | RELIC | MYTHIC | Life pulses through the metal like a second heartbeat. |
| `acc2_berserk_talisman` | TALISMAN | COMMON | It hungers for the thrill of the fray. |
| `acc2_berserk_ring` | RING | COMMON | It hungers for the thrill of the fray. |
| `acc2_berserk_charm` | CHARM | UNCOMMON | It hungers for the thrill of the fray. |
| `acc2_berserk_band` | BAND | RARE | It hungers for the thrill of the fray. |
| `acc2_berserk_amulet` | AMULET | EPIC | It hungers for the thrill of the fray. |
| `acc2_berserk_badge` | BADGE | EPIC | It hungers for the thrill of the fray. |
| `acc2_berserk_rune` | RUNE | MYTHIC | It hungers for the thrill of the fray. |
| `acc2_berserk_totem` | TOTEM | MYTHIC | It hungers for the thrill of the fray. |
| `acc2_berserk_artifact` | ARTIFACT | MYTHIC | It hungers for the thrill of the fray. |
| `acc2_berserk_relic` | RELIC | MYTHIC | It hungers for the thrill of the fray. |
| `acc2_swift_talisman` | TALISMAN | COMMON | Your steps grow light and quick. |
| `acc2_swift_ring` | RING | COMMON | Your steps grow light and quick. |
| `acc2_swift_charm` | CHARM | UNCOMMON | Your steps grow light and quick. |
| `acc2_swift_band` | BAND | RARE | Your steps grow light and quick. |
| `acc2_swift_amulet` | AMULET | EPIC | Your steps grow light and quick. |
| `acc2_swift_badge` | BADGE | EPIC | Your steps grow light and quick. |
| `acc2_swift_rune` | RUNE | MYTHIC | Your steps grow light and quick. |
| `acc2_swift_totem` | TOTEM | MYTHIC | Your steps grow light and quick. |
| `acc2_swift_artifact` | ARTIFACT | MYTHIC | Your steps grow light and quick. |
| `acc2_swift_relic` | RELIC | MYTHIC | Your steps grow light and quick. |
| `acc2_scholar_talisman` | TALISMAN | COMMON | Arcane study replenishes the mind. |
| `acc2_scholar_ring` | RING | COMMON | Arcane study replenishes the mind. |
| `acc2_scholar_charm` | CHARM | UNCOMMON | Arcane study replenishes the mind. |
| `acc2_scholar_band` | BAND | RARE | Arcane study replenishes the mind. |
| `acc2_scholar_amulet` | AMULET | EPIC | Arcane study replenishes the mind. |
| `acc2_scholar_badge` | BADGE | EPIC | Arcane study replenishes the mind. |
| `acc2_scholar_rune` | RUNE | MYTHIC | Arcane study replenishes the mind. |
| `acc2_scholar_totem` | TOTEM | MYTHIC | Arcane study replenishes the mind. |
| `acc2_scholar_artifact` | ARTIFACT | MYTHIC | Arcane study replenishes the mind. |
| `acc2_scholar_relic` | RELIC | MYTHIC | Arcane study replenishes the mind. |
| `acc2_cursed_talisman` | TALISMAN | COMMON | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_ring` | RING | COMMON | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_charm` | CHARM | UNCOMMON | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_band` | BAND | RARE | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_amulet` | AMULET | EPIC | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_badge` | BADGE | EPIC | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_rune` | RUNE | MYTHIC | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_totem` | TOTEM | MYTHIC | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_artifact` | ARTIFACT | MYTHIC | A sealed sliver of cursed energy seeps back into you. |
| `acc2_cursed_relic` | RELIC | MYTHIC | A sealed sliver of cursed energy seeps back into you. |
| `acc2_assassin_talisman` | TALISMAN | COMMON | Strike from the shadows, swift and sure. |
| `acc2_assassin_ring` | RING | COMMON | Strike from the shadows, swift and sure. |
| `acc2_assassin_charm` | CHARM | UNCOMMON | Strike from the shadows, swift and sure. |
| `acc2_assassin_band` | BAND | RARE | Strike from the shadows, swift and sure. |
| `acc2_assassin_amulet` | AMULET | EPIC | Strike from the shadows, swift and sure. |
| `acc2_assassin_badge` | BADGE | EPIC | Strike from the shadows, swift and sure. |
| `acc2_assassin_rune` | RUNE | MYTHIC | Strike from the shadows, swift and sure. |
| `acc2_assassin_totem` | TOTEM | MYTHIC | Strike from the shadows, swift and sure. |
| `acc2_assassin_artifact` | ARTIFACT | MYTHIC | Strike from the shadows, swift and sure. |
| `acc2_assassin_relic` | RELIC | MYTHIC | Strike from the shadows, swift and sure. |
| `acc2_phoenix_talisman` | TALISMAN | COMMON | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_ring` | RING | COMMON | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_charm` | CHARM | UNCOMMON | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_band` | BAND | RARE | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_amulet` | AMULET | EPIC | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_badge` | BADGE | EPIC | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_rune` | RUNE | MYTHIC | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_totem` | TOTEM | MYTHIC | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_artifact` | ARTIFACT | MYTHIC | Wreathed in embers that never burn its bearer. |
| `acc2_phoenix_relic` | RELIC | MYTHIC | Wreathed in embers that never burn its bearer. |
| `acc2_frost_talisman` | TALISMAN | COMMON | Cold clarity sharpens every movement. |
| `acc2_frost_ring` | RING | COMMON | Cold clarity sharpens every movement. |
| `acc2_frost_charm` | CHARM | UNCOMMON | Cold clarity sharpens every movement. |
| `acc2_frost_band` | BAND | RARE | Cold clarity sharpens every movement. |
| `acc2_frost_amulet` | AMULET | EPIC | Cold clarity sharpens every movement. |
| `acc2_frost_badge` | BADGE | EPIC | Cold clarity sharpens every movement. |
| `acc2_frost_rune` | RUNE | MYTHIC | Cold clarity sharpens every movement. |
| `acc2_frost_totem` | TOTEM | MYTHIC | Cold clarity sharpens every movement. |
| `acc2_frost_artifact` | ARTIFACT | MYTHIC | Cold clarity sharpens every movement. |
| `acc2_frost_relic` | RELIC | MYTHIC | Cold clarity sharpens every movement. |
| `acc2_void_talisman` | TALISMAN | COMMON | The abyss whispers power into your bones. |
| `acc2_void_ring` | RING | COMMON | The abyss whispers power into your bones. |
| `acc2_void_charm` | CHARM | UNCOMMON | The abyss whispers power into your bones. |
| `acc2_void_band` | BAND | RARE | The abyss whispers power into your bones. |
| `acc2_void_amulet` | AMULET | EPIC | The abyss whispers power into your bones. |
| `acc2_void_badge` | BADGE | EPIC | The abyss whispers power into your bones. |
| `acc2_void_rune` | RUNE | MYTHIC | The abyss whispers power into your bones. |
| `acc2_void_totem` | TOTEM | MYTHIC | The abyss whispers power into your bones. |
| `acc2_void_artifact` | ARTIFACT | MYTHIC | The abyss whispers power into your bones. |
| `acc2_void_relic` | RELIC | MYTHIC | The abyss whispers power into your bones. |
| `acc2_paragon_crest` | BADGE | MYTHIC | The mark of one who has surpassed every trial. |
| `acc2_eternal_sigil` | RUNE | MYTHIC | A rune carved before the first age of kings. |
| `acc2_sovereign_relic` | RELIC | MYTHIC | The crown-jewel of a fallen empire. |
| `acc2_ascendant_orb` | ARTIFACT | MYTHIC | A miniature star bound in gold filigree. |
| `acc2_abaddon_talisman` | TALISMAN | MYTHIC | Named for the end of all things. |

## Consumables (60)

| Id | Type | Rarity | Notes |
|----|------|--------|-------|
| `acc2_healing_draught` | POTION | COMMON | Restores 25% of max Health. |
| `acc2_healing_potion` | POTION | UNCOMMON | Restores 50% of max Health. |
| `acc2_greater_healing_potion` | POTION | RARE | Restores 100% of max Health. |
| `acc2_mana_draught` | POTION | COMMON | Restores 25% of max Mana. |
| `acc2_mana_potion` | POTION | UNCOMMON | Restores 50% of max Mana. |
| `acc2_greater_mana_potion` | POTION | RARE | Restores 100% of max Mana. |
| `acc2_cursed_vial` | POTION | RARE | Restores 40% of max Cursed Energy. |
| `acc2_greater_cursed_vial` | POTION | EPIC | Restores 80% of max Cursed Energy. |
| `acc2_vitality_tonic` | POTION | UNCOMMON | Restores 35% of max Health. |
| `acc2_arcane_tonic` | POTION | UNCOMMON | Restores 35% of max Mana. |
| `acc2_spirit_tonic` | POTION | RARE | Restores 30% of max Cursed Energy. |
| `acc2_restoration_flask` | POTION | EPIC | Restores Health, Mana, and Cursed Energy. |
| `acc2_elixir_strength` | ELIXIR | RARE | STRENGTH 90s |
| `acc2_elixir_swiftness` | ELIXIR | UNCOMMON | SPEED 120s |
| `acc2_elixir_iron_skin` | ELIXIR | RARE | RESISTANCE 60s |
| `acc2_elixir_haste` | ELIXIR | UNCOMMON | HASTE 120s |
| `acc2_elixir_phoenix` | ELIXIR | EPIC | FIRE_RESISTANCE 60s + REGENERATION 60s |
| `acc2_elixir_berserk` | ELIXIR | EPIC | STRENGTH 90s + SPEED 90s |
| `acc2_elixir_invisibility` | ELIXIR | RARE | INVISIBILITY 45s |
| `acc2_elixir_night_owl` | ELIXIR | COMMON | NIGHT_VISION 300s |
| `acc2_elixir_gills` | ELIXIR | UNCOMMON | WATER_BREATHING 180s + DOLPHINS_GRACE 180s |
| `acc2_elixir_titan` | ELIXIR | EPIC | HEALTH_BOOST 120s + ABSORPTION 120s |
| `acc2_elixir_frost` | ELIXIR | RARE | SLOW_FALLING 90s + RESISTANCE 60s |
| `acc2_elixir_poison` | ELIXIR | UNCOMMON | Cures poison and grants brief immunity. |
| `acc2_elixir_rage` | ELIXIR | LEGENDARY | STRENGTH 60s + SPEED 60s |
| `acc2_elixir_clarity` | ELIXIR | RARE | HASTE 90s + NIGHT_VISION 90s |
| `acc2_elixir_void` | ELIXIR | LEGENDARY | STRENGTH 45s + INVISIBILITY 45s |
| `acc2_scroll_recall` | SCROLL | RARE | Teleports you to world spawn. |
| `acc2_scroll_blink` | SCROLL | UNCOMMON | Blink 8 blocks forward. |
| `acc2_scroll_blink_far` | SCROLL | RARE | Blink 16 blocks forward. |
| `acc2_scroll_ascension` | SCROLL | RARE | Teleports you to the surface. |
| `acc2_scroll_warding` | SCROLL | EPIC | Resistance II + Absorption II. |
| `acc2_scroll_haste` | SCROLL | UNCOMMON | Speed II + Haste II. |
| `acc2_scroll_fury` | SCROLL | EPIC | Strength III + Speed II. |
| `acc2_scroll_sanctuary` | SCROLL | LEGENDARY | Regeneration III + Resistance III. |
| `acc2_scroll_mana` | SCROLL | RARE | Restores 60% Mana. |
| `acc2_scroll_heal` | SCROLL | RARE | Heals 50% max Health. |
| `acc2_scroll_curse` | SCROLL | EPIC | Restores 50% Cursed Energy. |
| `acc2_scroll_levitation` | SCROLL | EPIC | Brief levitation then slow fall. |
| `acc2_food_hearty_stew` | FOOD | COMMON | Sates hunger and heals a little. |
| `acc2_food_golden_feast` | FOOD | RARE | Saturation, Regeneration, and Absorption. |
| `acc2_food_spirit_bread` | FOOD | UNCOMMON | Sates hunger and restores Mana. |
| `acc2_food_cursed_jerky` | FOOD | RARE | Sates hunger and restores Cursed Energy. |
| `acc2_food_mushroom_risotto` | FOOD | UNCOMMON | Restorative food consumed on use. |
| `acc2_food_sea_salt_fish` | FOOD | COMMON | Restorative food consumed on use. |
| `acc2_food_honey_cake` | FOOD | UNCOMMON | Restorative food consumed on use. |
| `acc2_food_war_ration` | FOOD | RARE | Restorative food consumed on use. |
| `acc2_food_starfruit_tart` | FOOD | EPIC | Fully restores Health, Mana, and Cursed Energy. |
| `acc2_food_void_soup` | FOOD | LEGENDARY | Massive heal plus Strength and Resistance. |
| `acc2_bomb_flash` | BOMB | UNCOMMON | Detonates on use. Radius 6 blocks. |
| `acc2_bomb_fire` | BOMB | RARE | Detonates on use. Radius 4 blocks. |
| `acc2_bomb_frost` | BOMB | RARE | Detonates on use. Radius 5 blocks. |
| `acc2_bomb_poison` | BOMB | RARE | Detonates on use. Radius 5 blocks. |
| `acc2_bomb_smoke` | BOMB | COMMON | Detonates on use. Radius 4 blocks. |
| `acc2_bomb_holy` | BOMB | EPIC | Detonates on use. Radius 5 blocks. |
| `acc2_bomb_void` | BOMB | LEGENDARY | Detonates on use. Radius 6 blocks. |
| `acc2_bomb_gravity` | BOMB | EPIC | Detonates on use. Radius 5 blocks. |
| `acc2_bomb_shock` | BOMB | UNCOMMON | Detonates on use. Radius 4 blocks. |
| `acc2_bomb_healing` | BOMB | RARE | Detonates on use. Radius 5 blocks. |
| `acc2_bomb_cursed` | BOMB | EPIC | Detonates on use. Radius 5 blocks. |
