package com.political.expansion2.quests;

import com.political.expansion2.npc.NamedNpcBoss;
import com.political.expansion2.npc.NpcArchetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestRegistry {

    private static final Map<String, QuestDef> ALL = new LinkedHashMap<>();

    static {
        reg("fetch_iron_bundle", "Iron Bundle", "Bring 32 iron ingots.", QuestKind.FETCH,
                NpcArchetype.BLACKSMITH, "minecraft:iron_ingot", 32, 120, 0, true);
        reg("fetch_gold_tribute", "Gold Tribute", "Deliver 16 gold ingots.", QuestKind.FETCH,
                NpcArchetype.BANKER, "minecraft:gold_ingot", 16, 200, 0, true);
        reg("fetch_emerald_dues", "Emerald Dues", "Pay 8 emeralds.", QuestKind.FETCH,
                NpcArchetype.TAX_COLLECTOR, "minecraft:emerald", 8, 150, 0, true);
        reg("fetch_bone_charms", "Bone Charms", "Collect 24 bones.", QuestKind.FETCH,
                NpcArchetype.EXORCIST, "minecraft:bone", 24, 90, 0, true);
        reg("fetch_blaze_powder", "Blaze Batch", "Gather 12 blaze powder.", QuestKind.FETCH,
                NpcArchetype.ALCHEMIST, "minecraft:blaze_powder", 12, 180, 0, true);
        reg("fetch_ender_pearls", "Pearl Courier", "Deliver 4 ender pearls.", QuestKind.FETCH,
                NpcArchetype.ENCHANTER, "minecraft:ender_pearl", 4, 220, 0, true);
        reg("fetch_quartz_runes", "Quartz Runes", "Mine 32 quartz.", QuestKind.FETCH,
                NpcArchetype.RUNE_CARVER, "minecraft:quartz", 32, 100, 0, true);
        reg("fetch_amethyst_gems", "Amethyst Lot", "Bring 16 amethyst shards.", QuestKind.FETCH,
                NpcArchetype.GEM_CUTTER, "minecraft:amethyst_shard", 16, 140, 0, true);
        reg("fetch_wheat_feast", "Harvest Feast", "Deliver 64 wheat.", QuestKind.FETCH,
                NpcArchetype.INNKEEPER, "minecraft:wheat", 64, 80, 0, true);
        reg("fetch_rotten_fence", "Hot Goods", "Fence 32 rotten flesh.", QuestKind.FETCH,
                NpcArchetype.FENCE, "minecraft:rotten_flesh", 32, 70, 0, true);
        reg("deliver_quest_seal", "Broker Seal", "Deliver a bounty seal.", QuestKind.DELIVER,
                NpcArchetype.BOUNTY_BROKER, "quest2_bounty_seal", 1, 250, 0, false);
        reg("deliver_cursed_relic", "Relic Handoff", "Deliver a cursed relic.", QuestKind.DELIVER,
                NpcArchetype.RELIC_DEALER, "quest2_cursed_relic", 1, 300, 1, false);
        reg("deliver_grimoire_page", "Forbidden Page", "Bring a grimoire page.", QuestKind.DELIVER,
                NpcArchetype.CURSE_SCHOLAR, "quest2_grimoire_page", 1, 280, 0, false);
        reg("deliver_shrine_offering", "Shrine Offering", "Deliver shrine offering.", QuestKind.DELIVER,
                NpcArchetype.SHRINE_KEEPER, "quest2_shrine_offering", 1, 160, 0, true);
        reg("hold_map_fragment", "Map Fragment", "Carry a map fragment.", QuestKind.HOLD_ITEM,
                NpcArchetype.MAPMAKER, "quest2_map_fragment", 1, 120, 0, true);

        reg("kill_wisps", "Wisp Cull", "Slay 10 Cursed Wisps.", QuestKind.KILL_SPIRIT,
                NpcArchetype.SPIRIT_HUNTER, "curse_wisp", 10, 150, 0, true);
        reg("kill_larvae", "Larva Purge", "Slay 12 Grudge Larvae.", QuestKind.KILL_SPIRIT,
                NpcArchetype.EXORCIST, "grudge_larva", 12, 140, 0, true);
        reg("kill_imps", "Imp Roundup", "Slay 8 Shadow Imps.", QuestKind.KILL_SPIRIT,
                NpcArchetype.SPIRIT_HUNTER, "shadow_imp", 8, 130, 0, true);
        reg("kill_slitmouth", "Slit-Mouth Hunt", "Slay 6 Slit-Mouth Curses.", QuestKind.KILL_SPIRIT,
                NpcArchetype.CURSE_SCHOLAR, "slitmouth_curse", 6, 200, 0, true);
        reg("kill_brutes", "Brute Breaker", "Slay 5 Brute Curses.", QuestKind.KILL_SPIRIT,
                NpcArchetype.MERCENARY, "brute_curse", 5, 220, 0, true);
        reg("kill_spitters", "Spitter Silence", "Slay 8 Spitter Curses.", QuestKind.KILL_SPIRIT,
                NpcArchetype.SORCERER, "spitter_curse", 8, 180, 0, true);
        reg("kill_horned", "Horned Harvest", "Slay 4 Horned Curses.", QuestKind.KILL_SPIRIT,
                NpcArchetype.SPIRIT_HUNTER, "horned_curse", 4, 260, 0, true);
        reg("kill_veils", "Veil Shredder", "Slay 6 Veil Curses.", QuestKind.KILL_SPIRIT,
                NpcArchetype.SUMMONER, "veil_curse", 6, 240, 0, true);
        reg("kill_plague", "Plague Quarantine", "Slay 5 Plague Curses.", QuestKind.KILL_SPIRIT,
                NpcArchetype.HEALER, "plague_curse", 5, 230, 0, true);
        reg("kill_ember", "Ember Douser", "Slay 5 Ember Curses.", QuestKind.KILL_SPIRIT,
                NpcArchetype.ALCHEMIST, "ember_curse", 5, 210, 0, true);
        reg("kill_flame_cal", "Flame Calamity", "Slay 3 Flame Calamities.", QuestKind.KILL_SPIRIT,
                NpcArchetype.DOOMSDAY_PROPHET, "flame_calamity", 3, 350, 1, true);
        reg("kill_transfigured", "Soul Unbinding", "Slay 3 Transfigured Souls.", QuestKind.KILL_SPIRIT,
                NpcArchetype.GRIMOIRE_SELLER, "transfigured_soul", 3, 340, 0, true);

        reg("kill_zombies", "Undead Patrol", "Slay 20 zombies.", QuestKind.KILL_MOB,
                NpcArchetype.WARDEN, "minecraft:zombie", 20, 100, 0, true);
        reg("kill_skeletons", "Bone Yard", "Slay 20 skeletons.", QuestKind.KILL_MOB,
                NpcArchetype.WARDEN, "minecraft:skeleton", 20, 100, 0, true);
        reg("kill_spiders", "Web Clearance", "Slay 15 spiders.", QuestKind.KILL_MOB,
                NpcArchetype.SCOUT, "minecraft:spider", 15, 90, 0, true);
        reg("kill_blazes", "Nether Heat", "Slay 10 blazes.", QuestKind.KILL_MOB,
                NpcArchetype.WEAPON_SMITH, "minecraft:blaze", 10, 200, 0, true);
        reg("kill_endermen", "Void Stalkers", "Slay 8 endermen.", QuestKind.KILL_MOB,
                NpcArchetype.ENCHANTER, "minecraft:enderman", 8, 220, 0, true);
        reg("kill_phantoms", "Night Terrors", "Slay 6 phantoms.", QuestKind.KILL_MOB,
                NpcArchetype.TOWN_CRIER, "minecraft:phantom", 6, 180, 0, true);

        reg("bank_deposit_500", "Secure Savings", "Deposit 500 coin to bank.", QuestKind.BANK_DEPOSIT,
                NpcArchetype.BANKER, "", 500, 100, 0, false);
        reg("bank_deposit_2000", "Vault Builder", "Deposit 2000 coin.", QuestKind.BANK_DEPOSIT,
                NpcArchetype.LOAN_SHARK, "", 2000, 400, 1, false);
        reg("collect_coins_1000", "Coin Hoarder", "Hold 1000 coin at once.", QuestKind.COLLECT_COINS,
                NpcArchetype.AUCTIONEER, "", 1000, 150, 0, false);
        reg("treasury_donate_200", "Patron of State", "Donate 200 to treasury.", QuestKind.DEPOSIT_TREASURY,
                NpcArchetype.POLITICIAN, "", 200, 120, 0, true);
        reg("election_vote", "Civic Duty", "Vote during an election.", QuestKind.ELECTION,
                NpcArchetype.ELECTION_CLERK, "", 1, 180, 0, false);
        reg("awaken_ce_100", "Awaken Energy I", "Reach 100 cursed energy.", QuestKind.AWAKEN_CE,
                NpcArchetype.SORCERER, "", 100, 150, 0, false);
        reg("awaken_ce_max", "Awaken Energy II", "Fill cursed energy to max.", QuestKind.AWAKEN_CE,
                NpcArchetype.SUMMONER, "max", 1, 250, 0, false);
        reg("cursed_buy_in", "Tainted Investment", "Hold 300 coin for cursed merchant.", QuestKind.COLLECT_COINS,
                NpcArchetype.CURSED_MERCHANT, "pay:300", 300, 350, 0, true);

        reg("bounty_undead", "Undead Contract", "Start Undead bounty.", QuestKind.BOUNTY,
                NpcArchetype.BOUNTY_BROKER, "UNDEAD", 1, 200, 0, true);
        reg("bounty_arachnid", "Arachnid Contract", "Start Arachnid bounty.", QuestKind.BOUNTY,
                NpcArchetype.BOUNTY_BROKER, "ARACHNID", 1, 200, 0, true);
        reg("bounty_infernal", "Infernal Contract", "Start Infernal bounty.", QuestKind.BOUNTY,
                NpcArchetype.MERCENARY, "INFERNAL", 1, 220, 0, true);
        reg("bounty_void", "Void Contract", "Start Void bounty.", QuestKind.BOUNTY,
                NpcArchetype.SPIRIT_HUNTER, "VOID", 1, 240, 0, true);
        reg("bounty_decayed", "Decayed Contract", "Start Decayed bounty.", QuestKind.BOUNTY,
                NpcArchetype.EXORCIST, "DECAYED", 1, 200, 0, true);

        reg("exorcise_10", "Novice Exorcist", "Exorcise 10 curses.", QuestKind.EXORCISE_COUNT,
                NpcArchetype.EXORCIST, "", 10, 200, 0, false);
        reg("exorcise_50", "Master Exorcist", "Exorcise 50 curses.", QuestKind.EXORCISE_COUNT,
                NpcArchetype.CURSE_SCHOLAR, "", 50, 500, 2, false);
        reg("grade_2", "Grade Two", "Reach sorcerer grade 2.", QuestKind.REACH_GRADE,
                NpcArchetype.SORCERER, "", 2, 300, 1, false);
        reg("grade_4", "Calamity Scholar", "Reach sorcerer grade 4.", QuestKind.REACH_GRADE,
                NpcArchetype.GRIMOIRE_SELLER, "", 4, 600, 3, false);

        reg("herb_moonlit", "Moonlit Herbs", "Fetch 16 glow berries.", QuestKind.FETCH,
                NpcArchetype.HERBALIST, "minecraft:glow_berries", 16, 110, 0, true);
        reg("beast_lead", "Beast Lead", "Bring 8 leads.", QuestKind.FETCH,
                NpcArchetype.BEAST_TAMER, "minecraft:lead", 8, 95, 0, true);
        reg("armor_iron_set", "Iron Guard", "Deliver 24 iron ingots to armor smith.", QuestKind.FETCH,
                NpcArchetype.ARMOR_SMITH, "minecraft:iron_ingot", 24, 130, 0, true);
        reg("weapon_diamond", "Diamond Edge", "Deliver 2 diamonds.", QuestKind.FETCH,
                NpcArchetype.WEAPON_SMITH, "minecraft:diamond", 2, 400, 1, false);

        for (NamedNpcBoss b : NamedNpcBoss.values()) {
            reg("boss_" + b.id, "Defeat " + b.displayName, "Slay " + b.displayName + ".",
                    QuestKind.BOSS, bossGiver(b), b.id, 1, 400, 1, false);
        }
    }

    private QuestRegistry() {}

    private static NpcArchetype bossGiver(NamedNpcBoss b) {
        return switch (b) {
            case VALDRIS -> NpcArchetype.CURSED_MERCHANT;
            case MORGRIM -> NpcArchetype.ARMOR_SMITH;
            case SYLVA -> NpcArchetype.ENCHANTER;
            case RAZIEL -> NpcArchetype.BOUNTY_BROKER;
            case CROFT -> NpcArchetype.BANKER;
            case BLACKWOOD -> NpcArchetype.POLITICIAN;
            case MORBIDIUS -> NpcArchetype.HEALER;
            case ASHARA -> NpcArchetype.SORCERER;
            case GARRICK -> NpcArchetype.MERCENARY;
            case KAGURO -> NpcArchetype.SPIRIT_HUNTER;
            case THORN -> NpcArchetype.ELECTION_CLERK;
            case PYRION -> NpcArchetype.DOOMSDAY_PROPHET;
        };
    }

    private static void reg(String id, String title, String desc, QuestKind kind, NpcArchetype giver,
                            String target, int amount, int coins, int credits, boolean repeatable) {
        ALL.put(id, new QuestDef(id, title, desc, kind, giver, target, amount, coins, credits, repeatable));
    }

    public static QuestDef get(String id) { return ALL.get(id); }
    public static List<QuestDef> all() { return Collections.unmodifiableList(new ArrayList<>(ALL.values())); }
    public static List<QuestDef> forGiver(NpcArchetype arch) {
        List<QuestDef> out = new ArrayList<>();
        for (QuestDef q : ALL.values()) if (q.giver() == arch) out.add(q);
        return out;
    }
    public static int count() { return ALL.size(); }
}
