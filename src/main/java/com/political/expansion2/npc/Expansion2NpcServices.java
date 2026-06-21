package com.political.expansion2.npc;

import com.political.combat.StatManager;
import com.political.economy.BankManager;
import com.political.expansion2.quests.Expansion2QuestManager;
import com.political.npc.VillagerManager;
import com.political.politics.DataManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Service actions invoked from Expansion 2 dialogue trees. */
public final class Expansion2NpcServices {

    private static final Random RNG = new Random();
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private Expansion2NpcServices() {}

    public static List<String> servicesFor(NpcArchetype arch) {
        return switch (arch) {
            case HEALER, HERBALIST -> List.of("heal", "cure", "bless_minor");
            case BLACKSMITH, WEAPON_SMITH, ARMOR_SMITH -> List.of("smith_gear", "repair_hint", "forge_bless");
            case ENCHANTER, RUNE_CARVER -> List.of("enchant_bless", "rune_ward");
            case SORCERER, GRIMOIRE_SELLER, SUMMONER -> List.of("teach_energy", "ce_refill");
            case BOUNTY_BROKER, SPIRIT_HUNTER, MERCENARY -> List.of("bounty_tip", "hunter_mark");
            case BANKER, LOAN_SHARK -> List.of("bank_menu", "small_loan");
            case POLITICIAN, ELECTION_CLERK, DIPLOMAT -> List.of("election_info", "civic_donation");
            case CURSED_MERCHANT, FENCE, RELIC_DEALER -> List.of("cursed_trade", "fence_payout");
            case ALCHEMIST -> List.of("brew_buff", "antidote");
            case EXORCIST, SHRINE_KEEPER -> List.of("spirit_ward", "offering");
            case TAX_COLLECTOR -> List.of("pay_tax_hint");
            case AUCTIONEER -> List.of("auction_gift");
            case INNKEEPER -> List.of("rest_buff", "rumor_coin");
            case SCOUT, MAPMAKER -> List.of("scout_mark", "map_tip");
            case WARDEN, BEAST_TAMER -> List.of("guard_bless", "beast_charm");
            case GEM_CUTTER -> List.of("gem_sell");
            case CURSE_SCHOLAR -> List.of("grade_hint");
            case TOWN_CRIER, DOOMSDAY_PROPHET -> List.of("crier_buff");
            default -> List.of("generic_tip");
        };
    }

    public static String label(String svc) {
        return switch (svc) {
            case "heal" -> "Full restoration (40 coin)";
            case "cure" -> "Cure poison & wither (25 coin)";
            case "bless_minor" -> "Minor battle blessing (30 coin)";
            case "smith_gear" -> "Commission random gear (varies)";
            case "repair_hint" -> "Smithing advice (free)";
            case "forge_bless" -> "Forge blessing — strength (35 coin)";
            case "enchant_bless" -> "Arcane ward (45 coin)";
            case "rune_ward" -> "Rune resistance (40 coin)";
            case "teach_energy" -> "Cursed energy lecture (+CE, 20 coin)";
            case "ce_refill" -> "Refill cursed energy (50 coin)";
            case "bounty_tip" -> "Accept a bounty tip-quest";
            case "hunter_mark" -> "Mark next spirit (+tracking buff)";
            case "bank_menu" -> "Open bank advice";
            case "small_loan" -> "Emergency 100 coin loan";
            case "election_info" -> "Election status briefing";
            case "civic_donation" -> "Donate 50 to treasury";
            case "cursed_trade" -> "Risky cursed payout";
            case "fence_payout" -> "Fence hot goods (+coin)";
            case "brew_buff" -> "Drink a buff potion (30 coin)";
            case "antidote" -> "Antidote (20 coin)";
            case "spirit_ward" -> "Spirit repellent aura";
            case "offering" -> "Shrine offering (+luck buff)";
            case "pay_tax_hint" -> "Tax balance check";
            case "auction_gift" -> "Auction house stipend";
            case "rest_buff" -> "Rest — regen buff";
            case "rumor_coin" -> "Sell a rumor (+coin)";
            case "scout_mark" -> "Scout's vigilance (speed)";
            case "map_tip" -> "Cartography tip (+XP progress)";
            case "guard_bless" -> "Warden's protection";
            case "beast_charm" -> "Beast-speaker charm";
            case "gem_sell" -> "Appraise gems (+coin)";
            case "grade_hint" -> "Sorcerer grade counsel";
            case "crier_buff" -> "Hear the proclamation";
            default -> "Local advice";
        };
    }

    public static String run(ServerPlayer p, Villager v, NpcArchetype arch, String svc, String npcName) {
        String uuid = p.getStringUUID();
        return switch (svc) {
            case "heal" -> {
                if (!offCooldown(v, p, svc, 30_000L)) yield "Come back when the salves restock.";
                if (!DataManager.removeCoins(uuid, 40)) yield "You can't afford treatment.";
                p.setHealth(p.getMaxHealth());
                StatManager.addMana(p, StatManager.getMaxMana(p));
                StatManager.addCursedEnergy(p, StatManager.getMaxCursedEnergy(p) * 0.5);
                yield "There — flesh, mana, and cursed energy restored.";
            }
            case "cure" -> {
                if (!DataManager.removeCoins(uuid, 25)) yield "Insufficient coin.";
                p.removeEffect(MobEffects.POISON);
                p.removeEffect(MobEffects.WITHER);
                yield "Toxins purged.";
            }
            case "bless_minor" -> bless(p, uuid, 30, MobEffects.REGENERATION, 600, 0);
            case "forge_bless" -> bless(p, uuid, 35, MobEffects.STRENGTH, 900, 0);
            case "enchant_bless" -> bless(p, uuid, 45, MobEffects.ABSORPTION, 900, 1);
            case "rune_ward" -> bless(p, uuid, 40, MobEffects.RESISTANCE, 900, 1);
            case "teach_energy" -> {
                if (!DataManager.removeCoins(uuid, 20)) yield "Knowledge isn't free.";
                StatManager.addCursedEnergy(p, 40);
                yield "Feel that? That's potential.";
            }
            case "ce_refill" -> {
                if (!DataManager.removeCoins(uuid, 50)) yield "Your purse is lighter than your soul.";
                StatManager.refillCursedEnergy(p);
                yield "Cursed energy brimming.";
            }
            case "smith_gear" -> {
                VillagerManager.runBlacksmith(p, v, npcName);
                yield "Steel changed hands. Check your inventory.";
            }
            case "repair_hint" -> "Shift-click villagers for vanilla trades.";
            case "bounty_tip" -> {
                Expansion2QuestManager.offerRandomForArchetype(p, arch);
                yield "A contract has been added. Use /exp2quest list.";
            }
            case "hunter_mark" -> bless(p, uuid, 0, MobEffects.GLOWING, 1200, 0);
            case "bank_menu" -> "Wallet: " + DataManager.getCoins(uuid) + " coin. Bank: " + BankManager.balance(uuid);
            case "small_loan" -> {
                if (DataManager.getCoins(uuid) >= 50) yield "You don't look destitute.";
                DataManager.addCoins(uuid, 100);
                yield "100 coin advanced.";
            }
            case "election_info" -> com.political.politics.ElectionManager.isElectionActive()
                    ? "Elections are LIVE. Cast your vote."
                    : "No election running.";
            case "civic_donation" -> {
                if (!DataManager.removeCoins(uuid, 50)) yield "Treasury rejects empty pockets.";
                DataManager.addTreasury(50);
                yield "Generous. The settlement remembers.";
            }
            case "cursed_trade" -> {
                if (!offCooldown(v, p, svc, 120_000L)) yield "My cursed stock is depleted.";
                if (RNG.nextInt(100) < 30) {
                    p.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
                    yield "The goods disagreed with you.";
                }
                int gain = 40 + RNG.nextInt(80);
                DataManager.addCoins(uuid, gain);
                yield "Tainted exchange: +" + gain + " coin.";
            }
            case "fence_payout" -> {
                int gain = 30 + RNG.nextInt(40);
                DataManager.addCoins(uuid, gain);
                yield "No names, no problems. +" + gain + " coin.";
            }
            case "brew_buff" -> bless(p, uuid, 30, MobEffects.FIRE_RESISTANCE, 1200, 0);
            case "antidote" -> {
                if (!DataManager.removeCoins(uuid, 20)) yield "Can't afford antidote.";
                p.removeEffect(MobEffects.POISON);
                yield "Antidote administered.";
            }
            case "spirit_ward" -> bless(p, uuid, 35, MobEffects.RESISTANCE, 900, 0);
            case "offering" -> bless(p, uuid, 25, MobEffects.LUCK, 1800, 0);
            case "pay_tax_hint" -> "Owed tax: " + DataManager.data().taxOwed.getOrDefault(uuid, 0) + " coin.";
            case "auction_gift" -> {
                int g = 15 + RNG.nextInt(25);
                DataManager.addCoins(uuid, g);
                yield "House stipend: +" + g + " coin.";
            }
            case "rest_buff" -> bless(p, uuid, 10, MobEffects.REGENERATION, 1800, 1);
            case "rumor_coin" -> {
                int g = 20 + RNG.nextInt(30);
                DataManager.addCoins(uuid, g);
                yield "Rumor sold. +" + g + " coin.";
            }
            case "scout_mark" -> bless(p, uuid, 25, MobEffects.SPEED, 1200, 0);
            case "map_tip" -> {
                DataManager.data().buffProgress.merge(uuid + "|exp2_maps", 1, Integer::sum);
                yield "Another landmark charted.";
            }
            case "guard_bless" -> bless(p, uuid, 30, MobEffects.RESISTANCE, 1200, 1);
            case "beast_charm" -> bless(p, uuid, 20, MobEffects.HERO_OF_THE_VILLAGE, 600, 0);
            case "gem_sell" -> {
                int g = 50 + RNG.nextInt(100);
                DataManager.addCoins(uuid, g);
                yield "Fine cut. +" + g + " coin.";
            }
            case "grade_hint" -> "Sorcerer grade: " + DataManager.sorcererGrade(uuid);
            case "crier_buff" -> bless(p, uuid, 0, MobEffects.STRENGTH, 600, 0);
            default -> arch.tagline;
        };
    }

    private static String bless(ServerPlayer p, String uuid, int cost, Holder<net.minecraft.world.effect.MobEffect> effect,
                                int ticks, int amp) {
        if (cost > 0 && !DataManager.removeCoins(uuid, cost)) return "You can't afford that blessing.";
        p.addEffect(new MobEffectInstance(effect, ticks, amp, false, true));
        return "Blessing granted.";
    }

    private static boolean offCooldown(Villager v, ServerPlayer p, String service, long ms) {
        String key = v.getStringUUID() + "|" + p.getStringUUID() + "|exp2|" + service;
        long now = System.currentTimeMillis();
        Long ready = COOLDOWNS.get(key);
        if (ready != null && now < ready) return false;
        COOLDOWNS.put(key, now + ms);
        return true;
    }
}
