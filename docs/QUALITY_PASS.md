# Quality Pass — Phase 1 + Phase 2 (MC 26.2)

Audit and polish pass across combat, tooltips, powers, GUIs, HUD, mobs/spirits, NPCs/quests, and lang.  
Build verified: `.\gradlew.bat build` — **GREEN**.

---

## Bugs fixed

| Area | Issue | Fix |
|------|-------|-----|
| **Combat — CursedGear** | Cursed-tool bonus damage lived in a second `AttackEntityCallback` that never ran (AbilityEngine returns `SUCCESS` first), so cursed weapons had no bonus bite | Moved bonus into `CursedGear.attackBonus()` + `AbilityEngine.onAttack()` single Skyblock hit |
| **Combat — Accessories** | Accessory Strength applied via `ATTACK_DAMAGE` attribute but Skyblock hits ignore vanilla attack; accessory STR was effectively dead | `Accessories.strengthBonus()` / `Accessories2.strengthBonus()` feed `StatManager.compute()`; removed `ATTACK_DAMAGE` modifier |
| **Quests — KILL_SPIRIT** | Phase-2 spirits (`spirit2_*` ids) did not count toward Phase-1 quest targets (`curse_wisp`, etc.) | `spiritMatchesQuest()` maps Phase-2 species to legacy quest targets by grade + id keywords |
| **NPCs — Dialogue** | Expansion-2 villagers could fall through to base `DialogueManager` if callback order changed | `DialogueManager` skips `Expansion2VillagerHooks.isExpansionNpc()` |
| **Powers — Networking** | `ActivatePowerC2S` could NPE if activation returned null | Null-check before `sendSystemMessage` |
| **Powers — PowerBridge** | Ambiguous operator precedence in Expansion-2 routing | Explicit parentheses for Power2 vs base Power routing |
| **Tooltips — ItemTooltips** | Broad `"armor"` strip removed Skyblock **Defense** lines | Strip only vanilla armor phrasing (`+N Armor`, slot headers), not Defense stat lines |
| **CursedGear display** | Display stacks lacked empty `ATTRIBUTE_MODIFIERS` | Set on `display()` stacks |

---

## Balance changes

| Change | Before | After | Rationale |
|--------|--------|-------|-----------|
| Expansion 2 mob natural spawn weight | 100% of spec weight | ~67% (`max(1, (w*2+2)/3)`) | Phase 1 + Phase 2 both register in overlapping biomes; reduces spawn pressure / lag |
| `PASSIVE_CE_EFFICIENCY` regen | +1 CE / second | +0.5 CE / second | Passive was outpacing combat spend at high max CE |

No changes to Skyblock base formula (`5 + weapon damage + STR×0.15`), crit, or ferocity math.

---

## Verified OK (no change needed)

- **ATTRIBUTE_MODIFIERS**: Phase 1/2 melee, ranged, armor, and `RpgItems`/`ItemStats.decorate()` all set `EMPTY`.
- **StatManager**: `ATTACK_DAMAGE` modifier pinned to 0; strength feeds Skyblock formula only.
- **Tooltips**: No dungeon footer; Skyblock layout consistent across `SkyblockTooltipBuilder` and expansion builders.
- **PowersScreen2**: Eight tabs align with `Power.Origin` + `Power2.Category` enum order.
- **HUD** (`PoliticalClient.renderHud`): Health / CE / Mana bars + stat chips; vanilla hearts removed.
- **fabric.mod.json**: Description already accurate for 26.2 feature set.
- **Duplicate registrations**: No duplicate item/entity registry calls found; multiple `UseItemCallback` handlers correctly return `PASS` for unrelated items.
- **Lang**: Core keys present for powers, keys, cursed gear, Phase 1 expansion; item display names mostly set via custom lore/name on gear.

---

## Known follow-ups (not in this pass)

- **PerkManager** still multiplies vanilla `ATTACK_DAMAGE` for elected perks (`DOUBLE_DAMAGE`, etc.) — does not affect Skyblock hits but may confuse players inspecting F3/attributes.
- **BOUNTY** quest completion relies on `BountyManager.questStatus()` heuristics; edge cases if slayer + exp2 bounty overlap.
- **Phase 2 item lang**: Many `wpn2_*`, `acc2_*`, `arm2_*` items use programmatic display names; add `en_us.json` entries incrementally if raw ids appear in creative search.

---

## Files touched

- `com/political/combat/AbilityEngine.java`
- `com/political/combat/StatManager.java`
- `com/political/curse/CursedGear.java`
- `com/political/expansion/accessories/Accessories.java`
- `com/political/expansion2/accessories/Accessories2.java`
- `com/political/expansion2/mobs/ExpansionMobs2.java`
- `com/political/expansion2/powers/PowerBridge.java`
- `com/political/expansion2/powers/PowerManager2.java`
- `com/political/expansion2/quests/Expansion2QuestManager.java`
- `com/political/net/ModNetworking.java`
- `com/political/npc/DialogueManager.java`
- `com/political/client/ItemTooltips.java`

---

## Build status

```
.\gradlew.bat build
BUILD SUCCESSFUL
```
